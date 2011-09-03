package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.type.Atom;
import de.tudresden.inf.lat.uel.core.type.Equation;
import de.tudresden.inf.lat.uel.core.type.IndexedSet;
import de.tudresden.inf.lat.uel.core.type.OntologyImpl;

/**
 * An object implementing this class has convenience methods used to build a UEL
 * ontology using an OWL ontology.
 * 
 * @author Julian Mendez
 */
public class OntologyBuilder {

	private static final String freshConstantPrefix = "var";
	private static final Logger logger = Logger.getLogger(OntologyBuilder.class
			.getName());

	private IndexedSet<Atom> atomManager = null;
	private int freshConstantIndex = 0;

	public OntologyBuilder(IndexedSet<Atom> manager) {
		if (manager == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.atomManager = manager;
	}

	private Atom createFlattenAtom(String newAtomName, Set<Atom> atomSet,
			Set<Equation> newEquations) {
		Atom child = createNewAtom();
		Atom newAtom = new Atom(newAtomName, true, false, child);
		Integer childId = getAtomManager().addAndGetIndex(child);
		Set<Integer> atomIdSet = new HashSet<Integer>();
		for (Atom a : atomSet) {
			atomIdSet.add(getAtomManager().addAndGetIndex(a));
		}
		Equation eq = new Equation(childId, atomIdSet, false);
		newEquations.add(eq);

		return newAtom;
	}

	private Atom createNewAtom() {
		String str = freshConstantPrefix + freshConstantIndex;
		freshConstantIndex++;
		Atom ret = new Atom(str, false, true, null);
		getAtomManager().add(ret);
		return ret;
	}

	/**
	 * Returns a new UEL ontology using an OWL ontology.
	 * 
	 * @param owlOntology
	 *            OWL ontology
	 * @return a new UEL ontology using an OWL ontology
	 */
	public OntologyImpl createOntology(OWLOntology owlOntology) {
		if (owlOntology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		OntologyImpl ret = new OntologyImpl();
		Map<OWLClass, OWLClassExpression> definitions = getDefinitions(owlOntology);
		Set<Equation> setOfEquations = new HashSet<Equation>();
		for (OWLClass key : definitions.keySet()) {
			setOfEquations.addAll(processDefinition(key, definitions.get(key)));
		}

		Map<OWLClass, Set<OWLClassExpression>> primitiveDefinitions = getPrimitiveDefinitions(owlOntology);
		for (OWLClass key : primitiveDefinitions.keySet()) {
			setOfEquations.addAll(processPrimitiveDefinition(key,
					primitiveDefinitions.get(key)));
		}

		for (Equation eq : setOfEquations) {
			if (eq.isPrimitive()) {
				ret.putPrimitiveDefinition(eq.getLeft(), eq);
			} else {
				ret.putDefinition(eq.getLeft(), eq);
			}
		}
		return ret;
	}

	/**
	 * Returns the name given to an OWL class in UEL.
	 * 
	 * @param cls
	 *            an OWL class
	 * @return the name given to an OWL class in UEL
	 */
	public Integer getAtomId(OWLClass cls) {
		if (cls == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Atom atom = processClass(cls).iterator().next();
		return getAtomManager().addAndGetIndex(atom);
	}

	public IndexedSet<Atom> getAtomManager() {
		return this.atomManager;
	}

	/**
	 * Returns all the definitions present in an OWL ontology.
	 * 
	 * @param owlOntology
	 *            OWL ontology
	 * @return all definitions present in an OWL ontology
	 */
	public Map<OWLClass, OWLClassExpression> getDefinitions(
			OWLOntology owlOntology) {
		if (owlOntology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Map<OWLClass, OWLClassExpression> ret = new HashMap<OWLClass, OWLClassExpression>();
		Set<OWLEquivalentClassesAxiom> setOfAxioms = owlOntology
				.getAxioms(AxiomType.EQUIVALENT_CLASSES);
		for (OWLEquivalentClassesAxiom axiom : setOfAxioms) {
			Set<OWLClassExpression> operands = axiom.getClassExpressions();
			if (operands.size() == 2) {
				Iterator<OWLClassExpression> it = operands.iterator();
				OWLClassExpression first = it.next();
				OWLClassExpression second = it.next();
				if (!(first instanceof OWLClass) && second instanceof OWLClass) {
					OWLClassExpression other = first;
					first = second;
					second = other;
				}
				if (first instanceof OWLClass && second instanceof OWLClass) {
					logger.warning("Definition '" + axiom
							+ "' is ambiguous, including only '" + first
							+ "':='" + second);
				}
				if (first instanceof OWLClass) {
					ret.put((OWLClass) first, second);
				}
			}
		}
		return ret;
	}

	/**
	 * Returns all the primitive definitions present in an OWL ontology unless
	 * OWL Thing is the super class.
	 * 
	 * @param owlOntology
	 *            OWL ontology
	 * @return all the primitive definitions present in an OWL ontology unless
	 *         OWL Thing is the super class
	 */
	public Map<OWLClass, Set<OWLClassExpression>> getPrimitiveDefinitions(
			OWLOntology owlOntology) {
		if (owlOntology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Map<OWLClass, Set<OWLClassExpression>> ret = new HashMap<OWLClass, Set<OWLClassExpression>>();
		Set<OWLSubClassOfAxiom> setOfAxioms = owlOntology
				.getAxioms(AxiomType.SUBCLASS_OF);
		for (OWLSubClassOfAxiom axiom : setOfAxioms) {
			if (!axiom.getSuperClass().isOWLThing()) {
				if (axiom.getSubClass() instanceof OWLClass) {
					OWLClass definiendum = (OWLClass) axiom.getSubClass();
					Set<OWLClassExpression> definiensSet = ret.get(definiendum);
					if (definiensSet == null) {
						definiensSet = new HashSet<OWLClassExpression>();
						ret.put(definiendum, definiensSet);
					}
					definiensSet.add(axiom.getSuperClass());
				}
			}
		}
		return ret;
	}

	public Equation makeEquation(Atom definiendum, Set<Atom> definiens,
			boolean primitive) {
		if (definiendum == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (definiens == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Integer definiendumId = getAtomManager().addAndGetIndex(definiendum);

		Set<Integer> definiensIds = new HashSet<Integer>();
		for (Atom atom : definiens) {
			definiensIds.add(getAtomManager().addAndGetIndex(atom));
		}

		return new Equation(definiendumId, definiensIds, primitive);
	}

	private Set<Atom> processClass(OWLClass cls) {
		Set<Atom> ret = new HashSet<Atom>();
		Atom newAtom = new Atom(cls.toStringID(), false);
		getAtomManager().add(newAtom);
		ret.add(newAtom);
		return ret;
	}

	private Set<Atom> processClassExpression(OWLClassExpression classExpr,
			Set<Equation> newEquations) {
		Set<Atom> ret = new HashSet<Atom>();
		if (classExpr instanceof OWLClass) {
			ret = processClass((OWLClass) classExpr);
		} else if (classExpr instanceof OWLObjectIntersectionOf) {
			ret = processIntersection((OWLObjectIntersectionOf) classExpr,
					newEquations);
		} else if (classExpr instanceof OWLObjectSomeValuesFrom) {
			ret = processSomeValuesRestriction(
					(OWLObjectSomeValuesFrom) classExpr, newEquations);
		} else {
			logger.warning("Ignoring class expression '" + classExpr + "'.");
		}
		return ret;
	}

	public Set<Equation> processDefinition(OWLClass definiendum,
			OWLClassExpression definiens) {
		if (definiendum == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (definiens == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Set<Equation> newEquations = new HashSet<Equation>();
		Set<Atom> left = processClass(definiendum);
		Set<Atom> right = processClassExpression(definiens, newEquations);
		Set<Equation> ret = new HashSet<Equation>();
		ret.add(makeEquation(left.iterator().next(), right, false));
		ret.addAll(newEquations);
		return ret;
	}

	private Set<Atom> processIntersection(OWLObjectIntersectionOf intersection,
			Set<Equation> newEquations) {
		Set<Atom> ret = new HashSet<Atom>();
		for (OWLClassExpression operand : intersection.getOperands()) {
			ret.addAll(processClassExpression(operand, newEquations));
		}
		return ret;
	}

	public Map<Integer, OWLClass> processNames(Set<OWLClass> set) {
		if (set == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Map<Integer, OWLClass> ret = new HashMap<Integer, OWLClass>();
		for (OWLClass cls : set) {
			ret.put(getAtomId(cls), cls);
		}
		return ret;
	}

	public Set<Equation> processPrimitiveDefinition(OWLClass definiendum,
			Set<OWLClassExpression> definiensSet) {
		if (definiendum == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		if (definiensSet == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Set<Equation> newEquations = new HashSet<Equation>();
		Set<Atom> subClassSet = processClass(definiendum);
		Set<Atom> superClassSet = new HashSet<Atom>();
		for (OWLClassExpression clsExpr : definiensSet) {
			superClassSet.addAll(processClassExpression(clsExpr, newEquations));
		}
		Set<Equation> ret = new HashSet<Equation>();
		ret.add(makeEquation(subClassSet.iterator().next(), superClassSet, true));
		ret.addAll(newEquations);
		return ret;
	}

	private Set<Atom> processSomeValuesRestriction(
			OWLObjectSomeValuesFrom someValuesRestriction,
			Set<Equation> newEquations) {
		Set<Atom> ret = new HashSet<Atom>();
		Set<Atom> atomSet = processClassExpression(
				someValuesRestriction.getFiller(), newEquations);
		Atom newAtom = null;
		String newAtomName = someValuesRestriction.getProperty()
				.getNamedProperty().toStringID();
		if (atomSet.size() == 1) {
			Atom atom = atomSet.iterator().next();
			if (!atom.isRoot()) {
				newAtom = new Atom(newAtomName, true, false, atom);
				getAtomManager().add(newAtom);
			} else {
				newAtom = createFlattenAtom(newAtomName, atomSet, newEquations);
			}
		} else if (atomSet.size() > 1) {
			newAtom = createFlattenAtom(newAtomName, atomSet, newEquations);
		}
		ret.add(newAtom);
		return ret;
	}

}
