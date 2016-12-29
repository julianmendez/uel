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
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.processor.BasicOntologyProvider;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.UnificationAlgorithm;

public class AlternativeUelStarter {

	private Set<OWLOntology> ontologies;
	private OWLOntologyManager ontologyManager;
	private UnificationAlgorithm uelProcessor;
	private boolean verbose = false;
	private OWLClass owlThingAlias = null;
	private boolean markUndefAsVariables = true;

	public AlternativeUelStarter(OWLOntology ontology) {
		if (ontology == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		this.ontologyManager = ontology.getOWLOntologyManager();
		this.ontologies = new HashSet<>();
		this.ontologies.add(ontology);
	}

	public AlternativeUelStarter(OWLOntologyManager ontologyManager, Set<OWLOntology> ontologies) {
		if (ontologies == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		this.ontologyManager = ontologyManager;
		this.ontologies = ontologies;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setOwlThingAlias(OWLClass owlThingAlias) {
		this.owlThingAlias = owlThingAlias;
	}

	public void markUndefAsVariables(boolean markUndefAsVariables) {
		this.markUndefAsVariables = markUndefAsVariables;
	}

	public static void main(String[] args) {
		int argIdx = 0;
		String mainFilename = "";
		String posFilename = "";
		String negFilename = "";
		String varFilename = "";
		String owlThingAliasName = "";
		boolean printInfo = false;
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

		Iterator<Set<OWLEquivalentClassesAxiom>> result = starter.modifyOntologyAndSolve(posOntology, negOntology,
				variables, algorithmName);
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
			reader.lines().forEach(line -> System.out.println(line));
			System.out.flush();
			reader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	static Set<OWLClass> loadVariables(String filename) {
		try {
			if (filename.isEmpty()) {
				return Collections.emptySet();
			}

			OWLDataFactory factory = OWLManager.getOWLDataFactory();
			Set<OWLClass> variables = new HashSet<>();

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

	private static OWLOntology loadOntology(String filename, OWLOntologyManager manager) {
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

	public Iterator<Set<OWLEquivalentClassesAxiom>> modifyOntologyAndSolve(OWLOntology positiveProblem,
			OWLOntology negativeProblem, Set<OWLClass> variables, String algorithmName) {

		UelModel uelModel = new UelModel(new BasicOntologyProvider(ontologyManager));
		uelModel.setupGoal(ontologies, positiveProblem, negativeProblem, owlThingAlias, true);

		return modifyOntologyAndSolve(uelModel, variables, algorithmName);
	}

	public Iterator<Set<OWLEquivalentClassesAxiom>> modifyOntologyAndSolve(Set<OWLSubClassOfAxiom> subsumptions,
			Set<OWLEquivalentClassesAxiom> equations, Set<OWLSubClassOfAxiom> dissubsumptions,
			Set<OWLEquivalentClassesAxiom> disequations, Set<OWLClass> variables, String algorithmName) {

		UelModel uelModel = new UelModel(new BasicOntologyProvider(ontologyManager));
		uelModel.setupGoal(ontologies, subsumptions, equations, dissubsumptions, disequations, owlThingAlias, true);

		return modifyOntologyAndSolve(uelModel, variables, algorithmName);
	}

	private Iterator<Set<OWLEquivalentClassesAxiom>> modifyOntologyAndSolve(UelModel uelModel, Set<OWLClass> variables,
			String algorithmName) {

		uelModel.makeClassesUserVariables(variables);

		if (markUndefAsVariables) {
			uelModel.makeAllUndefClassesUserVariables();
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

	public List<Entry<String, String>> getStats() {
		return uelProcessor.getInfo();
	}

}
