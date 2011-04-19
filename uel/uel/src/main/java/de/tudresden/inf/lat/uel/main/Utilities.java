package de.tudresden.inf.lat.uel.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 
 * This is a static class containing helpful tools that do not belong anywhere
 * else :)
 * 
 * 
 * @author Barbara Morawska
 * 
 */

public class Utilities {

	public static int MaxNbr = 0;

	private Utilities() {
	};

	/**
	 * This method questions a user if another unifier should be computed. It
	 * returns true if a user answers "Y" and false otherwise.
	 * 
	 * stdout and stdin is used.
	 * 
	 * @return boolean
	 */
	public static boolean questionYN() {
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
	 * This method analyzes the option string given to the Main class as one of
	 * the parameters.
	 * 
	 * 
	 * 
	 * @param argument
	 * @return
	 */

	public static int option(String argument) {

		boolean test = false;

		if (argument.contains("t")) {

			Unifier.setTest(true);

		}

		if (argument.contains("h")) {

			return 1;

		} else if (argument.contains("a")) {

			return 2;

		} else if (argument.contains("x")) {

			return 3;

		} else if (argument.contains("n")) {

			return 4;

		} else if (argument.contains("w")) {

			return 6;
		} else {

			if (test) {

				argument = argument.substring(0, argument.length() - 1);

			}

			MaxNbr = Integer.parseInt(argument);

			return 5;
		}

	}

}
