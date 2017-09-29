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
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
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
	static final String SNOMED_MODULE_PATH = WORK_DIR + "Ontologies/snomed2017-";
	static final String SNOMED_PATH = SNOMED_MODULE_PATH + "english.owl";
	static final String SNOMED_RESTR_PATH = SNOMED_MODULE_PATH + "restrictions-no-imports.owl";

	public static final String[] PARENT_CLASSES = new String[] {
			//
			"Body structure (body structure)",
			//
			//// "Anatomical structure (body structure)",
			//
			//// "Morphologically abnormal structure (morphologic abnormality)",
			//
			"Clinical finding (finding)",
			//
			"Clinical history and observation findings (finding)",
			//
			"Functional finding (finding)",
			//
			"Disease (disorder)",
			//
			//// "Adverse reaction (disorder)",
			//
			"Poisoning (disorder)",
			//
			//// "Traumatic injury (disorder)",
			//
			"Finding by site (finding)",
			//
			"Event (event)",
			//
			"Observable entity (observable entity)",
			//
			"Pharmaceutical / biologic product (product)",
			//
			"Procedure (procedure)",
			//
	};

	private static final int[] ROLE_GROUP_NO = new int[] { 2 };

	private static final String[] TEST_ALGORITHMS = new String[] {
			//
			UnificationAlgorithmFactory.SAT_BASED_ALGORITHM,
			//
			// UnificationAlgorithmFactory.ASP_BASED_ALGORITHM,
			//
	};

	static final int MAX_SIBLINGS = 100;
	static final int MAX_ATOMS = 240;
	static final boolean CHECK_UNIFIERS = true;
	private static final int MAX_TESTS = 100;
	private static final long TIMEOUT = 5 * 60 * 1000;

	private static String currentParentClass;
	private static String currentModulePath;
	private static String currentClassesList;
	private static Date startTime;
	private static Stopwatch elapsedTimer;
	private static List<SNOMEDResult> results;
	private static UelOptions options;
	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;
	private static OWLOntology snomedModule;
	private static OWLOntology snomedRestrictions;
	private static Set<OWLOntology> bg;

	static OWLClass cls(String uri) {
		if (factory == null) {
			factory = OWLManager.getOWLDataFactory();
		}
		return factory.getOWLClass(IRI.create(uri));
	}

	static OWLObjectProperty prp(String uri) {
		if (factory == null) {
			factory = OWLManager.getOWLDataFactory();
		}
		return factory.getOWLObjectProperty(IRI.create(uri));
	}

	static void printResults() {
		if (results != null) {
			try {
				System.out.println("Saving results to file...");
				if (currentParentClass != null) {
					currentParentClass = currentParentClass.replace('/', '-');
				}
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
				float successSolutions = 0;
				float failureSolutions = 0;
				float successTimes = 0;
				float failureTimes = 0;
				float totalTimes = 0;
				for (int i = 0; i < results.size(); i++) {
					SNOMEDResult result = results.get(i);
					out.println("----------------------------------------+----------+------+------");
					out.printf("%-40s|%-10s|%5ds|%6d%n", result.goalClass, result.goalStatus, result.buildGoal,
							result.goalSize);
					totalTimes += result.buildGoal;

					Set<SNOMEDAlgorithmStatus> collectedResults = new HashSet<SNOMEDAlgorithmStatus>();
					for (SNOMEDAlgorithmResult algorithmResult : result.algorithmResults) {
						if ((algorithmResult.status == SNOMEDAlgorithmStatus.SUCCESS)
								|| (algorithmResult.status == SNOMEDAlgorithmStatus.FAILURE)) {
							collectedResults.add(algorithmResult.status);
						}
						if (algorithmResult.status == SNOMEDAlgorithmStatus.SUCCESS) {
							successSolutions += algorithmResult.numberOfSolutions;
							successTimes += algorithmResult.totalTime;
						}
						if (algorithmResult.status == SNOMEDAlgorithmStatus.FAILURE) {
							failureSolutions += algorithmResult.numberOfSolutions;
							failureTimes += algorithmResult.totalTime;
						}
						out.printf(" *** %-35s|%-10s|%5ds|%6d%n",
								UnificationAlgorithmFactory.shortString(algorithmResult.unificationAlgorithmName),
								algorithmResult.status, algorithmResult.totalTime, algorithmResult.numberOfSolutions);
						totalTimes += algorithmResult.totalTime;
					}
					if (result.goalStatus == SNOMEDGoalStatus.COMPLETE) {
						// disregard incomplete test cases for averaging
						if (collectedResults.contains(SNOMEDAlgorithmStatus.SUCCESS)) {
							successful++;
							if (collectedResults.contains(SNOMEDAlgorithmStatus.FAILURE)) {
								disagreed++;
							}
						}
						if (!collectedResults.isEmpty()) {
							complete++;
						}
					}
				}
				out.println();

				out.println("* Test summary:");
				Format f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
				out.println("Start time: " + f.format(startTime));
				out.println("End time: " + f.format(Calendar.getInstance().getTime()));
				out.println("Elapsed time: " + elapsedTimer.elapsed(TimeUnit.SECONDS) + " s");
				out.println("Elapsed time (without ontology loading time): " + totalTimes + " s");
				out.println();

				out.println("Total test cases: " + results.size());
				out.println("Completed test cases: " + ((int) complete) + " ("
						+ (100 * complete / (float) results.size()) + "% of all test cases)");
				out.println("Successful test cases: " + ((int) successful) + " (" + (100 * successful / complete)
						+ "% of all completed test cases)");
				out.println();

				out.println("Average time of successful test cases: " + (successTimes / successful));
				out.println("    (only valid if just one algorithm was tested)");
				out.println(
						"Average number of solutions for successful test cases: " + (successSolutions / successful));
				out.println("    (only valid if just one algorithm was tested)");
				out.println();

				out.println("Average time of failed test cases: " + (failureTimes / (complete - successful)));
				out.println("    (only valid if just one algorithm was tested)");
				out.println("Average number of solutions for failed test cases: "
						+ (failureSolutions / (complete - successful)));
				out.println("    (only valid if just one algorithm was tested)");
				out.println();

				out.println("Test cases with disagreement: " + ((int) disagreed) + " (" + (100 * disagreed / complete)
						+ "% of all completed test cases)");
				out.println("    (only relevant if more than one algorithm was tested)");

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

		// 'Difficulty writing (finding)'
		// runSingleTest("Clinical history and observation findings (finding)",
		// 2, "http://snomed.info/id/102938007");

		// 'Does use the elements of language (finding)'
		// runSingleTest("Functional finding (finding)", 0,
		// "http://snomed.info/id/288606006");

		for (String parentClass : PARENT_CLASSES) {
			for (int rg : ROLE_GROUP_NO) {
				runModuleTests(parentClass, rg);
				System.out.println();
				System.out.println();
			}
		}

		for (int rg : ROLE_GROUP_NO) {
			runFullTests(rg);
		}
	}

	private static void runModuleTests(String parentClass, int rg) {
		System.out.println("========== Module tests for parent class: " + parentClass + " [" + rg + " RoleGroup(s)]");

		initializePrivateFields(parentClass, rg);

		try {
			// randomly select classes with full definition from the SNOMED
			// module
			randomTests();
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}

		printResults();
	}

	private static void runFullTests(int rg) {
		System.out.println("========== Full tests for SNOMED CT [" + rg + " RoleGroup(s)]");

		initializePrivateFields(null, rg);
		try {
			randomTests();
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}

		printResults();
	}

	private static void initializePrivateFields(String parentClass, int rg) {
		currentParentClass = parentClass;
		if (parentClass != null) {
			currentModulePath = SNOMED_MODULE_PATH + parentClass.replace('/', '-') + ".owl";
			currentClassesList = SNOMED_MODULE_PATH + parentClass.replace('/', '-') + ".txt";
		} else {
			currentModulePath = SNOMED_PATH;
			currentClassesList = null;
		}
		startTime = Calendar.getInstance().getTime();
		elapsedTimer = Stopwatch.createStarted();
		results = new ArrayList<SNOMEDResult>();
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		snomedModule = AlternativeUelStarter.loadOntology(currentModulePath, manager);
		snomedRestrictions = AlternativeUelStarter.loadOntology(SNOMED_RESTR_PATH, manager);
		bg = new HashSet<OWLOntology>(Arrays.asList(snomedModule, snomedRestrictions));

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
	}

	private static void runSingleTest(String parentClass, int rg, String id) {
		initializePrivateFields(parentClass, rg);

		OWLClass goalClass = cls(id);
		OWLClassExpression goalExpression = ((OWLEquivalentClassesAxiom) snomedModule
				.getAxioms(goalClass, Imports.EXCLUDED).iterator().next()).getClassExpressionsMinus(goalClass)
						.iterator().next();

		try {
			runSingleTest(goalClass, goalExpression);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	private static void randomTests() throws InterruptedException, IOException {
		List<OWLEquivalentClassesAxiom> definitions = new ArrayList<OWLEquivalentClassesAxiom>();
		if (currentClassesList != null) {
			definitions = Files.lines(Paths.get(currentClassesList)).map(line -> line.substring(1, line.length() - 1))
					.map(line -> factory.getOWLClass(IRI.create(line)))
					.flatMap(cls -> snomedModule.getAxioms(cls, Imports.EXCLUDED).stream())
					.filter(OWLEquivalentClassesAxiom.class::isInstance).map(OWLEquivalentClassesAxiom.class::cast)
					.collect(Collectors.toList());
		} else {
			definitions = snomedModule.getAxioms(AxiomType.EQUIVALENT_CLASSES).stream().collect(Collectors.toList());
		}

		Random rnd = new Random();
		System.out.println("Loading finished (" + definitions.size() + " classes with full definitions).");

		for (int i = 0; (i < MAX_TESTS) && (definitions.size() > 0); i++) {
			OWLEquivalentClassesAxiom axiom = definitions.get(rnd.nextInt(definitions.size()));
			definitions.remove(axiom);
			OWLClass goalClass = axiom.getNamedClasses().iterator().next();
			OWLClassExpression goalExpression = axiom.getClassExpressionsMinus(goalClass).iterator().next();
			System.out.println();
			System.out.print("***** [" + (i + 1) + "] ");
			runSingleTest(goalClass, goalExpression);
		}
	}

	private static void runSingleTest(OWLClass goalClass, OWLClassExpression goalExpression)
			throws InterruptedException {

		System.out.println("Goal class: " + getLabel(goalClass));

		printThreadInfo();
		SNOMEDTestInitialization initRunner = new SNOMEDTestInitialization(options, snomedModule, bg, goalClass,
				goalExpression);
		Thread initThread = new Thread(initRunner);
		initThread.start();
		initThread.join(TIMEOUT);
		SNOMEDResult result = initRunner.result;
		if (initThread.isAlive()) {
			System.out.println("Timeout!");
			printThreadInfo();
			initThread.interrupt();
			initThread.join(1000);
			if (initThread.isAlive()) {
				initThread.stop();
			}
			result.goalStatus = SNOMEDGoalStatus.TIMEOUT;
			result.buildGoal = TIMEOUT / 1000;
		}
		results.add(result);

		if (result.goalStatus == SNOMEDGoalStatus.SUCCESS) {
			// the goal was constructed successfully, start unification tests
			UnifierIterator iterator = initRunner.iterator;
			OWLOntology pos = initRunner.pos;
			OWLOntology neg = initRunner.neg;
			OWLAxiom goalAxiom = initRunner.goalAxiom;
			initRunner = null;
			initThread = null;

			for (int i = 0; i < TEST_ALGORITHMS.length; i++) {
				printThreadInfo();
				System.out.println("** Unification algorithm " + (i + 1) + ": " + TEST_ALGORITHMS[i]);
				options.unificationAlgorithmName = TEST_ALGORITHMS[i];
				iterator = iterator.resetModel();

				SNOMEDTest algorithmRunner = new SNOMEDTest(iterator, options, pos, neg, goalAxiom);
				Thread algorithmThread = new Thread(algorithmRunner);
				algorithmThread.setUncaughtExceptionHandler((t, ex) -> {
					printCurrentTime();
					ex.printStackTrace();
				});

				algorithmThread.start();
				algorithmThread.join(TIMEOUT);
				SNOMEDAlgorithmResult algorithmResult = algorithmRunner.result;

				if (algorithmThread.isAlive()) {
					System.out.println("Timeout!");
					printThreadInfo();

					algorithmThread.interrupt();
					algorithmThread.join(1000);
					if (algorithmThread.isAlive()) {
						try {
							algorithmThread.stop();
						} catch (ThreadDeath t) {
						}
						// force cleanup from outside in case the algorithm is
						// blocking, e.g., waiting for JSON output of clingo
						// TODO: find non-blocking JSON parser ;-)
						// TODO: find a way to interrupt MiniSat
						try {
							iterator.cleanup();
						} catch (RuntimeException ex) {
							printCurrentTime();
							ex.printStackTrace();
						}
					}

					algorithmResult.status = SNOMEDAlgorithmStatus.TIMEOUT;
					algorithmResult.totalTime = TIMEOUT / 1000;
				}

				result.algorithmResults.add(algorithmResult);
			}

			// all individual tests for this goal class have been completed
			result.goalStatus = SNOMEDGoalStatus.COMPLETE;
		}
	}

	private static void printCurrentTime() {
		System.out.println(new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(Calendar.getInstance().getTime()));
	}

	private static String getLabel(OWLEntity entity) {
		return EntitySearcher.getAnnotations(entity, snomedModule, factory.getRDFSLabel()).iterator().next().getValue()
				.asLiteral().get().getLiteral();
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
