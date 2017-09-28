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
import org.semanticweb.owlapi.model.AxiomType;
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

import com.google.common.collect.Sets;

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

	private static Function<Set<OWLClassExpression>, OWLClassExpression> constructIntersection = e -> OWLManager
			.getOWLDataFactory().getOWLObjectIntersectionOf(e);

	private static final String flatteningVariablePrefix = "var";

	private Set<OWLClass> getNamedDisjuncts(OWLClassExpression e) {
		return e.asDisjunctSet().stream().filter(expr -> !expr.isAnonymous()).map(expr -> expr.asOWLClass())
				.filter(cls -> !getDirectSuperclasses(cls).isEmpty()).collect(Collectors.toSet());
	}

	private static <R> Function<Set<OWLClassExpression>, R> exception(String message) {
		return e -> {
			throw new RuntimeException(message);
		};
	}

	private final AtomManager atomManager;
	private boolean expandPrimitiveDefinitions;
	private int flatteningVariableIndex = 0;
	private final Map<Integer, OWLClass> nameMap = new HashMap<Integer, OWLClass>();
	private final Set<OWLOntology> ontologies;
	private final OWLClass top;
	private final Set<Integer> visited = new HashSet<Integer>();
	private final Map<Set<Integer>, Integer> flatteningInvMap = new HashMap<Set<Integer>, Integer>();

	/**
	 * Construct a new UEL ontology
	 * 
	 * @param atomManager
	 *            the atom manager
	 * @param ontologies
	 *            the background ontologies
	 * @param top
	 *            the top concept (or an alias)
	 * @param expandPrimitiveDefinitions
	 *            indicates whether to expand simple primitive definitions, or
	 *            mark the subclass as a constant
	 */
	public UelOntology(AtomManager atomManager, Set<OWLOntology> ontologies, OWLClass top,
			boolean expandPrimitiveDefinitions) {
		this.atomManager = atomManager;
		this.ontologies = ontologies;
		this.top = top;
		this.expandPrimitiveDefinitions = expandPrimitiveDefinitions;
	}

	private Optional<OWLClass> checkUsedClass(Integer conceptNameId) {
		// check if the given concept name occurs in the background ontologies
		IRI iri = IRI.create(atomManager.printConceptName(conceptNameId));
		if (!ontologies.stream().anyMatch(ont -> ont.containsClassInSignature(iri))) {
			return Optional.empty();
		}
		return Optional.of(OWLManager.getOWLDataFactory().getOWLClass(iri));
	}

	private Integer classToId(OWLClass cls, boolean onlyTypes) {
		return atomManager.createConceptName(cls.toStringID(), onlyTypes);
	}

	private Integer createOrReuseFlatteningDefinition(Set<Integer> atomIds, Set<Definition> newDefinitions) {
		Integer varId = flatteningInvMap.get(atomIds);
		if (varId == null) {
			varId = createFreshFlatteningVariable();
			flatteningInvMap.put(atomIds, varId);
			newDefinitions.add(new Definition(varId, atomIds, false));
		}
		return varId;
	}

	private Integer createFreshFlatteningVariable() {
		String str = flatteningVariablePrefix + flatteningVariableIndex;
		flatteningVariableIndex++;
		Integer varId = atomManager.createConceptName(str, false);
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

	private Set<Integer> flattenClass(OWLClass cls, Set<Integer> newNames, boolean onlyTypes) {
		Integer atomId = classToId(cls, onlyTypes);
		if (!visited.contains(atomId)) {
			// only consider new concept names that have not yet been processed
			newNames.add(atomId);
			nameMap.put(atomId, cls);
		}
		return Collections.singleton(atomId);
	}

	private Set<Integer> flattenClassExpression(OWLClassExpression expression, Set<Definition> newDefinitions,
			Set<Integer> newNames, boolean onlyTypes) {
		if (expression instanceof OWLClass) {
			return flattenClass((OWLClass) expression, newNames, onlyTypes);
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
		Set<Integer> atomIds = new HashSet<>();
		for (OWLClassExpression operand : conjunction.getOperands()) {
			atomIds.addAll(flattenClassExpression(operand, newDefinitions, newNames, false));
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
		Set<Integer> fillerIds = flattenClassExpression(existentialRestriction.getFiller(), newDefinitions, newNames,
				false);
		Integer fillerId = null;

		if (fillerIds.size() == 0) {
			// the empty conjunction is top
			fillerId = getTop();
		} else if (fillerIds.size() == 1) {
			fillerId = fillerIds.iterator().next();
		}

		if ((fillerId == null) || !atomManager.getAtom(fillerId).isConceptName()) {
			// if we have more than one atom id in 'fillerIds' or the only atom
			// id is not a concept name, then we need a separate definition in
			// order to obtain a flat atom
			fillerId = createOrReuseFlatteningDefinition(fillerIds, newDefinitions);
		}

		Integer atomId = atomManager.createExistentialRestriction(roleName, fillerId);
		return Collections.singleton(atomId);
	}

	/**
	 * Retrieve the top-level concept name (below top) above the given concept
	 * name in the subsumption hierarchy. This only applies to concept names
	 * that appear in the background ontologies.
	 * 
	 * @param conceptNameId
	 *            the atom id of the concept name
	 * @return an Optional object containing the atom id of the top-level
	 *         concept name, if it exists, and an empty Optional otherwise
	 */
	public Optional<Integer> getClassification(Integer conceptNameId) {
		Optional<OWLClass> currentClass = checkUsedClass(conceptNameId);
		// extract the atom representing the top-level hierarchy that
		// 'currentClass' is contained in
		Optional<OWLClass> previousClass = Optional.empty();
		while (currentClass.isPresent() && !currentClass.get().equals(top)) {
			previousClass = currentClass;
			Set<OWLClass> classes = getDirectSuperclasses(previousClass.get());
			if (classes.isEmpty()) {
				currentClass = Optional.empty();
			} else {
				currentClass = Optional.of(classes.iterator().next());
			}
		}
		return previousClass.map(cls -> classToId(cls, true));
	}

	public boolean isSubclass(Integer subId, Integer superId) {
		Optional<OWLClass> subclass = checkUsedClass(subId);
		Optional<OWLClass> superclass = checkUsedClass(superId);
		if (!(subclass.isPresent() && superclass.isPresent())) {
			return false;
		}
		return isSubclass(subclass.get(), superclass.get());
	}

	private boolean isSubclass(OWLClass subclass, OWLClass superclass) {
		if (subclass.equals(superclass)) {
			return true;
		}
		if (subclass.equals(top)) {
			return false;
		}
		return getDirectSuperclasses(subclass).stream().anyMatch(cls -> isSubclass(cls, superclass));
	}

	public OWLClassExpression getDefinition(OWLClass cls) {
		return extractInformation(ont -> getDefinition(ont, cls),
				exception("Multiple candidate definitions found for class: " + cls), Function.identity(), () -> null);
	}

	private Stream<OWLClassExpression> getDefinition(OWLOntology ont, OWLClass cls) {
		return ont.getEquivalentClassesAxioms(cls).stream().flatMap(ax -> ax.getClassExpressionsMinus(cls).stream());
	}

	public Set<Integer> getKnownSuperclasses(Integer atomId) {
		Optional<OWLClass> cls = checkUsedClass(atomId);
		if (!cls.isPresent()) {
			return Collections.singleton(atomId);
		}

		Set<Integer> ret = new HashSet<Integer>();
		ret.add(atomId);
		Set<OWLClass> superclasses = new HashSet<OWLClass>();
		superclasses.add(cls.get());

		while (!superclasses.isEmpty()) {
			superclasses = superclasses.stream().flatMap(c -> getDirectSuperclasses(c).stream()).peek(c -> {
				if (nameMap.containsValue(c)) {
					ret.add(classToId(c, true));
				}
			}).collect(Collectors.toSet());
		}

		return ret;
	}

	public Set<Integer> getMostSpecificSuperclasses(Integer conceptNameId, Set<Integer> types) {
		Optional<OWLClass> cls = checkUsedClass(conceptNameId);
		if (!cls.isPresent()) {
			return Collections.emptySet();
		}

		Set<OWLClass> typeClasses = new HashSet<OWLClass>();
		for (Integer type : types) {
			Optional<OWLClass> typeClass = checkUsedClass(type);
			if (typeClass.isPresent()) {
				typeClasses.add(typeClass.get());
			}
		}

		Set<Integer> ret = new HashSet<Integer>();
		Set<OWLClass> superclasses = new HashSet<OWLClass>();
		superclasses.add(cls.get());

		while (!superclasses.isEmpty()) {
			superclasses = superclasses.stream().flatMap(c -> getDirectSuperclasses(c).stream()).filter(c -> {
				if (typeClasses.contains(c)) {
					ret.add(classToId(c, true));
					return false;
				} else {
					return true;
				}
			}).collect(Collectors.toSet());
		}

		return ret;
	}

	public Set<OWLClass> getDirectSuperclasses(OWLClass cls) {
		if (cls.equals(top)) {
			return Collections.emptySet();
		}

		OWLClassExpression def = getDefinition(cls);
		if (def == null) {
			def = getPrimitiveDefinition(cls);
		}

		Set<OWLClass> superclasses = new HashSet<OWLClass>();
		if (def != null) {
			for (OWLClassExpression expr : def.asConjunctSet()) {
				if (!expr.isAnonymous()) {
					superclasses.add(expr.asOWLClass());
				}
			}
		}
		return superclasses;
	}

	/**
	 * Retrieve the domain of a given role from the background ontologies.
	 * 
	 * @param roleId
	 *            the role id
	 * @return a set containing all named disjuncts of the domain restriction
	 *         for the role, or 'null' if the role has no domain restriction
	 */
	public Set<OWLClass> getDomain(Integer roleId) {
		OWLObjectProperty prop = toOWLObjectProperty(roleId);
		return extractInformation(ont -> getDomain(ont, prop),
				exception("Multiple candidate domains found for property: " + prop), this::getNamedDisjuncts,
				() -> null);
	}

	private Stream<OWLClassExpression> getDomain(OWLOntology ont, OWLObjectProperty prop) {
		return ont.getObjectPropertyDomainAxioms(prop).stream().map(ax -> ax.getDomain());
	}

	/**
	 * Retrieve the siblings of the given concept name from the background
	 * ontologies, but only those that have not yet been processed.
	 * 
	 * @param conceptNameId
	 *            the atom id of the concept name
	 * @param union
	 *            indicates whether to return the union ('true') or the
	 *            intersection ('false') of the sets of siblings w.r.t.
	 *            different superclasses
	 * @param limit
	 *            sets of siblings greater than this number will not be returned
	 * @param includePrimitiveDefinitions
	 *            indicates whether to include siblings that only have a
	 *            primitive definition
	 * @return the set of all new siblings
	 */
	public Set<OWLClass> getSiblings(Integer conceptNameId, boolean union, int limit,
			boolean includePrimitiveDefinitions) {
		Optional<OWLClass> cls = checkUsedClass(conceptNameId);
		if (!cls.isPresent()) {
			return Collections.emptySet();
		} else {
			Stream<Set<OWLClass>> siblingSets = getDirectSuperclasses(cls.get()).stream()
					.map(parent -> extractInformation(ont -> getOtherChildren(ont, parent, includePrimitiveDefinitions),
							Function.identity(), Collections::singleton, Collections::emptySet));

			if (union && (limit >= 0)) {
				// for the union, first filter out sets above the limit
				siblingSets = siblingSets.filter(set -> set.size() <= limit);
			}

			Set<OWLClass> collected = siblingSets.reduce(union ? Sets::union : Sets::intersection)
					.orElse(Collections.emptySet());

			if ((!union) && (limit >= 0) && (collected.size() > limit)) {
				// for the intersection, apply the filter only after combining
				// the sets
				return Collections.emptySet();
			} else {
				return collected;
			}
		}
	}

	private Stream<OWLClass> getOtherChildren(OWLOntology ont, OWLClass cls, boolean includePrimitiveDefinitions) {
		Stream<OWLClass> subClasses = ont
				// Get all OWLEquivalentClassesAxioms that mention 'cls', ...
				.getAxioms(OWLEquivalentClassesAxiom.class, cls, Imports.EXCLUDED, Navigation.IN_SUPER_POSITION)
				// ... do not directly mention 'cls', ...
				.stream()
				// .peek(ax -> System.out.println("1 " + ax))
				.filter(ax -> !ax.getClassExpressions().contains(cls))
				// .peek(ax -> System.out.println("2 " + ax))
				// ... but contain 'cls' as a conjunct in one of the equivalent
				// expressions.
				.filter(ax -> ax.getClassExpressions().stream().anyMatch(expr -> expr.asConjunctSet().contains(cls)))
				// .peek(ax -> System.out.println("3 " + ax))
				// Then get the concept name that this axiom defines.
				.map(ax -> ax.getNamedClasses().iterator().next());
		// .peek(c -> System.out.println("4 " + c));

		// TODO ignore primitive definitions?
		if (includePrimitiveDefinitions) {
			subClasses = Stream
					.concat(subClasses,
							ont
									// Get all OWLSubClassOfAxioms that mention
									// 'cls', ...
									.getAxioms(OWLSubClassOfAxiom.class, cls, Imports.EXCLUDED,
											Navigation.IN_SUPER_POSITION)
									.stream()
									// ... are not a primitive defintion of
									// 'cls', ...
									.filter(ax -> !ax.getSubClass().equals(cls))
									// ... but contain 'cls' as a conjunct of
									// the superclass expression.
									.filter(ax -> ax.getSuperClass().asConjunctSet().contains(cls))
									// Then get the concept name that this axiom
									// defines.
									.map(ax -> ax.getSubClass()).filter(expr -> !expr.isAnonymous())
									.map(expr -> expr.asOWLClass()));
		}

		return subClasses.filter(c -> !nameMap.containsValue(c));
		// .peek(c -> System.out.println("5 " + c));
		// return subClasses1.filter(c -> !nameMap.containsValue(c));
	}

	public OWLClassExpression getPrimitiveDefinition(OWLClass cls) {
		return extractInformation(ont -> getPrimitiveDefinition(ont, cls), constructIntersection, Function.identity(),
				() -> null);
	}

	private Stream<OWLClassExpression> getPrimitiveDefinition(OWLOntology ont, OWLClass cls) {
		return ont.getSubClassAxiomsForSubClass(cls).stream().map(ax -> ax.getSuperClass());
	}

	/**
	 * Retrieve the range of a given role from the background ontologies.
	 * 
	 * @param roleId
	 *            the role id
	 * @return a set containing all named disjuncts of the range restriction for
	 *         the role, or 'null' if the role has no range restriction
	 */
	public Set<OWLClass> getRange(Integer roleId) {
		OWLObjectProperty prop = toOWLObjectProperty(roleId);
		return extractInformation(ont -> getRange(ont, prop),
				exception("Multiple candidate ranges found for property: " + prop), this::getNamedDisjuncts,
				() -> null);
	}

	private Stream<OWLClassExpression> getRange(OWLOntology ont, OWLObjectProperty prop) {
		return ont.getObjectPropertyRangeAxioms(prop).stream().map(ax -> ax.getRange());
	}

	/**
	 * Retrieve the atom id of the top concept.
	 * 
	 * @return top id
	 */
	public Integer getTop() {
		return classToId(top, false);
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
			if (!expandPrimitiveDefinitions) {
				// stop expanding primitive definitions and simply make the
				// class constant
				if (!expression.isAnonymous()) {
					return;
				}
			}
		}

		Set<Integer> right = flattenClassExpression(expression, newDefinitions, toVisit, false);
		atomManager.makeDefinitionVariable(id);
		newDefinitions.add(new Definition(id, right, primitive));
	}

	/**
	 * Recursively translate an OWL class expression into UEL's atom ids and
	 * definitions.
	 * 
	 * @param expression
	 *            the input expression
	 * @param newDefinitions
	 *            a set of new definitions produced by this method
	 * @param onlyTypes
	 *            only types
	 * @return the UEL representation of the input expression, as a set
	 *         (conjunction) of atom ids
	 */
	public Set<Integer> processClassExpression(OWLClassExpression expression, Set<Definition> newDefinitions,
			boolean onlyTypes) {
		Set<Integer> toVisit = new HashSet<Integer>();
		Set<Integer> conjunction = flattenClassExpression(expression, newDefinitions, toVisit, onlyTypes);

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

	/**
	 * Returns the definitions using a set of variable identifiers.
	 * 
	 * @param varIds
	 *            variable identifiers
	 * @return the definitions
	 */
	public Set<OWLClass> extractDefinitionsUsing(Set<Integer> varIds) {
		Set<OWLClass> classes = varIds.stream().map(this::checkUsedClass).filter(cls -> cls.isPresent())
				.map(cls -> cls.get()).collect(Collectors.toSet());
		return extractInformation(o -> f(o, classes), Function.identity(), cls -> Collections.singleton(cls),
				() -> Collections.emptySet());
		// TODO also for full definitions!
	}

	Stream<OWLClass> f(OWLOntology o, Set<OWLClass> classes) {
		Stream<OWLSubClassOfAxiom> s2 = o.getAxioms(AxiomType.SUBCLASS_OF).stream();
		return s2
				.filter(ax -> Sets.intersection(ax.getSuperClass().asConjunctSet(), classes).size() > 1
						&& !ax.getSubClass().isAnonymous())
				.map(ax -> ax.getSubClass().asOWLClass()).filter(cls -> !nameMap.containsValue(cls));
	}

}
