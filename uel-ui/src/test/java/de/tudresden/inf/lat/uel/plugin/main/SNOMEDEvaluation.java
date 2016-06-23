/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;

import com.google.common.base.Stopwatch;

import de.tudresden.inf.lat.uel.core.main.AlternativeUelStarter;
import de.tudresden.inf.lat.uel.core.processor.UelOptions;
import de.tudresden.inf.lat.uel.core.processor.UelOptions.UndefBehavior;
import de.tudresden.inf.lat.uel.core.processor.UelOptions.Verbosity;
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;
import de.tudresden.inf.lat.uel.plugin.main.SNOMEDResult.SNOMEDStatus;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDEvaluation {

	// private static final String WORK_DIR = "C:\\Users\\Stefan\\Work\\";
	static final String WORK_DIR = "/Users/stefborg/Documents/";
	static final String OUTPUT_PATH = WORK_DIR + "Projects/uel-snomed/results.txt";
	static final String SNOMED_PATH = WORK_DIR + "Ontologies/snomed-english-rdf.owl";
	static final String SNOMED_RESTR_PATH = WORK_DIR + "Ontologies/snomed-restrictions.owl";
	// private static final String POS_PATH = WORK_DIR +
	// "Projects/uel-snomed/uel-snomed-pos.owl";
	// private static final String NEG_PATH = WORK_DIR +
	// "Projects/uel-snomed/uel-snomed-neg.owl";
	// private static final String CONSTRAINTS_PATH = WORK_DIR +
	// "Projects/uel-snomed/constraints_const.owl";
	private static final int MAX_TESTS = 100;
	private static final long TIMEOUT = 10 * 60 * 1000;
	private static List<SNOMEDResult> results = new ArrayList<SNOMEDResult>();
	private static UelOptions options = new UelOptions();

	static OWLClass cls(OWLDataFactory factory, String name) {
		return factory.getOWLClass(IRI.create("http://www.ihtsdo.org/" + name));
	}

	static OWLObjectProperty prp(OWLDataFactory factory, String name) {
		return factory.getOWLObjectProperty(IRI.create("http://www.ihtsdo.org/" + name));
	}

	/**
	 * Entry point for tests.
	 * 
	 * @param args
	 *            arguments (ignored)
	 */
	public static void main(String[] args) {

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (results != null) {
				try {
					System.out.println("Saving results to file...");
					PrintStream out = new PrintStream(OUTPUT_PATH);
					out.println(options);
					out.println("Timeout (s): " + (TIMEOUT / 1000));
					out.println();
					out.println(
							"Goal class                              |Status    |Build |Size  |Pre   |First |Goal  |All   |Number");
					out.println(
							"----------------------------------------+----------+------+------+------+------+------+------+------");
					for (SNOMEDResult result : results) {
						out.printf("%-40s|%-10s|%5ds|%6d|%5ds|%5ds|%5ds|%5ds|%6d%n", result.goalClass, result.status,
								result.buildGoal, result.goalSize, result.preprocessing, result.firstUnifier,
								result.goalUnifier, result.allUnifiers, result.numberOfSolutions);
					}
					out.close();
				} catch (FileNotFoundException ex) {
					ex.printStackTrace();
				}
			}
		}));

		options.verbosity = Verbosity.SHORT;
		options.undefBehavior = UndefBehavior.CONSTANTS;
		options.snomedMode = true;
		options.unificationAlgorithmName = UnificationAlgorithmFactory.SAT_BASED_ALGORITHM;
		options.expandPrimitiveDefinitions = true;
		options.restrictUndefContext = true;
		options.numberOfRoleGroups = 3;
		options.minimizeSolutions = true;
		options.noEquivalentSolutions = true;
		options.numberOfSiblings = -1;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLOntology snomed = AlternativeUelStarter.loadOntology(SNOMED_PATH, manager);
		OWLOntology snomedRestrictions = AlternativeUelStarter.loadOntology(SNOMED_RESTR_PATH, manager);
		Set<OWLOntology> bg = new HashSet<OWLOntology>(Arrays.asList(snomed, snomedRestrictions));

		// 'Difficulty writing (finding)': 30s; new: 21 s / 6,7 min
		// OWLClass goalClass = cls("SCT_102938007");

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

		// 'Unable to air laundry (finding)': needs 3 RoleGroups; 1min (5) / >
		// 20min
		// OWLClass goalClass = cls("SCT_286073006");

		// 'Echoencephalogram abnormal (finding)': too large
		// OWLClass goalClass = cls(factory, "SCT_274538008");

		// 'Primary malignant neoplasm of pyriform sinus (disorder)'
		// OWLClass goalClass = cls(factory, "SCT_93978008");

		// test single class
		// OWLClassExpression goalExpression = ((OWLEquivalentClassesAxiom)
		// snomed.getAxioms(goalClass, Imports.EXCLUDED)
		// .iterator().next()).getClassExpressionsMinus(goalClass).iterator().next();
		// runTest(options, snomed, bg, goalClass, goalExpression);

		// randomly select classes with full definition from SNOMED
		List<OWLEquivalentClassesAxiom> definitions = new ArrayList<OWLEquivalentClassesAxiom>(
				snomed.getAxioms(AxiomType.EQUIVALENT_CLASSES));
		Random rnd = new Random();

		for (int i = 0; i < MAX_TESTS; i++) {
			OWLEquivalentClassesAxiom axiom = definitions.get(rnd.nextInt(definitions.size()));
			OWLClass goalClass = axiom.getNamedClasses().iterator().next();
			OWLClassExpression goalExpression = axiom.getClassExpressionsMinus(goalClass).iterator().next();
			printThreadInfo();
			System.out
					.println("***** [" + i + "] Goal class: "
							+ EntitySearcher
									.getAnnotations(goalClass, manager.getOntologies(),
											OWLManager.getOWLDataFactory().getRDFSLabel())
									.iterator().next().getValue());

			SNOMEDTest test = new SNOMEDTest(options, snomed, bg, goalClass, goalExpression);
			test.start();
			try {
				test.join(TIMEOUT);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				return;
			}

			SNOMEDResult result = test.result;
			if (test.isAlive()) {
				test.interrupt();
				result.status = SNOMEDStatus.TIMEOUT;
			}
			results.add(result);
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

	static void printThreadInfo() {
		for (Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
			Thread t = e.getKey();
			System.out.println("Thread " + t.getName() + ", " + t.getState());
			for (StackTraceElement el : e.getValue()) {
				System.out.println(el);
			}
		}
	}
}
