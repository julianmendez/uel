/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tudresden.inf.lat.uel.core.main.AlternativeUelStarter;
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDEvaluation {

	private static final String WORK_DIR = "C:\\Users\\Stefan\\Work\\";
	// private static final String WORK_DIR = "/Users/stefborg/Documents/";
	private static final String SNOMED_PATH = WORK_DIR + "Ontologies/snomed-english-rdf.owl";
	private static final String SNOMED_RESTR_PATH = WORK_DIR + "Ontologies/snomed-restrictions.owl";
	private static final String POS_PATH = WORK_DIR + "Projects/uel-snomed-pos2.owl";
	private static final String NEG_PATH = WORK_DIR + "Projects/uel-snomed-neg2.owl";

	private static OWLClass var(String name) {
		return OWLManager.getOWLDataFactory().getOWLClass(IRI.create("http://www.ihtsdo.org/" + name));
	}

	public static void main(String[] args) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology snomed = AlternativeUelStarter.loadOntology(SNOMED_PATH, manager);
		OWLOntology snomedRestrictions = AlternativeUelStarter.loadOntology(SNOMED_RESTR_PATH, manager);
		AlternativeUelStarter starter = new AlternativeUelStarter(manager,
				new HashSet<OWLOntology>(Arrays.asList(snomed, snomedRestrictions)));
		starter.setVerbose(true);
		starter.markUndefAsAuxVariables(true);
		starter.setSnomedMode(true);

		OWLOntology pos = AlternativeUelStarter.loadOntology(POS_PATH, manager);
		OWLOntology neg = AlternativeUelStarter.loadOntology(NEG_PATH, manager);
		// OWLOntology neg = UelModel.EMPTY_ONTOLOGY;
		// String[] varNames = { "X" };
		String[] varNames = { "X", "Y", "Z", "W" };
		Iterator<Set<OWLEquivalentClassesAxiom>> iterator = starter.modifyOntologyAndSolve(pos, neg,
				Arrays.asList(varNames).stream().map(SNOMEDEvaluation::var).collect(Collectors.toSet()),
				UnificationAlgorithmFactory.SAT_BASED_ALGORITHM);

		System.out.println("Press RETURN to start computing unifiers.");

		try {
			Scanner in = new Scanner(System.in);
			int i = 0;
			while (in.hasNextLine()) {
				in.nextLine();
				i++;
				System.out.println();
				System.out.println("--- " + i);
				if (iterator.hasNext()) {

					Set<OWLEquivalentClassesAxiom> unifier = iterator.next();
					// OWLOntologyManager ontologyManager =
					// OWLManager.createOWLOntologyManager();
					// OWLOntology ontology = ontologyManager.createOntology();
					// ontologyManager.addAxioms(ontology, unifier);
					// OWLManager.createOWLOntologyManager().saveOntology(ontology,
					// new FunctionalSyntaxDocumentFormat(),
					// System.out);

					System.out.println();

					System.out.flush();
				} else {
					System.out.println("No more unifiers.");
					break;
				}
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
