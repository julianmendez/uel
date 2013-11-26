package de.tudresden.inf.lat.uel.plugin.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
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
import de.tudresden.inf.lat.uel.type.api.SmallEquation;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

public class AlternativeUelStarter {

	public static final String classPrefix = "http://uel.sourceforge.net/entities/auxclass#A";
	private OWLOntology auxOntology;
	private int classCounter = 0;
	private Map<OWLClassExpression, OWLClass> mapOfAuxClassExpr = new HashMap<OWLClassExpression, OWLClass>();
	private OWLOntology ontology;
	private UelProcessor uelProcessor;
	private boolean verbose = false;

	public AlternativeUelStarter(OWLOntology ontology, boolean verbose) {
		this.verbose = verbose;
		initializeOntologies(ontology);
	}

	/**
	 * Constructs a new UEL starter.
	 * 
	 * @param ontology
	 *            OWL ontology
	 */
	public AlternativeUelStarter(OWLOntology ontology) {
		initializeOntologies(ontology);
	}

	private void initializeOntologies(OWLOntology ontology) {
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

				OWLAxiom newDefinition = factory.getOWLEquivalentClassesAxiom(
						ret, expr);
				this.auxOntology.getOWLOntologyManager().addAxiom(auxOntology,
						newDefinition);
			}
		}
		return ret;
	}

	public String getId(OWLClass cls) {
		return UelController.getId(cls);
	}

	private String isVariable(ConceptName name, AtomManager atomManager,
			Set<Integer> userVariables) {
		if (name.isVariable()) {
			if (userVariables.contains(atomManager.getAtoms().addAndGetIndex(
					name))) {
				return "uv";
			} else {
				return "v";
			}
		} else {
			return "c";
		}
	}

	public static void main(String[] args) {
		int argIdx = 0;
		String mainFilename = "";
		String subsFilename = "";
		String dissubsFilename = "";
		String varFilename = "";
		boolean printInfo = false;
		int processorIdx = 0;
		while (argIdx < args.length) {
			switch (args[argIdx]) {
			case "-s":
				argIdx++;
				subsFilename = args[argIdx];
				break;
			case "-d":
				argIdx++;
				dissubsFilename = args[argIdx];
				break;
			case "-v":
				argIdx++;
				varFilename = args[argIdx];
				break;
			case "-p":
				argIdx++;
				try {
					processorIdx = Integer.parseInt(args[argIdx]) - 1;
				} catch (NumberFormatException e) {
					System.err.println("Invalid processor index.");
					return;
				}
				break;
			case "-h":
				printSyntax();
				return;
			case "-i":
				printInfo = true;
				break;
			default:
				mainFilename = args[argIdx];
				break;
			}
			argIdx++;
		}

		AlternativeUelStarter starter = new AlternativeUelStarter(
				loadOntology(mainFilename), printInfo);
		OWLOntology subsumptions = loadOntology(subsFilename);
		if (subsumptions == null) {
			return;
		}
		OWLOntology dissubsumptions = loadOntology(dissubsFilename);
		if (dissubsumptions == null) {
			return;
		}
		Set<OWLClass> variables = loadVariables(varFilename);
		if (variables == null) {
			return;
		}
		List<String> processorNames = UelProcessorFactory.getProcessorNames();
		if ((processorIdx < 0) || (processorIdx >= processorNames.size())) {
			System.err.println("Invalid processor index.");
			return;
		}
		String processorName = processorNames.get(processorIdx);

		Iterator<Set<OWLUelClassDefinition>> result = starter
				.modifyOntologyAndSolve(subsumptions, dissubsumptions,
						variables, processorName);
		int unifierIdx = 1;
		while (result.hasNext()) {
			System.out.println("Unifier " + unifierIdx + ":");
			Set<OWLUelClassDefinition> unifier = result.next();
			for (OWLUelClassDefinition def : unifier) {
				System.out
						.println(def.asOWLEquivalentClassesAxiom().toString());
			}
			System.out.println();
			unifierIdx++;
		}
		if (unifierIdx == 1) {
			System.out.println("Not unifiable.");
		}

		if (printInfo) {
			System.out.println("Stats:");
			for (Entry<String, String> entry : starter.getStats()) {
				System.out.println(entry.getKey() + ":");
				System.out.println(entry.getValue());
			}
		}
	}

	private static void printSyntax() {
		System.out
				.println("Usage: uel [-s subsumptions.owl] [-d dissubsumptions.owl] [-v variables.txt] [-p processorIndex] [-h] [-i] [ontology.owl]");
	}

	private static Set<OWLClass> loadVariables(String filename) {
		if (filename.isEmpty()) {
			return Collections.emptySet();
		}
		try {
			OWLDataFactory factory = OWLManager.createOWLOntologyManager()
					.getOWLDataFactory();
			BufferedReader input = new BufferedReader(new FileReader(new File(
					filename)));
			Set<OWLClass> variables = new HashSet<OWLClass>();
			String line = "";
			while (line != null) {
				line = input.readLine();
				if ((line != null) && !line.isEmpty()) {
					variables.add(factory.getOWLClass(IRI.create(line)));
				}
			}
			input.close();
			return variables;
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file '" + filename + "'.");
			return null;
		} catch (IOException e) {
			System.err.println("Unknown I/O error while reading file '"
					+ filename + "'.");
			System.err.println(e.getMessage());
			return null;
		}
	}

	private static OWLOntology loadOntology(String filename) {
		try {
			OWLOntologyManager ontologyManager = OWLManager
					.createOWLOntologyManager();
			if (filename.isEmpty()) {
				return ontologyManager.createOntology();
			}
			InputStream input = new FileInputStream(new File(filename));
			ontologyManager.loadOntologyFromOntologyDocument(input);
			return ontologyManager.getOntologies().iterator().next();
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file '" + filename + "'.");
			return null;
		} catch (OWLOntologyCreationException e) {
			System.err.println("Could not create ontology from file '"
					+ filename + "'.");
			System.err.println(e.getMessage());
			return null;
		}
	}

	public Iterator<Set<OWLUelClassDefinition>> modifyOntologyAndSolve(
			OWLOntology subsumptions, OWLOntology dissubsumptions,
			Set<OWLClass> variables, String processorName) {
		return modifyOntologyAndSolve(
				subsumptions.getAxioms(AxiomType.SUBCLASS_OF, false),
				dissubsumptions.getAxioms(AxiomType.SUBCLASS_OF, false),
				variables, processorName);
	}

	public Iterator<Set<OWLUelClassDefinition>> modifyOntologyAndSolve(
			Set<OWLSubClassOfAxiom> subsumptions,
			Set<OWLSubClassOfAxiom> dissubsumptions, Set<OWLClass> variables,
			String processorName) {

		// add two definitions for each subsumption to the ontology
		for (OWLSubClassOfAxiom subsumption : subsumptions) {
			findAuxiliaryDefinition(subsumption.getSubClass());
			findAuxiliaryDefinition(subsumption.getSuperClass());
		}

		// construct (small!) disequations from the dissubsumptions
		Set<OWLEquivalentClassesAxiom> disequations = new HashSet<OWLEquivalentClassesAxiom>();
		for (OWLSubClassOfAxiom dissubsumption : dissubsumptions) {
			OWLClass auxSubClass = findAuxiliaryDefinition(dissubsumption
					.getSubClass());
			OWLClass auxSuperClass = findAuxiliaryDefinition(dissubsumption
					.getSuperClass());
			OWLDataFactory factory = this.auxOntology.getOWLOntologyManager()
					.getOWLDataFactory();

			OWLClassExpression conjunction = factory
					.getOWLObjectIntersectionOf(auxSubClass, auxSuperClass);
			OWLClass auxConjunction = findAuxiliaryDefinition(conjunction);
			OWLEquivalentClassesAxiom disequation = factory
					.getOWLEquivalentClassesAxiom(auxSubClass, auxConjunction);
			disequations.add(disequation);
		}

		UelModel model = new UelModel();
		model.loadOntology(this.ontology, this.auxOntology);

		PluginGoal goal = new PluginGoal(model.getAtomManager(),
				model.getOntology());

		// add the subsumptions themselves to the PluginGoal
		for (OWLSubClassOfAxiom subsumption : subsumptions) {
			String subClassId = getId(findAuxiliaryDefinition(subsumption
					.getSubClass()));
			String superClassId = getId(findAuxiliaryDefinition(subsumption
					.getSuperClass()));

			// System.out.println(subClassId + " subsumed by " + superClassId);

			goal.addGoalSubsumption(subClassId, superClassId);
		}

		// add the disequations
		for (OWLEquivalentClassesAxiom disequation : disequations) {
			Iterator<OWLClassExpression> expressions = disequation
					.getClassExpressions().iterator();
			String class1Id = getId((OWLClass) expressions.next());
			String class2Id = getId((OWLClass) expressions.next());

			// System.out.println(class1Id + " not equivalent to " + class2Id);

			goal.addGoalDisequation(class1Id, class2Id);
		}

		// translate the variables to the IDs, and mark them as variables in the
		// PluginGoal
		AtomManager atomManager = goal.getAtomManager();
		for (OWLClass var : variables) {
			String name = getId(var);
			ConceptName conceptName = atomManager.createConceptName(name, true);
			Integer atomId = atomManager.getAtoms().addAndGetIndex(conceptName);
			// System.out.println("user variable: " + name);
			goal.makeUserVariable(atomId);
		}

		// mark the auxiliary variables as auxiliary
		for (OWLClass auxVar : mapOfAuxClassExpr.values()) {
			String name = getId(auxVar);
			ConceptName conceptName = atomManager.createConceptName(name, true);
			Integer atomId = atomManager.getAtoms().addAndGetIndex(conceptName);
			// System.out.println("aux. variable: " + name);
			goal.makeAuxiliaryVariable(atomId);
		}

		goal.updateUelInput();
		UelInput input = goal.getUelInput();

		// output unification problem
		if (verbose) {
			System.out.println("Final number of equations: "
					+ goal.getUelInput().getEquations().size());
			System.out.println("Unification problem:");
			print(input.getEquations(), input.getGoalDisequations(),
					atomManager, input.getUserVariables());
		}

		uelProcessor = UelProcessorFactory
				.createProcessor(processorName, input);

		UnifierTranslator translator = new UnifierTranslator(ontology
				.getOWLOntologyManager().getOWLDataFactory(), atomManager,
				input.getUserVariables(), goal.getAuxiliaryVariables());
		return new UnifierIterator(uelProcessor, translator);
	}

	public List<Entry<String, String>> getStats() {
		return uelProcessor.getInfo();
	}

	private void print(Integer atomId, AtomManager atomManager,
			Set<Integer> userVariables) {
		Atom atom = atomManager.getAtoms().get(atomId);
		if (atom.isExistentialRestriction()) {
			ExistentialRestriction ex = (ExistentialRestriction) atom;
			System.out.print("(exists "
					+ atomManager.getRoleName(ex.getRoleId()) + " "
					+ atomManager.getConceptName(ex.getConceptNameId()) + "["
					+ isVariable(ex.getChild(), atomManager, userVariables)
					+ "])");
		} else {
			ConceptName name = (ConceptName) atom;
			System.out
					.print(atomManager.getConceptName(name.getConceptNameId())
							+ "["
							+ isVariable(name, atomManager, userVariables)
							+ "]");
		}
	}

	private void print(Set<Equation> equations,
			Set<SmallEquation> disequations, AtomManager atomManager,
			Set<Integer> userVariables) {
		for (Equation eq : equations) {
			print(eq.getLeft(), atomManager, userVariables);
			System.out.print(" = ");
			for (Integer atomId : eq.getRight()) {
				print(atomId, atomManager, userVariables);
				System.out.print(" + ");
			}
			System.out.println();
		}
		for (SmallEquation eq : disequations) {
			print(eq.getLeft(), atomManager, userVariables);
			System.out.print(" != ");
			print(eq.getRight(), atomManager, userVariables);
			System.out.println();
		}
	}

}
