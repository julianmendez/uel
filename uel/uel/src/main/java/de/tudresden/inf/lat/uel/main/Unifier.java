package de.tudresden.inf.lat.uel.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudresden.inf.lat.uel.sattranslator.Translator;

/**
 * This static class calls provides variations of unification procedure
 * depending on the input and output required.
 * 
 * @author Barbara Morawska
 * 
 */

public class Unifier {

	private static boolean test = false;

	private static File satinput;

	private static File satoutput;

	private static File result;

	private static int numberofsolutions = 0;

	private static Translator translator = new Translator();

	private Unifier() {
	};

	/**
	 * This method is used in the class Utilities.java If the option t is used
	 * by the Main class, test is set to true. This means that an additional
	 * file <filename>.TBox is created. This file is then used in testing
	 * obtained unifiers for correctness with Tester.
	 * 
	 * 
	 */
	public static void setTest(boolean value) {

		test = value;

	}

	/**
	 * If test is true, <filename>.TBox is created.
	 */
	public static boolean getTest() {

		return test;

	}

	/**
	 * This method is the basic unification procedure. It returns true if the
	 * goal is unifiable and false otherwise.
	 * 
	 * It creates an input file <satinput> for a sat solver, by calling the
	 * static method of Translator. It calls MiniSat on this file. The output of
	 * MiniSat is saved in file <satoutput>. It calls Translator again to detect
	 * if MiniSat returned "satisfiable" and to translate MiniSat output into a
	 * unifier. The unifier is written into result.
	 * 
	 * @param filename
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */

