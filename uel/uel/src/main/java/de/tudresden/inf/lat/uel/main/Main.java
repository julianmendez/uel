package de.tudresden.inf.lat.uel.main;



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

		logger.setLevel(Level.INFO);
		logger.addHandler(new OutputStreamHandler(System.out));
		logger.setUseParentHandlers(false);

		if (args.length == 1 && !args[0].equalsIgnoreCase("-h")) {

			Unifier.unify0(args[0]);

		} else if (args.length > 1 && args[0].startsWith("-")) {

			if (args.length == 3)

				Ontology.loadOntology(args[2]);

			String argument = args[0].substring(1);

			switch (Utilities.option(argument)) {

			case 1:

				Unifier.help();
				break;

			case 2:

				Unifier.unifyA(args[1]);
				break;

			case 3:

				Unifier.unifyX(args[1]);
				break;

			case 4:

				Unifier.unifyN(args[1]);
				break;

			case 5:

				Unifier.unifyInt(Utilities.MaxNbr, args[1]);
				break;

			case 6:

				Unifier.unifySimple(args[1]);
				break;

			default:

				logger.info("Wrong option.");
				Unifier.help();

				break;

			}

		} else if (args.length == 2 && !args[0].startsWith("-")) {

			Ontology.loadOntology(args[1]);

			Unifier.unify0(args[0]);

		} else {

			Unifier.help();

		}

	}

}
