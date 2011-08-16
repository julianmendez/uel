package de.tudresden.inf.lat.uel.plugin.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tudresden.inf.lat.uel.core.log.OutputStreamHandler;
import de.tudresden.inf.lat.uel.core.sat.MiniSatSolver;
import de.tudresden.inf.lat.uel.core.type.Equation;
import de.tudresden.inf.lat.uel.core.type.FAtom;
import de.tudresden.inf.lat.uel.core.type.Goal;
import de.tudresden.inf.lat.uel.core.type.Ontology;
import de.tudresden.inf.lat.uel.plugin.processor.OntologyBuilder;

/**
 * This class starts a unifier from the command line.
 * 
 * @author Barbara Morawska
 */
public class UelConsole {

	private static final Logger logger = Logger.getLogger(Main.class.getName());
	private static final String unifSuffix = ".unif";

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
	 * @throws IOException
	 */

	public static void main(String[] args) throws IOException {
		(new UelConsole()).run(args);
	}

	private int maxNbr = 0;

	public UelConsole() {
	}

	private List<Equation> createEquations(Ontology ontology, Set<String> input)
			throws IOException {
		List<Equation> ret = new ArrayList<Equation>();
		for (String cls : input) {
			Equation equation = ontology.getDefinition(cls);
			if (equation == null) {
				equation = ontology.getPrimitiveDefinition(cls);
			}
			if (equation != null) {
				ret.add(equation);
			}
		}
		return ret;
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

	private void initialize(Goal goal, Unifier unifier, String filename)
			throws IOException {
		// FIXME this method uses an empty set of variables

		Ontology ontology = loadOntology(new File(filename));
		Set<String> input = ontology.getDefinitionIds();
		List<Equation> equationList = createEquations(ontology, input);
		Iterator<String> inputIt = input.iterator();
		FAtom left = new FAtom(inputIt.next(), false, true, null);
		FAtom right = new FAtom(inputIt.next(), false, true, null);
		goal.initialize(equationList, left, right, new HashSet<String>());
	}

	private Ontology loadOntology(File file) throws IOException {
		OWLOntologyManager owlOntologyManager = OWLManager
				.createOWLOntologyManager();
		OWLOntology owlOntology = null;
		try {
			owlOntology = owlOntologyManager
					.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException e) {
			throw new IOException(e);
		}
		Ontology ret = new Ontology();
		OntologyBuilder builder = new OntologyBuilder(ret);
		builder.loadOntology(owlOntology);
		return ret;
	}

	/**
	 * This method analyzes the option string given to the Main class as one of
	 * the parameters.
	 * 
	 * @param argument
	 * @param unifier
	 * @return the option number
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

			maxNbr = Integer.parseInt(argument);

			return 5;
		}

	}

	public void run(String[] args) throws IOException {
		Unifier unifier = new Unifier(new MiniSatSolver());
		Ontology ontology = new Ontology();
		Goal goal = new Goal(ontology);

		logger.setLevel(Level.INFO);
		logger.addHandler(new OutputStreamHandler(System.out));
		logger.setUseParentHandlers(false);

		if (args.length == 1 && !args[0].equalsIgnoreCase("-h")) {

			initialize(goal, unifier, args[0]);
			unifier.unify0(goal);

		} else if (args.length > 1 && args[0].startsWith("-")) {

			if (args.length == 3) {
				ontology = loadOntology(new File(args[2]));
			}

			String argument = args[0].substring(1);

			switch (option(argument, unifier)) {

			case 1:

				help();
				break;

			case 2:

				initialize(goal, unifier, args[1]);
				store(args[1], unifier.unifyA(goal));
				break;

			case 3:

				initialize(goal, unifier, args[1]);
				store(args[1], unifier.unifyX(goal));
				break;

			case 4:

				initialize(goal, unifier, args[1]);
				unifier.unifyN(goal);
				break;

			case 5:

				initialize(goal, unifier, args[1]);
				store(args[1], unifier.unifyInt(goal, maxNbr));
				break;

			case 6:

				initialize(goal, unifier, args[1]);
				store(args[1], unifier.unifySimple(goal));
				break;

			default:

				logger.info("Wrong option.");
				help();

				break;

			}

		} else if (args.length == 2 && !args[0].startsWith("-")) {
			ontology = loadOntology(new File(args[1]));

			initialize(goal, unifier, args[0]);
			unifier.unify0(goal);

		} else {

			help();

		}

	}

	private void store(String output, String str) throws IOException {
		FileWriter writer = new FileWriter(new File(output.concat(unifSuffix)));
		writer.write(str);
		writer.flush();
		writer.close();
	}

}
