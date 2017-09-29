package de.tudresden.inf.lat.uel.core.processor;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.google.common.collect.Sets;

import de.tudresden.inf.lat.uel.core.renderer.StringRenderer;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Disequation;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.Subsumption;
import de.tudresden.inf.lat.uel.type.impl.DefinitionSet;

/**
 * This class is a goal of unification.
 * 
 * @author Barbara Morawska
 * @author Julian Mendez
 * @author Stefan Borgwardt
 */
public class UelOntologyGoal implements Goal {

	private final AtomManager atomManager;
	private final DefinitionSet definitions = new DefinitionSet();

	private final Map<Integer, Integer> directSupertype = new HashMap<>();
	private final Set<Disequation> disequations = new HashSet<>();
	private final Set<Dissubsumption> dissubsumptions = new HashSet<>();
	private final Map<Integer, Set<Integer>> domains = new HashMap<>();
	private final Set<Equation> equations = new HashSet<>();
	private final Map<Integer, Set<Integer>> subsumers = new HashMap<>();
	private UelOntology ontology;
	private final Map<Integer, Set<Integer>> ranges = new HashMap<>();
	private StringRenderer renderer;
	private boolean restrictUndefContext = false;
	private final Map<Integer, Integer> roleGroupTypes = new HashMap<>();
	private final Map<Integer, Integer> roleNumberRestrictions = new HashMap<>();
	private final Set<Subsumption> subsumptions = new HashSet<>();
	private final Map<Integer, Integer> typeAssignment = new HashMap<>();
	private final Set<Integer> types = new HashSet<Integer>();
	private final String snomedRoleGroupUri, snomedCtConceptUri;

	/**
	 * Construct a new goal for UEL.
	 * 
	 * @param manager
	 *            the global AtomManager to be used for storage and indexing of
	 *            all 'local' flat atoms
	 * @param renderer
	 *            renderer
	 * @param ontology
	 *            the background ontology
	 */
	public UelOntologyGoal(AtomManager manager, UelOntology ontology, StringRenderer renderer,
			String snomedRoleGroupUri, String snomedCtConceptUri) {
		this.atomManager = manager;
		this.ontology = ontology;
		this.renderer = renderer;
		this.top = ontology.getTop();
		this.snomedRoleGroupUri = snomedRoleGroupUri;
		this.snomedCtConceptUri = snomedCtConceptUri;
	}

	private final Integer top;

	@Override
	public boolean isTop(Integer id) {
		return top.equals(id);
	}

	private void addDefinition(Definition definition) {
		definitions.add(definition);
	}

	/**
	 * Add a new definition to the goal. Actually, this is considered to be part
	 * of the background ontology. If you want to add a definition to the goal,
	 * use 'addEquation' instead.
	 * 
	 * @param definiendum
	 *            the class to be defined
	 * @param definiens
	 *            the definition of the class
	 */
	public void addDefinition(OWLClass definiendum, OWLClassExpression definiens) {
		Definition newDefinition = createAxiom(Definition.class, definiendum, definiens);
		addDefinition(newDefinition);
		atomManager.makeDefinitionVariable(newDefinition.getDefiniendum());
	}

	/**
	 * Add a new disequation to the goal.
	 * 
	 * @param axiom
	 *            the disequation encoded as an OWLEquivalentClassesAxiom
	 */
	public void addDisequation(OWLEquivalentClassesAxiom axiom) {
		disequations.add(createAxiom(Disequation.class, axiom));
	}

	/**
	 * Add a new dissubsumption to the goal.
	 * 
	 * @param axiom
	 *            the dissubsumption encoded as an OWLSubClassOfAxiom
	 */
	public void addDissubsumption(OWLSubClassOfAxiom axiom) {
		dissubsumptions.add(createAxiom(Dissubsumption.class, axiom));
	}

