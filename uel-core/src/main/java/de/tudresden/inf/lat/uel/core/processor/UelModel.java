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
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.type.KRSSRenderer;
import de.tudresden.inf.lat.uel.core.type.UnifierTranslator;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.AtomManagerImpl;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

/**
 * An object of this class connects the graphical user interface with the
 * processor.
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

	private boolean allUnifiersFound;
	private AtomManager atomManager;
	private int currentUnifierIndex;
	private UelOntologyGoal goal;
	private Map<String, String> shortFormMap;
	private OntologyProvider provider;
	private UelProcessor uelProcessor;
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
			while (uelProcessor.computeNextUnifier()) {
				Unifier result = uelProcessor.getUnifier();
				if (!unifierList.contains(result)) {
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

	public void initializeUelProcessor(String name) {
		uelProcessor = UelProcessorFactory.createProcessor(name, goal);
	}

	// public Integer getAtomId(Atom atom) {
	// return atomManager.getAtoms().addAndGetIndex(atom);
	// }

	// public Integer getAtomId(OWLClass owlClass) {
	// return getAtomId(owlClass.toStringID());
	// }

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

	public List<OWLOntology> getOntologyList() {
		List<OWLOntology> list = new ArrayList<OWLOntology>();
		list.add(EMPTY_ONTOLOGY);
		list.addAll(provider.getOntologies());
		return list;
	}

	public KRSSRenderer getRenderer(boolean shortForm) {
		return new KRSSRenderer(atomManager, shortForm ? shortFormMap : null);
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

	public UnifierTranslator getTranslator() {
		return new UnifierTranslator(atomManager);
	}

	public UelProcessor getUelProcessor() {
		return this.uelProcessor;
	}

	public Set<String> getUserVariableNames() {
		Set<String> names = new HashSet<String>();
		for (Integer varId : atomManager.getUserVariables()) {
			names.add(atomManager.printConceptName(varId));
		}
		return names;
	}

	public List<Unifier> getUnifierList() {
		return Collections.unmodifiableList(unifierList);
	}

	public Goal getGoal() {
		return goal;
	}

	public void loadOntology(File file) {
		provider.loadOntology(file);
		recomputeShortFormMap();
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

	public String printCurrentUnifier(boolean shortForm) {
		Unifier unifier = getCurrentUnifier();
		if (unifier == null) {
			return "";
		}
		return getRenderer(shortForm).printDefinitions(unifier.getDefinitions(), true);
	}

	public String printGoal(boolean shortForm) {
		return getRenderer(shortForm).printGoal(goal);
	}

	public void recomputeShortFormMap() {
		shortFormMap = new HashMap<String, String>();
		for (OWLOntology ontology : provider.getOntologies()) {
			addAllShortForms(ontology, ontology.getClassesInSignature());
			addAllShortForms(ontology, ontology.getObjectPropertiesInSignature());
		}
	}

	public static String removeQuotes(String str) {
		String ret = str;
		if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
			ret = str.substring(1, str.length() - 1);
		}
		return ret;
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

	public void setShortFormMap(Map<String, String> map) {
		shortFormMap = map;
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

		uelProcessor = null;
		unifierList = new ArrayList<Unifier>();
		currentUnifierIndex = -1;
		allUnifiersFound = false;
		recomputeShortFormMap();

		OWLClass top = (owlThingAlias != null) ? owlThingAlias : OWLManager.getOWLDataFactory().getOWLThing();
		atomManager = new AtomManagerImpl(top.toStringID());

		goal = new UelOntologyGoal(atomManager, new UelOntology(atomManager, bgOntologies, owlThingAlias));

		goal.addPositiveAxioms(subsumptions);
		goal.addPositiveAxioms(equations);
		goal.addNegativeAxioms(dissubsumptions);
		goal.addNegativeAxioms(disequations);

		goal.disposeOntology();
	}

	public void makeClassesUserVariables(Set<OWLClass> variables) {
		for (OWLClass var : variables) {
			atomManager.makeUserVariable(getAtomId(var.toStringID()));
		}
	}

	public void makeUndefClassesUserVariables(Set<OWLClass> variables) {
		for (OWLClass var : variables)
			atomManager.makeUserVariable(getAtomId(var.toStringID() + AtomManager.UNDEF_SUFFIX));
	}

	public void makeNamesUserVariables(Set<String> variables) {
		for (String var : variables) {
			atomManager.makeUserVariable(getAtomId(var));
		}
	}

	public void makeUndefNamesUserVariables(Set<String> variables) {
		for (String var : variables) {
			atomManager.makeUserVariable(getAtomId(var + AtomManager.UNDEF_SUFFIX));
		}
	}

}