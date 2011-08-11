package de.tudresden.inf.lat.uel.plugin.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudresden.inf.lat.uel.core.sat.SatInput;
import de.tudresden.inf.lat.uel.core.sat.Solver;
import de.tudresden.inf.lat.uel.core.sat.Translator;
import de.tudresden.inf.lat.uel.core.type.Goal;

/**
 * This class calls provides variations of unification procedure depending on
 * the input and output required.
 * 
 * @author Barbara Morawska
 */
class Unifier {

	private static final Logger logger = Logger.getLogger(Unifier.class
			.getName());

	private int numberofsolutions = 0;

	private Solver solver = null;
	private boolean test = false;

	public Unifier(Solver s) {
		solver = s;
	}

	/**
	 * If test is true, <code>filename</code>.TBox is created.
	 */
	public boolean getTest() {

		return test;

	}

	/**
	 * This method questions a user if another unifier should be computed. It
	 * returns true if a user answers "Y" and false otherwise.
	 * 
	 * stdout and stdin is used.
	 * 
	 * @return boolean
	 */
	public boolean questionYN() {
		System.out.print("Should I compute another unifier? (Y/N) ");

		String answer = "Y";

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			answer = in.readLine();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (answer.equalsIgnoreCase("Y")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method is used in the class Utilities.java If the option t is used
	 * by the Main class, test is set to true. This means that an additional
	 * file <code>filename</code>.TBox is created. This file is then used in
	 * testing obtained unifiers for correctness with Tester.
	 * 
	 * 
	 */
	public void setTest(boolean value) {

		test = value;

	}

	/**
	 * This method is the basic unification procedure. It returns true if the
	 * goal is unifiable and false otherwise.
	 * 
	 * It creates an input for a SAT solver, by using a method in Translator,
	 * and calls a solver with this input. It calls Translator again to detect
	 * if the solver returned "satisfiable" and to translate the output into a
	 * result.
	 * 
	 * @param translator
	 *            translator
	 * @param result
	 *            result
	 * @throws IOException
	 * @return true if and only if the goal is unifiable
	 */
	public boolean unify(Translator translator, Writer result)
			throws IOException {
		String res = solver.solve(translator.getSatInput().toString());

		StringReader satoutputReader = new StringReader(res);
		boolean response = translator.toTBox(satoutputReader, result);
		return response;
	}

	/**
	 * This method is used by Main class if the options string is empty (i.e. if
	 * the parameters for Main are input file and optionally an ontology file).
	 * 
	 * This method reads an input file, translates the goal equation into a set
	 * of flat equations with variables. If the goal is unifiable, then it
	 * writes UNIFIABLE to the stdout. If it is ununifiable, then it writes
	 * UNUNIFIABLE to the stdout. It does not create any output file.
	 * 
	 * It deletes some additional files created by the unification procedure.
	 * 
	 * @param goal
	 *            goal
	 * @throws Exception
	 */
	public void unify0(Goal goal) throws IOException {

		Translator translator = new Translator(goal);

		String satoutputStr = solver.solve(translator.getSatInput().toString());

		Pattern answer = Pattern.compile("^" + Solver.msgSat);
		BufferedReader reader = new BufferedReader(new StringReader(
				satoutputStr));
		String line = reader.readLine();
		Matcher manswer = answer.matcher(line);

		if (manswer.find()) {
			logger.info("UNIFIABLE");
		} else {
			logger.info("UNUNIFIABLE");
		}
	}

	/**
	 * This method is used by Main class if the options string contains "a".
	 * 
	 * 
	 * This method reads an input file, translates the goal equation into a set
	 * of flat equations with variables. (If the ontology file is provided, it
	 * loads ontology definitions too).
	 * 
	 * If the goal is ununifiable, then it writes UNUNIFIABLE to the output file
	 * and to the stdout.
	 * 
	 * If the goal is unifiable, then it writes UNIFIABLE to stdout and computes
	 * a unifier, while writing it to the output file. Next, it asks the user if
	 * he wants to see another unifier. If the answer is "Y" (or "y") then it
	 * computes the next unifier, etc. It terminates if the answer is not "Y"
	 * (or "y") or if there are no more unifiers.
	 * 
	 * 
	 * All computed unifiers are written to the output file.
	 * 
	 * @param goal
	 *            goal
	 * @throws Exception
	 */

	public String unifyA(Goal goal) throws IOException {

		StringWriter result = new StringWriter();
		Translator translator = new Translator(goal);

		boolean unifiable = unify(translator, result);

		SatInput satinput = translator.getSatInput();

		if (unifiable) {
			logger.info("UNIFIABLE\n" + "Unifier stored in file.");

			boolean test = questionYN();

			while (test && unifiable) {

				numberofsolutions++;

				satinput.add(translator.getUpdate().toString());
				String satoutputStr = solver.solve(satinput.toString());

				translator.reset();

				unifiable = translator.toTBox(new StringReader(satoutputStr),
						result);

				if (unifiable) {
					logger.info(numberofsolutions + " UNIFIER\n"
							+ "Unifier appended to file.");

					test = questionYN();
				} else {
					logger.info("NO MORE UNIFIERS");
				}

			}

			logger.info("Have a good day!");
		} else {
			logger.info("UNUNIFIABLE");
		}
		return result.toString();
	}

	/**
	 * This method is used by Main class if the options string contains an
	 * integer <i>.
	 * 
	 * 
	 * This method reads an input file, translates the goal equation into a set
	 * of flat equations with variables. (If the ontology file is provided, the
	 * method loads ontology definitions too).
	 * 
	 * If the goal is ununifiable, then it writes UNUNIFIABLE to the output file
	 * and to the stdout.
	 * 
	 * If the goal is unifiable, then it writes UNIFIABLE to stdout and computes
	 * <i> unifiers, while writing them to the output file.
	 * 
	 * @param goal
	 *            goal
	 * @throws Exception
	 */
	public String unifyInt(Goal goal, int max) throws IOException {

		StringWriter result = new StringWriter();

		Translator translator = new Translator(goal);

		int nbrUnifiers = 0;

		boolean unifiable = unify(translator, result);

		SatInput satinput = translator.getSatInput();

		if (unifiable) {

			logger.info("UNIFIABLE\n" + "Unifier stored in file.");

			nbrUnifiers++;

			while (unifiable && nbrUnifiers < max) {

				numberofsolutions++;

				satinput.add(translator.getUpdate().toString());
				String satoutputStr = solver.solve(satinput.toString());

				translator.reset();
				unifiable = translator.toTBox(new StringReader(satoutputStr),
						result);

				if (unifiable) {
					logger.info(numberofsolutions + " UNIFIER\n"
							+ "Unifier stored in file.");
					nbrUnifiers++;

				} else {
					logger.info("NO MORE UNIFIERS");
				}
			}

			logger.info("Have a good day!");
		} else {
			logger.info("UNUNIFIABLE");
		}

		return result.toString();
	}

	/**
	 * This method is used by Main class if the options string contains "n". If
	 * options contain also "t", then a file <code>filename</code>.TBox is
	 * created, for testing with Tester.
	 * 
	 * This method reads an input file, translates the goal equation into a set
	 * of flat equations with variables. (If the ontology file is provided, it
	 * loads ontology definitions too).
	 * 
	 * If the goal is ununifiable, then it writes UNUNIFIABLE to the output file
	 * and to the stdout.
	 * 
	 * If the goal is unifiable, then it writes UNIFIABLE to stdout and computes
	 * the number of all local unifiers.
	 * 
	 * The method does not write the unifiers into an output file.
	 * 
	 * @param goal
	 *            goal
	 * @throws Exception
	 */
	public void unifyN(Goal goal) throws IOException {
		Translator translator = new Translator(goal);

		boolean unifiable = true;
		boolean message = false;

		numberofsolutions = 0;

		SatInput satinput = translator.getSatInput();

		while (unifiable) {

			String satoutputStr = solver.solve(satinput.toString());

			StringReader satoutputReader = new StringReader(satoutputStr);
			if (translator.toTBox(satoutputReader)) {

				message = true;
				numberofsolutions++;

				satinput.add(translator.getUpdate().toString());
				satoutputStr = solver.solve(satinput.toString());

				translator.reset();

			} else {

				unifiable = false;
			}

		}

		if (message) {
			logger.info("UNIFIABLE\n" + numberofsolutions + " unifiers found");
		} else {
			logger.info("UNUNIFIABLE");
		}
	}

	/**
	 * This method is used by Main class if the options string contains "w" If
	 * ontology file (optional) is provided, it is loaded.
	 * 
	 * This method reads an input file, translates the goal equation into a set
	 * of flat equations with variables.
	 * 
	 * 
	 * If the options string provided for Main class contained "t" an additional
	 * file <code>filename</code>.TBox is created. This file is useful for
	 * testing unifiers with Tester.
	 * 
	 * 
	 * If the goal is unifiable, then the method writes UNIFIABLE and a unifier
	 * to the output file <code>filename</code>.unif and it writes UNIFIABLE to
	 * the stdout. If it is ununifiable, then it writes UNUNIFIABLE to the
	 * output file <code>filename</code>.unif and to the stdout. It deletes some
	 * additional files created by the unification procedure.
	 * 
	 * @param goal
	 *            goal
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public String unifySimple(Goal goal) throws IOException {

		StringWriter result = new StringWriter();
		Translator translator = new Translator(goal);

		if (unify(translator, result)) {
			logger.info("UNIFIABLE\n" + "Unifier stored in file.");
		} else {
			logger.info("UNUNIFIABLE");
		}

		return result.toString();
	}

	/**
	 * This method is used by Main class if the options string contains "x".
	 * 
	 * 
	 * This method reads an input file, translates the goal equation into a set
	 * of flat equations with variables. If ontology file is provided, it loads
	 * ontology definitions too.
	 * 
	 * If the options string provided for Main class contained "t" an additional
	 * file <code>filename</code>.TBox is created. This file is useful for
	 * testing unifiers with Tester.
	 * 
	 * If the goal is unifiable, then it writes UNIFIABLE to stdout and computes
	 * ALL local unifiers, while writing them to the output file. If it is
	 * ununifiable, then it writes UNUNIFIABLE to the output file and to the
	 * stdout. It deletes some additional files created by the unification
	 * procedure. It will terminate, but after an exponential time in the size
	 * of the goal, in the worst case. The output file can be very big.
	 * 
	 * @param goal
	 *            goal
	 * @throws Exception
	 */
	public String unifyX(Goal goal) throws IOException {

		StringWriter result = new StringWriter();
		Translator translator = new Translator(goal);

		boolean unifiable = unify(translator, result);

		SatInput satinput = translator.getSatInput();

		if (unifiable) {

			numberofsolutions++;
			logger.info("UNIFIABLE\n" + "Unifier stored in file.");

			while (unifiable) {

				numberofsolutions++;

				satinput.add(translator.getUpdate().toString());
				String satoutputStr = solver.solve(satinput.toString());

				translator.reset();

				unifiable = translator.toTBox(new StringReader(satoutputStr),
						result);

				if (unifiable) {
					logger.info(numberofsolutions + " UNIFIER\n"
							+ "Unifier appended to " + result.toString());
				} else {
					logger.info("NO MORE UNIFIERS");
				}

			}

			logger.info("Have a good day!");
		} else {
			logger.info("UNUNIFIABLE");
		}

		return result.toString();
	}

}
