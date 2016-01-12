package de.tudresden.inf.lat.uel.core.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;

@RunWith(value = Parameterized.class)
public class AlternativeUelStarterTest {

	private static final String apath = "src/test/resources/";
	private static final String prefix = "alt-test";
	private static final String ontologyFilename = "-ontology.krss";
	private static final String subsFilename = "-subsumptions.krss";
	private static final String dissubsFilename = "-dissubsumptions.krss";
	private static final String varFilename = "-variables.txt";
	private static final String testFilename = ".test";
	private static final int maxTest = 4;

	private OWLOntology mainOntology;
	private OWLOntology subsumptions;
	private OWLOntology dissubsumptions;
	private Set<OWLClass> variables;
	private Integer numberOfUnifiers;

	public AlternativeUelStarterTest(OWLOntology mainOntology, OWLOntology subsumptions, OWLOntology dissubsumptions,
			Set<OWLClass> variables, Integer numberOfUnifiers) {
		this.mainOntology = mainOntology;
		this.subsumptions = subsumptions;
		this.dissubsumptions = dissubsumptions;
		this.variables = variables;
		this.numberOfUnifiers = numberOfUnifiers;
	}

	@Parameters(name = "{index}")
	public static Collection<Object[]> data() {
		Collection<Object[]> data = new ArrayList<Object[]>();

		for (int i = 1; i <= maxTest; i++) {
			try {
				String baseFilename = apath + prefix + String.format("%02d", i);

				OWLOntology mainOntology = ProcessorTest.loadKRSSOntology(baseFilename + ontologyFilename);
				OWLOntology subsumptions = ProcessorTest.loadKRSSOntology(baseFilename + subsFilename);
				OWLOntology dissubsumptions = ProcessorTest.loadKRSSOntology(baseFilename + dissubsFilename);
				Set<OWLClass> variables = AlternativeUelStarter.loadVariables(baseFilename + varFilename);

				BufferedReader testFile = new BufferedReader(new FileReader(baseFilename + testFilename));
				Integer numberOfUnifiers = Integer.parseInt(testFile.readLine());
				testFile.close();

				data.add(new Object[] { mainOntology, subsumptions, dissubsumptions, variables, numberOfUnifiers });
			} catch (OWLOntologyCreationException | IOException ex) {
				throw new RuntimeException(ex);
			}
		}

		return data;
	}

	@Test
	public void tryOntology() throws OWLOntologyCreationException, IOException {

		AlternativeUelStarter starter = new AlternativeUelStarter(mainOntology);
		// starter.setVerbose(true);

		UnifierIterator iterator = (UnifierIterator) starter.modifyOntologyAndSolve(subsumptions, dissubsumptions,
				variables, UnificationAlgorithmFactory.SAT_BASED_ALGORITHM);

		Set<OWLAxiom> background = iterator.getUelModel().renderDefinitions();

		Integer actualNumberOfUnifiers = 0;
		while (iterator.hasNext()) {
			actualNumberOfUnifiers++;
			Set<OWLEquivalentClassesAxiom> unifier = iterator.next();
			OWLOntology extendedOntology = ProcessorTest.createOntology(background, unifier);
			// try {
			// System.out.println();
			// System.out.println("---" + actualNumberOfUnifiers);
			// OWLManager.createOWLOntologyManager().saveOntology(extendedOntology,
			// new FunctionalSyntaxDocumentFormat(), System.out);
			// System.out.println();
			// } catch (OWLOntologyStorageException e) {
			// e.printStackTrace();
			// }

			OWLReasoner reasoner = new JcelReasonerFactory().createNonBufferingReasoner(extendedOntology);
			reasoner.precomputeInferences();

			for (OWLAxiom pos : subsumptions.getAxioms()) {
				// System.out.println(pos + ": " + reasoner.isEntailed(pos));
				Assert.assertTrue(reasoner.isEntailed(pos));
			}
			for (OWLAxiom neg : dissubsumptions.getAxioms()) {
				// System.out.println(neg + ": " + reasoner.isEntailed(neg));
				Assert.assertTrue(!reasoner.isEntailed(neg));
			}

			reasoner.dispose();
		}

		Assert.assertEquals(numberOfUnifiers, actualNumberOfUnifiers);
	}
}
