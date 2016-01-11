package de.tudresden.inf.lat.uel.core.main;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.KRSS2DocumentFormat;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasoner;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
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
				OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
				OWLDataFactory factory = manager.getOWLDataFactory();

				OWLOntology mainOntology = AlternativeUelStarter.loadOntology(baseFilename + ontologyFilename, manager);
				OWLOntology subsumptions = AlternativeUelStarter.loadOntology(baseFilename + subsFilename, manager);
				OWLOntology dissubsumptions = AlternativeUelStarter.loadOntology(baseFilename + dissubsFilename,
						manager);
				Set<OWLClass> variables = AlternativeUelStarter.loadVariables(baseFilename + varFilename, factory);

				BufferedReader testFile = new BufferedReader(new FileReader(baseFilename + testFilename));
				Integer numberOfUnifiers = Integer.parseInt(testFile.readLine());
				testFile.close();

				data.add(new Object[] { mainOntology, subsumptions, dissubsumptions, variables, numberOfUnifiers });
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

		return data;
	}

	private OWLReasoner createReasoner(OWLOntology ontology) throws OWLOntologyCreationException {
		JcelReasoner reasoner = new JcelReasoner(ontology, false);
		reasoner.precomputeInferences();
		return reasoner;
	}

	String toString(OWLOntology ontology) {
		StringBuffer sbuf = new StringBuffer();

		for (OWLAxiom axiom : ontology.getAxioms()) {
			sbuf.append(axiom.toString());
			sbuf.append("\n");
		}
		return sbuf.toString();
	}

	@Test
	public void tryOntology() throws OWLOntologyCreationException, IOException {

		AlternativeUelStarter starter = new AlternativeUelStarter(mainOntology);
		// starter.setVerbose(true);

		UnifierIterator iterator = (UnifierIterator) starter.modifyOntologyAndSolve(subsumptions, dissubsumptions,
				variables, UnificationAlgorithmFactory.SAT_BASED_ALGORITHM);

		UelModel uelModel = iterator.getUelModel();
		// TODO replace by OWLRenderer
		String krssDefinitions = uelModel.getStringRenderer(false, null)
				.renderDefinitions(uelModel.getGoal().getDefinitions());
		System.out.println(krssDefinitions);
		System.out.println();
		System.out.println();

		int actualNumberOfUnifiers = 0;
		while (iterator.hasNext()) {
			Set<OWLEquivalentClassesAxiom> unifier = iterator.next();
			actualNumberOfUnifiers++;
			System.out.println();
			System.out.println("--- " + actualNumberOfUnifiers);
			for (OWLEquivalentClassesAxiom def : unifier) {
				System.out.println(def.toString());
			}
			System.out.println();

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology auxOntology = manager
					.loadOntologyFromOntologyDocument(new ByteArrayInputStream(krssDefinitions.getBytes()));
			for (OWLEquivalentClassesAxiom def : unifier) {
				manager.addAxiom(auxOntology, def);
			}
			try {
				manager.saveOntology(auxOntology, new KRSS2DocumentFormat(), System.out);
			} catch (OWLOntologyStorageException e) {
				e.printStackTrace();
			}

			OWLReasoner reasoner = createReasoner(auxOntology);

			for (OWLAxiom pos : subsumptions.getAxioms()) {
				Assert.assertTrue(reasoner.isEntailed(pos));
			}
			for (OWLAxiom neg : dissubsumptions.getAxioms()) {
				Assert.assertTrue(!reasoner.isEntailed(neg));
			}

		}

		Assert.assertEquals(numberOfUnifiers, (Integer) actualNumberOfUnifiers);
	}
}
