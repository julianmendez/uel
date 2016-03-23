/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
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

	private static final String SNOMED_PATH = "/Users/stefborg/Documents/Ontologies/snomed-english-rdf.owl";
	private static final String SNOMED_RESTR_PATH = "/Users/stefborg/Documents/Ontologies/snomed-restrictions.owl";
	private static final String POS_PATH = "/Users/stefborg/Documents/Projects/uel-snomed-pos2.owl";
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
		OWLOntology neg = AlternativeUelStarter.loadOntology(NEG_PATH, manager);
		Set<OWLClass> vars = Collections
				.singleton(OWLManager.getOWLDataFactory().getOWLClass(IRI.create("http://www.ihtsdo.org/X")));
		Iterator<Set<OWLEquivalentClassesAxiom>> iterator = starter.modifyOntologyAndSolve(pos, neg, vars,
				UnificationAlgorithmFactory.SAT_BASED_ALGORITHM);

		try {
			Scanner in = new Scanner(System.in);
			int i = 0;
			while (in.hasNextLine()) {
				if (iterator.hasNext()) {
					Set<OWLEquivalentClassesAxiom> unifier = iterator.next();
					OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
					OWLOntology ontology = ontologyManager.createOntology();
					ontologyManager.addAxioms(ontology, unifier);
					System.out.println();
					System.out.println("--- " + i);
					OWLManager.createOWLOntologyManager().saveOntology(ontology, new FunctionalSyntaxDocumentFormat(),
							System.out);
					System.out.println();
					System.out.flush();
				} else {
					System.out.println("No more unifiers.");
					return;
				}
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
