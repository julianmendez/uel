package de.tudresden.inf.lat.uel.core.type;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;

public class UnifierTranslator {

	private final AtomManager atomManager;
	private final OWLDataFactory dataFactory;

	public UnifierTranslator(AtomManager atomManager) {
		this.atomManager = atomManager;
		this.dataFactory = OWLManager.getOWLDataFactory();
	}

	public Set<OWLUelClassDefinition> createOWLUelClassDefinitions(Set<Definition> definitions) {
		Set<OWLUelClassDefinition> ret = new HashSet<OWLUelClassDefinition>();
		for (Definition definition : definitions) {
			if (atomManager.getUserVariables().contains(definition.getDefiniendum())) {
				OWLClass definiendum = getClassFor(definition.getDefiniendum());
				OWLClassExpression definiens = toOWLClassExpression(definition.getRight(), definitions);
				ret.add(new OWLUelClassDefinitionImpl(definiendum, definiens));
			}
		}
		return ret;
	}

	public AtomManager getAtomManager() {
		return this.atomManager;
	}

	private OWLClass getClassFor(Integer atomId) {
		String conceptName = atomManager.printConceptName(atomId);
		return dataFactory.getOWLClass(IRI.create(conceptName));
	}

	private OWLObjectProperty getObjectPropertyFor(Integer atomId) {
		String roleName = atomManager.printRoleName(atomId);
		return dataFactory.getOWLObjectProperty(IRI.create(roleName));
	}

	private OWLClassExpression getOWLClassExpression(Integer atomId, Set<Definition> definitions) {
		OWLClassExpression expr = null;

		if (atomManager.getExistentialRestrictions().contains(atomId)) {
			Integer childId = atomManager.getChild(atomId);
			OWLClassExpression childExpr;
			if (atomManager.getFlatteningVariables().contains(childId)) {
				childExpr = toOWLClassExpression(getDefinition(childId, definitions), definitions);
			} else {
				childExpr = getClassFor(childId);
			}

			OWLObjectProperty objectProperty = getObjectPropertyFor(atomId);
			expr = dataFactory.getOWLObjectSomeValuesFrom(objectProperty, childExpr);

		} else {
			expr = getClassFor(atomId);
		}

		return expr;
	}

	private Set<Integer> getDefinition(Integer atomId, Set<Definition> definitions) {
		for (Definition definition : definitions) {
			if (definition.getDefiniendum().equals(atomId)) {
				return definition.getRight();
			}
		}
		throw new IllegalArgumentException("Atom has no definition.");
	}

	private OWLClassExpression toOWLClassExpression(Set<Integer> atomIds, Set<Definition> definitions) {
		OWLClassExpression ret = null;

		if (atomIds.isEmpty()) {

			ret = dataFactory.getOWLThing();

		} else if (atomIds.size() == 1) {

			ret = getOWLClassExpression(atomIds.iterator().next(), definitions);

		} else {

			Set<OWLClassExpression> classExpressions = new HashSet<OWLClassExpression>();
			for (Integer atomId : atomIds) {
				classExpressions.add(getOWLClassExpression(atomId, definitions));
			}
			ret = dataFactory.getOWLObjectIntersectionOf(classExpressions);

		}

		return ret;
	}

}
