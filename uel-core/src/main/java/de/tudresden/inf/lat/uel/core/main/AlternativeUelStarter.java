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
import java.util.Map.Entry;
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
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.UnificationAlgorithm;

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
		String owlThingAliasName = "";
		boolean printInfo = false;
		boolean snomedMode = false;
		int algorithmIdx = 0;
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
					owlThingAliasName = args[argIdx];
					break;
				case 'a':
					argIdx++;
					try {
						algorithmIdx = Integer.parseInt(args[argIdx]) - 1;
					} catch (NumberFormatException e) {
						System.err.println("Invalid algorithm index.");
						return;
					}
					break;
				case 'h':
					printSyntax();
					return;
				case 'i':
					printInfo = true;
					break;
				case 's':
					snomedMode = true;
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
		AlternativeUelStarter starter = new AlternativeUelStarter(loadOntology(mainFilename, manager));
		starter.setVerbose(printInfo);
		starter.setSnomedMode(snomedMode);
		if (!owlThingAliasName.isEmpty()) {
			starter.setOwlThingAlias(OWLManager.getOWLDataFactory().getOWLClass(IRI.create(owlThingAliasName)));
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
		List<String> algorithmNames = UnificationAlgorithmFactory.getAlgorithmNames();
		if ((algorithmIdx < 0) || (algorithmIdx >= algorithmNames.size())) {
			System.err.println("Invalid algorithm index.");
			return;
		}
		String algorithmName = algorithmNames.get(algorithmIdx);

		// TODO add options
		Iterator<Set<OWLEquivalentClassesAxiom>> result = starter.modifyOntologyAndSolve(posOntology, negOntology, null,
				variables, algorithmName, true);
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

		if (printInfo) {
			System.out.println("Stats:");
			for (Entry<String, String> entry : starter.getStats()) {
				System.out.println(entry.getKey() + ":");
				System.out.println(entry.getValue());
			}
		}
	}

	private static void printSyntax() {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(AlternativeUelStarter.class.getResourceAsStream("/help")));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			System.out.flush();
			reader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private Set<OWLOntology> ontologies;
	private UnificationAlgorithm uelProcessor;
	private boolean verbose = false;
	private boolean snomedMode = false;
	private OWLClass owlThingAlias = null;
	private boolean markUndefAsVariables = true;
	private boolean markUndefAsAuxVariables = false;

	/**
	 * Construct a new starter for UEL.
	 * 
	 * @param ontology
	 *            the background ontology
	 */
	public AlternativeUelStarter(OWLOntology ontology) {
		if (ontology == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		this.ontologies = new HashSet<OWLOntology>();
		this.ontologies.add(ontology);
	}

	/**
	 * Construct a new starter for UEL
	 * 
	 * @param ontologies
	 *            the set of background ontologies
	 */
	public AlternativeUelStarter(Set<OWLOntology> ontologies) {
		if (ontologies == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		this.ontologies = ontologies;
	}

	/**
	 * Retrieve additional information about the unification process.
	 * 
	 * @return a list of string entries, labeled by strings
	 */
	public List<Entry<String, String>> getStats() {
		return uelProcessor.getInfo();
	}

	/**
	 * Mark all UNDEF classes as auxiliary variables (they are not shown in the
	 * unifiers).
	 * 
	 * @param markUndefAsAuxVariables
	 *            indicates whether UNDEF classes should be marked as auxiliary
	 *            variables, defaults to 'false'
	 */
	public void markUndefAsAuxVariables(boolean markUndefAsAuxVariables) {
		this.markUndefAsAuxVariables = markUndefAsAuxVariables;
		this.markUndefAsVariables = false;
	}

	/**
	 * Mark all UNDEF classes as user variables (these are shown in the
	 * unifiers).
	 * 
	 * @param markUndefAsVariables
	 *            indicates whether UNDEF classes should be marked as user
	 *            variables, defaults to 'true'
	 */
	public void markUndefAsVariables(boolean markUndefAsVariables) {
		this.markUndefAsVariables = markUndefAsVariables;
		this.markUndefAsAuxVariables = false;
	}

	/**
	 * Start the unification process.
	 * 
	 * @param positiveProblem
	 *            the positive part of the unification problem
	 * @param negativeProblem
	 *            the negative part of the unification problem
	 * @param constraints
	 *            additional constraints to be loaded after all processing
	 *            finished (only relevant in SNOMED mode)
	 * @param variables
	 *            the set of user variables
	 * @param algorithmName
	 *            the name of the unification algorithm to be used (see
	 *            UnificationAlgorithmFactory)
	 * @param expandPrimitiveDefinitions
	 * @return an iterator yielding all produced unifiers (as sets of
	 *         OWLEquivalentClassesAxioms describing the definitions of the user
	 *         variables)
	 */
	public Iterator<Set<OWLEquivalentClassesAxiom>> modifyOntologyAndSolve(OWLOntology positiveProblem,
			OWLOntology negativeProblem, OWLOntology constraints, Set<OWLClass> variables, String algorithmName,
			boolean expandPrimitiveDefinitions) {

		OWLOntologyManager manager;
		if (ontologies.size() > 0) {
			manager = ontologies.iterator().next().getOWLOntologyManager();
		} else if (positiveProblem != null) {
			manager = positiveProblem.getOWLOntologyManager();
		} else if (negativeProblem != null) {
			manager = negativeProblem.getOWLOntologyManager();
		} else if (constraints != null) {
			manager = constraints.getOWLOntologyManager();
		} else {
			manager = OWLManager.createOWLOntologyManager();
		}

		UelModel uelModel = new UelModel(new BasicOntologyProvider(manager));
		uelModel.setupGoal(ontologies, positiveProblem, negativeProblem, constraints, owlThingAlias, snomedMode, true,
				expandPrimitiveDefinitions);

		return modifyOntologyAndSolve(uelModel, variables, algorithmName);
	}

	private Iterator<Set<OWLEquivalentClassesAxiom>> modifyOntologyAndSolve(UelModel uelModel, Set<OWLClass> variables,
			String algorithmName) {

		uelModel.makeClassesVariables(variables.stream(), false, true);

		if (markUndefAsVariables) {
			uelModel.makeAllUndefClassesVariables(true);
		}

		if (markUndefAsAuxVariables) {
			uelModel.makeAllUndefClassesVariables(false);
		}

		if (verbose) {
			// output unification problem
			Goal goal = uelModel.getGoal();
			AtomManager atomManager = goal.getAtomManager();
			System.out.println("Final number of atoms: " + atomManager.size());
			System.out.println("Final number of constants: " + atomManager.getConstants().size());
			System.out.println("Final number of variables: " + atomManager.getVariables().size());
			System.out.println("Final number of user variables: " + atomManager.getUserVariables().size());
			System.out.println("Final number of equations: " + goal.getEquations().size());
			System.out.println("Final number of disequations: " + goal.getDisequations().size());
			System.out.println("Final number of subsumptions: " + goal.getSubsumptions().size());
			System.out.println("Final number of dissubsumptions: " + goal.getDissubsumptions().size());
			System.out.println("(Dis-)Unification problem:");
			System.out.println(uelModel.printGoal());
		}

		uelModel.initializeUnificationAlgorithm(algorithmName);
		return new UnifierIterator(uelModel);
	}

	/**
	 * Set an alias for 'owl:Thing', e.g., 'SNOMED CT Concept'. The background
	 * ontology is not expanded above this class.
	 * 
	 * @param owlThingAlias
	 *            the OWL class serving as owl:Thing
	 */
	public void setOwlThingAlias(OWLClass owlThingAlias) {
		this.owlThingAlias = owlThingAlias;
	}

	/**
	 * EXPERIMENTAL. Enable 'SNOMED mode', which extracts additional information
	 * about the type hierarchy from the background ontology and makes some
	 * other modifications to the unfication process.
	 * 
	 * @param snomedMode
	 *            indicates whether 'SNOMED mode' should be activated
	 */
	public void setSnomedMode(boolean snomedMode) {
		this.snomedMode = snomedMode;
		if (snomedMode) {
			this.markUndefAsVariables = false;
			this.markUndefAsAuxVariables = false;
		}
	}

	/**
	 * Switch debug output.
	 * 
	 * @param verbose
	 *            indicates whether additional information about the unification
	 *            process should be shown.
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
