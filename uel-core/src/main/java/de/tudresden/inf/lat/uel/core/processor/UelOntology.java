package de.tudresden.inf.lat.uel.core.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;

/**
 * An object of this class is a UEL ontology that pulls in definitions from
 * background OWL ontologies as needed. All definitions produced by this class
 * will be flat.
 *
 * @author Stefan Borgwardt
 */
class UelOntology {

	private static Function<Set<OWLClassExpression>, OWLClassExpression> constructIntersection = e -> OWLManager
			.getOWLDataFactory().getOWLObjectIntersectionOf(e);

	private static final String flatteningVariablePrefix = "var";

	private static Function<OWLClassExpression, Set<OWLClass>> getNamedDisjuncts = e -> {
		System.out.println(e);
		return e.asDisjunctSet().stream().filter(expr -> !expr.isAnonymous()).map(expr -> expr.asOWLClass())
				.collect(Collectors.toSet());
	};

	private static <R> Function<Set<OWLClassExpression>, R> exception(String message) {
		return e -> {
			throw new RuntimeException(message);
		};
	}

	private final AtomManager atomManager;
	private int flatteningVariableIndex = 0;
	private final Map<Integer, OWLClass> nameMap = new HashMap<Integer, OWLClass>();
	private final Set<OWLOntology> ontologies;
	private final OWLClass top;
	private final Set<Integer> visited = new HashSet<Integer>();

	public UelOntology(AtomManager atomManager, Set<OWLOntology> ontologies, OWLClass top) {
		this.atomManager = atomManager;
		this.ontologies = ontologies;
		this.top = top;
	}

	private Integer createFreshFlatteningDefinition(Set<Integer> atomIds, Set<Definition> newDefinitions) {
		Integer varId = createFreshFlatteningVariable();
		newDefinitions.add(new Definition(varId, atomIds, false));
		return varId;
	}

	private Integer createFreshFlatteningVariable() {
		String str = flatteningVariablePrefix + flatteningVariableIndex;
		flatteningVariableIndex++;
		Integer varId = atomManager.createConceptName(str);
		atomManager.makeFlatteningVariable(varId);
		return varId;
	}

	private <R> R extractInformation(Function<OWLOntology, Stream<OWLClassExpression>> extractor,
			Function<Set<OWLClassExpression>, R> ifMultiple, Function<OWLClassExpression, R> ifSingleton) {
		Set<OWLClassExpression> expr = ontologies.stream().flatMap(extractor).collect(Collectors.toSet());
		if (expr.size() < 1) {
			return null;
		}
		if (expr.size() > 1) {
			return ifMultiple.apply(expr);
		}
		return ifSingleton.apply(expr.iterator().next());
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
		Set<Integer> fillerIds = flattenClassExpression(existentialRestriction.getFiller(), newDefinitions, newNames);
		Integer fillerId = null;

		if (fillerIds.size() == 0) {
			// the empty conjunction is top
			fillerId = atomManager.createConceptName(top.toStringID());
		} else if (fillerIds.size() == 1) {
			fillerId = fillerIds.iterator().next();
		}

		if ((fillerId == null) || !atomManager.getAtom(fillerId).isConceptName()) {
			// if we have more than one atom id in 'fillerIds' or the only atom
			// id is not a concept name, then we need to introduce a new
			// definition in order to obtain a flat atom
			fillerId = createFreshFlatteningDefinition(fillerIds, newDefinitions);
		}

		Integer atomId = atomManager.createExistentialRestriction(roleName, fillerId);
		return Collections.singleton(atomId);
	}

