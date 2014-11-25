package de.tudresden.inf.lat.uel.plugin.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

public class UnifierTranslator {

	private final AtomManager atomManager;
	private final OWLDataFactory dataFactory;
	private final Set<Integer> userVariables;
	private final Set<Integer> auxiliaryVariables;

	public UnifierTranslator(OWLDataFactory factory, AtomManager atomManager,
			Set<Integer> userVariables, Set<Integer> auxiliaryVariables) {
		this.dataFactory = factory;
		this.atomManager = atomManager;
		this.userVariables = userVariables;
		this.auxiliaryVariables = auxiliaryVariables;
	}

	public AtomManager getAtomManager() {
		return this.atomManager;
	}

	public Set<OWLUelClassDefinition> createOWLUelClassDefinition(
			Set<Equation> equations) {

		Set<OWLUelClassDefinition> ret = new HashSet<OWLUelClassDefinition>();
		for (Equation equation : equations) {
			if (userVariables.contains(equation.getLeft())) {
				OWLClass definiendum = getClassFor(equation.getLeft());

				Set<Atom> right = new HashSet<Atom>();
				for (Integer atomId : equation.getRight()) {
					right.add(this.atomManager.getAtoms().get(atomId));
				}

				OWLClassExpression definiens = toOWLClassExpression(right,
						equations);

				ret.add(new OWLUelClassDefinitionImpl(definiendum, definiens,
						this.dataFactory));
			}
		}
		return ret;
	}

	private OWLClass getClassFor(Integer conceptId) {
		String conceptName = this.atomManager.getConceptName(conceptId);
		return this.dataFactory.getOWLClass(IRI.create(conceptName));
	}

	private OWLObjectProperty getObjectPropertyFor(Integer objectPropertyId) {
		String roleName = this.atomManager.getRoleName(objectPropertyId);
		return this.dataFactory.getOWLObjectProperty(IRI.create(roleName));
	}

	private OWLClassExpression getOWLClassExpression(Atom atom,
			Set<Equation> equations) {
		OWLClassExpression ret = null;

		if (atom.isExistentialRestriction()) {

			ConceptName child = ((ExistentialRestriction) atom).getChild();
			Integer conceptId = this.atomManager.getAtoms().getIndex(child);

			OWLObjectProperty objectProperty = getObjectPropertyFor(((ExistentialRestriction) atom)
					.getRoleId());

			OWLClassExpression classExpression;
			if (this.auxiliaryVariables.contains(conceptId)) {
				classExpression = toOWLClassExpression(
						getSetOfSubsumers(child, equations), equations);
			} else {
				classExpression = getClassFor(child.getConceptNameId());
			}

			ret = this.dataFactory.getOWLObjectSomeValuesFrom(objectProperty,
					classExpression);

		} else {
			ConceptName concept = (ConceptName) atom;
			Integer conceptId = this.atomManager.getAtoms().getIndex(concept);
			ret = getClassFor(conceptId);
		}
		return ret;
	}

	private Collection<Atom> getSetOfSubsumers(Atom atom,
			Set<Equation> equations) {
		Set<Atom> ret = new HashSet<Atom>();
		for (Equation equation : equations) {
			if (equation.getLeft().equals(
					this.atomManager.getAtoms().addAndGetIndex(atom))) {
				for (Integer id : equation.getRight()) {
					ret.add(this.atomManager.getAtoms().get(id));
				}
			}
		}
		return ret;
	}

	private OWLClassExpression toOWLClassExpression(
			Collection<Atom> setOfSubsumers, Set<Equation> equations) {

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
