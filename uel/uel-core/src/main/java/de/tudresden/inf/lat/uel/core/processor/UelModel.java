package de.tudresden.inf.lat.uel.core.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.type.AtomManager;
import de.tudresden.inf.lat.uel.core.type.AtomManagerImpl;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;

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
	private Set<Set<Equation>> unifierSet = new HashSet<Set<Equation>>();
	private PluginGoal pluginGoal = null;
	private OWLOntology auxOntology = null;
	private Map<OWLClassExpression, OWLClass> mapOfAuxClassExpr = new HashMap<OWLClassExpression, OWLClass>();

	private int classCounter = 0;

	/**
	 * Constructs a new processor.
	 */
	public UelModel() {
	}

	/**
	 * Computes the next unifier. This unifier can be equivalent to another one
	 * already computed.
	 * 
	 * @return <code>true</code> if and only if more unifiers can be computed
	 */
	public boolean computeNextUnifier() throws InterruptedException {
		while (this.uelProcessor.computeNextUnifier()) {
			Set<Equation> result = this.uelProcessor.getUnifier()
					.getEquations();
			if (!this.unifierSet.contains(result)) {
				this.unifierList.add(result);
				this.unifierSet.add(result);
				return true;
			}
		}
		return false;
	}

	public void configure(OWLOntologyManager ontologyManager,
			Set<OWLOntology> bgOntologies, OWLOntology positiveProblem,
			OWLOntology negativeProblem, OWLClass owlThingAlias) {
		configure(ontologyManager, bgOntologies,
				positiveProblem.getAxioms(AxiomType.SUBCLASS_OF),
				positiveProblem.getAxioms(AxiomType.EQUIVALENT_CLASSES),
				negativeProblem.getAxioms(AxiomType.SUBCLASS_OF),
				negativeProblem.getAxioms(AxiomType.EQUIVALENT_CLASSES),
				owlThingAlias);
	}

	public void configure(OWLOntologyManager ontologyManager,
			Set<OWLOntology> bgOntologies,
			Set<OWLSubClassOfAxiom> subsumptions,
			Set<OWLEquivalentClassesAxiom> equations,
			Set<OWLSubClassOfAxiom> dissubsumptions,
			Set<OWLEquivalentClassesAxiom> disequations, OWLClass owlThingAlias) {

		OWLDataFactory factory = ontologyManager.getOWLDataFactory();
		try {
			this.auxOntology = ontologyManager.createOntology();
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

		// construct (small!) disequations from the dissubsumptions ...
		Set<OWLEquivalentClassesAxiom> myDisequations = new HashSet<OWLEquivalentClassesAxiom>();
		for (OWLSubClassOfAxiom dissubsumption : dissubsumptions) {
			OWLClass auxSubClass = findAuxiliaryDefinition(dissubsumption
					.getSubClass());
			OWLClass auxSuperClass = findAuxiliaryDefinition(dissubsumption
					.getSuperClass());

			OWLClassExpression conjunction = factory
					.getOWLObjectIntersectionOf(auxSubClass, auxSuperClass);
			OWLClass auxConjunction = findAuxiliaryDefinition(conjunction);
			OWLEquivalentClassesAxiom disequation = factory
					.getOWLEquivalentClassesAxiom(auxSubClass, auxConjunction);
			myDisequations.add(disequation);
		}
		// ... and replace original disequations by ones between variables
		// (we assume that all equations contain exactly two class expressions)
		for (OWLEquivalentClassesAxiom disequation : disequations) {
			List<OWLClassExpression> exprs = disequation
					.getClassExpressionsAsList();
			OWLClass auxClass1 = findAuxiliaryDefinition(exprs.get(0));
			OWLClass auxClass2 = findAuxiliaryDefinition(exprs.get(1));

			myDisequations.add(factory.getOWLEquivalentClassesAxiom(auxClass1,
					auxClass2));
		}

		DynamicOntology dynamicOntology = new DynamicOntology(
				new OntologyBuilder(getAtomManager()));
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		ontologies.addAll(bgOntologies);
		ontologies.add(auxOntology);
		dynamicOntology.load(ontologies, owlThingAlias);
		this.pluginGoal = new PluginGoal(getAtomManager(), dynamicOntology);

		// add the subsumptions themselves to the PluginGoal ...
		for (OWLSubClassOfAxiom subsumption : subsumptions) {
			String subClassId = getId(findAuxiliaryDefinition(subsumption
					.getSubClass()));
			String superClassId = getId(findAuxiliaryDefinition(subsumption
					.getSuperClass()));

			// System.out.println(subClassId + " subsumed by " + superClassId);

			pluginGoal.addGoalSubsumption(subClassId, superClassId);
		}
		// ... and do the same for the equations
		for (OWLEquivalentClassesAxiom equation : equations) {
			List<OWLClassExpression> exprs = equation
					.getClassExpressionsAsList();
			String classId1 = getId(findAuxiliaryDefinition(exprs.get(0)));
			String classId2 = getId(findAuxiliaryDefinition(exprs.get(1)));

			pluginGoal.addGoalEquation(classId1, classId2);
		}

		// add the disequations
		for (OWLEquivalentClassesAxiom disequation : myDisequations) {
			Iterator<OWLClassExpression> expressions = disequation
					.getClassExpressions().iterator();
			String class1Id = getId((OWLClass) expressions.next());
			String class2Id = getId((OWLClass) expressions.next());

			// System.out.println(class1Id + " not equivalent to " + class2Id);

			pluginGoal.addGoalDisequation(class1Id, class2Id);
		}

		// mark the auxiliary variables as auxiliary
		for (OWLClass auxVar : mapOfAuxClassExpr.values()) {
			String name = getId(auxVar);
			ConceptName conceptName = atomManager.createConceptName(name, true);
			Integer atomId = atomManager.getAtoms().addAndGetIndex(conceptName);
			// System.out.println("aux. variable: " + name);
			pluginGoal.makeAuxiliaryVariable(atomId);
		}
		
		pluginGoal.updateUelInput();

	}

	public OWLClass findAuxiliaryDefinition(OWLClassExpression expr) {
		OWLClass ret = null;
		if (!expr.isAnonymous()) {
			ret = expr.asOWLClass();
		} else {
			ret = this.mapOfAuxClassExpr.get(expr);
			if (ret == null) {
				this.classCounter++;
				OWLOntologyManager manager = this.auxOntology
						.getOWLOntologyManager();
				OWLDataFactory factory = manager.getOWLDataFactory();
				IRI iri = IRI.create(classPrefix + classCounter);
				ret = factory.getOWLClass(iri);

				this.mapOfAuxClassExpr.put(expr, ret);

				OWLAxiom newDefinition = factory.getOWLEquivalentClassesAxiom(
						ret, expr);
				this.auxOntology.getOWLOntologyManager().addAxiom(auxOntology,
						newDefinition);
			}
		}
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

	public List<Set<Equation>> getUnifierList() {
		return Collections.unmodifiableList(this.unifierList);
	}

	public void reset() {
		this.pluginGoal = null;
		this.mapOfAuxClassExpr.clear();
		this.classCounter = 0;
		this.auxOntology = null;
		this.unifierList.clear();
		this.unifierSet.clear();
	}

	public void setProcessorName(String name) {
		if (!UelProcessorFactory.getProcessorNames().contains(name)) {
			throw new IllegalArgumentException("Processor name is invalid: '"
					+ name + "'.");
		}
		this.processorName = name;
	}

	public void setUelProcessor(UelProcessor processor) {
		this.uelProcessor = processor;
	}

}