	public Integer getClassification(Integer conceptNameId) {
		// check if the given concept name can have a classification at all (in
		// the background ontologies)
		IRI iri = IRI.create(atomManager.printConceptName(conceptNameId));
		if (!ontologies.stream().anyMatch(ont -> ont.containsClassInSignature(iri))) {
			return null;
		}

		// extract the atom representing the top-level hierarchy that 'iri' is
		// contained in
		OWLClass currentClass = OWLManager.getOWLDataFactory().getOWLClass(iri);
		OWLClass previousClass = null;
		while ((currentClass != null) && !currentClass.equals(top)) {
			previousClass = currentClass;
			currentClass = null;
			OWLClassExpression def = getDefinition(previousClass);
			if (def == null) {
				def = getPrimitiveDefinition(previousClass);
			}
			if (def != null) {
				for (OWLClassExpression expr : def.asConjunctSet()) {
					if (!expr.isAnonymous()) {
						currentClass = expr.asOWLClass();
					}
				}
			}
		}
		return atomManager.createConceptName(previousClass.toStringID());
	}

	private OWLClassExpression getDefinition(OWLClass cls) {
		return extractInformation(ont -> getDefinition(ont, cls),
				exception("Multiple candidate definitions found for class: " + cls), Function.identity());
	}

	private Stream<OWLClassExpression> getDefinition(OWLOntology ont, OWLClass cls) {
		return ont.getEquivalentClassesAxioms(cls).stream().flatMap(ax -> ax.getClassExpressionsMinus(cls).stream());
	}

	public Set<OWLClass> getDomain(Integer roleId) {
		OWLObjectProperty prop = toOWLObjectProperty(roleId);
		return extractInformation(ont -> getDomain(ont, prop),
				exception("Multiple candidate domains found for property: " + prop), getNamedDisjuncts);
	}

	private Stream<OWLClassExpression> getDomain(OWLOntology ont, OWLObjectProperty prop) {
		Set<OWLObjectPropertyDomainAxiom> s = ont.getObjectPropertyDomainAxioms(prop);
		System.out.println("#opda: " + s.size());
		return s.stream().map(ax -> ax.getDomain());
	}

	private OWLClassExpression getPrimitiveDefinition(OWLClass cls) {
		return extractInformation(ont -> getPrimitiveDefinition(ont, cls), constructIntersection, Function.identity());
	}

	private Stream<OWLClassExpression> getPrimitiveDefinition(OWLOntology ont, OWLClass cls) {
		return ont.getSubClassAxiomsForSubClass(cls).stream().map(ax -> ax.getSuperClass()).filter(c -> !c.equals(top));
	}

	public Set<OWLClass> getRange(Integer roleId) {
		OWLObjectProperty prop = toOWLObjectProperty(roleId);
		return extractInformation(ont -> getRange(ont, prop),
				exception("Multiple candidate ranges found for property: " + prop), getNamedDisjuncts);
	}

	private Stream<OWLClassExpression> getRange(OWLOntology ont, OWLObjectProperty prop) {
		Set<OWLObjectPropertyRangeAxiom> s = ont.getObjectPropertyRangeAxioms(prop);
		System.out.println("#opra: " + s.size());
		return s.stream().map(ax -> ax.getRange());
	}

	private void loadFlatDefinition(Integer id, Set<Definition> newDefinitions, Set<Integer> toVisit) {
		OWLClass cls = nameMap.get(id);
		if (cls.equals(top)) {
			// do not expand definitions beyond top
			return;
		}

		OWLClassExpression definition = getDefinition(cls);
		OWLClassExpression primitiveDefinition = getPrimitiveDefinition(cls);
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
		newDefinitions.add(new Definition(id, right, primitive));
	}

	public Set<Integer> processClassExpression(OWLClassExpression expression, Set<Definition> newDefinitions) {
		Set<Integer> toVisit = new HashSet<Integer>();
		Set<Integer> conjunction = flattenClassExpression(expression, newDefinitions, toVisit);

		while (!toVisit.isEmpty()) {
			Integer nameId = toVisit.iterator().next();

			if (!visited.contains(nameId)) {
				loadFlatDefinition(nameId, newDefinitions, toVisit);
				visited.add(nameId);
				toVisit.remove(nameId);
			}
		}
		return conjunction;
	}

	private OWLObjectProperty toOWLObjectProperty(Integer roleId) {
		IRI roleIRI = IRI.create(atomManager.getRoleName(roleId));
		System.out.println(roleIRI.toString());
		return OWLManager.getOWLDataFactory().getOWLObjectProperty(roleIRI);
	}

}
