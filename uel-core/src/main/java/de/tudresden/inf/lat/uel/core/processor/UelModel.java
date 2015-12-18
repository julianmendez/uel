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
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.type.KRSSRenderer;
import de.tudresden.inf.lat.uel.core.type.UnifierTranslator;
import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.AtomManagerImpl;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

// TODO make this a singleton?

/**
 * An object of this class connects the graphical user interface with the
 * processor.
 * 
 * @author Julian Mendez
 */
public class UelModel {

	public static final String UEL_IRI_PREFIX = "http://uel.sourceforge.net/";
	public static final String UEL_AUX_CLASS_PREFIX = UEL_IRI_PREFIX + "entities/auxclass#A";
	public static final OWLOntology EMPTY_ONTOLOGY = createEmptyOntology();

	private static OWLOntology createEmptyOntology() {
		try {
			return OWLManager.createOWLOntologyManager().createOntology(IRI.create("empty"));
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getId(OWLEntity entity) {
		return entity.getIRI().toURI().toString();
	}

	private boolean allUnifiersFound;
	private AtomManager atomManager;
	private OWLOntology auxOntology;
	private int classCounter = 0;
	private int currentUnifierIndex;
	private Map<OWLClassExpression, Integer> mapOfAuxClassExpr;
	private PluginGoal pluginGoal;
	private Map<String, String> shortFormMap;
	private UelOntologyProvider provider;
	private UelProcessor uelProcessor;
	private List<Set<Equation>> unifierList;

	public UelModel(UelOntologyProvider provider) {
		this.provider = provider;
	}

	private Integer abbreviateClassExpression(OWLClassExpression expr) {
		this.classCounter++;
		OWLOntologyManager manager = this.auxOntology.getOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		IRI iri = IRI.create(UEL_AUX_CLASS_PREFIX + classCounter);
		OWLClass newClass = factory.getOWLClass(iri);
		Integer ret = getAtomId(newClass);

		this.mapOfAuxClassExpr.put(expr, ret);

		OWLAxiom newDefinition = factory.getOWLEquivalentClassesAxiom(newClass, expr);
		manager.addAxiom(auxOntology, newDefinition);

		return ret;
	}

	private void addAllShortForms(OWLOntology ontology, Set<? extends OWLEntity> entities) {
		for (OWLEntity entity : entities) {
			String shortForm = getShortForm(entity, ontology);
			shortFormMap.put(getId(entity), removeQuotes(shortForm));
		}
	}

	public boolean allUnifiersFound() {
		return allUnifiersFound;
	}

	public boolean computeNextUnifier() throws InterruptedException {
		if (!allUnifiersFound) {
			while (this.uelProcessor.computeNextUnifier()) {
				Set<Equation> result = this.uelProcessor.getUnifier().getEquations();
				if (!this.unifierList.contains(result)) {
					this.unifierList.add(result);
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

	public void createUelProcessor(String name) {
		uelProcessor = UelProcessorFactory.createProcessor(name, getPluginGoal().getUelInput());
	}

	private Integer findAuxiliaryDefinition(OWLClassExpression expr) {

		Integer ret = this.mapOfAuxClassExpr.get(expr);
		if (ret != null) {
			return ret;
		}

		if (!expr.isAnonymous()) {
			return getAtomId(expr.asOWLClass());
		} else {
			if (expr.asConjunctSet().size() > 1) {
				return abbreviateClassExpression(expr);
			} else {
				if (!(expr instanceof OWLObjectSomeValuesFrom)) {
					throw new IllegalArgumentException(
							"Argument is neither a concept name, nor a conjunction, nor an existential restriction.");
				}
				OWLObjectSomeValuesFrom exists = (OWLObjectSomeValuesFrom) expr;
				String roleName = exists.getProperty().getNamedProperty().toStringID();
				OWLClassExpression filler = exists.getFiller();

				Integer fillerId;
				if (!filler.isAnonymous()) {
					fillerId = getAtomId(filler.asOWLClass());
				} else {
					fillerId = this.mapOfAuxClassExpr.get(filler);
					if (fillerId == null) {
						fillerId = abbreviateClassExpression(filler);
					}
				}

				ConceptName fillerConceptName = (ConceptName) atomManager.getAtom(fillerId);
				ExistentialRestriction exprExistentialRestriction = atomManager.createExistentialRestriction(roleName,
						fillerConceptName);
				return atomManager.getAtoms().getIndex(exprExistentialRestriction);
			}
		}
	}

	public Integer getAtomId(Atom atom) {
		return atomManager.getAtoms().addAndGetIndex(atom);
	}

	public Integer getAtomId(OWLClass owlClass) {
		return getAtomId(getId(owlClass));
	}

	public Integer getAtomId(String name) {
		ConceptName conceptName = atomManager.createConceptName(name, false);
		return atomManager.getAtoms().addAndGetIndex(conceptName);
	}

	public String getAtomName(Integer id) {
		return atomManager.printConceptName(atomManager.getAtom(id));
	}

	public Set<Equation> getCurrentUnifier() {
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

	public PluginGoal getPluginGoal() {
		return this.pluginGoal;
	}

	public KRSSRenderer getRenderer(boolean shortForm) {
		return new KRSSRenderer(atomManager, pluginGoal.getUserVariables(), pluginGoal.getAuxiliaryVariables(),
				shortForm ? shortFormMap : null);
	}

	private String getShortForm(OWLEntity entity, OWLOntology ontology) {
		if (provider.providesShortForms()) {
			return provider.getShortForm(entity);
		}

		for (OWLAnnotation annotation : entity.getAnnotations(ontology,
				OWLManager.getOWLDataFactory().getRDFSLabel())) {
			return annotation.getValue().toString();
		}

		return entity.getIRI().getShortForm();
	}

	public UnifierTranslator getTranslator() {
		return new UnifierTranslator(atomManager, pluginGoal.getUelInput().getUserVariables(),
				pluginGoal.getAuxiliaryVariables());
	}

	public UelProcessor getUelProcessor() {
		return this.uelProcessor;
	}

	public List<Set<Equation>> getUnifierList() {
		return Collections.unmodifiableList(this.unifierList);
	}

	public void loadOntology(File file) {
		provider.loadOntology(file);
		recomputeShortFormMap();
	}

	public void markUndefAsUserVariables() {
		// mark all "_UNDEF" variables as user variables
		for (Atom at : atomManager.getAtoms()) {
			if (at.isConceptName()) {
				String name = atomManager.printConceptName(at);
				if (name.endsWith(AtomManager.UNDEF_SUFFIX)) {
					pluginGoal.makeUserVariable(getAtomId(at));
				}
			}
		}
	}

	public String printCurrentUnifier(boolean shortForm) {
		Set<Equation> unifier = getCurrentUnifier();
		if (unifier == null) {
			return "";
		}
		return getRenderer(shortForm).printUnifier(unifier);
	}

	public String printPluginGoal(boolean shortForm) {
		return pluginGoal.print(getRenderer(shortForm));
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

	public void setupPluginGoal(Set<OWLOntology> bgOntologies, OWLOntology positiveProblem, OWLOntology negativeProblem,
			OWLClass owlThingAlias) {
		setupPluginGoal(bgOntologies, positiveProblem.getAxioms(AxiomType.SUBCLASS_OF),
				positiveProblem.getAxioms(AxiomType.EQUIVALENT_CLASSES),
				negativeProblem.getAxioms(AxiomType.SUBCLASS_OF),
				negativeProblem.getAxioms(AxiomType.EQUIVALENT_CLASSES), owlThingAlias);
	}

	public void setupPluginGoal(Set<OWLOntology> bgOntologies, Set<OWLSubClassOfAxiom> subsumptions,
			Set<OWLEquivalentClassesAxiom> equations, Set<OWLSubClassOfAxiom> dissubsumptions,
			Set<OWLEquivalentClassesAxiom> disequations, OWLClass owlThingAlias) {

		OWLClass top = (owlThingAlias != null) ? owlThingAlias : OWLManager.getOWLDataFactory().getOWLThing();
		atomManager = new AtomManagerImpl(getId(top));
		uelProcessor = null;
		pluginGoal = null;
		mapOfAuxClassExpr = new HashMap<OWLClassExpression, Integer>();
		classCounter = 0;
		auxOntology = null;
		unifierList = new ArrayList<Set<Equation>>();
		currentUnifierIndex = -1;
		allUnifiersFound = false;
		recomputeShortFormMap();

		try {
			this.auxOntology = OWLManager.createOWLOntologyManager().createOntology();
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e);
		}

		// add an auxiliary definition for each class expression in a
		// subsumption/equation to the ontology (if needed)
		for (OWLSubClassOfAxiom subsumption : subsumptions) {
			findAuxiliaryDefinition(subsumption.getSubClass());
			findAuxiliaryDefinition(subsumption.getSuperClass());
		}
		for (OWLEquivalentClassesAxiom equation : equations) {
			for (OWLClassExpression expr : equation.getClassExpressions()) {
				findAuxiliaryDefinition(expr);
			}
		}

		// construct disequations from the dissubsumptions ...
		Set<Equation> myDisequations = new HashSet<Equation>();
		for (OWLSubClassOfAxiom dissubsumption : dissubsumptions) {
			Integer subClassId = findAuxiliaryDefinition(dissubsumption.getSubClass());
			Integer superClassId = findAuxiliaryDefinition(dissubsumption.getSuperClass());

			Set<Integer> rightIds = new HashSet<Integer>();
			rightIds.add(subClassId);
			rightIds.add(superClassId);
			myDisequations.add(new EquationImpl(subClassId, rightIds, false));
		}
		// ... and replace original disequations by ones between flat atoms
		// (we assume that all equations contain exactly two class expressions)
		for (OWLEquivalentClassesAxiom disequation : disequations) {
			List<OWLClassExpression> exprs = disequation.getClassExpressionsAsList();
			Integer atomId1 = findAuxiliaryDefinition(exprs.get(0));
			Integer atomId2 = findAuxiliaryDefinition(exprs.get(1));

			myDisequations.add(new EquationImpl(atomId1, atomId2, false));
		}

		DynamicOntology dynamicOntology = new DynamicOntology(new OntologyBuilder(atomManager));
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		ontologies.addAll(bgOntologies);
		ontologies.add(auxOntology);
		dynamicOntology.load(ontologies, owlThingAlias);
		this.pluginGoal = new PluginGoal(atomManager, dynamicOntology);

		// add the subsumptions themselves to the PluginGoal ...
		for (OWLSubClassOfAxiom subsumption : subsumptions) {
			Integer subClassId = findAuxiliaryDefinition(subsumption.getSubClass());
			Integer superClassId = findAuxiliaryDefinition(subsumption.getSuperClass());

			// System.out.println(subClassId + " subsumed by " + superClassId);

			pluginGoal.addGoalSubsumption(subClassId, superClassId);
		}
		// ... and do the same for the equations
		for (OWLEquivalentClassesAxiom equation : equations) {
			List<OWLClassExpression> exprs = equation.getClassExpressionsAsList();
			Integer classId1 = findAuxiliaryDefinition(exprs.get(0));
			Integer classId2 = findAuxiliaryDefinition(exprs.get(1));

			pluginGoal.addGoalEquation(classId1, classId2);
		}

		// add the disequations
		for (Equation disequation : myDisequations) {
			pluginGoal.addGoalDisequation(disequation);
		}

		// mark the auxiliary variables as auxiliary
		for (Integer atomId : mapOfAuxClassExpr.values()) {
			pluginGoal.makeAuxiliaryVariable(atomId);
		}

		pluginGoal.updateUelInput();
	}

}