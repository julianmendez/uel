package de.tudresden.inf.lat.uel.plugin.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.UnificationAlgorithm;
import de.tudresden.inf.lat.uel.type.impl.AtomManagerImpl;

/**
 * This class is used to convert a given SAT problem in DIMACS CNF format into a
 * unification problem and solve it with a specified unification algorithm.
 * 
 * The translation is described in: Franz Baader and Ralf Kuesters. 'Matching
 * concept descriptions with existential restrictions'. In Proc. of the 7th Int.
 * Conf. on Principles of Knowledge Representation and Reasoning (KR'00), pages
 * 261-272. Morgan Kaufmann, 2000.
 * 
 * The runtimes for computing the first unifier and all unifiers and the
 * internal statistics of the algorithm are printed to the command line.
 * 
 * @author Stefan Borgwardt
 * 
 */
public class CNFChecker {

	private static final String SPACES = "\\s+";

	private Goal goal;

	/**
	 * Construct a new test object to run all different known algorithms on an
	 * input CNF file.
	 * 
	 * @param filename
	 *            the name of the CNF file
	 * @throws IOException
	 *             if there was an error parsing the input file
	 */
	public CNFChecker(String filename) throws IOException {
		constructInput(filename);
	}

	private void constructInput(String cnfFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(cnfFile));
		final AtomManager atomManager = new AtomManagerImpl();
		final Set<Equation> equations = new HashSet<>();

		String[] line;
		// ignore initial comments and empty lines
		do {
			line = reader.readLine().split(SPACES);
		} while ((line.length == 0) || (line[0].equals("c")));

		if ((line.length != 4) || !line[0].equals("p") || !line[1].equals("cnf")) {
			reader.close();
			throw new IOException("Input file is not in DIMACS CNF format!");
		}

		int numVariables = Integer.parseInt(line[2]);
		int numClauses = Integer.parseInt(line[3]);

		Integer uId = atomManager.createConceptName("U", false);
		atomManager.makeDefinitionVariable(uId);
		Integer vId = atomManager.createConceptName("V", false);
		atomManager.makeDefinitionVariable(vId);
		Integer aId = atomManager.createConceptName("A", false);
		Integer bId = atomManager.createConceptName("B", false);
		Integer raId = atomManager.createExistentialRestriction("r", aId);
		Integer rbId = atomManager.createExistentialRestriction("r", bId);

		// ensure that Xi / Xni encode the truth value of variable i (A - true,
		// B - false)
		equations.add(new Equation(Collections.singleton(uId), set(raId, rbId)));
		for (int var = 1; var <= numVariables; var++) {
			Integer xId = atomManager.createConceptName("X" + var, false);
			atomManager.makeUserVariable(xId);
			Integer xnId = atomManager.createConceptName("Xn" + var, false);
			atomManager.makeUserVariable(xnId);
			Integer rxId = atomManager.createExistentialRestriction("r", xId);
			Integer rxnId = atomManager.createExistentialRestriction("r", xnId);
			equations.add(new Equation(Collections.singleton(uId), set(rxId, rxnId)));
		}

		equations.add(new Equation(Collections.singleton(vId), set(aId, bId)));
		// translate all clauses
		for (int clause = 1; clause <= numClauses; clause++) {
			line = reader.readLine().split(SPACES);
			if ((line.length == 0) || !line[line.length - 1].equals("0")) {
				reader.close();
				throw new IOException("Input file is not in DIMACS CNF format!");
			}

			// construct concept names for all literals in this clause
			Set<Integer> literals = new HashSet<>();
			for (int litIdx = 0; litIdx < line.length - 1; litIdx++) {
				int literal = Integer.parseInt(line[litIdx]);
				if (literal == 0) {
					reader.close();
					throw new IOException("Input file is not in DIMACS CNF format!");
				}
				Integer atomId = (literal > 0) ? atomManager.createConceptName("X" + literal, false)
						: atomManager.createConceptName("Xn" + (-literal), false);
				atomManager.makeDefinitionVariable(atomId);
				literals.add(atomId);
			}
			literals.add(bId);

			// encode the clause
			equations.add(new Equation(Collections.singleton(vId), literals));
		}
		reader.close();

		System.out.println("equations: " + equations.size());
		goal = new Goal() {
			@Override
			public AtomManager getAtomManager() {
				return atomManager;
			}

			@Override
			public Set<Equation> getEquations() {
				return equations;
			}
		};
	}

	/**
	 * This is the main entry point of the test application.
	 * 
	 * @param args
	 *            a String array with two arguments: the DIMACS CNF file and the
	 *            name of the unification algorithm (either 'SAT' or 'Rule')
	 * @throws IOException
	 *             if the input is invalid
	 * @throws InterruptedException
	 *             if the execution is interrupted
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length == 1) {
			CNFChecker tester = new CNFChecker(args[0]);
			tester.runAlgorithm(UnificationAlgorithmFactory.SAT_BASED_ALGORITHM);
			tester.runAlgorithm(UnificationAlgorithmFactory.SAT_BASED_ALGORITHM_MINIMAL);
			tester.runAlgorithm(UnificationAlgorithmFactory.RULE_BASED_ALGORITHM);
			tester.runAlgorithm(UnificationAlgorithmFactory.ASP_BASED_ALGORITHM);
		} else {
			System.out.println("Parameters: <input DIMACS CNF file>");
		}
	}

	private static void printInfo(UnificationAlgorithm algorithm) {
		for (Entry<String, String> info : algorithm.getInfo()) {
			System.out.println(info.getKey() + ": " + info.getValue());
		}
		System.out.println();
	}

	/**
	 * Run the test on a given unification algorithm.
	 * 
	 * @param algorithmName
	 *            the string identifier of the algorithm
	 * @throws InterruptedException
	 *             if the execution is interrupted
	 */
	public void runAlgorithm(String algorithmName) throws InterruptedException {
		UnificationAlgorithm algorithm = UnificationAlgorithmFactory.instantiateAlgorithm(algorithmName, goal);
		int numberOfUnifiers = 0;
		long startTime = System.nanoTime();
		long firstTime = 0;

		boolean hasUnifiers = true;
		boolean first = true;
		while (hasUnifiers) {
			if (algorithm.computeNextUnifier()) {
				hasUnifiers = true;
				numberOfUnifiers++;
			} else {
				hasUnifiers = false;
			}
			if (first) {
				firstTime = System.nanoTime();
				first = false;
			}
		}

		long endTime = System.nanoTime();
		System.out.println("first: " + (firstTime - startTime));
		System.out.println("all: " + (endTime - startTime));
		System.out.println("unifiers: " + numberOfUnifiers);
		printInfo(algorithm);
	}

	private static <T> Set<T> set(T a, T b) {
		Set<T> set = new HashSet<>();
		set.add(a);
		set.add(b);
		return set;
	}

}
