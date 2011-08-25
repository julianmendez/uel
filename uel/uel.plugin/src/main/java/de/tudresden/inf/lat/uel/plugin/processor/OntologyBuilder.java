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

	private static final Logger logger = Logger.getLogger(OntologyBuilder.class
			.getName());

	private IndexedSet<Atom> atomManager = null;

	public OntologyBuilder(IndexedSet<Atom> manager) {
		if (manager == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.atomManager = manager;
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
		for (OWLClass key : definitions.keySet()) {
			ret.putDefinition(getName(key),
					processDefinition(key, definitions.get(key)));
		}

		Map<OWLClass, Set<OWLClassExpression>> primitiveDefinitions = getPrimitiveDefinitions(owlOntology);
		for (OWLClass key : primitiveDefinitions.keySet()) {
			ret.putPrimitiveDefinition(
					getName(key),
					processPrimitiveDefinition(key,
							primitiveDefinitions.get(key)));
		}
		return ret;
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
	 * Returns the name given to an OWL class in UEL.
	 * 
	 * @param cls
	 *            an OWL class
	 * @return the name given to an OWL class in UEL
	 */
	public String getName(OWLClass cls) {
		if (cls == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		return processClass(cls).iterator().next().toString();
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
		ret.add(newAtom);
		return ret;
	}

	private Set<Atom> processClassExpression(OWLClassExpression classExpr) {
		Set<Atom> ret = new HashSet<Atom>();
		if (classExpr instanceof OWLClass) {
			ret = processClass((OWLClass) classExpr);
		} else if (classExpr instanceof OWLObjectIntersectionOf) {
			ret = processIntersection((OWLObjectIntersectionOf) classExpr);
		} else if (classExpr instanceof OWLObjectSomeValuesFrom) {
			ret = processSomeValuesRestriction((OWLObjectSomeValuesFrom) classExpr);
		} else {
			logger.warning("Ignoring class expression '" + classExpr + "'.");
		}
		return ret;
	}

	public Equation processDefinition(OWLClass definiendum,
			OWLClassExpression definiens) {
		if (definiendum == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (definiens == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Set<Atom> left = processClass(definiendum);
		Set<Atom> right = processClassExpression(definiens);
		return makeEquation(left.iterator().next(), right, false);
	}

	private Set<Atom> processIntersection(OWLObjectIntersectionOf intersection) {
		Set<Atom> ret = new HashSet<Atom>();
		for (OWLClassExpression operand : intersection.getOperands()) {
			ret.addAll(processClassExpression(operand));
		}
		return ret;
	}

	public Map<String, OWLClass> processNames(Set<OWLClass> set) {
		if (set == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Map<String, OWLClass> ret = new HashMap<String, OWLClass>();
		for (OWLClass cls : set) {
			ret.put(getName(cls), cls);
		}
		return ret;
	}

	public Equation processPrimitiveDefinition(OWLClass definiendum,
			Set<OWLClassExpression> definiensSet) {
		if (definiendum == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		if (definiensSet == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Set<Atom> subClassSet = processClass(definiendum);
		Set<Atom> superClassSet = new HashSet<Atom>();
		for (OWLClassExpression clsExpr : definiensSet) {
			superClassSet.addAll(processClassExpression(clsExpr));
		}
		return makeEquation(subClassSet.iterator().next(), superClassSet, true);
	}

	private Set<Atom> processSomeValuesRestriction(
			OWLObjectSomeValuesFrom someValuesRestriction) {
		Set<Atom> ret = new HashSet<Atom>();
		Map<String, Atom> map = new HashMap<String, Atom>();
		Set<Atom> atomSet = processClassExpression(someValuesRestriction
				.getFiller());
		for (Atom atom : atomSet) {
			map.put(atom.toString(), atom);
		}
		Atom prop = new Atom(someValuesRestriction.getProperty()
				.getNamedProperty().toStringID(), true);
		Atom newAtom = new Atom(prop.toString(), true, map);
		ret.add(newAtom);
		return ret;
	}

}
