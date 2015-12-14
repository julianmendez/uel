package de.tudresden.inf.lat.uel.core.processor;

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
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.AtomManagerImpl;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

/**
 * An object of this class connects the graphical user interface with the
 * processor.
 * 
 * @author Julian Mendez
 */
public class UelModel {

	public static final String classPrefix = "http://uel.sourceforge.net/entities/auxclass#A";

	public static String getId(OWLEntity entity) {
		return entity.getIRI().toURI().toString();
	}

	private AtomManager atomManager = new AtomManagerImpl();
	private String processorName;
	private UelProcessor uelProcessor = null;
	private List<Set<Equation>> unifierList = new ArrayList<Set<Equation>>();
	private PluginGoal pluginGoal = null;
	private OWLOntology auxOntology = null;
	private Map<OWLClassExpression, Integer> mapOfAuxClassExpr = new HashMap<OWLClassExpression, Integer>();
	private Map<String, String> mapIdLabel = null;
	private int currentUnifierIndex = -1;
	private boolean allUnifiersFound = false;

	private int classCounter = 0;

	public UelModel() {
	}

	/**
	 * Computes the next unifier. This unifier can be equivalent to another one
	 * already computed.
	 * 
	 * @return <code>true</code> if and only if more unifiers can be computed
	 * 
	 * @throws InterruptedException
	 *             if the process is interrupted
	 */
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

	public void setMapIdLabel(Map<String, String> map) {
		mapIdLabel = map;
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

	public int getCurrentUnifierIndex() {
		return currentUnifierIndex;
	}

	public boolean allUnifiersFound() {
		return allUnifiersFound;
	}

	public void configure(Set<OWLOntology> bgOntologies, OWLOntology positiveProblem, OWLOntology negativeProblem,
			OWLClass owlThingAlias) {
		configure(bgOntologies, positiveProblem.getAxioms(AxiomType.SUBCLASS_OF),
				positiveProblem.getAxioms(AxiomType.EQUIVALENT_CLASSES),
				negativeProblem.getAxioms(AxiomType.SUBCLASS_OF),
				negativeProblem.getAxioms(AxiomType.EQUIVALENT_CLASSES), owlThingAlias);
	}

	public void configure(Set<OWLOntology> bgOntologies, Set<OWLSubClassOfAxiom> subsumptions,
			Set<OWLEquivalentClassesAxiom> equations, Set<OWLSubClassOfAxiom> dissubsumptions,
			Set<OWLEquivalentClassesAxiom> disequations, OWLClass owlThingAlias) {

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

	public Integer findAuxiliaryDefinition(OWLClassExpression expr) {

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

				ConceptName fillerConceptName = (ConceptName) atomManager.getAtoms().get(fillerId);
				ExistentialRestriction exprExistentialRestriction = atomManager.createExistentialRestriction(roleName,
						fillerConceptName);
				return atomManager.getAtoms().addAndGetIndex(exprExistentialRestriction);
			}
		}

	}

	public Integer getAtomId(OWLClass owlClass) {
		ConceptName conceptName = atomManager.createConceptName(getId(owlClass), false);
		return atomManager.getAtoms().addAndGetIndex(conceptName);
	}

	private Integer abbreviateClassExpression(OWLClassExpression expr) {
		this.classCounter++;
		OWLOntologyManager manager = this.auxOntology.getOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		IRI iri = IRI.create(classPrefix + classCounter);
		OWLClass newClass = factory.getOWLClass(iri);
		Integer ret = getAtomId(newClass);

		this.mapOfAuxClassExpr.put(expr, ret);

		OWLAxiom newDefinition = factory.getOWLEquivalentClassesAxiom(newClass, expr);
		manager.addAxiom(auxOntology, newDefinition);

		return ret;
	}

	public AtomManager getAtomManager() {
		return this.atomManager;
	}

	public PluginGoal getPluginGoal() {
		return this.pluginGoal;
	}

	public String getProcessorName() {
		return this.processorName;
	}

	public UelProcessor getUelProcessor() {
		return this.uelProcessor;
	}

	public KRSSRenderer getRenderer(boolean shortForm) {
		return new KRSSRenderer(atomManager, pluginGoal.getUserVariables(), pluginGoal.getAuxiliaryVariables(),
				shortForm ? mapIdLabel : null);
	}

	public Set<Equation> getCurrentUnifier() {
		if (unifierList.size() == 0) {
			return null;
		}
		return unifierList.get(currentUnifierIndex);
	}

	public String printCurrentUnifier(boolean shortForm) {
		Set<Equation> unifier = getCurrentUnifier();
		if (unifier == null) {
			return "";
		}
		return getRenderer(shortForm).printUnifier(getCurrentUnifier());
	}

	public List<Set<Equation>> getUnifierList() {
		return Collections.unmodifiableList(this.unifierList);
	}

	public String printPluginGoal(boolean shortForm) {
		return pluginGoal.print(getRenderer(shortForm));
	}

	public void reset() {
		this.atomManager = new AtomManagerImpl();
		this.pluginGoal = null;
		this.mapOfAuxClassExpr.clear();
		this.classCounter = 0;
		this.auxOntology = null;
		this.unifierList.clear();
	}

	public void setProcessorName(String name) {
		if (!UelProcessorFactory.getProcessorNames().contains(name)) {
			throw new IllegalArgumentException("Processor name is invalid: '" + name + "'.");
		}
		this.processorName = name;
	}

	public void setUelProcessor(UelProcessor processor) {
		this.uelProcessor = processor;
	}

}
