/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;

import com.google.common.base.Stopwatch;

import de.tudresden.inf.lat.uel.core.main.AlternativeUelStarter;
import de.tudresden.inf.lat.uel.core.main.UnifierIterator;
import de.tudresden.inf.lat.uel.core.processor.UelOptions;
import de.tudresden.inf.lat.uel.core.processor.UelOptions.UndefBehavior;
import de.tudresden.inf.lat.uel.core.processor.UelOptions.Verbosity;
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;
import de.tudresden.inf.lat.uel.plugin.main.SNOMEDAlgorithmResult.SNOMEDAlgorithmStatus;
import de.tudresden.inf.lat.uel.plugin.main.SNOMEDResult.SNOMEDGoalStatus;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDEvaluation {

	// private static final String WORK_DIR = "C:\\Users\\Stefan\\Work\\";
	private static final String WORK_DIR = "/Users/stefborg/Documents/";
	private static final String OUTPUT_PATH = WORK_DIR + "Projects/uel-snomed/results-";
	private static final String SNOMED_MODULE_PATH = WORK_DIR + "Ontologies/snomed-";
	static final String SNOMED_PATH = SNOMED_MODULE_PATH + "english-rdf.owl";
	static final String SNOMED_RESTR_PATH = WORK_DIR + "Ontologies/snomed-restrictions-no-imports.owl";

	private static final String[] PARENT_CLASSES = new String[] {
			// "Bleeding (finding)",
			"Body structure (body structure)", "Clinical finding (finding)",
			// "Clinical history and observation findings (finding)",
			"Disease (disorder)", "Event (event)",
			// "Finding by site (finding)",
			"Functional finding (finding)", "Observable entity (observable entity)" };

	private static final int[] ROLE_GROUP_NO = new int[] { 1, 2 };

	private static final String[] TEST_ALGORITHMS = new String[] {
			//
			UnificationAlgorithmFactory.SAT_BASED_ALGORITHM
			//
			,
			//
			UnificationAlgorithmFactory.ASP_BASED_ALGORITHM
			//
	};

	static final int MAX_SIBLINGS = 100;
	static final int MAX_ATOMS = 240;
	static final boolean CHECK_UNIFIERS = true;
	private static final int MAX_TESTS = 50;
	private static final long TIMEOUT = 5 * 60 * 1000;

	private static String currentParentClass;
	private static String currentModulePath;
	private static String currentClassesList;
	private static Date startTime;
	private static List<SNOMEDResult> results;
	private static UelOptions options;
	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;
	private static OWLOntology snomed;
	private static OWLOntology snomedRestrictions;
	private static Set<OWLOntology> bg;

	static OWLClass cls(String name) {
		if (factory == null) {
			factory = OWLManager.getOWLDataFactory();
		}
		return factory.getOWLClass(IRI.create("http://www.ihtsdo.org/" + name));
	}

	static OWLObjectProperty prp(String name) {
		if (factory == null) {
			factory = OWLManager.getOWLDataFactory();
		}
		return factory.getOWLObjectProperty(IRI.create("http://www.ihtsdo.org/" + name));
	}

	static void printResults() {
		if (results != null) {
			try {
				System.out.println("Saving results to file...");
				PrintStream out = new PrintStream(
						OUTPUT_PATH + new SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().getTime())
								+ "-" + currentParentClass + "-" + options.numberOfRoleGroups + "RG.txt");

				out.println("* UEL options:");
				out.println(options);

				out.println("* Test options:");
				out.println("Timeout (s): " + (TIMEOUT / 1000));
				out.println("Ontology path: " + currentModulePath);
				out.println("List of goal classes: " + currentClassesList);
				out.println("Cut-off for number of siblings of the goal class: " + MAX_SIBLINGS);
				out.println("Cut-off for number of atoms: " + MAX_ATOMS);
				out.println("Check unifiers for correctness: " + CHECK_UNIFIERS);
				out.println();

				out.println("* Test results:");
				out.println("Goal class / algorithm                  |  Result  | Time |   #");
				float successful = 0;
				float disagreed = 0;
				float complete = 0;
				for (int i = 0; i < results.size(); i++) {
					SNOMEDResult result = results.get(i);
					out.println("----------------------------------------+----------+------+------");
					out.printf("%-40s|%-10s|%5ds|%6d%n", result.goalClass, result.goalStatus, result.buildGoal,
							result.goalSize);

					Set<SNOMEDAlgorithmStatus> collectedResults = new HashSet<SNOMEDAlgorithmStatus>();
					for (SNOMEDAlgorithmResult algorithmResult : result.algorithmResults) {
						if ((algorithmResult.status == SNOMEDAlgorithmStatus.SUCCESS)
								|| (algorithmResult.status == SNOMEDAlgorithmStatus.FAILURE)) {
							collectedResults.add(algorithmResult.status);
						}
						out.printf(" *** %-35s|%-10s|%5ds|%6d%n",
								UnificationAlgorithmFactory.shortString(algorithmResult.unificationAlgorithmName),
								algorithmResult.status, algorithmResult.totalTime, algorithmResult.numberOfSolutions);
					}
					if (result.goalStatus == SNOMEDGoalStatus.COMPLETE) {
						// disregard incomplete test cases for averaging
						if (collectedResults.contains(SNOMEDAlgorithmStatus.SUCCESS)) {
							successful++;
							if (collectedResults.contains(SNOMEDAlgorithmStatus.FAILURE)) {
								disagreed++;
							}
						}
						complete++;
					}
				}
				out.println();

				out.println("* Test summary:");
				Format f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
				out.println("Start time: " + f.format(startTime));
				out.println("End time: " + f.format(Calendar.getInstance().getTime()));
				out.println("Total test cases: " + results.size());
				out.println("Completed test cases: " + ((int) complete) + " ("
						+ (100 * complete / (float) results.size()) + "% of all test cases)");
				out.println("Successful test cases: " + ((int) successful) + " (" + (100 * successful / complete)
						+ "% of all completed test cases)");
				out.println("Test cases with disagreement: " + ((int) disagreed) + " (" + (100 * disagreed / complete)
						+ "% of all completed test cases)");

				out.close();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Entry point for tests.
	 * 
	 * @param args
	 *            arguments (ignored)
	 */
	public static void main(String[] args) {

		Runtime.getRuntime().addShutdownHook(new Thread(SNOMEDEvaluation::printResults));

		for (String parentClass : PARENT_CLASSES) {
			for (int rg : ROLE_GROUP_NO) {
				runModuleTests(parentClass, rg);
				System.out.println();
				System.out.println();
			}
		}
	}

	private static void runModuleTests(String parentClass, int rg) {
		System.out.println("========== Module tests for parent class: " + parentClass + " [" + rg + " RoleGroup(s)]");

		currentParentClass = parentClass;
		currentModulePath = SNOMED_MODULE_PATH + parentClass + ".owl";
		currentClassesList = SNOMED_MODULE_PATH + parentClass + ".txt";
		startTime = Calendar.getInstance().getTime();
		results = new ArrayList<SNOMEDResult>();
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		snomed = AlternativeUelStarter.loadOntology(currentModulePath, manager);
		snomedRestrictions = AlternativeUelStarter.loadOntology(SNOMED_RESTR_PATH, manager);
		bg = new HashSet<OWLOntology>(Arrays.asList(snomed, snomedRestrictions));

		options = new UelOptions();
		options.verbosity = Verbosity.SHORT;
		options.undefBehavior = UndefBehavior.CONSTANTS;
		options.snomedMode = true;
		options.unificationAlgorithmName = TEST_ALGORITHMS[0];
		options.expandPrimitiveDefinitions = true;
		options.restrictUndefContext = true;
		options.numberOfRoleGroups = rg;
		options.minimizeSolutions = true;
		options.noEquivalentSolutions = true;
		options.numberOfSiblings = -1;

		try {
			// randomly select classes with full definition from the SNOMED
			// module
			randomTests();

			// Disagreement between SAT and ASP (2 RG)!? (SAT alg. does not find
			// a solution) -> fixed with extended compatibility check
			// runSingleTest("SCT_364805005");
			// runSingleTest("SCT_284782007");
			// runSingleTest("SCT_365278008");
			// runSingleTest("SCT_300723007");

			// 'Difficulty writing (finding)': 30s; new: 21 s / 6,7 min
			// singleTest("SCT_102938007");

			// 'Does not use words (finding)': 12s / 42s
			// OWLClass goalClass = cls("SCT_288613006");

			// 'Circumlocution (finding)': too large (1732 atoms), primitive!
			// OWLClass goalClass = cls("SCT_48364004");

			// 'Finding relating to crying (finding)': too large (1816 atoms)
			// OWLClass goalClass = cls("SCT_303220007");

			// 'Routine procedure (procedure)':
			// OWLClass goalClass = cls("SCT_373113001");

			// 'Contraception (finding)': impossible
			// OWLClass goalClass = cls("SCT_13197004");

			// 'Calculus finding (finding)': too large
			// OWLClass goalClass = cls("SCT_313413008");

			// 'Abnormal gallbladder function (finding)': huge (3718 atoms)
			// OWLClass goalClass = cls("SCT_51047007");

			// 'Unable to air laundry (finding)': needs 3 RoleGroups; 1min (5) /
			// >
			// 20min
			// OWLClass goalClass = cls("SCT_286073006");

			// 'Echoencephalogram abnormal (finding)': too large
			// OWLClass goalClass = cls(factory, "SCT_274538008");

			// 'Primary malignant neoplasm of pyriform sinus (disorder)'
			//

			// 'Entire left kidney (body structure)'
			// OWLClass goalClass = cls(factory, "SCT_362209008");

			// 'Finding related to ability to use contact lenses (finding)'
			// OWLClass goalClass = cls(factory, "SCT_365239009");

			// 'Biguanide overdose (disorder)'
			// singleTest("SCT_296872003", snomed, bg);

			// 'Chronic progressive epilepsia partialis continua (disorder)'
			// singleTest("SCT_39745004");

			// 'Does control trunk posture (finding)' -> test case for ASP
			// solver, which returned some incorrect unifiers
			// runSingleTest("SCT_284124002");

		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}

		printResults();
	}

	private static void runSingleTest(String id) throws InterruptedException {
		OWLClass goalClass = cls(id);
		OWLClassExpression goalExpression = ((OWLEquivalentClassesAxiom) snomed.getAxioms(goalClass, Imports.EXCLUDED)
				.iterator().next()).getClassExpressionsMinus(goalClass).iterator().next();
		runSingleTest(goalClass, goalExpression);
	}

	private static void randomTests() throws InterruptedException, IOException {
		List<OWLEquivalentClassesAxiom> definitions = new ArrayList<OWLEquivalentClassesAxiom>();
		Files.lines(Paths.get(currentClassesList)).map(line -> line.substring(1, line.length() - 1))
				.map(line -> factory.getOWLClass(IRI.create(line)))
				.flatMap(cls -> snomed.getAxioms(cls, Imports.EXCLUDED).stream())
				.filter(ax -> ax instanceof OWLEquivalentClassesAxiom)
				.forEach(ax -> definitions.add((OWLEquivalentClassesAxiom) ax));

		Random rnd = new Random();
		System.out.println("Loading finished.");

		for (int i = 0; (i < MAX_TESTS) && (definitions.size() > 0); i++) {
			OWLEquivalentClassesAxiom axiom = definitions.get(rnd.nextInt(definitions.size()));
			definitions.remove(axiom);
			OWLClass goalClass = axiom.getNamedClasses().iterator().next();
			OWLClassExpression goalExpression = axiom.getClassExpressionsMinus(goalClass).iterator().next();
			System.out.println();
			System.out.print("***** [" + i + "] ");
			runSingleTest(goalClass, goalExpression);
		}
	}

	private static void runSingleTest(OWLClass goalClass, OWLClassExpression goalExpression)
			throws InterruptedException {

		System.out.println("Goal class: " + EntitySearcher.getAnnotations(goalClass, snomed, factory.getRDFSLabel())
				.iterator().next().getValue().asLiteral().get().getLiteral());

		printThreadInfo();
		SNOMEDTestInitialization initRunner = new SNOMEDTestInitialization(options, snomed, bg, goalClass,
				goalExpression);
		Thread initThread = new Thread(initRunner);
		initThread.start();
		initThread.join(TIMEOUT);
		SNOMEDResult result = initRunner.result;
		if (initThread.isAlive()) {
			initThread.interrupt();
			result.goalStatus = SNOMEDGoalStatus.TIMEOUT;
		}
		results.add(result);

		if (result.goalStatus == SNOMEDGoalStatus.SUCCESS) {
			// the goal was constructed successfully, start unification tests
			UnifierIterator iterator = initRunner.iterator;
			OWLOntology pos = initRunner.pos;
			OWLOntology neg = initRunner.neg;
			OWLAxiom goalAxiom = initRunner.goalAxiom;
			initThread = null;

			for (int i = 0; i < TEST_ALGORITHMS.length; i++) {
				printThreadInfo();
				System.out.println("** Unification algorithm " + (i + 1) + ": " + TEST_ALGORITHMS[i]);
				options.unificationAlgorithmName = TEST_ALGORITHMS[i];
				iterator = iterator.resetModel();

				SNOMEDTest algorithmRunner = new SNOMEDTest(iterator, options, pos, neg, goalAxiom);
				Thread algorithmThread = new Thread(algorithmRunner);
				algorithmThread.start();
				algorithmThread.join(TIMEOUT);
				SNOMEDAlgorithmResult algorithmResult = algorithmRunner.result;
				if (algorithmThread.isAlive()) {
					System.out.println("Timeout!");
					printThreadInfo();
					algorithmThread.interrupt();
					algorithmThread.join();
					algorithmResult.status = SNOMEDAlgorithmStatus.TIMEOUT;
				}
				result.algorithmResults.add(algorithmResult);
			}

			// all individual tests for this goal class have been completed
			result.goalStatus = SNOMEDGoalStatus.COMPLETE;
		}
	}

	static long output(Stopwatch timer, String description, boolean reset) {
		System.out.println(description + ": " + timer);
		long elapsedTime = timer.elapsed(TimeUnit.SECONDS);
		if (reset) {
			timer.reset();
			timer.start();
		}
		return elapsedTime;
	}

	private static void printThreadInfo() {
		for (Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
			Thread t = e.getKey();
			if (!t.getName().equals("main") && !t.getName().equals("Finalizer")
					&& !t.getName().equals("Reference Handler") && !t.getName().equals("process reaper")
					&& !t.getName().equals("Signal Dispatcher")) {
				System.out.println("Thread " + t.getName() + ", " + t.getState());
				for (StackTraceElement el : e.getValue()) {
					System.out.println(el);
				}
			}
		}
	}
}
