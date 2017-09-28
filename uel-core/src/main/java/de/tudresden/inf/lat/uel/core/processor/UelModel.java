package de.tudresden.inf.lat.uel.core.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.tudresden.inf.lat.uel.core.processor.UelOptions.UndefBehavior;
import de.tudresden.inf.lat.uel.core.processor.UelOptions.Verbosity;
import de.tudresden.inf.lat.uel.core.renderer.OWLRenderer;
import de.tudresden.inf.lat.uel.core.renderer.StringRenderer;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.UnificationAlgorithm;
import de.tudresden.inf.lat.uel.type.impl.AtomManagerImpl;
import de.tudresden.inf.lat.uel.type.impl.DefinitionSet;
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
			return OWLManager.createOWLOntologyManager().createOntology(IRI.create("empty:"));
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e);
		}
	}

	private UnificationAlgorithm algorithm;
	private boolean allUnifiersFound;
	private AtomManager atomManager;
	private int currentUnifierIndex;
	private UelOntologyGoal goal;
	private UelOptions options;
	private UnifierPostprocessor postprocessor;
	private OntologyProvider provider;
	private List<Unifier> unifierList;

	/**
	 * Constructs a new UEL model.
	 * 
	 * @param provider
	 *            the OntologyProvider that should be used to load ontologies
	 *            and short forms
	 * @param options
	 *            the UelOptions that specify the parameters for unification
	 */
	public UelModel(OntologyProvider provider, UelOptions options) {
		this.provider = provider;
		this.options = options;
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

	public void cleanupUnificationAlgorithm() {
		if (algorithm != null) {
			algorithm.cleanup();
			algorithm = null;
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
		boolean first = false;
		if (!allUnifiersFound) {
			while (algorithm.computeNextUnifier()) {
				first = false;

				if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				Unifier result = algorithm.getUnifier();
				if (options.minimizeSolutions) {
					result = postprocessor.minimizeUnifier(result);
				}

				if (options.noEquivalentSolutions && !isNew(result)) {
					if (options.verbosity == Verbosity.FULL) {
						System.out.print(".");
					}
					continue;
				} else {
					unifierList.add(result);

					if (options.verbosity.level > 1) {
						if (unifierList.size() == 1) {
							first = true;
							printAlgorithmInfo();
						}
					}

					if (options.verbosity.level > 0) {
						System.out
								.println("Unifier " + unifierList.size() + ((options.verbosity.level > 1) ? ":" : ""));
					}

					switch (options.verbosity) {
					case FULL:
						System.out.println(getStringRenderer(null).renderUnifier(result, true, true));
						break;
					case NORMAL:
						System.out.println(printUnifier(result));
						break;
					default:
						break;
					}

					return true;
				}
			}
		}
		allUnifiersFound = true;
		if (options.verbosity.level > 1) {
			if (!first) {
				printAlgorithmInfo();
			}
		}
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

	/**
	 * Translates a given concept name to an atom id. This method adds the
	 * corresponding atom to the atom manager if it was not present before.
	 * 
	 * @param name
	 *            the concept name
	 * @return the id of the atom representing 'name'
	 */
	public Integer getAtomId(String name) {
		return atomManager.createConceptName(name, false);
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
		List<OWLOntology> list = new ArrayList<>();
		list.add(EMPTY_ONTOLOGY);
		list.addAll(provider.getOntologies());
		return list;
	}

	/**
	 * Return the current options.
	 * 
	 * @return the current options
	 */
	public UelOptions getOptions() {
		return options;
	}

	/**
	 * Construct a renderer for output of unifiers etc. as OWL API objects.
	 * 
	 * @param background
	 *            the background definitions used for formatting unifiers
	 * @return a new OWLRenderer object
	 */
	public OWLRenderer getOWLRenderer(DefinitionSet background) {
		return new OWLRenderer(atomManager, background);
	}

	private OWLClass getOWLThing(OWLClass owlThingAlias, boolean snomedMode) {
		if (owlThingAlias != null)
			return owlThingAlias;
		else if (snomedMode)
			// 'SNOMED CT Concept'
			return OWLManager.getOWLDataFactory().getOWLClass(IRI.create(options.snomedCtConceptUri));
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
	public StringRenderer getStringRenderer(DefinitionSet background) {
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
		Set<String> names = new HashSet<>();
		for (Integer varId : atomManager.getUserVariables()) {
			names.add(atomManager.printConceptName(varId));
		}
		return names;
	}

	/**
	 * Initializes the unification algorithm with the current goal.
	 */
	public void initializeUnificationAlgorithm() {
		unifierList = new ArrayList<Unifier>();
		currentUnifierIndex = -1;
		allUnifiersFound = false;
		algorithm = UnificationAlgorithmFactory.instantiateAlgorithm(options.unificationAlgorithmName, goal);
		algorithm.setShortFormMap(getStringRenderer(null)::getShortForm);
		postprocessor = new UnifierPostprocessor(atomManager, goal, getStringRenderer(null));
	}

	private boolean isNew(Unifier newUnifier) throws InterruptedException {
		for (Unifier oldUnifier : unifierList) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			if (postprocessor.areEquivalent(oldUnifier, newUnifier)) {
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

	private void makeAllUndefClassesVariables(boolean userVariables) {
		// first copy the list of constants since we need to modify it
		Set<Integer> constants = new HashSet<Integer>(atomManager.getConstants());
		makeIdsVariables(
				constants.stream().filter(id -> atomManager.printConceptName(id).endsWith(AtomManager.UNDEF_SUFFIX)),
				userVariables);
	}

	private void makeClassesVariables(Stream<OWLClass> variables, boolean addUndefSuffix, boolean userVariables) {
		makeNamesVariables(
				variables.map(
						addUndefSuffix ? (cls -> cls.toStringID() + AtomManager.UNDEF_SUFFIX) : (OWLClass::toStringID)),
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

	private void printAlgorithmInfo() {
		System.out.println("Information about the algorithm:");
		for (Entry<String, String> e : algorithm.getInfo()) {
			System.out.println(e.getKey() + ":");
			System.out.println(e.getValue());
		}
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

	private void printGoalInfo() {
		// output unification problem
		System.out.println("Number of atoms: " + atomManager.size());
		System.out.println("Number of constants: " + atomManager.getConstants().size());
		System.out.println("Number of variables: " + atomManager.getVariables().size());
		System.out.println("Number of user variables: " + atomManager.getUserVariables().size());
		System.out.println("Number of definitions: " + goal.getDefinitions().size());
		System.out.println("Number of equations: " + goal.getEquations().size());
		System.out.println("Number of disequations: " + goal.getDisequations().size());
		System.out.println("Number of subsumptions: " + goal.getSubsumptions().size());
		System.out.println("Number of dissubsumptions: " + goal.getDissubsumptions().size());
		System.out.println("(Dis-)Unification problem:");
		System.out.println(printGoal());
	}

	/**
	 * Prints the given unifier using the default StringRenderer.
	 * 
	 * @param unifier
	 *            a unifier
	 * @return a string representation of the unifier
	 */
	public String printUnifier(Unifier unifier) {
		return getStringRenderer(unifier.getDefinitions()).renderUnifier(unifier, false, true);
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
		return getOWLRenderer(null).renderAxioms(goal.getDefinitions().values());
	}

	/**
	 * Renders the given unifier as a set of OWLAxioms.
	 * 
	 * @param unifier
	 *            a unifier
	 * @return the OWL representation of the unifier
	 */
	public Set<OWLAxiom> renderUnifier(Unifier unifier) {
		return getOWLRenderer(unifier.getDefinitions()).renderUnifier(unifier, false, false);
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
	 * @param userVariables
	 *            a set of OWLClasses to be marked as user variables
	 * @param resetShortFormCache
	 *            indicates whether the cached short forms should be reloaded
	 *            from the OntologyProvider
	 */
	public void setupGoal(Set<OWLOntology> bgOntologies, OWLOntology positiveProblem, OWLOntology negativeProblem,
			OWLOntology constraintOntology, Set<OWLClass> userVariables, boolean resetShortFormCache) {

		atomManager = new AtomManagerImpl();

		if (resetShortFormCache) {
			resetShortFormCache();
		}

		OWLClass owlThing = getOWLThing(options.owlThingAlias, options.snomedMode);
		goal = new UelOntologyGoal(atomManager,
				new UelOntology(atomManager, bgOntologies, owlThing, options.expandPrimitiveDefinitions),
				getStringRenderer(null), options.snomedRoleGroupUri, options.snomedCtConceptUri);

		if (positiveProblem != null) {
			goal.addPositiveAxioms(positiveProblem.getAxioms());
		}
		if (negativeProblem != null) {
			goal.addNegativeAxioms(negativeProblem.getAxioms());
		}

		// define 'owlThing' as the empty conjunction
		goal.addDefinition(owlThing, OWLManager.getOWLDataFactory().getOWLObjectIntersectionOf());

		// extract types from background ontologies
		if (options.snomedMode) {
			// System.out.println(printGoal());
			goal.extractSiblings(options.numberOfSiblings);
			goal.extractTypes();
			goal.introduceBlankExistentialRestrictions();
			goal.extractCompatibilityRelation();
			goal.introduceRoleNumberRestrictions(options.numberOfRoleGroups);
		}

		goal.setRestrictUndefContext(options.restrictUndefContext);

		if (constraintOntology != null) {
			goal.addNegativeAxioms(constraintOntology.getAxioms());
		}

		if (userVariables != null) {
			makeClassesVariables(userVariables.stream(), false, true);
		}

		if (options.undefBehavior == UndefBehavior.USER_VARIABLES) {
			makeAllUndefClassesVariables(true);
		}

		if (options.undefBehavior == UndefBehavior.INTERNAL_VARIABLES) {
			makeAllUndefClassesVariables(false);
		}

		if (options.verbosity.level > 1) {
			printGoalInfo();
		}

		goal.disposeOntology();
		if (resetShortFormCache) {
			cacheShortForms();
		}
	}
}
