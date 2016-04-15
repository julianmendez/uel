package de.tudresden.inf.lat.uel.core.processor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Disequation;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.Subsumption;

/**
 * This class is a goal of unification.
 * 
 * @author Barbara Morawska
 * @author Julian Mendez
 * @author Stefan Borgwardt
 */
class UelOntologyGoal implements Goal {

	private final AtomManager atomManager;
	private final Set<Definition> definitions = new HashSet<Definition>();
	private final Map<Integer, Integer> directSupertype = new HashMap<Integer, Integer>();
	private final Set<Disequation> disequations = new HashSet<Disequation>();
	private final Set<Dissubsumption> dissubsumptions = new HashSet<Dissubsumption>();
	private final Map<Integer, Set<Integer>> domains = new HashMap<Integer, Set<Integer>>();
	private final Set<Equation> equations = new HashSet<Equation>();
	private UelOntology ontology;
	private final Map<Integer, Set<Integer>> ranges = new HashMap<Integer, Set<Integer>>();
	private final Map<Integer, Integer> roleGroupTypes = new HashMap<Integer, Integer>();
	private final Set<Subsumption> subsumptions = new HashSet<Subsumption>();

	private final Set<Integer> types = new HashSet<Integer>();

	public UelOntologyGoal(AtomManager manager, UelOntology ontology) {
		this.atomManager = manager;
		this.ontology = ontology;
	}

	public void addDefinition(OWLClass definiendum, OWLClassExpression definiens) {
		Definition newDefinition = createAxiom(Definition.class, definiendum, definiens);
		definitions.add(newDefinition);
		atomManager.makeDefinitionVariable(newDefinition.getDefiniendum());
	}

	public void addDisequation(OWLEquivalentClassesAxiom axiom) {
		disequations.add(createAxiom(Disequation.class, axiom));
	}

	public void addDissubsumption(OWLSubClassOfAxiom axiom) {
		dissubsumptions.add(createAxiom(Dissubsumption.class, axiom));
	}

	public void addEquation(OWLEquivalentClassesAxiom axiom) {
		equations.add(createAxiom(Equation.class, axiom));
	}

	public void addNegativeAxioms(Set<? extends OWLAxiom> axioms) {
		for (OWLAxiom axiom : axioms) {
			if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
				addDisequation((OWLEquivalentClassesAxiom) axiom);
			} else if (axiom.isOfType(AxiomType.SUBCLASS_OF)) {
				addDissubsumption((OWLSubClassOfAxiom) axiom);
			} else {
				throw new RuntimeException("Unsupported axiom type: " + axiom);
			}
		}
	}

	public void addPositiveAxioms(Set<? extends OWLAxiom> axioms) {
		for (OWLAxiom axiom : axioms) {
			if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
				addEquation((OWLEquivalentClassesAxiom) axiom);
			} else if (axiom.isOfType(AxiomType.SUBCLASS_OF)) {
				addSubsumption((OWLSubClassOfAxiom) axiom);
			} else {
				throw new RuntimeException("Unsupported axiom type: " + axiom);
			}
		}
	}

	public void addSubsumption(OWLSubClassOfAxiom axiom) {
		subsumptions.add(createAxiom(Subsumption.class, axiom));
	}

	private <T extends Axiom> T createAxiom(Class<T> type, OWLClassExpression left, OWLClassExpression right) {
		Set<Definition> newDefinitions = new HashSet<Definition>();
		Set<Integer> leftIds = ontology.processClassExpression(left, newDefinitions);
		Set<Integer> rightIds = ontology.processClassExpression(right, newDefinitions);
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

	public void disposeOntology() {
		ontology = null;
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
	}

	private void introduceRoleGroupTypes() {
		// copy type hierarchy
		for (Integer type : types) {
			if (type.equals(ontology.getTop())) {
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
			domains.put(type, domain.stream().map(t -> roleGroupTypes.get(t)).collect(Collectors.toSet()));
		}
	}

	private void extractTypeHierarchy() {
		// designate the top concept as the most general type
		Integer topId = ontology.getTop();
		types.add(topId);

		// extract type hierarchy
		for (Integer type : types) {
			// traverse the class hierarchy and try to find another type
			Optional<Integer> supertype = Optional.of(type);
			do {
				supertype = ontology.getDirectSuperclass(supertype.get());
			} while (supertype.isPresent() && !types.contains(supertype.get()));

			if (supertype.isPresent()) {
				directSupertype.put(type, supertype.get());
			}
		}
	}

	private void extractTopLevelTypes() {
		// extract all used top-level concept names from the background
		// ontology; skip those that only occur in the goal or are UNDEF names
		atomManager.getVariables().stream().map(ontology::getClassification).filter(Optional::isPresent)
				.map(Optional::get).forEach(types::add);
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
					Set<Integer> domain = processClasses(domainClasses);
					domains.put(roleId, domain);
					types.addAll(domain);
				}

				Set<OWLClass> rangeClasses = ontology.getRange(roleId);
				if (rangeClasses != null) {
					Set<Integer> range = processClasses(rangeClasses);
					ranges.put(roleId, range);
					types.addAll(range);
				}
			}
			remainingRoleIds = new HashSet<Integer>(atomManager.getRoleIds());
			remainingRoleIds.removeAll(processedRoleIds);
		}
	}

	@Override
	public AtomManager getAtomManager() {
		return atomManager;
	}

	@Override
	public Set<Definition> getDefinitions() {
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

	@Override
	public Set<Integer> getTypes() {
		return types;
	}

	public boolean hasNegativePart() {
		return !disequations.isEmpty() || !dissubsumptions.isEmpty();
	}

	private Set<Integer> processClasses(Set<OWLClass> classes) {
		Set<Definition> newDefinitions = new HashSet<Definition>();
		Set<Integer> classIds = new HashSet<Integer>();
		for (OWLClass newClass : classes) {
			classIds.addAll(ontology.processClassExpression(newClass, newDefinitions));
		}
		processDefinitions(newDefinitions);
		return classIds;
	}

	private void processDefinitions(Set<Definition> newDefinitions) {
		for (Definition newDefinition : newDefinitions) {
			// only full definitions are allowed
			if (newDefinition.isPrimitive()) {
				definitions.add(processPrimitiveDefinition(newDefinition));
			} else {
				definitions.add(newDefinition);
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
