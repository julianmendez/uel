package de.tudresden.inf.lat.uel.core.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.renderer.OWLRenderer;
import de.tudresden.inf.lat.uel.core.renderer.StringRenderer;
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
					return true;
				}
			}
		}
		allUnifiersFound = true;
		return false;
	}

	/**
	 * Creates a new anonymous ontology.
	 * 
	 * @return the new ontology
	 */
	public OWLOntology createOntology() {
		return provider.createOntology();
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
		algorithm = UnificationAlgorithmFactory.instantiateAlgorithm(name, goal);
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
	 * Marks a given set of variables as user variables.
	 * 
	 * @param variables
	 *            a set of variables given as instances of OWLClass
	 */
	public void makeClassesUserVariables(Set<OWLClass> variables) {
		for (OWLClass var : variables) {
			atomManager.makeUserVariable(getAtomId(var.toStringID()));
		}
	}

	/**
	 * Marks a given set of variables as user variables.
	 * 
	 * @param variables
	 *            a set of variable names
	 */
	public void makeNamesUserVariables(Set<String> variables) {
		for (String var : variables) {
			atomManager.makeUserVariable(getAtomId(var));
		}
	}

	/**
	 * Marks the 'undef' versions of a given set of variables as user variables.
	 * 
	 * @param variables
	 *            a set of variables given as instances of OWLClass
	 */
	public void makeUndefClassesUserVariables(Set<OWLClass> variables) {
		for (OWLClass var : variables)
			atomManager.makeUserVariable(getAtomId(var.toStringID() + AtomManager.UNDEF_SUFFIX));
	}

	/**
	 * Marks the 'undef' versions of a given set of variables as user variables.
	 * 
	 * @param variables
	 *            a set of variable names
	 */
	public void makeUndefNamesUserVariables(Set<String> variables) {
		for (String var : variables) {
			atomManager.makeUserVariable(getAtomId(var + AtomManager.UNDEF_SUFFIX));
		}
	}

	/**
	 * Marks all 'undef' variables as user variables.
	 */
	public void makeAllUndefClassesUserVariables() {
		// mark all "_UNDEF" variables as user variables
		// copy the list of constants since we need to modify it
		Set<Integer> constants = new HashSet<Integer>(atomManager.getConstants());
		for (Integer atomId : constants) {
			String name = atomManager.printConceptName(atomId);
			if (name.endsWith(AtomManager.UNDEF_SUFFIX)) {
				atomManager.makeUserVariable(atomId);
			}
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
		return getStringRenderer(null).renderGoal(goal);
	}

	/**
	 * Prints the given unifier using the default StringRenderer.
	 * 
	 * @param unifier
	 *            a unifier
	 * @return a string representation of the unifier
	 */
	public String printUnifier(Unifier unifier) {
		return getStringRenderer(unifier.getDefinitions()).renderUnifier(unifier);
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
		return getOWLRenderer(unifier.getDefinitions()).renderUnifier(unifier);
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
	 * @param owlThingAlias
	 *            (optional) an alias for owl:Thing, e.g., 'SNOMED CT Concept'
	 * @param snomedMode
	 *            indicates if "SNOMED mode" should be activated, loading
	 *            additional type information
	 */
	public void setupGoal(Set<OWLOntology> bgOntologies, OWLOntology positiveProblem, OWLOntology negativeProblem,
			OWLClass owlThingAlias, boolean snomedMode, boolean resetShortFormCache) {
		setupGoal(bgOntologies, positiveProblem.getAxioms(AxiomType.SUBCLASS_OF),
				positiveProblem.getAxioms(AxiomType.EQUIVALENT_CLASSES),
				negativeProblem.getAxioms(AxiomType.SUBCLASS_OF),
				negativeProblem.getAxioms(AxiomType.EQUIVALENT_CLASSES), owlThingAlias, snomedMode,
				resetShortFormCache);
	}

	/**
	 * Initializes the unification goal with the given ontologies and axioms.
	 * 
	 * @param bgOntologies
	 *            a set of background ontologies with definitions
	 * @param subsumptions
	 *            the goal subsumptions, as OWLSubClassOfAxioms
	 * @param equations
	 *            the goal equations, as binary OWLEquivalentClassesAxioms
	 * @param dissubsumptions
	 *            the goal dissubsumptions, as OWLSubClassOfAxioms
	 * @param disequations
	 *            the goal disequations, as binary OWLEquivalentClassesAxioms
	 * @param owlThingAlias
	 *            (optional) an alias for owl:Thing, e.g., 'SNOMED CT Concept'
	 * @param snomedMode
	 *            indicates if "SNOMED mode" should be activated, loading
	 *            additional type information
	 */
	public void setupGoal(Set<OWLOntology> bgOntologies, Set<OWLSubClassOfAxiom> subsumptions,
			Set<OWLEquivalentClassesAxiom> equations, Set<OWLSubClassOfAxiom> dissubsumptions,
			Set<OWLEquivalentClassesAxiom> disequations, OWLClass owlThingAlias, boolean snomedMode,
			boolean resetShortFormCache) {

		algorithm = null;
		unifierList = new ArrayList<Unifier>();
		currentUnifierIndex = -1;
		allUnifiersFound = false;
		atomManager = new AtomManagerImpl();

		if (resetShortFormCache) {
			resetShortFormCache();
		}

		OWLClass owlThing = getOWLThing(owlThingAlias, snomedMode);
		goal = new UelOntologyGoal(atomManager, new UelOntology(atomManager, bgOntologies, owlThing), snomedMode, provider);

		goal.addPositiveAxioms(subsumptions);
		goal.addPositiveAxioms(equations);
		goal.addNegativeAxioms(dissubsumptions);
		goal.addNegativeAxioms(disequations);

		// define 'owlThing' as the empty conjunction
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		goal.addEquation(factory.getOWLEquivalentClassesAxiom(owlThing, factory.getOWLObjectIntersectionOf()));
		Integer owlThingId = atomManager.createConceptName(owlThing.toStringID());
		atomManager.makeDefinitionVariable(owlThingId);

		// extract types from background ontologies
		if (snomedMode) {
			goal.extractTypes();
		}

		goal.disposeOntology();
		if (resetShortFormCache) {
			cacheShortForms();
		}
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
}
