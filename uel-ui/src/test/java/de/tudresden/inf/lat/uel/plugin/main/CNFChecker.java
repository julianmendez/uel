package de.tudresden.inf.lat.uel.plugin.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import de.tudresden.inf.lat.uel.core.processor.UelProcessorFactory;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.SmallEquation;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.AtomManagerImpl;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;
import de.tudresden.inf.lat.uel.type.impl.UelInputImpl;

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

	private UelInput input;
	private AtomManager atomManager;

	/**
	 * Construct a new test object to run all different known processors on an
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
		atomManager = new AtomManagerImpl();
		Set<Equation> equations = new HashSet<Equation>();
		Set<Integer> userVariables = new HashSet<Integer>();

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

		ConceptName conceptU = atomManager.createConceptName("U", true);
		Integer uId = atomManager.getAtoms().getIndex(conceptU);
		// userVariables.add(uId);
		ConceptName conceptV = atomManager.createConceptName("V", true);
		Integer vId = atomManager.getAtoms().getIndex(conceptV);
		// userVariables.add(vId);
		ConceptName conceptA = atomManager.createConceptName("A", false);
		Integer aId = atomManager.getAtoms().getIndex(conceptA);
		ConceptName conceptB = atomManager.createConceptName("B", false);
		Integer bId = atomManager.getAtoms().getIndex(conceptB);
		Integer raId = atomManager.getAtoms().getIndex(atomManager.createExistentialRestriction("r", conceptA));
		Integer rbId = atomManager.getAtoms().getIndex(atomManager.createExistentialRestriction("r", conceptB));

		// ensure that Xi / Xni encode the truth value of variable i (A - true,
		// B - false)
		equations.add(new EquationImpl(uId, set(raId, rbId), false));
		for (int var = 1; var <= numVariables; var++) {
			ConceptName X = atomManager.createConceptName("X" + var, true);
			userVariables.add(atomManager.getIndex(X));
			ConceptName Xn = atomManager.createConceptName("Xn" + var, true);
			userVariables.add(atomManager.getIndex(Xn));
			Integer rx = atomManager.getAtoms().getIndex(atomManager.createExistentialRestriction("r", X));
			Integer rxn = atomManager.getAtoms().getIndex(atomManager.createExistentialRestriction("r", Xn));
			equations.add(new EquationImpl(uId, set(rx, rxn), false));
		}

		equations.add(new EquationImpl(vId, set(aId, bId), false));
		// translate all clauses
		for (int clause = 1; clause <= numClauses; clause++) {
			line = reader.readLine().split(SPACES);
			if ((line.length == 0) || !line[line.length - 1].equals("0")) {
				reader.close();
				throw new IOException("Input file is not in DIMACS CNF format!");
			}

			// construct concept names for all literals in this clause
			Integer[] literals = new Integer[line.length];
			for (int litIdx = 0; litIdx < line.length - 1; litIdx++) {
				int literal = Integer.parseInt(line[litIdx]);
				if (literal == 0) {
					reader.close();
					throw new IOException("Input file is not in DIMACS CNF format!");
				}
				if (literal > 0) {
					literals[litIdx] = atomManager.getAtoms()
							.getIndex(atomManager.createConceptName("X" + literal, true));
				} else {
					literals[litIdx] = atomManager.getAtoms()
							.getIndex(atomManager.createConceptName("Xn" + (-literal), true));
				}
			}
			literals[line.length - 1] = bId;

			// encode the clause
			equations.add(new EquationImpl(vId, set(literals), false));
		}
		reader.close();

		System.out.println("equations: " + equations.size());
		input = new UelInputImpl(atomManager.getAtoms(), Collections.<Equation> emptySet(), equations,
				Collections.<SmallEquation> emptySet(), userVariables);
	}

	/**
	 * This is the main entry point of the test application.
	 * 
	 * @param args
	 *            a String array with two arguments: the DIMACS CNF file and the
	 *            name of the unification algorithm (either 'SAT' or 'Rule')
	 * @throws IOException
	 *             if the input is invalid
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length == 1) {
			CNFChecker tester = new CNFChecker(args[0]);
			tester.runProcessor(UelProcessorFactory.SAT_BASED_ALGORITHM);
			tester.runProcessor(UelProcessorFactory.SAT_BASED_ALGORITHM_MINIMAL);
			tester.runProcessor(UelProcessorFactory.RULE_BASED_ALGORITHM);
			tester.runProcessor(UelProcessorFactory.ASP_BASED_ALGORITHM);
		} else {
			System.out.println("Parameters: <input DIMACS CNF file>");
		}
	}

	private static void printInfo(UelProcessor processor) {
		for (Entry<String, String> info : processor.getInfo()) {
			System.out.println(info.getKey() + ": " + info.getValue());
		}
		System.out.println();
	}

	/**
	 * Run the test on a given UEL processor.
	 * 
	 * @param processorName
	 *            the string identifier of the processor
	 */
	public void runProcessor(String processorName) throws InterruptedException {
		UelProcessor processor = UelProcessorFactory.createProcessor(processorName, input);
		int numberOfUnifiers = 0;
		long startTime = System.nanoTime();
		long firstTime = 0;

		boolean hasUnifiers = true;
		boolean first = true;
		while (hasUnifiers) {
			if (processor.computeNextUnifier()) {
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
		printInfo(processor);
	}

	private static <T> Set<T> set(T[] elements) {
		return new HashSet<T>(Arrays.asList(elements));
	}

	private static <T> Set<T> set(T a, T b) {
		Set<T> set = new HashSet<T>();
		set.add(a);
		set.add(b);
		return set;
	}

}
