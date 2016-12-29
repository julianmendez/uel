package de.tudresden.inf.lat.uel.core.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tudresden.inf.lat.uel.core.processor.BasicOntologyProvider;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UelOptions;
import de.tudresden.inf.lat.uel.core.processor.UelOptions.Verbosity;
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;

/**
 * This class provides an acces point to UEL without the user interface.
 * 
 * @author Stefan Borgwardt
 */
public class AlternativeUelStarter {

	/**
	 * Convenience method for loading an OWL ontology from a file.
	 * 
	 * @param filename
	 *            the path to the ontology file
	 * @param manager
	 *            a pre-existing OWL ontology manager
	 * @return the created OWL ontology, or 'null' if the loading failed
	 */
	public static OWLOntology loadOntology(String filename, OWLOntologyManager manager) {
		try {
			if (filename.isEmpty()) {
				return manager.createOntology();
			}

			InputStream input = new FileInputStream(new File(filename));
			return manager.loadOntologyFromOntologyDocument(input);
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file '" + filename + "'.");
			return null;
		} catch (OWLOntologyCreationException e) {
			System.err.println("Could not create ontology from file '" + filename + "'.");
			System.err.println(e.getMessage());
			return null;
		}
	}

	/**
	 * Convenience method for loading a list of OWL class IRIs from a text file.
	 * 
	 * @param filename
	 *            the path to the file
	 * @return the set of OWL classes, or 'null' if the loading failed
	 */
	static Set<OWLClass> loadVariables(String filename) {
		try {
			if (filename.isEmpty()) {
				return Collections.emptySet();
			}

			OWLDataFactory factory = OWLManager.getOWLDataFactory();
			Set<OWLClass> variables = new HashSet<OWLClass>();

			for (String line : Files.readAllLines(Paths.get(filename))) {
				if (!line.isEmpty()) {
					variables.add(factory.getOWLClass(IRI.create(line)));
				}
			}

			return variables;
		} catch (IOException e) {
			System.err.println("Error while reading file '" + filename + "'.");
			System.err.println(e.getMessage());
			return null;
		}
	}

