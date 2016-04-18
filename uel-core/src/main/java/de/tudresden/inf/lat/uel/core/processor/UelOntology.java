package de.tudresden.inf.lat.uel.core.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.Navigation;

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

	private static Function<OWLClassExpression, Set<OWLClass>> getNamedDisjuncts = e -> e.asDisjunctSet().stream()
			.filter(expr -> !expr.isAnonymous()).map(expr -> expr.asOWLClass()).collect(Collectors.toSet());

	private static final Function<IRI, OWLClass> iriToClass = iri -> OWLManager.getOWLDataFactory().getOWLClass(iri);

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

	private Optional<IRI> checkUsedIRI(Integer conceptNameId) {
		// check if the given concept name occurs in the background ontologies
		IRI iri = IRI.create(atomManager.printConceptName(conceptNameId));
		if (!ontologies.stream().anyMatch(ont -> ont.containsClassInSignature(iri))) {
			return Optional.empty();
		}
		return Optional.of(iri);
	}

	private Integer classToId(OWLClass cls) {
		return atomManager.createConceptName(cls.toStringID());
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

	private <R, S> R extractInformation(Function<OWLOntology, Stream<S>> extractor, Function<Set<S>, R> ifMultiple,
			Function<S, R> ifSingleton, Supplier<R> ifEmpty) {
		Set<S> expr = ontologies.stream().flatMap(extractor).collect(Collectors.toSet());
		if (expr.size() < 1) {
			return ifEmpty.get();
		}
		if (expr.size() > 1) {
			return ifMultiple.apply(expr);
		}
		return ifSingleton.apply(expr.iterator().next());
	}

	private Set<Integer> flattenClass(OWLClass cls, Set<Integer> newNames) {
		Integer atomId = classToId(cls);
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
			fillerId = getTop();
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

	public Optional<Integer> getClassification(Integer conceptNameId) {
		Optional<OWLClass> currentClass = checkUsedIRI(conceptNameId).map(iriToClass);
		// extract the atom representing the top-level hierarchy that
		// 'currentClass' is contained in
		Optional<OWLClass> previousClass = Optional.empty();
		while (currentClass.isPresent() && !currentClass.get().equals(top)) {
			previousClass = currentClass;
			currentClass = getDirectSuperclass(previousClass.get());
		}
		return previousClass.map(this::classToId);
	}

	private OWLClassExpression getDefinition(OWLClass cls) {
		return extractInformation(ont -> getDefinition(ont, cls),
				exception("Multiple candidate definitions found for class: " + cls), Function.identity(), null);
	}

	private Stream<OWLClassExpression> getDefinition(OWLOntology ont, OWLClass cls) {
		return ont.getEquivalentClassesAxioms(cls).stream().flatMap(ax -> ax.getClassExpressionsMinus(cls).stream());
	}

	public Optional<Integer> getDirectSuperclass(Integer conceptNameId) {
		return checkUsedIRI(conceptNameId).map(iriToClass).flatMap(this::getDirectSuperclass).map(this::classToId);
	}

	private Optional<OWLClass> getDirectSuperclass(OWLClass cls) {
		if (cls.equals(top)) {
			return Optional.empty();
		}

		OWLClassExpression def = getDefinition(cls);
		if (def == null) {
			def = getPrimitiveDefinition(cls);
		}

		Optional<OWLClass> superclass = Optional.empty();
		if (def != null) {
			for (OWLClassExpression expr : def.asConjunctSet()) {
				if (!expr.isAnonymous()) {
					superclass = Optional.of(expr.asOWLClass());
				}
			}
		}
		return superclass;
	}

	public Set<OWLClass> getDomain(Integer roleId) {
		OWLObjectProperty prop = toOWLObjectProperty(roleId);
		return extractInformation(ont -> getDomain(ont, prop),
				exception("Multiple candidate domains found for property: " + prop), getNamedDisjuncts, null);
	}

	private Stream<OWLClassExpression> getDomain(OWLOntology ont, OWLObjectProperty prop) {
		return ont.getObjectPropertyDomainAxioms(prop).stream().map(ax -> ax.getDomain());
	}

	public Set<OWLClass> getOtherChildren(Integer parentId) {
		Optional<OWLClass> parentClass = checkUsedIRI(parentId).map(iriToClass);
		if (!parentClass.isPresent()) {
			return Collections.emptySet();
		}

		return extractInformation(ont -> getOtherChildren(ont, parentClass.get()), Function.identity(),
				Collections::<OWLClass> singleton, Collections::emptySet);
	}

	private Stream<OWLClass> getOtherChildren(OWLOntology ont, OWLClass cls) {
		Stream<OWLClass> subClasses1 = ont
				.getAxioms(OWLEquivalentClassesAxiom.class, cls, Imports.EXCLUDED, Navigation.IN_SUPER_POSITION)
				.stream().filter(ax -> !ax.getClassExpressions().contains(cls))
				.map(ax -> ax.getNamedClasses().iterator().next());
		Stream<OWLClass> subClasses2 = ont
				.getAxioms(OWLSubClassOfAxiom.class, cls, Imports.EXCLUDED, Navigation.IN_SUPER_POSITION).stream()
				.filter(ax -> !ax.getSubClass().equals(cls)).map(ax -> ax.getSubClass()).filter(expr -> !expr.isAnonymous())
				.map(expr -> expr.asOWLClass());
		return Stream.concat(subClasses1, subClasses2).filter(c -> !nameMap.containsValue(c));
	}

	private OWLClassExpression getPrimitiveDefinition(OWLClass cls) {
		return extractInformation(ont -> getPrimitiveDefinition(ont, cls), constructIntersection, Function.identity(),
				null);
	}

	private Stream<OWLClassExpression> getPrimitiveDefinition(OWLOntology ont, OWLClass cls) {
		return ont.getSubClassAxiomsForSubClass(cls).stream().map(ax -> ax.getSuperClass());
	}

	public Set<OWLClass> getRange(Integer roleId) {
		OWLObjectProperty prop = toOWLObjectProperty(roleId);
		return extractInformation(ont -> getRange(ont, prop),
				exception("Multiple candidate ranges found for property: " + prop), getNamedDisjuncts, null);
	}

	private Stream<OWLClassExpression> getRange(OWLOntology ont, OWLObjectProperty prop) {
		return ont.getObjectPropertyRangeAxioms(prop).stream().map(ax -> ax.getRange());
	}

	public Integer getTop() {
		return classToId(top);
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
		return OWLManager.getOWLDataFactory().getOWLObjectProperty(roleIRI);
	}

}
