package de.tudresden.inf.lat.uel.core.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
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
 */
public class UelModel {

	public static final OWLOntology EMPTY_ONTOLOGY = createEmptyOntology();

	private static OWLOntology createEmptyOntology() {
		try {
			return OWLManager.createOWLOntologyManager().createOntology(IRI.create("empty"));
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e);
		}
	}

	public static String removeQuotes(String str) {
		String ret = str;
		if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
			ret = str.substring(1, str.length() - 1);
		}
		return ret;
	}

	private boolean allUnifiersFound;
	private AtomManager atomManager;
	private int currentUnifierIndex;
	private UelOntologyGoal goal;
	private Map<String, String> shortFormMap;
	private OntologyProvider provider;
	private UnificationAlgorithm algorithm;
	private List<Unifier> unifierList;

	public UelModel(OntologyProvider provider) {
		this.provider = provider;
	}

	private void addAllShortForms(OWLOntology ontology, Set<? extends OWLEntity> entities) {
		for (OWLEntity entity : entities) {
			String shortForm = getShortForm(entity, ontology);
			shortFormMap.put(entity.toStringID(), removeQuotes(shortForm));
		}
	}

	public boolean allUnifiersFound() {
		return allUnifiersFound;
	}

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

	public Integer getAtomId(String name) {
		return atomManager.createConceptName(name);
	}

	public Unifier getCurrentUnifier() {
		if (unifierList.size() == 0) {
			return null;
		}
		return unifierList.get(currentUnifierIndex);
	}

	public int getCurrentUnifierIndex() {
		return currentUnifierIndex;
	}

	public Goal getGoal() {
		return goal;
	}

	public List<OWLOntology> getOntologyList() {
		List<OWLOntology> list = new ArrayList<OWLOntology>();
		list.add(EMPTY_ONTOLOGY);
		list.addAll(provider.getOntologies());
		return list;
	}

	public OWLRenderer getOWLRenderer(Set<Definition> background) {
		return new OWLRenderer(atomManager, background);
	}

	private String getShortForm(OWLEntity entity, OWLOntology ontology) {
		if (provider.providesShortForms()) {
			return provider.getShortForm(entity);
		}

		for (OWLAnnotationAssertionAxiom annotation : ontology.getAnnotationAssertionAxioms(entity.getIRI())) {
			if (annotation.getProperty().equals(OWLManager.getOWLDataFactory().getRDFSLabel())) {
				return annotation.getValue().toString();
			}
		}

		return entity.getIRI().getShortForm();
	}

	public StringRenderer getStringRenderer(Set<Definition> background) {
		return StringRenderer.createInstance(atomManager, shortFormMap, background);
	}

	public UnificationAlgorithm getUnificationAlgorithm() {
		return this.algorithm;
	}

	public List<Unifier> getUnifierList() {
		return Collections.unmodifiableList(unifierList);
	}

	public Set<String> getUserVariableNames() {
		Set<String> names = new HashSet<String>();
		for (Integer varId : atomManager.getUserVariables()) {
			names.add(atomManager.printConceptName(varId));
		}
		return names;
	}

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

	public void loadOntology(File file) {
		provider.loadOntology(file);
		recomputeShortFormMap();
	}

	public void makeClassesUserVariables(Set<OWLClass> variables) {
		for (OWLClass var : variables) {
			atomManager.makeUserVariable(getAtomId(var.toStringID()));
		}
	}

	public void makeNamesUserVariables(Set<String> variables) {
		for (String var : variables) {
			atomManager.makeUserVariable(getAtomId(var));
		}
	}

	public void makeUndefClassesUserVariables(Set<OWLClass> variables) {
		for (OWLClass var : variables)
			atomManager.makeUserVariable(getAtomId(var.toStringID() + AtomManager.UNDEF_SUFFIX));
	}

	public void makeUndefNamesUserVariables(Set<String> variables) {
		for (String var : variables) {
			atomManager.makeUserVariable(getAtomId(var + AtomManager.UNDEF_SUFFIX));
		}
	}

	public void markUndefAsUserVariables() {
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

	public String printCurrentUnifier() {
		Unifier unifier = getCurrentUnifier();
		if (unifier == null) {
			return "";
		}
		return printUnifier(unifier);
	}

	public String printGoal() {
		return getStringRenderer(null).renderGoal(goal);
	}

	public String printUnifier(Unifier unifier) {
		return getStringRenderer(unifier.getDefinitions()).renderUnifier(unifier);
	}

	public void recomputeShortFormMap() {
		shortFormMap = new HashMap<String, String>();
		for (OWLOntology ontology : provider.getOntologies()) {
			addAllShortForms(ontology, ontology.getClassesInSignature());
			addAllShortForms(ontology, ontology.getObjectPropertiesInSignature());
		}
	}

	public Set<OWLAxiom> renderCurrentUnifier() {
		Unifier unifier = getCurrentUnifier();
		if (unifier == null) {
			return null;
		}
		return renderUnifier(unifier);
	}

	public Set<OWLAxiom> renderDefinitions() {
		return getOWLRenderer(null).renderAxioms(goal.getDefinitions());
	}

	public Set<OWLAxiom> renderUnifier(Unifier unifier) {
		return getOWLRenderer(unifier.getDefinitions()).renderUnifier(unifier);
	}

	public void setCurrentUnifierIndex(int index) {
		if (index < 0) {
			currentUnifierIndex = -1;
		} else if (index >= unifierList.size()) {
			currentUnifierIndex = unifierList.size() - 1;
		} else {
			currentUnifierIndex = index;
		}
	}

	public void setupGoal(Set<OWLOntology> bgOntologies, OWLOntology positiveProblem, OWLOntology negativeProblem,
			OWLClass owlThingAlias) {
		setupGoal(bgOntologies, positiveProblem.getAxioms(AxiomType.SUBCLASS_OF),
				positiveProblem.getAxioms(AxiomType.EQUIVALENT_CLASSES),
				negativeProblem.getAxioms(AxiomType.SUBCLASS_OF),
				negativeProblem.getAxioms(AxiomType.EQUIVALENT_CLASSES), owlThingAlias);
	}

	public void setupGoal(Set<OWLOntology> bgOntologies, Set<OWLSubClassOfAxiom> subsumptions,
			Set<OWLEquivalentClassesAxiom> equations, Set<OWLSubClassOfAxiom> dissubsumptions,
			Set<OWLEquivalentClassesAxiom> disequations, OWLClass owlThingAlias) {

		algorithm = null;
		unifierList = new ArrayList<Unifier>();
		currentUnifierIndex = -1;
		allUnifiersFound = false;
		atomManager = new AtomManagerImpl();

		recomputeShortFormMap();

		OWLClass top = (owlThingAlias != null) ? owlThingAlias : OWLManager.getOWLDataFactory().getOWLThing();
		goal = new UelOntologyGoal(atomManager, new UelOntology(atomManager, bgOntologies, top));

		goal.addPositiveAxioms(subsumptions);
		goal.addPositiveAxioms(equations);
		goal.addNegativeAxioms(dissubsumptions);
		goal.addNegativeAxioms(disequations);

		// define top as the empty conjunction
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		goal.addEquation(factory.getOWLEquivalentClassesAxiom(top, factory.getOWLObjectIntersectionOf()));
		Integer topId = atomManager.createConceptName(top.toStringID());
		atomManager.makeDefinitionVariable(topId);

		goal.disposeOntology();
	}

}