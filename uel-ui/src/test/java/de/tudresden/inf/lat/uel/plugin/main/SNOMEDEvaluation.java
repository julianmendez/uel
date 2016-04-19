/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import de.tudresden.inf.lat.uel.core.main.AlternativeUelStarter;
import de.tudresden.inf.lat.uel.core.main.UnifierIterator;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDEvaluation {

	// private static final String WORK_DIR = "C:\\Users\\Stefan\\Work\\";
	private static final String WORK_DIR = "/Users/stefborg/Documents/";
	private static final String SNOMED_PATH = WORK_DIR + "Ontologies/snomed-english-rdf.owl";
	private static final String SNOMED_RESTR_PATH = WORK_DIR + "Ontologies/snomed-restrictions.owl";
	private static final String POS_PATH = WORK_DIR + "Projects/uel-snomed/uel-snomed-pos3.owl";
	private static final String NEG_PATH = WORK_DIR + "Projects/uel-snomed/uel-snomed-neg2.owl";

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
		// starter.markUndefAsVariables(false);
		// starter.markUndefAsAuxVariables(true);
		starter.setSnomedMode(true);

		OWLOntology pos = AlternativeUelStarter.loadOntology(POS_PATH, manager);
		OWLOntology neg = AlternativeUelStarter.loadOntology(NEG_PATH, manager);
		// OWLOntology neg = UelModel.EMPTY_ONTOLOGY;
		// String[] varNames = { "X" };
		String[] varNames = { "X", "Y", "Z", "W" };
		UnifierIterator iterator = (UnifierIterator) starter.modifyOntologyAndSolve(pos, neg,
				Arrays.asList(varNames).stream().map(SNOMEDEvaluation::var).collect(Collectors.toSet()),
				UnificationAlgorithmFactory.SAT_BASED_ALGORITHM);

		System.out.println("Press RETURN to start computing unifiers.");

		Set<OWLAxiom> background = iterator.getUelModel().renderDefinitions();
		UelModel model = iterator.getUelModel();

		try {
			Scanner in = new Scanner(System.in);
			int i = 0;
			boolean skip = false;

			while (skip || in.hasNextLine()) {
				if (!skip) {
					if (in.nextLine().equals("a")) {
						skip = true;
					}
				}
				i++;
				System.out.println();
				System.out.println("--- " + i);
				if (iterator.hasNext()) {

					// TODO compute unifiers modulo equivalence

					System.out.println(model.getStringRenderer(null).renderUnifier(model.getCurrentUnifier(), true));

					Set<OWLEquivalentClassesAxiom> unifier = iterator.next();
					OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
					OWLOntology extendedOntology = ontologyManager.createOntology();
					ontologyManager.addAxioms(extendedOntology, background);
					ontologyManager.addAxioms(extendedOntology, unifier);

					ontologyManager.saveOntology(extendedOntology, new FunctionalSyntaxDocumentFormat(), System.out);

					OWLReasoner reasoner = new JcelReasonerFactory().createNonBufferingReasoner(extendedOntology);
					reasoner.precomputeInferences();

					for (OWLAxiom a : pos.getAxioms(AxiomType.SUBCLASS_OF)) {
						System.out.println(a + " (pos): " + reasoner.isEntailed(a));
						// Assert.assertTrue(reasoner.isEntailed(a));
					}
					for (OWLAxiom a : neg.getAxioms(AxiomType.SUBCLASS_OF)) {
						System.out.println(a + " (neg): " + reasoner.isEntailed(a));
						// Assert.assertTrue(!reasoner.isEntailed(a));
					}

					reasoner.dispose();

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