	/**
	 * Command line interface for UEL. For more information, see the '-h'
	 * option.
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		int argIdx = 0;
		String mainFilename = "";
		String posFilename = "";
		String negFilename = "";
		String varFilename = "";
		UelOptions options = new UelOptions();

		while (argIdx < args.length) {
			if ((args[argIdx].length() == 2) && (args[argIdx].charAt(0) == '-')) {
				switch (args[argIdx].charAt(1)) {
				case 'p':
					argIdx++;
					posFilename = args[argIdx];
					break;
				case 'n':
					argIdx++;
					negFilename = args[argIdx];
					break;
				case 'v':
					argIdx++;
					varFilename = args[argIdx];
					break;
				case 't':
					argIdx++;
					if (!args[argIdx].isEmpty()) {
						options.owlThingAlias = OWLManager.getOWLDataFactory().getOWLClass(IRI.create(args[argIdx]));
					}
					break;
				case 'a':
					argIdx++;
					try {
						int algorithmIdx = Integer.parseInt(args[argIdx]) - 1;
						List<String> algorithmNames = UnificationAlgorithmFactory.getAlgorithmNames();
						if ((algorithmIdx < 0) || (algorithmIdx >= algorithmNames.size())) {
							System.err.println("Invalid algorithm index.");
							return;
						}
						options.unificationAlgorithmName = algorithmNames.get(algorithmIdx);
					} catch (NumberFormatException e) {
						System.err.println("Invalid algorithm index.");
						return;
					}
					break;
				case 'h':
					printSyntax();
					return;
				case 'i':
					options.verbosity = Verbosity.NORMAL;
					break;
				case 's':
					options.snomedMode = true;
					break;
				default:
					mainFilename = args[argIdx];
					break;
				}
			} else {
				mainFilename = args[argIdx];
			}
			argIdx++;
		}

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology bgOntology = loadOntology(mainFilename, manager);
		if (bgOntology == null) {
			return;
		}
		OWLOntology posOntology = loadOntology(posFilename, manager);
		if (posOntology == null) {
			return;
		}
		OWLOntology negOntology = loadOntology(negFilename, manager);
		if (negOntology == null) {
			return;
		}
		Set<OWLClass> variables = loadVariables(varFilename);
		if (variables == null) {
			return;
		}

		Iterator<Set<OWLEquivalentClassesAxiom>> result = AlternativeUelStarter.solve(bgOntology, posOntology,
				negOntology, null, variables, options);
		int unifierIdx = 1;
		while (result.hasNext()) {
			System.out.println("Unifier " + unifierIdx + ":");
			Set<OWLEquivalentClassesAxiom> unifier = result.next();
			for (OWLEquivalentClassesAxiom def : unifier) {
				System.out.println(def.toString());
			}
			System.out.println();
			unifierIdx++;
		}
		if (unifierIdx == 1) {
			System.out.println("Not unifiable.");
		}

	}

	private static void printSyntax() {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(AlternativeUelStarter.class.getResourceAsStream("/help")));
			reader.lines().forEach(line -> System.out.println(line));
			System.out.flush();
			reader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Start the unification process.
	 * 
	 * @param bgOntology
	 *            the background ontology
	 * @param positiveProblem
	 *            the positive part of the unification problem
	 * @param negativeProblem
	 *            the negative part of the unification problem
	 * @param constraints
	 *            additional constraints to be loaded after all processing
	 *            finished (only relevant in SNOMED mode)
	 * @param variables
	 *            the set of user variables
	 * @param options
	 *            describes the execution options of UEL
	 * @return an iterator yielding all produced unifiers (as sets of
	 *         OWLEquivalentClassesAxioms describing the definitions of the user
	 *         variables)
	 */
	public static Iterator<Set<OWLEquivalentClassesAxiom>> solve(OWLOntology bgOntology, OWLOntology positiveProblem,
			OWLOntology negativeProblem, OWLOntology constraints, Set<OWLClass> variables, UelOptions options) {
		return solve(Collections.singleton(bgOntology), positiveProblem, negativeProblem, constraints, variables,
				options);
	}

	/**
	 * Start the unification process.
	 * 
	 * @param bgOntologies
	 *            the background ontologies
	 * @param positiveProblem
	 *            the positive part of the unification problem
	 * @param negativeProblem
	 *            the negative part of the unification problem
	 * @param constraints
	 *            additional constraints to be loaded after all processing
	 *            finished (only relevant in SNOMED mode)
	 * @param variables
	 *            the set of user variables
	 * @param options
	 *            describes the execution options of UEL
	 * @return an iterator yielding all produced unifiers (as sets of
	 *         OWLEquivalentClassesAxioms describing the definitions of the user
	 *         variables)
	 */
	public static Iterator<Set<OWLEquivalentClassesAxiom>> solve(Set<OWLOntology> bgOntologies,
			OWLOntology positiveProblem, OWLOntology negativeProblem, OWLOntology constraints, Set<OWLClass> variables,
			UelOptions options) {

		OWLOntologyManager manager;
		if (bgOntologies.size() > 0) {
			manager = bgOntologies.iterator().next().getOWLOntologyManager();
		} else if (positiveProblem != null) {
			manager = positiveProblem.getOWLOntologyManager();
		} else if (negativeProblem != null) {
			manager = negativeProblem.getOWLOntologyManager();
		} else if (constraints != null) {
			manager = constraints.getOWLOntologyManager();
		} else {
			manager = OWLManager.createOWLOntologyManager();
		}

		UelModel uelModel = new UelModel(new BasicOntologyProvider(manager), options);
		uelModel.setupGoal(bgOntologies, positiveProblem, negativeProblem, constraints, variables, true);

		uelModel.initializeUnificationAlgorithm();
		return new UnifierIterator(uelModel);
	}

}
