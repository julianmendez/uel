package de.tudresden.inf.lat.uel.core.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.processor.PluginGoal;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UelProcessorFactory;
import de.tudresden.inf.lat.uel.core.type.AtomManager;
import de.tudresden.inf.lat.uel.core.type.OWLUelClassDefinition;
import de.tudresden.inf.lat.uel.core.type.UnifierTranslator;
import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.SmallEquation;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

public class AlternativeUelStarter {

	private Set<OWLOntology> ontologies;
	private OWLOntologyManager ontologyManager;
	private UelProcessor uelProcessor;
	private boolean verbose = false;
	private OWLClass owlThingAlias = null;
	private boolean markUndefAsVariables = true;

	public AlternativeUelStarter(OWLOntology ontology) {
		if (ontology == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		this.ontologyManager = ontology.getOWLOntologyManager();
		this.ontologies = new HashSet<OWLOntology>();
		this.ontologies.add(ontology);
	}

	public AlternativeUelStarter(OWLOntologyManager ontologyManager,
			Set<OWLOntology> ontologies) {
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
		String subsFilename = "";
		String dissubsFilename = "";
		String varFilename = "";
		String owlThingAliasName = "";
		boolean printInfo = false;
		int processorIdx = 0;
		while (argIdx < args.length) {
			if ((args[argIdx].length() == 2) && (args[argIdx].charAt(0) == '-')) {
				switch (args[argIdx].charAt(1)) {
				case 's':
					argIdx++;
					subsFilename = args[argIdx];
					break;
				case 'd':
					argIdx++;
					dissubsFilename = args[argIdx];
					break;
				case 'v':
					argIdx++;
					varFilename = args[argIdx];
					break;
				case 't':
					argIdx++;
					owlThingAliasName = args[argIdx];
					break;
				case 'p':
					argIdx++;
					try {
						processorIdx = Integer.parseInt(args[argIdx]) - 1;
					} catch (NumberFormatException e) {
						System.err.println("Invalid processor index.");
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
		OWLDataFactory factory = manager.getOWLDataFactory();
		AlternativeUelStarter starter = new AlternativeUelStarter(loadOntology(
				mainFilename, manager));
		starter.setVerbose(printInfo);
		if (!owlThingAliasName.isEmpty()) {
			starter.setOwlThingAlias(factory.getOWLClass(IRI
					.create(owlThingAliasName)));
		}

		OWLOntology subsumptions = loadOntology(subsFilename, manager);
		if (subsumptions == null) {
			return;
		}
		OWLOntology dissubsumptions = loadOntology(dissubsFilename, manager);
		if (dissubsumptions == null) {
			return;
		}
		Set<OWLClass> variables = loadVariables(varFilename, factory);
		if (variables == null) {
			return;
		}
		List<String> processorNames = UelProcessorFactory.getProcessorNames();
		if ((processorIdx < 0) || (processorIdx >= processorNames.size())) {
			System.err.println("Invalid processor index.");
			return;
		}
		String processorName = processorNames.get(processorIdx);

		Iterator<Set<OWLUelClassDefinition>> result = starter
				.modifyOntologyAndSolve(subsumptions, dissubsumptions,
						variables, processorName);
		int unifierIdx = 1;
		while (result.hasNext()) {
			System.out.println("Unifier " + unifierIdx + ":");
			Set<OWLUelClassDefinition> unifier = result.next();
			for (OWLUelClassDefinition def : unifier) {
				System.out
						.println(def.asOWLEquivalentClassesAxiom().toString());
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
		System.out
				.println("Usage: uel [-s subsumptions.owl] [-d dissubsumptions.owl] [-v variables.txt] [-t owl:Thing_alias] [-p processorIndex] [-h] [-i] [ontology.owl]");
	}

	public static Set<OWLClass> loadVariables(String filename,
			OWLDataFactory factory) {
		if (filename.isEmpty()) {
			return Collections.emptySet();
		}
		try {

			BufferedReader input = new BufferedReader(new FileReader(new File(
					filename)));
			Set<OWLClass> variables = new HashSet<OWLClass>();
			String line = "";
			while (line != null) {
				line = input.readLine();
				if ((line != null) && !line.isEmpty()) {
					variables.add(factory.getOWLClass(IRI.create(line)));
				}
			}
			input.close();
			return variables;
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file '" + filename + "'.");
			return null;
		} catch (IOException e) {
			System.err.println("Unknown I/O error while reading file '"
					+ filename + "'.");
			System.err.println(e.getMessage());
			return null;
		}
	}

	public static OWLOntology loadOntology(String filename,
			OWLOntologyManager manager) {
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
			System.err.println("Could not create ontology from file '"
					+ filename + "'.");
			System.err.println(e.getMessage());
			return null;
		}
	}

	public Iterator<Set<OWLUelClassDefinition>> modifyOntologyAndSolve(
			OWLOntology positiveProblem, OWLOntology negativeProblem,
			Set<OWLClass> variables, String processorName) {

		UelModel uelModel = new UelModel();
		uelModel.configure(ontologies, positiveProblem, negativeProblem,
				owlThingAlias);

		return modifyOntologyAndSolve(uelModel, variables, processorName);
	}

	public Iterator<Set<OWLUelClassDefinition>> modifyOntologyAndSolve(
			Set<OWLSubClassOfAxiom> subsumptions,
			Set<OWLEquivalentClassesAxiom> equations,
			Set<OWLSubClassOfAxiom> dissubsumptions,
			Set<OWLEquivalentClassesAxiom> disequations,
			Set<OWLClass> variables, String processorName) {

		UelModel uelModel = new UelModel();
		uelModel.configure(ontologies, subsumptions, equations,
				dissubsumptions, disequations, owlThingAlias);

		return modifyOntologyAndSolve(uelModel, variables, processorName);
	}

	private Iterator<Set<OWLUelClassDefinition>> modifyOntologyAndSolve(
			UelModel uelModel, Set<OWLClass> variables, String processorName) {

		AtomManager atomManager = uelModel.getAtomManager();
		PluginGoal goal = uelModel.getPluginGoal();

		// translate the variables to the IDs, and mark them as variables in the
		// PluginGoal
		for (OWLClass var : variables) {
			goal.makeUserVariable(uelModel.getAtomId(var));
		}

		if (markUndefAsVariables) {
			// mark all "_UNDEF" variables as user variables
			for (Atom at : atomManager.getAtoms()) {
				if (at.isConceptName()) {
					String name = atomManager.getConceptName(at
							.getConceptNameId());
					if (name.endsWith(AtomManager.UNDEF_SUFFIX)) {
						goal.makeUserVariable(atomManager.getAtoms().getIndex(
								at));
					}
				}
			}
		}

		goal.updateUelInput();
		UelInput input = goal.getUelInput();

		// output unification problem
		if (verbose) {
			System.out.println("Final number of atoms: "
					+ goal.getUelInput().getAtomManager().size());
			System.out.println("Final number of constants: "
					+ goal.getConstants().size());
			System.out.println("Final number of variables: "
					+ goal.getVariables().size());
			System.out.println("Final number of user variables: "
					+ goal.getUelInput().getUserVariables().size());
			System.out.println("Final number of equations: "
					+ goal.getUelInput().getEquations().size());
			System.out.println("Unification problem:");
			System.out.println(goal.toString());
		}

		uelProcessor = UelProcessorFactory
				.createProcessor(processorName, input);

		UnifierTranslator translator = new UnifierTranslator(
				ontologyManager.getOWLDataFactory(), atomManager,
				input.getUserVariables(), goal.getAuxiliaryVariables());
		return new UnifierIterator(uelProcessor, translator);
	}

	public List<Entry<String, String>> getStats() {
		return uelProcessor.getInfo();
	}

}
