package de.tudresden.inf.lat.uel.core.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;

/**
 * An object of this class is a UEL ontology that pulls in definitions from
 * background OWL ontologies as needed. All definitions produced by this class
 * will be flat.
 *
 * @author Stefan Borgwardt
 */
public class UelOntology {

	private static final String flatteningVariablePrefix = "var";
	private int flatteningVariableIndex = 0;

	private final Set<Integer> visited = new HashSet<Integer>();
	private final Map<Integer, OWLClass> nameMap = new HashMap<Integer, OWLClass>();
	private final AtomManager atomManager;
	private final Set<OWLOntology> ontologies;
	private final OWLClass owlThingAlias;

	public UelOntology(AtomManager atomManager, Set<OWLOntology> ontologies, OWLClass owlThingAlias) {
		this.atomManager = atomManager;
		this.ontologies = ontologies;
		this.owlThingAlias = owlThingAlias;
	}

	private Integer createFlattenedAtom(String roleName, Set<Integer> atomIds, Set<Definition> newDefinitions) {
		Integer varId = createFreshFlatteningVariable();
		newDefinitions.add(new Definition(varId, atomIds, false));
		return atomManager.createExistentialRestriction(roleName, varId);
	}

	public Integer createFreshFlatteningVariable() {
		String str = flatteningVariablePrefix + flatteningVariableIndex;
		flatteningVariableIndex++;
		Integer varId = atomManager.createConceptName(str);
		atomManager.makeFlatteningVariable(varId);
		return varId;
	}

	private Set<Integer> flattenClass(OWLClass cls, Set<Integer> newNames) {
		Integer atomId = atomManager.createConceptName(cls.toStringID());
		if (!visited.contains(atomId)) {
			// only consider new concept names that have not yet been processed
			newNames.add(atomId);
			nameMap.put(atomId, cls);
		}
		return Collections.singleton(atomId);
	}

	private Set<Integer> flattenClassExpression(OWLClassExpression expression, Set<Definition> newDefinitions,
			Set<Integer> newNames) {
		if (expression instanceof OWLClass) {
			return flattenClass((OWLClass) expression, newNames);
		}
		if (expression instanceof OWLObjectIntersectionOf) {
			return flattenConjunction((OWLObjectIntersectionOf) expression, newDefinitions, newNames);
		}
		if (expression instanceof OWLObjectSomeValuesFrom) {
			return flattenExistentialRestriction((OWLObjectSomeValuesFrom) expression, newDefinitions, newNames);
		}
		throw new RuntimeException("Unsupported class expression: " + expression);
	}

	private Set<Integer> flattenConjunction(OWLObjectIntersectionOf conjunction, Set<Definition> newDefinitions,
			Set<Integer> newNames) {
		Set<Integer> atomIds = new HashSet<Integer>();
		for (OWLClassExpression operand : conjunction.getOperands()) {
			atomIds.addAll(flattenClassExpression(operand, newDefinitions, newNames));
		}
		return atomIds;
	}

	private Set<Integer> flattenExistentialRestriction(OWLObjectSomeValuesFrom existentialRestriction,
			Set<Definition> newDefinitions, Set<Integer> newNames) {
		OWLObjectPropertyExpression propertyExpr = existentialRestriction.getProperty();
		if (propertyExpr.isAnonymous()) {
			throw new RuntimeException("Unsupported object property expression: " + propertyExpr);
		}

		String roleName = propertyExpr.getNamedProperty().toStringID();
		Integer atomId = null;
		Set<Integer> fillerIds = flattenClassExpression(existentialRestriction.getFiller(), newDefinitions, newNames);

		if (fillerIds.size() == 1) {
			Integer fillerId = fillerIds.iterator().next();
			if (atomManager.getAtom(fillerId).isConceptName()) {
				atomId = atomManager.createExistentialRestriction(roleName, fillerId);
			} else {
				atomId = createFlattenedAtom(roleName, fillerIds, newDefinitions);
			}
		} else if (fillerIds.size() > 1) {
			atomId = createFlattenedAtom(roleName, fillerIds, newDefinitions);
		}
		return Collections.singleton(atomId);
	}

	public Set<Integer> processClassExpression(OWLClassExpression expression, Set<Definition> newDefinitions) {
		Set<Integer> toVisit = new HashSet<Integer>();
		Set<Integer> conjunction = flattenClassExpression(expression, newDefinitions, toVisit);

		while (!toVisit.isEmpty()) {
			Integer nameId = toVisit.iterator().next();

			if (!visited.contains(nameId)) {
				createFlatDefinition(nameId, newDefinitions, toVisit);
				visited.add(nameId);
				toVisit.remove(nameId);
			}
		}
		return conjunction;
	}

	private void createFlatDefinition(Integer id, Set<Definition> newDefinitions, Set<Integer> toVisit) {
		OWLClass cls = nameMap.get(id);

		OWLClassExpression definition = loadDefinition(cls);
		OWLClassExpression primitiveDefinition = loadPrimitiveDefinition(cls);
		if ((definition == null) && (primitiveDefinition == null)) {
			return;
		}
		if ((definition != null) && (primitiveDefinition != null)) {
			throw new RuntimeException(
					"The following class has both a full definition and primitive definition(s): " + cls);
		}

		boolean primitive;
		OWLClassExpression expression;
		if (definition != null) {
			primitive = false;
			expression = definition;
		} else {
			// 'primitiveDefinition' is not null
			primitive = true;
			expression = primitiveDefinition;
		}

		Set<Integer> right = flattenClassExpression(expression, newDefinitions, toVisit);
		atomManager.makeDefinitionVariable(id);
		// TODO introduce new 'undef' variables for primitive definitions
		// already here?
		newDefinitions.add(new Definition(id, right, primitive));
	}

	private OWLClassExpression loadDefinition(OWLClass cls) {
		Set<OWLClassExpression> possibleDefinitions = new HashSet<OWLClassExpression>();
		for (OWLOntology ontology : ontologies) {
			for (OWLEquivalentClassesAxiom definingAxiom : ontology.getEquivalentClassesAxioms(cls)) {
				possibleDefinitions.addAll(definingAxiom.getClassExpressionsMinus(cls));
			}
		}
		if (possibleDefinitions.size() < 1) {
			return null;
		}
		if (possibleDefinitions.size() > 1) {
			throw new RuntimeException("Multiple candidate definitions found for class: " + cls);
		}
		return possibleDefinitions.iterator().next();
	}

	private OWLClassExpression loadPrimitiveDefinition(OWLClass cls) {
		Set<OWLClassExpression> allDefinitions = new HashSet<OWLClassExpression>();
		for (OWLOntology ontology : ontologies) {
			for (OWLSubClassOfAxiom definingAxiom : ontology.getSubClassAxiomsForSubClass(cls)) {
				OWLClassExpression superClass = definingAxiom.getSuperClass();
				if (!superClass.isOWLThing() && !superClass.equals(owlThingAlias)) {
					allDefinitions.add(definingAxiom.getSuperClass());
				}
			}
		}
		if (allDefinitions.size() < 1) {
			return null;
		}
		if (allDefinitions.size() > 1) {
			return OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf(allDefinitions);
		}
		return allDefinitions.iterator().next();
	}
}
