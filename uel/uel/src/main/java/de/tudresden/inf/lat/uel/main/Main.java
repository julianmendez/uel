package de.tudresden.inf.lat.uel.main;

import java.io.BufferedReader;
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

		Unifier unifier = new Unifier();

		logger.setLevel(Level.INFO);
		logger.addHandler(new OutputStreamHandler(System.out));
		logger.setUseParentHandlers(false);

		if (args.length == 1 && !args[0].equalsIgnoreCase("-h")) {

			unifier.setFileName(args[0]);
			unifier.unify0();

		} else if (args.length > 1 && args[0].startsWith("-")) {

			if (args.length == 3) {

				Ontology.loadOntology(args[2]);

			}

			String argument = args[0].substring(1);

			switch (Utilities.option(argument, unifier)) {

			case 1:

				help();
				break;

			case 2:

				unifier.setFileName(args[1]);
				unifier.unifyA();
				break;

			case 3:

				unifier.setFileName(args[1]);
				unifier.unifyX();
				break;

			case 4:

				unifier.setFileName(args[1]);
				unifier.unifyN();
				break;

			case 5:

				unifier.setFileName(args[1]);
				unifier.unifyInt(Utilities.MaxNbr);
				break;

			case 6:

				unifier.setFileName(args[1]);
				unifier.unifySimple();
				break;

			default:

				logger.info("Wrong option.");
				help();

				break;

			}

		} else if (args.length == 2 && !args[0].startsWith("-")) {

			Ontology.loadOntology(args[1]);

			unifier.setFileName(args[0]);
			unifier.unify0();

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
	public static void help() {

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

}
