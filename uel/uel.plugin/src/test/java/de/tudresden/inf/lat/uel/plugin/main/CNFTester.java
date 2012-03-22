package de.tudresden.inf.lat.uel.plugin.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import de.tudresden.inf.lat.uel.plugin.processor.UelProcessorFactory;
import de.tudresden.inf.lat.uel.plugin.type.AtomManager;
import de.tudresden.inf.lat.uel.plugin.type.AtomManagerImpl;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;
import de.tudresden.inf.lat.uel.type.impl.UelInputImpl;

/**
 * This class is used to convert a given SAT problem in DIMACS CNF format into a unification
 * problem and solve it with a specified unification algorithm.
 *
 * The translation is described in:
 * Franz Baader and Ralf Küsters. 'Matching concept descriptions with existential restrictions'.
 * In Proc. of the 7th Int. Conf. on Principles of Knowledge Representation and Reasoning (KR'00),
 * pages 261-272. Morgan Kaufmann, 2000.
 * 
 * The runtimes for computing the first unifier and all unifiers and the internal statistics of the
 * algorithm are printed to the command line.
 * 
 * @author Stefan Borgwardt
 *
 */
public class CNFTester {

	private static final String SPACES = "\\s+";

	/**
	 * This is the main entry point of the test application.
	 * @param args a String array with two arguments: the DIMACS CNF file and the name of the
	 *     unification algorithm (either 'SAT' or 'Rule') 
	 * @throws IOException if the input is invalid
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 2) {
			(new CNFTester()).run(args[0], args[1]);
		} else {
			System.out
					.println("Parameters: <input DIMACS CNF file> [SAT|Rule]");
		}
	}

	private void run(String cnfFile, String processorName) throws IOException {
		UelInput input = constructInput(cnfFile);
		UelProcessor processor;
		if (processorName.equals("SAT")) {
			processor = UelProcessorFactory.createProcessor(
					UelProcessorFactory.SAT_BASED_ALGORITHM, input);
		} else if (processorName.equals("Rule")) {
			processor = UelProcessorFactory.createProcessor(
					UelProcessorFactory.RULE_BASED_ALGORITHM, input);
		} else {
			throw new IOException(
					"Unknown processor. Please specify either 'SAT' or 'Rule'.");
		}

		long startTime = System.nanoTime();
		long firstTime = 0;

		boolean hasUnifiers = true;
		boolean first = true;
		while (hasUnifiers) {
			hasUnifiers = processor.computeNextUnifier();
			if (first) {
				firstTime = System.nanoTime();
				first = false;
			}
		}

		long endTime = System.nanoTime();
		System.out.println("first: " + (firstTime - startTime));
		System.out.println("all: " + (endTime - startTime));
		printInfo(processor);
	}

	private UelInput constructInput(String cnfFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(cnfFile));
		AtomManager atomManager = new AtomManagerImpl();
		Set<Equation> equations = new HashSet<Equation>();

		String[] line;
		// ignore initial comments and empty lines
		do {
			line = reader.readLine().split(SPACES);
		} while ((line.length == 0) || (line[0].equals("c")));

		if ((line.length != 4) || !line[0].equals("p")
				|| !line[1].equals("cnf")) {
			throw new IOException("Input file is not in DIMACS CNF format!");
		}

		int numVariables = Integer.parseInt(line[2]);
		int numClauses = Integer.parseInt(line[3]);

		ConceptName conceptU = atomManager.createConceptName("U", true);
		Integer uId = atomManager.getAtoms().getIndex(conceptU);
		ConceptName conceptV = atomManager.createConceptName("V", true);
		Integer vId = atomManager.getAtoms().getIndex(conceptV);
		ConceptName conceptA = atomManager.createConceptName("A", false);
		Integer aId = atomManager.getAtoms().getIndex(conceptA);
		ConceptName conceptB = atomManager.createConceptName("B", false);
		Integer bId = atomManager.getAtoms().getIndex(conceptB);
		Integer raId = atomManager.getAtoms().getIndex(
				atomManager.createExistentialRestriction("r", conceptA));
		Integer rbId = atomManager.getAtoms().getIndex(
				atomManager.createExistentialRestriction("r", conceptB));

		// ensure that Xi / Xni encode the truth value of variable i (A - true,
		// B - false)
		equations.add(new EquationImpl(uId, set(raId, rbId), false));
		for (int var = 1; var <= numVariables; var++) {
			ConceptName X = atomManager.createConceptName("X" + var, true);
			ConceptName Xn = atomManager.createConceptName("Xn" + var, true);
			Integer rx = atomManager.getAtoms().getIndex(
					atomManager.createExistentialRestriction("r", X));
			Integer rxn = atomManager.getAtoms().getIndex(
					atomManager.createExistentialRestriction("r", Xn));
			equations.add(new EquationImpl(uId, set(rx, rxn), false));
		}

		equations.add(new EquationImpl(vId, set(aId, bId), false));
		// translate all clauses
		for (int clause = 1; clause <= numClauses; clause++) {
			line = reader.readLine().split(SPACES);
			if ((line.length == 0) || !line[line.length - 1].equals("0")) {
				throw new IOException("Input file is not in DIMACS CNF format!");
			}

			// construct concept names for all literals in this clause
			Integer[] literals = new Integer[line.length];
			for (int litIdx = 0; litIdx < line.length - 1; litIdx++) {
				int literal = Integer.parseInt(line[litIdx]);
				if (literal == 0) {
					throw new IOException(
							"Input file is not in DIMACS CNF format!");
				}
				if (literal > 0) {
					literals[litIdx] = atomManager.getAtoms().getIndex(
							atomManager.createConceptName("X" + literal, true));
				} else {
					literals[litIdx] = atomManager.getAtoms().getIndex(
							atomManager.createConceptName("Xn" + (-literal),
									true));
				}
			}
			literals[line.length - 1] = bId;

			// encode the clause
			equations.add(new EquationImpl(vId, set(literals), false));
		}

		System.out.println(equations.size());
		return new UelInputImpl(atomManager.getAtoms(), equations, Collections
				.<Integer> emptySet());
	}

	private <T> Set<T> set(T... elements) {
		return new HashSet<T>(Arrays.asList(elements));
	}

	private void printInfo(UelProcessor processor) {
		for (Entry<String, String> info : processor.getInfo().entrySet()) {
			System.out.println(info.getKey() + ": " + info.getValue());
		}
		System.out.println();
	}

}
