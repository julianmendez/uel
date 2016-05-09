package de.tudresden.inf.lat.uel.core.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.tudresden.inf.lat.uel.core.renderer.OWLRenderer;
import de.tudresden.inf.lat.uel.core.renderer.StringRenderer;
import de.tudresden.inf.lat.uel.sat.solver.SatUnificationAlgorithm;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.UnificationAlgorithm;
import de.tudresden.inf.lat.uel.type.impl.AtomManagerImpl;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

/**
 * An object of this class connects the graphical user interface with the
 * unification algorithm.
 * 
 * @author Julian Mendez
 * @author Stefan Borgwardt
 */
public class UelModel {

	/**
	 * A special 'empty' ontology used in the selection combo boxes.
	 */
	public static final OWLOntology EMPTY_ONTOLOGY = createEmptyOntology();

	private static OWLOntology createEmptyOntology() {
		try {
			return OWLManager.createOWLOntologyManager().createOntology(IRI.create("empty"));
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean allUnifiersFound;
	private AtomManager atomManager;
	private int currentUnifierIndex;
	private UelOntologyGoal goal;
	private OntologyProvider provider;
	private UnificationAlgorithm algorithm;
	private List<Unifier> unifierList;

	/**
	 * Constructs a new UEL model.
	 * 
	 * @param provider
	 *            the OntologyProvider that should be used to load ontologies
	 *            and short forms
	 */
	public UelModel(OntologyProvider provider) {
		this.provider = provider;
	}

	/**
	 * Indicates whether the unification algorithm has finished its search for
	 * unifiers.
	 * 
	 * @return 'true' iff the unification algorithm does not compute any more
	 *         unifiers
	 */
	public boolean allUnifiersFound() {
		return allUnifiersFound;
	}

	private void cacheShortForms() {
		for (Integer atomId : atomManager.getConstants()) {
			provider.getShortForm(atomManager.printConceptName(atomId));
		}
		for (Integer atomId : atomManager.getVariables()) {
			provider.getShortForm(atomManager.printConceptName(atomId));
		}
		for (Integer roleId : atomManager.getRoleIds()) {
			provider.getShortForm(atomManager.getRoleName(roleId));
		}
	}

	/**
	 * Uses the unification algorithm to obtain the next unifier. This method
	 * iterates as long as the unification algorithm returns unifiers that are
	 * already in the unifier list.
	 * 
	 * @return 'true' if a new unifier was found
	 * @throws InterruptedException
	 *             if the computation in this thread was interrupted from
	 *             outside
	 */
	public boolean computeNextUnifier() throws InterruptedException {
		if (!allUnifiersFound) {
			while (algorithm.computeNextUnifier()) {
				Unifier result = algorithm.getUnifier();
				if (isNew(result)) {
					unifierList.add(result);
					// System.out.println(getStringRenderer(null).renderUnifier(result,
					// true, true, true));
					// System.out.println(printUnifier(result));
					return true;
				}
			}
		}
		allUnifiersFound = true;
		return false;
	}

	/**
	 * Creates a new anonymous ontology containing all axioms of the given one.
	 * 
	 * @param original
	 *            the original ontology
	 * @return the new ontology
	 */
	public OWLOntology createOntology(OWLOntology original) {
		try {
			OWLOntology newOntology = OWLManager.createOWLOntologyManager().createOntology();
			newOntology.getOWLOntologyManager().addAxioms(newOntology, original.getAxioms());
			return newOntology;
		} catch (OWLOntologyCreationException ex) {
			throw new RuntimeException(ex);
		}
	}

	private boolean equalsModuloUserVariables(Set<Definition> defs1, Set<Definition> defs2) {
		// since both unifiers must define all variables, it suffices to check
		// one inclusion
		for (Definition def1 : defs1) {
			if (atomManager.getUserVariables().contains(def1.getDefiniendum())) {
				if (!defs2.contains(def1)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Translates a given concept name to an atom id. This method adds the
	 * corresponding atom to the atom manager if it was not present before.
	 * 
	 * @param name
	 *            the concept name
	 * @return the id of the atom representing 'name'
	 */
	public Integer getAtomId(String name) {
		return atomManager.createConceptName(name);
	}

	/**
	 * Return the currently selected unifier.
	 * 
	 * @return the current unifier
	 */
	public Unifier getCurrentUnifier() {
		if (unifierList.size() == 0) {
			return null;
		}
		return unifierList.get(currentUnifierIndex);
	}

	/**
	 * Return the index of the currently selected unifier.
	 * 
	 * @return the current unifier index
	 */
	public int getCurrentUnifierIndex() {
		return currentUnifierIndex;
	}

	/**
	 * Return the goal used for the unification algorithm.
	 * 
	 * @return the goal
	 */
	public Goal getGoal() {
		return goal;
	}

	/**
	 * Return the list of currently loaded ontologies, including a special
	 * 'empty' ontology.
	 * 
	 * @return the list of loaded ontologies
	 */
	public List<OWLOntology> getOntologyList() {
		List<OWLOntology> list = new ArrayList<OWLOntology>();
		list.add(EMPTY_ONTOLOGY);
		list.addAll(provider.getOntologies());
		return list;
	}

	/**
	 * Construct a renderer for output of unifiers etc. as OWL API objects.
	 * 
	 * @param background
	 *            the background definitions used for formatting unifiers
	 * @return a new OWLRenderer object
	 */
	public OWLRenderer getOWLRenderer(Set<Definition> background) {
		return new OWLRenderer(atomManager, background);
	}

	private OWLClass getOWLThing(OWLClass owlThingAlias, boolean snomedMode) {
		if (owlThingAlias != null)
			return owlThingAlias;
		else if (snomedMode)
			// 'SNOMED CT Concept'
			return OWLManager.getOWLDataFactory().getOWLClass(IRI.create("http://www.ihtsdo.org/SCT_138875005"));
		else
			// owl:Thing
			return OWLManager.getOWLDataFactory().getOWLThing();
	}

	/**
	 * Construct a renderer for output of unifiers etc. as strings. The actual
	 * format is specified in the method 'StringRenderer.createInstance'.
	 * 
	 * @param background
	 *            the background definitions used for formatting unifiers
	 * @return a new StringRenderer object
	 */
	public StringRenderer getStringRenderer(Set<Definition> background) {
		return StringRenderer.createInstance(atomManager, provider, background);
	}

	/**
	 * Returns the current unification algorithm, if it has already been
	 * initialized.
	 * 
	 * @return the current unification algorithm, or 'null'
	 */
	public UnificationAlgorithm getUnificationAlgorithm() {
		return algorithm;
	}

	/**
	 * Returns the list of all unifiers that have already been computed.
	 * 
	 * @return the current list of unifiers
	 */
	public List<Unifier> getUnifierList() {
		return Collections.unmodifiableList(unifierList);
	}

	/**
	 * Returns the names of all concept names marked as user variables.
	 * 
	 * @return the current set of user variable names
	 */
	public Set<String> getUserVariableNames() {
		Set<String> names = new HashSet<String>();
		for (Integer varId : atomManager.getUserVariables()) {
			names.add(atomManager.printConceptName(varId));
		}
		return names;
	}

	/**
	 * Initializes the unification algorithm with the current goal.
	 * 
	 * @param name
	 *            The string identifier of the unification algorithm, as defined
	 *            by 'UnificationAlgorithmFactory'
	 */
	public void initializeUnificationAlgorithm(String name) {
		unifierList = new ArrayList<Unifier>();
		currentUnifierIndex = -1;
		allUnifiersFound = false;
		algorithm = UnificationAlgorithmFactory.instantiateAlgorithm(name, goal);

		// TODO for debugging
		if (algorithm instanceof SatUnificationAlgorithm) {
			((SatUnificationAlgorithm) algorithm).setShortFormMap(getStringRenderer(null)::getShortForm);
		}
	}

	private boolean isNew(Unifier result) {
		for (Unifier unifier : unifierList) {
			if (equalsModuloUserVariables(unifier.getDefinitions(), result.getDefinitions())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Loads an ontology from a file and adds it to the set of currently loaded
	 * ontologies.
	 * 
	 * @param file
	 *            the input file
	 */
	public void loadOntology(File file) {
		provider.loadOntology(file);
	}

	/**
	 * Marks all 'undef' variables as variables.
	 * 
	 * @param userVariables
	 *            a flag indicating whether to create user variables or
	 *            definition variables
	 */
	public void makeAllUndefClassesVariables(boolean userVariables) {
		// first copy the list of constants since we need to modify it
		Set<Integer> constants = new HashSet<Integer>(atomManager.getConstants());
		makeIdsVariables(
				constants.stream().filter(id -> atomManager.printConceptName(id).endsWith(AtomManager.UNDEF_SUFFIX)),
				userVariables);
	}

	/**
	 * Marks a given set of classes as variables.
	 * 
	 * @param variables
	 *            a set of variables given as instances of OWLClass
	 * @param addUndefSuffix
	 *            a flag indicating whether to add an _UNDEF suffix to the given
	 *            classes
	 * @param userVariables
	 *            a flag indicating whether to create user variables ('true') or
	 *            definition variables ('false')
	 */
	public void makeClassesVariables(Stream<OWLClass> variables, boolean addUndefSuffix, boolean userVariables) {
		makeNamesVariables(variables
				.map(addUndefSuffix ? (cls -> cls.toStringID() + AtomManager.UNDEF_SUFFIX) : (cls -> cls.toStringID())),
				userVariables);
	}

	private void makeIdsVariables(Stream<Integer> variables, boolean userVariables) {
		variables.forEach(userVariables ? atomManager::makeUserVariable : atomManager::makeDefinitionVariable);
	}

	/**
	 * Marks the given concept names as variables.
	 * 
	 * @param variables
	 *            a stream of concept names
	 * @param userVariables
	 *            indicates whether the concept names should be user variables
	 *            ('true') or definition variables ('false')
	 */
	public void makeNamesVariables(Stream<String> variables, boolean userVariables) {
		makeIdsVariables(variables.map(this::getAtomId), userVariables);
	}

	/**
	 * Prints the current unifier using the default StringRenderer.
	 * 
	 * @return a string representation of the current unifier
	 */
	public String printCurrentUnifier() {
		Unifier unifier = getCurrentUnifier();
		if (unifier == null) {
			return "";
		}
		return printUnifier(unifier);
	}

	/**
	 * Prints the goal using the default StringRenderer.
	 * 
	 * @return a string representation of the unification goal
	 */
	public String printGoal() {
		return getStringRenderer(null).renderGoal(goal, true);
	}

	/**
	 * Prints the given unifier using the default StringRenderer.
	 * 
	 * @param unifier
	 *            a unifier
	 * @return a string representation of the unifier
	 */
	public String printUnifier(Unifier unifier) {
		return getStringRenderer(unifier.getDefinitions()).renderUnifier(unifier, true, false, true);
	}

	/**
	 * Renders the current unifier as a set of OWLAxioms.
	 * 
	 * @return the OWL representation of the current unifier
	 */
	public Set<OWLAxiom> renderCurrentUnifier() {
		Unifier unifier = getCurrentUnifier();
		if (unifier == null) {
			return null;
		}
		return renderUnifier(unifier);
	}

	/**
	 * Renders the background definitions as a set of OWLAxioms.
	 * 
	 * @return the OWL representation of the background definitions
	 */
	public Set<OWLAxiom> renderDefinitions() {
		return getOWLRenderer(null).renderAxioms(goal.getDefinitions());
	}

	/**
	 * Renders the given unifier as a set of OWLAxioms.
	 * 
	 * @param unifier
	 *            a unifier
	 * @return the OWL representation of the unifier
	 */
	public Set<OWLAxiom> renderUnifier(Unifier unifier) {
		return getOWLRenderer(unifier.getDefinitions()).renderUnifier(unifier, true, false, false);
	}

	/**
	 * Replace the given atom by its UNDEF version, if it exists; otherwise,
	 * return the input.
	 * 
	 * @param atomId
	 *            the id of the atom
	 * @return the atom, possibly replaced by its UNDEF version
	 */
	public Integer replaceByUndefId(Integer atomId) {
		if (atomManager.getDefinitionVariables().contains(atomId)) {
			for (Integer undefId : goal.getDefinition(atomId).getRight()) {
				if (atomManager.getAtom(undefId).isConceptName()) {
					if (atomManager.printConceptName(undefId).endsWith(AtomManager.UNDEF_SUFFIX)) {
						return undefId;
					}
				}
			}
		}
		return atomId;
	}

	/**
	 * Resets the internal cache of the short form provider.
	 */
	public void resetShortFormCache() {
		provider.resetCache();
	}

	/**
	 * Sets the index of the currently selected unifier in the unifier list.
	 * 
	 * @param index
	 *            the new index
	 */
	public void setCurrentUnifierIndex(int index) {
		if (index < 0) {
			currentUnifierIndex = -1;
		} else if (index >= unifierList.size()) {
			currentUnifierIndex = unifierList.size() - 1;
		} else {
			currentUnifierIndex = index;
		}
	}

	private void setUndefVariablesFromTypes(Set<Integer> siblingUndefIds) {
		for (Integer undefId : atomManager.getUndefNames()) {
			Integer origId = atomManager.removeUndef(undefId);
			if (!goal.getTypes().contains(origId) && !siblingUndefIds.contains(undefId)) {
				// all UNDEF names belonging to types or siblings are
				// constants, all others are variables
				atomManager.makeUserVariable(undefId);
			}
		}
	}

	/**
	 * Initializes the unification goal with the given ontologies.
	 * 
	 * @param bgOntologies
	 *            a set of background ontologies with definitions
	 * @param positiveProblem
	 *            the positive part of the unification problem
	 * @param negativeProblem
	 *            the negative part of the unification problem
	 * @param constraintOntology
	 *            additional negative constraints to be added after all
	 *            pre-processing
	 * @param owlThingAlias
	 *            (optional) an alias for owl:Thing, e.g., 'SNOMED CT Concept'
	 * @param snomedMode
	 *            indicates whether "SNOMED mode" should be activated, loading
	 *            additional type information
	 * @param resetShortFormCache
	 *            indicates whether the cached short forms should be reloaded
	 *            from the OntologyProvider
	 */
	public void setupGoal(Set<OWLOntology> bgOntologies, OWLOntology positiveProblem, OWLOntology negativeProblem,
			OWLOntology constraintOntology, OWLClass owlThingAlias, boolean snomedMode, boolean resetShortFormCache) {

		atomManager = new AtomManagerImpl();

		if (resetShortFormCache) {
			resetShortFormCache();
		}

		OWLClass owlThing = getOWLThing(owlThingAlias, snomedMode);
		goal = new UelOntologyGoal(atomManager, new UelOntology(atomManager, bgOntologies, owlThing));

		if (positiveProblem != null) {
			goal.addPositiveAxioms(positiveProblem.getAxioms());
		}
		if (negativeProblem != null) {
			goal.addNegativeAxioms(negativeProblem.getAxioms());
		}

		// define 'owlThing' as the empty conjunction
		goal.addDefinition(owlThing, OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf());

		// extract types from background ontologies
		if (snomedMode) {
			Set<Integer> siblingUndefIds = goal.extractSiblings();
			goal.extractTypes();
			setUndefVariablesFromTypes(siblingUndefIds);
			goal.introduceDissubsumptionsForUndefVariables();
			goal.introduceBlankExistentialRestrictions();
		}

		if (constraintOntology != null) {
			goal.addNegativeAxioms(constraintOntology.getAxioms());
		}

		goal.disposeOntology();
		if (resetShortFormCache) {
			cacheShortForms();
		}
	}
}
