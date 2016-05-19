package de.tudresden.inf.lat.uel.core.processor;

import java.lang.reflect.InvocationTargetException;
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
	private final Map<Integer, Integer> directSupertype = new HashMap<Integer, Integer>();
	private final Set<Disequation> disequations = new HashSet<Disequation>();
	private final Set<Dissubsumption> dissubsumptions = new HashSet<Dissubsumption>();
	private final Map<Integer, Set<Integer>> domains = new HashMap<Integer, Set<Integer>>();
	private final boolean enforceUniqueExistentialRestrictions;
	private final Set<Equation> equations = new HashSet<Equation>();
	private final Set<Set<Integer>> ideals = new HashSet<Set<Integer>>();
	private UelOntology ontology;
	private final Map<Integer, Set<Integer>> ranges = new HashMap<Integer, Set<Integer>>();
	private final Map<Integer, Integer> roleGroupTypes = new HashMap<Integer, Integer>();
	private final Set<Subsumption> subsumptions = new HashSet<Subsumption>();
	private final Map<Integer, Integer> typeAssignment = new HashMap<Integer, Integer>();
	private final Set<Integer> types = new HashSet<Integer>();

	/**
	 * Construct a new goal for UEL.
	 * 
	 * @param manager
	 *            the global AtomManager to be used for storage and indexing of
	 *            all 'local' flat atoms
	 * @param ontology
	 *            the background ontology
	 * @param enforceUniqueExistentialRestrictions
	 */
	public UelOntologyGoal(AtomManager manager, UelOntology ontology, boolean enforceUniqueExistentialRestrictions) {
		this.atomManager = manager;
		this.ontology = ontology;
		this.enforceUniqueExistentialRestrictions = enforceUniqueExistentialRestrictions;
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
		if (!enforceUniqueExistentialRestrictions) {
			return true;
		}

		if (!atomManager.getDefinitionVariables().contains(atomId1) && !atomManager.getConstants().contains(atomId1)) {
			return true;
		}
		if (!atomManager.getDefinitionVariables().contains(atomId2) && !atomManager.getConstants().contains(atomId2)) {
			return true;
		}

		if (atomManager.getUndefNames().contains(atomId1)) {
			atomId1 = atomManager.removeUndef(atomId1);
		}
		if (atomManager.getUndefNames().contains(atomId2)) {
			atomId2 = atomManager.removeUndef(atomId2);
		}

		Integer type1 = types.contains(atomId1) ? atomId1 : typeAssignment.get(atomId1);
		Integer type2 = types.contains(atomId2) ? atomId2 : typeAssignment.get(atomId2);
		if ((type1 == null) || (type2 == null) || areDisjoint(type1, type2)) {
			return true;
		}

		if (atomId1.equals(atomId2)) {
			return true;
		}

		for (Set<Integer> ideal : ideals) {
			if (ideal.contains(atomId1) && ideal.contains(atomId2)) {
				return true;
			}
		}

		return false;
	}

	private <S, T> Set<T> collectSets(Set<S> input, Function<S, Set<T>> mapper) {
		return input.stream().map(mapper).flatMap(Set::stream).collect(Collectors.toSet());
	}

	private <S, T> Set<T> collectSets(Set<S> input, Predicate<S> filter, Function<S, Set<T>> mapper) {
		return input.stream().filter(filter).map(mapper).flatMap(Set::stream).collect(Collectors.toSet());
	}

	public void computeCompatibilityRelation(StringRenderer renderer) {
		Set<Integer> processed = new HashSet<Integer>();
		Set<Integer> toProcess = new HashSet<Integer>(
				Sets.union(atomManager.getDefinitionVariables(), atomManager.getConstants()));
		while (!toProcess.isEmpty()) {
			Integer varId = toProcess.iterator().next();
			Set<Integer> superclasses = ontology.getKnownSuperclasses(varId);
			ideals.add(superclasses);
			processed.addAll(superclasses);
			toProcess.removeAll(processed);
		}

		printIdeals(renderer);

		// minimize the set of ideals
		Set<Set<Integer>> notMaximal = new HashSet<Set<Integer>>();
		for (Set<Integer> ideal1 : ideals) {
			for (Set<Integer> ideal2 : ideals) {
				if (!ideal1.equals(ideal2) && ideal1.containsAll(ideal2)) {
					notMaximal.add(ideal2);
				}
			}
		}
		ideals.removeAll(notMaximal);

		printIdeals(renderer);
	}

	private void printIdeals(StringRenderer renderer) {
		int i = 1;
		for (Set<Integer> ideal : ideals) {
			System.out.println(renderer.renderAtomList("Ideal " + i, ideal));
			i++;
		}
		System.out.println("#####################");
		System.out.println();
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

	@Override
	public boolean enforceUniqueExistentialRestrictions() {
		return enforceUniqueExistentialRestrictions;
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
	 * @param renderer
	 *            for debugging
	 * @return he set of IDs of the UNDEF variables introduced to directly
	 *         define the siblings (i.e., not the ones belonging to their
	 *         superclasses)
	 */
	public Set<Integer> extractSiblings(StringRenderer renderer) {
		// find all parents of leaves (ids that are not used in other defs) that
		// do not occur in the goal
		Set<Integer> leafIds = filterSet(Sets.union(atomManager.getDefinitionVariables(), atomManager.getConstants()),
				id -> !types.contains(id) && isLeaf(id) && notInGoal(id));
		System.out.println(renderer.renderAtomList("Leaves", leafIds));

		// pull in all siblings of leaves from ontology
		Set<OWLClass> siblings = collectSets(leafIds, id -> true, ontology::getSiblings);
		Set<Integer> siblingIds = processClasses(siblings, false);
		System.out.println(renderer.renderAtomList("Siblings", siblingIds));

		// return all UNDEF variables created for the siblings' definitions
		// (only the "most specific" ones)
		return collectSets(siblingIds, id -> true, this::getTopLevelUndefIds);
	}

	private void extractTopLevelTypes() {
		// extract all used top-level concept names from the background
		// ontology; skip those that only occur in the goal or are UNDEF names
		new HashSet<Integer>(Sets.union(atomManager.getVariables(), atomManager.getConstants())).stream()
				.map(ontology::getClassification).filter(Optional::isPresent).map(Optional::get).forEach(types::add);
	}

	private void extractTypeHierarchy() {
		// designate the top concept as the most general type
		types.add(ontology.getTop(true));

		// extract type hierarchy
		for (Integer type : types) {
			// traverse the class hierarchy and try to find another type
			Set<Integer> supertypes = ontology.getMostSpecificSuperclasses(type, types);
			if (supertypes.size() > 1) {
				throw new RuntimeException("A type can only have one direct supertype.");
			}
			if (supertypes.size() == 1) {
				directSupertype.put(type, supertypes.iterator().next());
			}
		}
	}

	private void extractTypeAssignment() {
		for (Integer conceptNameId : Sets.union(atomManager.getConstants(), atomManager.getVariables())) {
			if (!types.contains(conceptNameId)) {
				Set<Integer> supertypes = ontology.getMostSpecificSuperclasses(conceptNameId, types);
				if (supertypes.size() > 1) {
					throw new RuntimeException("A concept name cannot belong to different types.");
				}
				if (supertypes.size() == 1) {
					typeAssignment.put(conceptNameId, supertypes.iterator().next());
				}
			}
		}
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
			if (type.equals(ontology.getTop(true))) {
				// keep the top concept as the unique most general type
				roleGroupTypes.put(type, type);
			} else {
				roleGroupTypes.put(type, atomManager.createRoleGroupConceptName(type));
			}
		}
		for (Integer type : types) {
			Integer supertype = directSupertype.get(type);
			if (supertype != null) {
				directSupertype.put(roleGroupTypes.get(type), roleGroupTypes.get(supertype));
			}
		}

		// finally add all newly created types to the collection
		types.addAll(roleGroupTypes.values());

		// change the domains of all roles to the corresponding 'role group
		// types'
		for (Integer type : domains.keySet()) {
			Set<Integer> domain = domains.get(type);
			domains.put(type, mapSet(domain, roleGroupTypes::get));
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
}
