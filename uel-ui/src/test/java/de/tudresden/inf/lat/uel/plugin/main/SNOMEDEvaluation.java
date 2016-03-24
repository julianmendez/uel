/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tudresden.inf.lat.uel.core.main.AlternativeUelStarter;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDEvaluation {

	private static final String SNOMED_PATH = "/Users/stefborg/Documents/Ontologies/snomed-english-rdf.owl";
	private static final String SNOMED_RESTR_PATH = "/Users/stefborg/Documents/Ontologies/snomed-restrictions.owl";
	private static final String POS_PATH = "/Users/stefborg/Documents/Projects/uel-snomed-pos3.owl";
	private static final String NEG_PATH = "/Users/stefborg/Documents/Projects/uel-snomed-neg2.owl";

	public static void main(String[] args) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology snomed = AlternativeUelStarter.loadOntology(SNOMED_PATH, manager);
		OWLOntology snomedRestrictions = AlternativeUelStarter.loadOntology(SNOMED_RESTR_PATH, manager);
		AlternativeUelStarter starter = new AlternativeUelStarter(manager,
				new HashSet(Arrays.asList(snomed, snomedRestrictions)));
		starter.setVerbose(true);
		starter.markUndefAsVariables(true);
		starter.setSnomedMode(true);

		OWLOntology pos = AlternativeUelStarter.loadOntology(POS_PATH, manager);
		// OWLOntology neg = AlternativeUelStarter.loadOntology(NEG_PATH,
		// manager);
		OWLOntology neg = UelModel.EMPTY_ONTOLOGY;
		Set<OWLClass> vars = new HashSet<OWLClass>();
		vars.add(OWLManager.getOWLDataFactory().getOWLClass(IRI.create("http://www.ihtsdo.org/X")));
		// vars.add(OWLManager.getOWLDataFactory().getOWLClass(IRI.create("http://www.ihtsdo.org/Y")));
		// vars.add(OWLManager.getOWLDataFactory().getOWLClass(IRI.create("http://www.ihtsdo.org/Z")));
		// vars.add(OWLManager.getOWLDataFactory().getOWLClass(IRI.create("http://www.ihtsdo.org/W")));
		Iterator<Set<OWLEquivalentClassesAxiom>> iterator = starter.modifyOntologyAndSolve(pos, neg, vars,
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
					OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
					OWLOntology ontology = ontologyManager.createOntology();
					ontologyManager.addAxioms(ontology, unifier);
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