	private static boolean unify(String filename) throws IOException,
			InterruptedException {

		satinput = new File(filename.concat(".in"));

		satoutput = new File(filename.concat(".out"));

		result = new File(filename.concat(".unif"));

		translator.toDIMACS(satinput);

		ProcessBuilder pb = new ProcessBuilder("MiniSat", satinput.toString(),
				satoutput.toString());
		Process p = pb.start();

		p.waitFor();

		p.destroy();

		boolean response = translator.toTBox(satoutput, result);

		return response;

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
	 * file <filename>.TBox is created. This file is useful for testing unifiers
	 * with Tester.
	 * 
	 * 
	 * If the goal is unifiable, then the method writes UNIFIABLE and a unifier
	 * to the output file <filename>.unif and it writes UNIFIABLE to the stdout.
	 * If it is ununifiable, then it writes UNUNIFIABLE to the output file
	 * <filename>.unif and to the stdout. It deletes some additional files
	 * created by the unification procedure.
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public static void unifySimple(String filename) throws Exception {

		Goal.initialize(filename);

		if (unify(filename)) {

			Main.logger.info("UNIFIABLE\n" + "Unifier printed to "
					+ result.toString());
		} else {

			Main.logger.info("UNUNIFIABLE");
		}

		satinput.delete();
		satoutput.delete();

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
	 * file <filename>.TBox is created. This file is useful for testing unifiers
	 * with Tester.
	 * 
	 * If the goal is unifiable, then it writes UNIFIABLE to stdout and computes
	 * ALL local unifiers, while writing them to the output file. If it is
	 * ununifiable, then it writes UNUNIFIABLE to the output file and to the
	 * stdout. It deletes some additional files created by the unification
	 * procedure. It will terminate, but after an exponential time in the size
	 * of the goal, in the worst case. The output file can be very big.
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public static void unifyX(String filename) throws Exception {

		Goal.initialize(filename);

		boolean unifiable = unify(filename);

		if (unifiable) {

			numberofsolutions++;
			Main.logger.info("UNIFIABLE\n" + "Unifier printed to "
					+ result.toString());

			while (unifiable) {

				numberofsolutions++;

				PrintWriter satout = new PrintWriter(new BufferedWriter(
						new FileWriter(satinput, true)));

				String additionalline = translator.Update + " 0 ";

				satout.println(additionalline);

				satout.close();

				ProcessBuilder pb = new ProcessBuilder("MiniSat",
						satinput.toString(), satoutput.toString());
				Process p = pb.start();

				p.waitFor();

				p.destroy();

				translator.reset();
				unifiable = translator.toTBoxB(satoutput, result,
						numberofsolutions);

				if (unifiable) {

					Main.logger.info(numberofsolutions + " UNIFIER\n"
							+ "Unifier appended to " + result.toString());

				} else {

					Main.logger.info("NO MORE UNIFIERS");

				}

			}

			Main.logger.info("Have a good day!");

		} else {

			Main.logger.info("UNUNIFIABLE");

		}

		satinput.delete();
		satoutput.delete();

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
	 * @param filename
	 * @throws Exception
	 */

	public static void unifyA(String filename) throws Exception {

		Goal.initialize(filename);

		boolean unifiable = unify(filename);

		if (unifiable) {

			Main.logger.info("UNIFIABLE\n" + "Unifier printed to "
					+ result.toString());

			boolean test = Utilities.questionYN();

			while (test && unifiable) {

				numberofsolutions++;

				PrintWriter satout = new PrintWriter(new BufferedWriter(
						new FileWriter(satinput, true)));

				String additionalline = translator.Update + " 0 ";

				satout.println(additionalline);

				satout.close();

				ProcessBuilder pb = new ProcessBuilder("MiniSat",
						satinput.toString(), satoutput.toString());
				Process p = pb.start();

				p.waitFor();

				p.destroy();

				translator.reset();
				unifiable = translator.toTBoxB(satoutput, result,
						numberofsolutions);

				if (unifiable) {

					Main.logger.info(numberofsolutions + " UNIFIER\n"
							+ "Unifier appended to " + result.toString());

					test = Utilities.questionYN();
				} else {

					Main.logger.info("NO MORE UNIFIERS");

				}

			}

			Main.logger.info("Have a good day!");

		} else {

			Main.logger.info("UNUNIFIABLE");

		}

		satinput.delete();
		satoutput.delete();

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
	 * @param filename
	 * @throws Exception
	 */
	public static void unify0(String filename) throws Exception {

		satinput = new File(filename.concat(".in"));

		satoutput = new File(filename.concat(".out"));

		Goal.initialize(filename);

		translator.toDIMACS(satinput);

		ProcessBuilder pb = new ProcessBuilder("MiniSat", satinput.toString(),
				satoutput.toString());
		Process p = pb.start();

		p.waitFor();

		p.destroy();

		Pattern answer = Pattern.compile("^SAT");
		Matcher manswer;
		String line;

		FileReader fileReader = new FileReader(satoutput);

		BufferedReader reader = new BufferedReader(fileReader);

		line = reader.readLine();

		manswer = answer.matcher(line);

		if (manswer.find()) {

			Main.logger.info("UNIFIABLE");

		} else {

			Main.logger.info("UNUNIFIABLE");

		}

		satinput.delete();
		satoutput.delete();

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
	 * @param filename
	 * @throws Exception
	 */
	public static void unifyInt(int max, String filename) throws Exception {

		Goal.initialize(filename);

		int nbrUnifiers = 0;

		boolean unifiable = unify(filename);

		if (unifiable) {

			Main.logger.info("UNIFIABLE\n" + "Unifier printed to "
					+ result.toString());

			nbrUnifiers++;

			while (unifiable && nbrUnifiers < max) {

				numberofsolutions++;

				PrintWriter satout = new PrintWriter(new BufferedWriter(
						new FileWriter(satinput, true)));

				String additionalline = translator.Update + " 0 ";

				satout.println(additionalline);

				satout.close();

				ProcessBuilder pb = new ProcessBuilder("MiniSat",
						satinput.toString(), satoutput.toString());
				Process p = pb.start();

				p.waitFor();

				p.destroy();

				translator.reset();
				unifiable = translator.toTBoxB(satoutput, result,
						numberofsolutions);

				if (unifiable) {

					Main.logger.info(numberofsolutions + " UNIFIER\n"
							+ "Unifier appended to " + result.toString());
					nbrUnifiers++;

				} else {

					Main.logger.info("NO MORE UNIFIERS");

				}

			}

			Main.logger.info("Have a good day!");

		} else {

			Main.logger.info("UNUNIFIABLE");

		}

		satinput.delete();
		satoutput.delete();

	}

	/**
	 * This method is used by Main class if the options string contains "n". If
	 * options contain also "t", then a file <filename>.TBox is created, for
	 * testing with Tester.
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
	 * 
	 * 
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public static void unifyN(String filename) throws Exception {

		Goal.initialize(filename);

		boolean unifiable = true;
		boolean message = false;

		numberofsolutions = 0;

		satinput = new File(filename.concat(".in"));

		satoutput = new File(filename.concat(".out"));

		translator.toDIMACS(satinput);

		while (unifiable) {

			ProcessBuilder pb = new ProcessBuilder("MiniSat",
					satinput.toString(), satoutput.toString());
			Process p = pb.start();

			p.waitFor();

			p.destroy();

			if (translator.toTBox(satoutput)) {

				message = true;
				numberofsolutions++;

				PrintWriter satin = new PrintWriter(new BufferedWriter(
						new FileWriter(satinput, true)));

				String additionalline = translator.Update + " 0 ";

				satin.println(additionalline);

				satin.close();

				translator.reset();

			} else {

				unifiable = false;
			}

		}

		if (message) {

			Main.logger.info("UNIFIABLE\n" + numberofsolutions
					+ " unifiers found");

		} else {

			Main.logger.info("UNUNIFIABLE");
		}

		satinput.delete();
		satoutput.delete();

	}

	/**
	 * This method is used by Main class if the options string is "-h" or "-H"
	 * or if the options are not valid.
	 * 
	 * The method displays the help file readme.txt to stdout.
	 * 
	 * 
	 * 
	 */
	public static void help() {

		try {
			InputStream help = Unifier.class.getResourceAsStream("/readme.txt");
			BufferedReader helpreader = new BufferedReader(
					new InputStreamReader(help));

			String line;

			while (null != (line = helpreader.readLine())) {

				System.out.println(line);

			}
		} catch (Exception ex) {

			ex.printStackTrace();
		}

	}

}