	/**
	 * Add a new equation to the goal.
	 * 
	 * @param axiom
	 *            the equation encoded as an OWLEquivalentClassesAxiom
	 */
	public void addEquation(OWLEquivalentClassesAxiom axiom) {
		equations.add(createAxiom(Equation.class, axiom));
	}

	/**
	 * Add a set of negative goal axioms.
	 * 
	 * @param axioms
	 *            the goal axioms encoded as either OWLSubClassOfAxioms (for
	 *            dissubsumptions) or OWLEquivalentClassesAxioms (for
	 *            disequations)
	 */
	public void addNegativeAxioms(Set<? extends OWLAxiom> axioms) {
		for (OWLAxiom axiom : axioms) {
			if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
				addDisequation((OWLEquivalentClassesAxiom) axiom);
			} else if (axiom.isOfType(AxiomType.SUBCLASS_OF)) {
				addDissubsumption((OWLSubClassOfAxiom) axiom);
			} else {
				// ignore all other axioms
			}
		}
	}

	/**
	 * Add a set of positive goal axioms.
	 * 
	 * @param axioms
	 *            the goal axioms encoded as either OWLSubClassOfAxioms (for
	 *            subsumptions) or OWLEquivalentClassesAxioms (for equations)
	 */
	public void addPositiveAxioms(Set<? extends OWLAxiom> axioms) {
		for (OWLAxiom axiom : axioms) {
			if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
				addEquation((OWLEquivalentClassesAxiom) axiom);
			} else if (axiom.isOfType(AxiomType.SUBCLASS_OF)) {
				addSubsumption((OWLSubClassOfAxiom) axiom);
			} else {
				// ignore all other axioms
			}
		}
	}

	/**
	 * Add a new subsumption to the goal.
	 * 
	 * @param axiom
	 *            the subsumption encoded as an OWLSubClassOfAxiom
	 */
	public void addSubsumption(OWLSubClassOfAxiom axiom) {
		subsumptions.add(createAxiom(Subsumption.class, axiom));
	}

	@Override
	public boolean areCompatible(Integer atomId1, Integer atomId2) {
		if (subsumers.isEmpty()) {
			// no compatibility information was extracted
			return true;
		}

		if (atomId1.equals(atomId2)) {
			// every concept name is compatible with itself
			return true;
		}

		if (!(atomManager.getDefinitionVariables().contains(atomId1)
				&& atomManager.getDefinitionVariables().contains(atomId2))) {
			// compatibility is only checked for definition variables
			return true;
		}

		return subsumers.values().stream().anyMatch(ideal -> ideal.contains(atomId1) && ideal.contains(atomId2));
	}

	private <S, T> Set<T> collectSets(Set<S> input, Function<S, Set<T>> mapper) {
		return input.stream().map(mapper).flatMap(Set::stream).collect(Collectors.toSet());
	}

	private <S, T> Set<T> collectSets(Set<S> input, Predicate<S> filter, Function<S, Set<T>> mapper) {
		return input.stream().filter(filter).map(mapper).flatMap(Set::stream).collect(Collectors.toSet());
	}

	private <T extends Axiom> T createAxiom(Class<T> type, OWLClassExpression left, OWLClassExpression right) {
		Set<Definition> newDefinitions = new HashSet<Definition>();
		Set<Integer> leftIds = ontology.processClassExpression(left, newDefinitions, false);
		Set<Integer> rightIds = ontology.processClassExpression(right, newDefinitions, false);
		T newAxiom;
		try {
			newAxiom = type.getConstructor(Set.class, Set.class).newInstance(leftIds, rightIds);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		processDefinitions(newDefinitions);
		return newAxiom;
	}

	private <T extends Axiom> T createAxiom(Class<T> type, OWLEquivalentClassesAxiom axiom) {
		Iterator<OWLClassExpression> it = axiom.getClassExpressions().iterator();
		return createAxiom(type, it.next(), it.next());
	}

	private <T extends Axiom> T createAxiom(Class<T> type, OWLSubClassOfAxiom axiom) {
		return createAxiom(type, axiom.getSubClass(), axiom.getSuperClass());
	}

	/**
	 * Remove the reference to the background ontology once it is no longer
	 * used.
	 */
	public void disposeOntology() {
		ontology = null;
	}

	/**
	 * Extract the compatibility relation for concept names from the definitions
	 * of the background ontology.
	 */
	public void extractCompatibilityRelation() {
		for (Integer varId : Sets.difference(
				Sets.union(atomManager.getDefinitionVariables(), atomManager.getConstants()),
				atomManager.getUndefNames())) {
			subsumers.put(varId, Sets.union(getAllSuperclasses(varId), getAssertedSuperclasses(varId)));
		}

		// printIdeals(renderer);

		// remove redundant ideals
		// Set<Set<Integer>> notMaximal = new HashSet<Set<Integer>>();
		// for (Set<Integer> ideal1 : ideals) {
		// for (Set<Integer> ideal2 : ideals) {
		// if (!ideal1.equals(ideal2) && ideal1.containsAll(ideal2)) {
		// notMaximal.add(ideal2);
		// }
		// }
		// }
		// ideals.removeAll(notMaximal);

		// printIdeals(renderer);
	}

	private Set<Integer> getAllSuperclasses(Integer varId) {
		UnifierPostprocessor processor = new UnifierPostprocessor(atomManager, this, renderer);
		return new HashSet<Integer>(
				Sets.difference(Sets.union(atomManager.getDefinitionVariables(), atomManager.getConstants()),
						atomManager.getUndefNames())).stream().filter(atomId -> processor.isSubsumed(varId, atomId))
								.collect(Collectors.toSet());
	}

	private Set<Integer> getAssertedSuperclasses(Integer varId) {
		return subsumptions.stream().filter(sub -> (sub.getLeft().size() == 1 && sub.getLeft().contains(varId)))
				.flatMap(sub -> sub.getRight().stream())
				.filter(id -> !atomManager.getExistentialRestrictions().contains(id))
				.flatMap(id -> getAllSuperclasses(id).stream()).collect(Collectors.toSet());
	}

	private void extractDomainsAndRanges() {
		// extract all types from domain/range restrictions of used role names
		Set<Integer> processedRoleIds = new HashSet<Integer>();
		Set<Integer> remainingRoleIds = new HashSet<Integer>(atomManager.getRoleIds());
		while (!remainingRoleIds.isEmpty()) {
			for (Integer roleId : remainingRoleIds) {
				processedRoleIds.add(roleId);

				Set<OWLClass> domainClasses = ontology.getDomain(roleId);
				if (domainClasses != null) {
					Set<Integer> domain = processClasses(domainClasses, true);
					domains.put(roleId, domain);
					types.addAll(domain);
				}

				Set<OWLClass> rangeClasses = ontology.getRange(roleId);
				if (rangeClasses != null) {
					Set<Integer> range = processClasses(rangeClasses, true);
					ranges.put(roleId, range);
					types.addAll(range);
				}
			}
			remainingRoleIds = new HashSet<Integer>(atomManager.getRoleIds());
			remainingRoleIds.removeAll(processedRoleIds);
		}
	}

	/**
	 * Extract 'sibling' classes for all defined classes that are not otherwise
	 * used (in other definitions), and do not occur directly in the
	 * user-specified goal.
	 * 
	 * @param limit
	 *            limit the number of siblings extracted for each leaf; leaves
	 *            with more siblings will be ignored
	 */
	public void extractSiblings(int limit) {
		// find all parents of leaves (ids that are not used in other defs) that
		// do not occur in the goal
		Set<Integer> leafIds = filterSet(Sets.union(atomManager.getDefinitionVariables(), atomManager.getConstants()),
				id -> !types.contains(id) && isLeaf(id) && notInGoal(id));
		// System.out.println(renderer.renderAtomList("Leaves", leafIds));

		// pull in all siblings of leaves from ontology
		// Set<OWLClass> siblings = collectSets(leafIds, id -> true, id ->
		// ontology.getSiblings(id, true));
		// Set<Integer> siblingIds = processClasses(siblings, false);
		for (Integer leaf : leafIds) {
			Set<OWLClass> s1 = ontology.getSiblings(leaf, true, limit, true);
			processClasses(s1, false);
			// System.out.println(renderer.renderAtomList("Siblings of " +
			// renderer.renderAtom(leaf, false), s2));
		}

		// return all UNDEF variables created for the siblings' definitions
		// (only the "most specific" ones)
		// return collectSets(siblingIds, id -> true,
		// this::getTopLevelUndefIds);
	}

	private void extractTopLevelTypes() {
		// extract all used top-level concept names from the background
		// ontology; skip those that only occur in the goal or are UNDEF names
		new HashSet<Integer>(Sets.union(atomManager.getVariables(), atomManager.getConstants())).stream()
				.map(ontology::getClassification).filter(Optional::isPresent).map(Optional::get).forEach(types::add);
	}

	private void extractTypeAssignment() {
		// for (Integer conceptNameId : Sets.union(atomManager.getConstants(),
		// atomManager.getVariables())) {
		// if (!types.contains(conceptNameId)) {
		// Set<Integer> supertypes =
		// ontology.getMostSpecificSuperclasses(conceptNameId, types);
		// List<Integer> minimalTypes = UnifierPostprocessor
		// .minimize((s, t) -> subtypeOrEquals(s, t) ? -1 : (subtypeOrEquals(t,
		// s) ? 1 : 0), supertypes);
		// if (minimalTypes.size() > 1) {
		// System.out.println(renderer.renderAtomList(
		// "Extracted types for " + renderer.renderAtom(conceptNameId, false),
		// minimalTypes));
		// throw new RuntimeException("A concept name cannot belong to different
		// types.");
		// }
		// if (minimalTypes.size() == 1) {
		// typeAssignment.put(conceptNameId, minimalTypes.iterator().next());
		// }
		// }
		// }
	}

	private void extractTypeHierarchy() {
		// designate the top concept as the most general type
		// types.add(ontology.getTop());

		// extract type hierarchy
		// for (Integer type : types) {
		// // traverse the class hierarchy and try to find another type
		// Set<Integer> supertypes = ontology.getMostSpecificSuperclasses(type,
		// types);
		// if (supertypes.size() > 1) {
		// throw new RuntimeException("A type can only have one direct
		// supertype.");
		// }
		// if (supertypes.size() == 1) {
		// directSupertype.put(type, supertypes.iterator().next());
		// }
		// }
	}

	/**
	 * Extract (SNOMED) type information from domain and range restrictions and
	 * the concept definitions.
	 */
	public void extractTypes() {
		extractDomainsAndRanges();
		extractTopLevelTypes();
		extractTypeHierarchy();
		introduceRoleGroupTypes();
		// System.out.println(renderer.renderGoal(this, true));
		extractTypeAssignment();
	}

	private <S> Set<S> filterSet(Set<S> input, Predicate<S> filter) {
		return input.stream().filter(filter).collect(Collectors.toSet());
	}

	@Override
	public AtomManager getAtomManager() {
		return atomManager;
	}

	@Override
	public Set<Integer> getDefiniens(Integer varId) {
		return definitions.getDefiniens(varId);
	}

	@Override
	public Definition getDefinition(Integer varId) {
		return definitions.getDefinition(varId);
	}

	@Override
	public DefinitionSet getDefinitions() {
		return definitions;
	}

	@Override
	public Integer getDirectSupertype(Integer type) {
		return directSupertype.get(type);
	}

	@Override
	public Set<Disequation> getDisequations() {
		return disequations;
	}

	@Override
	public Set<Dissubsumption> getDissubsumptions() {
		return dissubsumptions;
	}

	@Override
	public Map<Integer, Set<Integer>> getDomains() {
		return domains;
	}

	@Override
	public Set<Equation> getEquations() {
		return equations;
	}

	@Override
	public Map<Integer, Set<Integer>> getRanges() {
		return ranges;
	}

	@Override
	public Map<Integer, Integer> getRoleGroupTypes() {
		return roleGroupTypes;
	}

	@Override
	public Map<Integer, Integer> getRoleNumberRestrictions() {
		return roleNumberRestrictions;
	}

	@Override
	public Set<Subsumption> getSubsumptions() {
		return subsumptions;
	}

	private Set<Integer> getTopLevelUndefIds(Integer atomId) {
		// extract most specific UNDEF names used in the definition of 'atomId'
		Definition def = definitions.getDefinition(atomId);
		if (def == null) {
			return Collections.emptySet();
		}

		Integer undefId = getUndefIdFromPrimitiveDefinition(def);
		if (undefId != null) {
			return Collections.singleton(undefId);
		} else {
			return collectSets(def.getRight(), id -> atomManager.getExistentialRestrictions().contains(id),
					id -> getTopLevelUndefIds(atomManager.getChild(id)));
		}
	}

	@Override
	public Map<Integer, Integer> getTypeAssignment() {
		return typeAssignment;
	}

	@Override
	public Set<Integer> getTypes() {
		return types;
	}

	private Integer getUndefIdFromPrimitiveDefinition(Definition definition) {
		for (Integer atomId : definition.getRight()) {
			if (atomManager.getConstants().contains(atomId)) {
				if (atomManager.printConceptName(atomId).endsWith(AtomManager.UNDEF_SUFFIX)) {
					return atomId;
				}
			}
		}
		return null;
	}

	public boolean hasNegativePart() {
		return !disequations.isEmpty() || !dissubsumptions.isEmpty();
	}

	/**
	 * Introduce 'blank' existential restrictions, one for each used role name,
	 * to be used in local solutions.
	 */
	public void introduceBlankExistentialRestrictions() {
		for (Integer roleId : atomManager.getRoleIds()) {
			atomManager.createBlankExistentialRestriction(roleId);
		}
	}

	private void introduceRoleGroupTypes() {
		// copy type hierarchy
		for (Integer type : types) {
			// if (!type.equals(top)) {
			roleGroupTypes.put(type, atomManager.createRoleGroupConceptName(type));
			// }
		}
		// for (Integer type : types) {
		// Integer supertype = directSupertype.get(type);
		// if (supertype != null) {
		// directSupertype.put(roleGroupTypes.get(type),
		// roleGroupTypes.get(supertype));
		// }
		// }

		// finally add all newly created types to the collection
		types.addAll(roleGroupTypes.values());

		// change the domains of all roles to the corresponding 'role group
		// types'
		for (Integer roleId : domains.keySet()) {
			String roleName = atomManager.getRoleName(roleId);
			// 'Laterality', 'Has dose form', and 'Has active ingredient' do not
			// occur inside RoleGroups
			if (!roleName.contains("272741003") && !roleName.contains("411116001") && !roleName.contains("127489000")) {
				Set<Integer> domain = domains.get(roleId);
				domains.put(roleId, mapSet(domain, roleGroupTypes::get));
			}
		}
	}

	/**
	 * Initialize the restrictions on the number of simultaneous role
	 * restrictions in one substitution set.
	 * 
	 * @param numberOfRoleGroups
	 *            the number of 'RoleGroups' allowed (0 - unlimited)
	 */
	public void introduceRoleNumberRestrictions(int numberOfRoleGroups) {
		for (Integer roleId : atomManager.getRoleIds()) {
			String roleName = atomManager.getRoleName(roleId);
			if (roleName.equals(snomedRoleGroupUri)) {
				// restrict the number of RoleGroups in the same substitution
				// set
				roleNumberRestrictions.put(roleId, numberOfRoleGroups);
			} else if (roleName.contains("363698007") || roleName.contains("116676008")) {
				// a RoleGroup may contain several 'Finding sites' or
				// 'Associated morphologies'
				roleNumberRestrictions.put(roleId, 0);
			} else {
				// all other roles may only have one restriction per RoleGroup
				roleNumberRestrictions.put(roleId, 1);
			}
		}
	}

	private boolean isLeaf(Integer atomId) {
		return !definitions.values().stream().anyMatch(d -> d.getRight().contains(atomId));
	}

	private <S, T> Set<T> mapSet(Set<S> input, Function<S, T> mapper) {
		return input.stream().map(mapper).collect(Collectors.toSet());
	}

	private <S, T> Set<T> mapSet(Set<S> input, Predicate<S> filter, Function<S, Optional<T>> mapper) {
		return input.stream().filter(filter).map(mapper).filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toSet());
	}

	private boolean notInAxioms(Set<? extends Axiom> axioms, Integer atomId) {
		return axioms.stream()
				.allMatch(axiom -> !axiom.getLeft().contains(atomId) && !axiom.getRight().contains(atomId));
	}

	private boolean notInGoal(Integer atomId) {
		return notInAxioms(equations, atomId) && notInAxioms(subsumptions, atomId) && notInAxioms(disequations, atomId)
				&& notInAxioms(dissubsumptions, atomId);
	}

	// private void printIdeals(StringRenderer renderer) {
	// int i = 1;
	// for (Set<Integer> ideal : ideals) {
	// System.out.println(renderer.renderAtomList("Ideal " + i, ideal));
	// i++;
	// }
	// System.out.println("#####################");
	// System.out.println();
	// }

	private Set<Integer> processClasses(Set<OWLClass> classes, boolean onlyTypes) {
		Set<Definition> newDefinitions = new HashSet<Definition>();
		Set<Integer> classIds = new HashSet<Integer>();
		for (OWLClass newClass : classes) {
			classIds.addAll(ontology.processClassExpression(newClass, newDefinitions, onlyTypes));
		}
		processDefinitions(newDefinitions);
		return classIds;
	}

	private void processDefinitions(Set<Definition> newDefinitions) {
		for (Definition newDefinition : newDefinitions) {
			// only full definitions are allowed
			if (newDefinition.isPrimitive()) {
				addDefinition(processPrimitiveDefinition(newDefinition));
			} else {
				addDefinition(newDefinition);
			}
		}
	}

	private Definition processPrimitiveDefinition(Definition def) {
		Integer defId = def.getDefiniendum();
		Integer undefId = atomManager.createUndefConceptName(defId);

		// create full definition
		Set<Integer> newRightIds = new HashSet<Integer>(def.getRight());
		newRightIds.add(undefId);
		return new Definition(defId, newRightIds, false);
	}

	@Override
	public boolean restrictUndefContext() {
		return restrictUndefContext;
	}

	/**
	 * Indicate whether the UNDEF names should be restricted to the context of
	 * their original definitions.
	 * 
	 * @param restrictUndefContext
	 *            the option value
	 */
	public void setRestrictUndefContext(boolean restrictUndefContext) {
		this.restrictUndefContext = restrictUndefContext;
	}

	@Override
	public String SNOMED_RoleGroup_URI() {
		return snomedRoleGroupUri;
	}

	@Override
	public String SNOMED_CT_Concept_URI() {
		return snomedCtConceptUri;
	}

	@Override
	public boolean isCommonSubsumee(Integer subsumee, Integer subsumer1, Integer subsumer2) {
		Set<Integer> ideal = subsumers.get(subsumee);
		if (ideal == null) {
			return false;
		}
		return ideal.containsAll(Arrays.asList(subsumer1, subsumer2));
	}
}
