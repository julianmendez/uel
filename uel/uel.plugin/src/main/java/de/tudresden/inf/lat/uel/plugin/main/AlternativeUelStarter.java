package de.tudresden.inf.lat.uel.plugin.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.plugin.processor.PluginGoal;
import de.tudresden.inf.lat.uel.plugin.processor.UelModel;
import de.tudresden.inf.lat.uel.plugin.processor.UelProcessorFactory;
import de.tudresden.inf.lat.uel.plugin.type.AtomManager;
import de.tudresden.inf.lat.uel.plugin.type.OWLUelClassDefinition;
import de.tudresden.inf.lat.uel.plugin.type.UnifierTranslator;
import de.tudresden.inf.lat.uel.plugin.ui.UelController;
import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

public class AlternativeUelStarter {

	public static final String classPrefix = "http://uel.sourceforge.net/entities/auxclass#A";
	private OWLOntology auxOntology;
	private int classCounter = 0;
	private Map<OWLClassExpression, OWLClass> mapOfAuxClassExpr = new HashMap<OWLClassExpression, OWLClass>();
	private OWLOntology ontology;

	/**
	 * Constructs a new UEL starter.
	 * 
	 * @param ontology
	 *            OWL ontology
	 */
	public AlternativeUelStarter(OWLOntology ontology) {
		if (ontology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.ontology = ontology;
		try {
			this.auxOntology = ontology.getOWLOntologyManager()
					.createOntology();
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e);
		}
	}

	public OWLClass findAuxiliaryDefinition(OWLClassExpression expr) {
		OWLClass ret = null;
		if (expr.isClassExpressionLiteral()) {
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
				
				OWLAxiom newDefinition = factory.getOWLEquivalentClassesAxiom(ret, expr);
				this.auxOntology.getOWLOntologyManager().addAxiom(auxOntology, newDefinition);
			}
		}
		return ret;
	}

	public String getId(OWLClass cls) {
		return UelController.getId(cls);
	}

	public Iterator<Set<OWLUelClassDefinition>> modifyOntologyAndSolve(
			Set<OWLSubClassOfAxiom> subsumptions, Set<OWLClass> variables) {

		// add two definitions for each subsumption to the ontology
		for (OWLSubClassOfAxiom subsumption : subsumptions) {
			findAuxiliaryDefinition(subsumption.getSubClass());
			findAuxiliaryDefinition(subsumption.getSuperClass());
		}

		UelModel model = new UelModel();
		model.loadOntology(this.ontology, this.auxOntology);

		PluginGoal goal = new PluginGoal(model.getAtomManager(),
				model.getOntology());

		// add the subsumption themselves to the PluginGoal
		for (OWLSubClassOfAxiom subsumption : subsumptions) {
			String subClassId = getId(findAuxiliaryDefinition(subsumption
					.getSubClass()));
			String superClassId = getId(findAuxiliaryDefinition(subsumption
					.getSuperClass()));

			goal.addSubsumption(subClassId, superClassId);
		}

		// translate the variables to the IDs, and mark them as variables in the
		// PluginGoal
		AtomManager atomManager = goal.getAtomManager();
		for (OWLClass var : variables) {
			String name = getId(var);
			ConceptName conceptName = atomManager.createConceptName(name, true);
			Integer atomId = atomManager.getAtoms().addAndGetIndex(conceptName);
			goal.makeVariable(atomId);
		}

		goal.updateUelInput();
		
		// output unification problem for debugging
//		print(goal.getUelInput().getEquations(), goal.getAtomManager(), goal.getUelInput().getUserVariables());

		UelProcessor satProcessor = UelProcessorFactory.createProcessor(
				UelProcessorFactory.SAT_BASED_ALGORITHM, goal.getUelInput());
		model.configureUelProcessor(satProcessor);

		// satProcessor.getUnifier();

		UnifierTranslator translator = new UnifierTranslator(ontology
				.getOWLOntologyManager().getOWLDataFactory(), atomManager, goal
				.getUelInput().getUserVariables());
		return new UnifierIterator(satProcessor, translator);
	}

	private void print(Set<Equation> equations, AtomManager atomManager, Set<Integer> userVariables) {
		for (Equation eq : equations) {
			print(eq.getLeft(), atomManager, userVariables);
			System.out.print(" = ");
			for (Integer atomId : eq.getRight()) {
				print(atomId, atomManager, userVariables);
				System.out.print(" + ");
			}
			System.out.println();
		}
	}

	private void print(Integer atomId, AtomManager atomManager, Set<Integer> userVariables) {
		Atom atom = atomManager.getAtoms().get(atomId);
		if (atom.isExistentialRestriction()) {
			ExistentialRestriction ex = (ExistentialRestriction) atom;
			System.out.print("(exists "
					+ atomManager.getRoleName(ex.getRoleId())
					+ " "
					+ atomManager.getConceptName(ex.getConceptNameId())
					+ "["
					+ isVariable(ex.getChild(), atomManager, userVariables)
					+ "])");
		} else {
			ConceptName name = (ConceptName) atom;
			System.out.print(atomManager.getConceptName(name.getConceptNameId())
					+ "["
					+ isVariable(name, atomManager, userVariables)
					+ "]");
		}
	}
	
	private String isVariable(ConceptName name, AtomManager atomManager, Set<Integer> userVariables) {
		if (name.isVariable()) {
			if (userVariables.contains(atomManager.getAtoms().addAndGetIndex(name))) {
				return "uv";
			} else {
				return "v";
			}
		} else {
			return "c";
		}
	}

}
