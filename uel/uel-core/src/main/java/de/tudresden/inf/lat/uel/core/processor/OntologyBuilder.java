package de.tudresden.inf.lat.uel.core.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import de.tudresden.inf.lat.uel.core.type.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

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

	private AtomManager atomManager = null;
	private int freshConstantIndex = 0;

	public OntologyBuilder(AtomManager manager) {
		if (manager == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.atomManager = manager;
	}

	private Atom createFlattenAtom(String roleName, Set<Atom> atomSet,
			Set<Equation> newEquations) {
		ConceptName child = createNewAtom();
		child.setAuxiliaryVariable(true);
		ExistentialRestriction newAtom = this.atomManager
				.createExistentialRestriction(roleName, child);
		Integer childId = getAtoms().getIndex(child);
		Set<Integer> atomIdSet = new HashSet<Integer>();
		for (Atom a : atomSet) {
			atomIdSet.add(getAtoms().addAndGetIndex(a));
		}
		Equation eq = new EquationImpl(childId, atomIdSet, false);
		newEquations.add(eq);

		return newAtom;
	}

	public ConceptName createNewAtom() {
		String str = freshConstantPrefix + freshConstantIndex;
		freshConstantIndex++;
		return this.atomManager.createConceptName(str, true);
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
		return getAtoms().addAndGetIndex(atom);
	}

	public AtomManager getAtomManager() {
		return this.atomManager;
	}

	public IndexedSet<Atom> getAtoms() {
		return this.atomManager.getAtoms();
	}

	public Equation makeEquation(Atom definiendum, Set<Atom> definiens,
			boolean primitive) {
		if (definiendum == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (definiens == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Integer definiendumId = getAtoms().addAndGetIndex(definiendum);

		Set<Integer> definiensIds = new HashSet<Integer>();
		for (Atom atom : definiens) {
			definiensIds.add(getAtoms().addAndGetIndex(atom));
		}

		return new EquationImpl(definiendumId, definiensIds, primitive);
	}

	private Set<Atom> processClass(OWLClass cls) {
		Set<Atom> ret = new HashSet<Atom>();
		ret.add(this.atomManager.createConceptName(cls.toStringID(), false));
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
		String roleName = someValuesRestriction.getProperty()
				.getNamedProperty().toStringID();
		if (atomSet.size() == 1) {
			Atom atom = atomSet.iterator().next();
			if (atom.isConceptName()) {
				ConceptName concept = (ConceptName) atom;
				newAtom = this.atomManager.createExistentialRestriction(
						roleName, concept);
			} else {
				newAtom = createFlattenAtom(roleName, atomSet, newEquations);
			}
		} else if (atomSet.size() > 1) {
			newAtom = createFlattenAtom(roleName, atomSet, newEquations);
		}
		ret.add(newAtom);
		return ret;
	}

}
