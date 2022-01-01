package de.tudresden.inf.lat.uel.core.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.Assertions;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import de.tudresden.inf.lat.uel.core.processor.UelOptions;
import de.tudresden.inf.lat.uel.core.processor.UelOptions.UndefBehavior;
import de.tudresden.inf.lat.uel.core.processor.UelOptions.Verbosity;
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;

/**
 * Test class for 'AlternativeUelStarter'.
 * 
 * @author Stefan Borgwardt
 *
 */
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

	/**
	 * Construct a new test object with the given input. Input is constructed in
	 * the method 'data'.
	 * 
	 * @param mainOntology
	 *            the background ontology
	 * @param subsumptions
	 *            the positive goal ontology
	 * @param dissubsumptions
	 *            the negative goal ontology
	 * @param variables
	 *            the set of user variables
	 * @param numberOfUnifiers
	 *            the expected number of unifiers
	 */
	public AlternativeUelStarterTest(OWLOntology mainOntology, OWLOntology subsumptions, OWLOntology dissubsumptions,
			Set<OWLClass> variables, Integer numberOfUnifiers) {
		this.mainOntology = mainOntology;
		this.subsumptions = subsumptions;
		this.dissubsumptions = dissubsumptions;
		this.variables = variables;
		this.numberOfUnifiers = numberOfUnifiers;
	}

	/**
	 * Construct all inputs for this test.
	 * 
	 * @return a Collection of parameter combinations, as required by JUnit
	 */
	public static Collection<Object[]> data() {
		Collection<Object[]> data = new ArrayList<>();

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

	/**
	 * Run a single test.
	 * 
	 * @throws OWLOntologyCreationException
	 *             indicates that the ontology for verifying the results could
	 *             not be created
	 */
	public void tryOntology() throws OWLOntologyCreationException {

		UelOptions options = new UelOptions();
		options.undefBehavior = UndefBehavior.CONSTANTS;
		options.unificationAlgorithmName = UnificationAlgorithmFactory.SAT_BASED_ALGORITHM;
		options.verbosity = Verbosity.SILENT;

		UnifierIterator iterator = (UnifierIterator) AlternativeUelStarter.solve(mainOntology, subsumptions,
				dissubsumptions, null, variables, options);

		Set<OWLAxiom> background = iterator.getUelModel().renderDefinitions();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		Integer actualNumberOfUnifiers = 0;
		while (iterator.hasNext()) {
			actualNumberOfUnifiers++;
			Set<OWLEquivalentClassesAxiom> unifier = iterator.next();
			OWLOntology extendedOntology = ProcessorTest.clearManagerAndCreateOntology(manager, background, unifier);
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
				Assertions.assertTrue(reasoner.isEntailed(pos));
			}
			for (OWLAxiom neg : dissubsumptions.getAxioms()) {
				// System.out.println(neg + ": " + reasoner.isEntailed(neg));
				Assertions.assertTrue(!reasoner.isEntailed(neg));
			}

			reasoner.dispose();
		}

		iterator.cleanup();

		Assertions.assertEquals(numberOfUnifiers, actualNumberOfUnifiers);
	}
}
