package de.tudresden.inf.lat.uel.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tudresden.inf.lat.uel.infhandler.OutputStreamHandler;
import de.tudresden.inf.lat.uel.ontmanager.Ontology;

/**
 * This class starts a unifier from the command line.
 * 
 * @author Barbara Morawska
 * 
 */
public class Main {

	static final Logger logger = Logger.getLogger("Unifier");

	private int MaxNbr = 0;

	/**
	 * Starts a unifier from the command line.
	 * 
	 * @param args
	 *            a list containing the command line parameters first parameter:
	 *            options (optional); second ( or first if there are no options)
	 *            parameter: input file (required); third parameter: an ontology
	 *            file (optional, but if options string is non-empty it should
	 *            contain "t" in order for ontology to be loaded);
	 * 
	 * @throws Exception
	 * @author Barbara Morawska
	 * 
	 */

	public static void main(String[] args) throws Exception {
		(new Main()).run(args);
	}

	public Main() {
	}

	public void run(String[] args) throws Exception {
		Unifier unifier = new Unifier();
		Ontology ontology = new Ontology();
		Goal goal = new Goal(ontology);

		logger.setLevel(Level.INFO);
		logger.addHandler(new OutputStreamHandler(System.out));
		logger.setUseParentHandlers(false);

		if (args.length == 1 && !args[0].equalsIgnoreCase("-h")) {

			goal.initialize(args[0], unifier.getTest());
			unifier.setFileName(args[0]);
			unifier.unify0(goal);

		} else if (args.length > 1 && args[0].startsWith("-")) {

			if (args.length == 3) {

				ontology.loadOntology(new InputStreamReader(
						new FileInputStream(args[2])));

			}

			String argument = args[0].substring(1);

			switch (option(argument, unifier)) {

			case 1:

				help();
				break;

			case 2:

				goal.initialize(args[1], unifier.getTest());
				unifier.setFileName(args[1]);
				unifier.unifyA(goal);
				break;

			case 3:

				goal.initialize(args[1], unifier.getTest());
				unifier.setFileName(args[1]);
				unifier.unifyX(goal);
				break;

			case 4:

				goal.initialize(args[1], unifier.getTest());
				unifier.setFileName(args[1]);
				unifier.unifyN(goal);
				break;

			case 5:

				goal.initialize(args[1], unifier.getTest());
				unifier.setFileName(args[1]);
				unifier.unifyInt(goal, MaxNbr);
				break;

			case 6:

				goal.initialize(args[1], unifier.getTest());
				unifier.setFileName(args[1]);
				unifier.unifySimple(goal);
				break;

			default:

				logger.info("Wrong option.");
				help();

				break;

			}

		} else if (args.length == 2 && !args[0].startsWith("-")) {

			ontology.loadOntology(new InputStreamReader(new FileInputStream(
					args[1])));

			goal.initialize(args[0], unifier.getTest());
			unifier.setFileName(args[0]);
			unifier.unify0(goal);

		} else {

			help();

		}

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
	public void help() {

		try {
			InputStream help = Main.class.getResourceAsStream("/readme.txt");
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

	/**
	 * This method analyzes the option string given to the Main class as one of
	 * the parameters.
	 * 
	 * 
	 * 
	 * @param argument
	 * @return
	 */

	public int option(String argument, Unifier unifier) {

		boolean test = false;

		if (argument.contains("t")) {

			unifier.setTest(true);

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
