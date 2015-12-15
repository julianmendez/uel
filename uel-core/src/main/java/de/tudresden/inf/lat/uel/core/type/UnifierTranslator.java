package de.tudresden.inf.lat.uel.core.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;

public class UnifierTranslator {

	private final AtomManager atomManager;
	private final Set<Integer> auxiliaryVariables;
	private final OWLDataFactory dataFactory;
	private final Set<Integer> userVariables;

	public UnifierTranslator(OWLDataFactory factory, AtomManager atomManager, Set<Integer> userVariables,
			Set<Integer> auxiliaryVariables) {
		this.dataFactory = factory;
		this.atomManager = atomManager;
		this.userVariables = userVariables;
		this.auxiliaryVariables = auxiliaryVariables;
	}

	public Set<OWLUelClassDefinition> createOWLUelClassDefinition(Set<Equation> equations) {
		Set<OWLUelClassDefinition> ret = new HashSet<OWLUelClassDefinition>();
		for (Equation equation : equations) {
			if (userVariables.contains(equation.getLeft())) {
				OWLClass definiendum = getClassFor(atomManager.getAtom(equation.getLeft()));

				Set<Atom> right = new HashSet<Atom>();
				for (Integer atomId : equation.getRight()) {
					right.add(atomManager.getAtom(atomId));
				}

				OWLClassExpression definiens = toOWLClassExpression(right, equations);

				ret.add(new OWLUelClassDefinitionImpl(definiendum, definiens, dataFactory));
			}
		}
		return ret;
	}

	public AtomManager getAtomManager() {
		return this.atomManager;
	}

	private OWLClass getClassFor(Atom atom) {
		String conceptName = this.atomManager.printConceptName(atom);
		return this.dataFactory.getOWLClass(IRI.create(conceptName));
	}

	private OWLObjectProperty getObjectPropertyFor(Atom atom) {
		String roleName = this.atomManager.printRoleName(atom);
		return this.dataFactory.getOWLObjectProperty(IRI.create(roleName));
	}

	private OWLClassExpression getOWLClassExpression(Atom atom, Set<Equation> equations) {
		OWLClassExpression ret = null;

		if (atom.isExistentialRestriction()) {

			ConceptName child = atom.getConceptName();
			Integer childId = this.atomManager.getAtoms().getIndex(child);

			OWLObjectProperty objectProperty = getObjectPropertyFor(atom);

			OWLClassExpression classExpression;
			if (this.auxiliaryVariables.contains(childId)) {
				classExpression = toOWLClassExpression(getSetOfSubsumers(child, equations), equations);
			} else {
				classExpression = getClassFor(child);
			}

			ret = this.dataFactory.getOWLObjectSomeValuesFrom(objectProperty, classExpression);

		} else {
			ret = getClassFor(atom);
		}
		return ret;
	}

	private Collection<Atom> getSetOfSubsumers(Atom atom, Set<Equation> equations) {
		Set<Atom> ret = new HashSet<Atom>();
		for (Equation equation : equations) {
			if (equation.getLeft().equals(atomManager.getIndex(atom))) {
				for (Integer id : equation.getRight()) {
					ret.add(atomManager.getAtom(id));
				}
			}
		}
		return ret;
	}

	private OWLClassExpression toOWLClassExpression(Collection<Atom> setOfSubsumers, Set<Equation> equations) {

		OWLClassExpression ret = null;

		if (setOfSubsumers.isEmpty()) {

			ret = this.dataFactory.getOWLThing();

		} else if (setOfSubsumers.size() == 1) {

			Atom atom = setOfSubsumers.iterator().next();
			ret = getOWLClassExpression(atom, equations);

		} else {

			Set<OWLClassExpression> classExpressions = new HashSet<OWLClassExpression>();
			for (Atom atom : setOfSubsumers) {
				classExpressions.add(getOWLClassExpression(atom, equations));
			}
			ret = this.dataFactory.getOWLObjectIntersectionOf(classExpressions);

		}
		return ret;
	}

}
