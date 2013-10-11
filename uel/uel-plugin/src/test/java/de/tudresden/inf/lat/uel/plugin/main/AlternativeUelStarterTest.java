package de.tudresden.inf.lat.uel.plugin.main;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasoner;
import de.tudresden.inf.lat.uel.plugin.processor.PluginGoal;
import de.tudresden.inf.lat.uel.plugin.type.AtomManager;
import de.tudresden.inf.lat.uel.plugin.type.OWLUelClassDefinition;
import de.tudresden.inf.lat.uel.type.api.Equation;

public class AlternativeUelStarterTest extends TestCase {

	private static final String apath = "src/test/resources/";
	private static final String conceptX = "x#X";
	private static final String conceptY = "x#Y";
	private static final String ontologyName = apath + "testOntology-alt-";
	private static final String subsName = apath + "testSubsumptions-alt-";
	private static final String dissubsName = apath
			+ "testDissubsumptions-alt-";
	private static final String fileExt = ".krss";

	private OWLOntology parseOntology(OWLOntologyManager ontologyManager,
			InputStream input) throws OWLOntologyCreationException {

		return ontologyManager.loadOntologyFromOntologyDocument(input);
	}

	private Set<OWLSubClassOfAxiom> parseGoal(
			OWLOntologyManager ontologyManager, InputStream input)
			throws OWLOntologyCreationException {

		OWLOntology ontology = ontologyManager
				.loadOntologyFromOntologyDocument(input);
		return ontology.getAxioms(AxiomType.SUBCLASS_OF);
	}

	private OWLReasoner createReasoner(OWLOntology ontology)
			throws OWLOntologyCreationException {
		JcelReasoner reasoner = new JcelReasoner(ontology, false);
		reasoner.precomputeInferences();
		return reasoner;
	}

	private <T> Set<T> set(T ... elements) {
		Set<T> ret = new HashSet<T>();
		for (T e : elements) {
			ret.add(e);
		}
		return ret;
	}

	public void test01() throws OWLOntologyCreationException, IOException {
		tryOntology("01", set(conceptX, conceptY), 13);
	}

	public void test02() throws OWLOntologyCreationException, IOException {
		tryOntology("02", set(conceptX), 0);
	}
	
	public void test03() throws OWLOntologyCreationException, IOException {
		tryOntology("03", set(conceptX), 1);
	}

	private String toString(OWLOntology ontology) {
		StringBuffer sbuf = new StringBuffer();

		for (OWLAxiom axiom : ontology.getAxioms()) {
			sbuf.append(axiom.toString());
			sbuf.append("\n");
		}
		return sbuf.toString();
	}

	private void tryOntology(String testId, Set<String> varNames,
			Integer expectedNumberOfUnifiers)
			throws OWLOntologyCreationException, IOException {

//		Map<String, OWLClass> idClassMap = new HashMap<String, OWLClass>();

		OWLOntologyManager ontologyManager = OWLManager
				.createOWLOntologyManager();
		OWLOntology owlOntology = parseOntology(ontologyManager,
				new FileInputStream(ontologyName + testId + fileExt));
		Set<OWLSubClassOfAxiom> goalSubsumptions = parseGoal(ontologyManager,
				new FileInputStream(subsName + testId + fileExt));
		Set<OWLSubClassOfAxiom> goalDissubsumptions = parseGoal(
				ontologyManager, new FileInputStream(dissubsName + testId
						+ fileExt));

		Set<OWLClass> variables = new HashSet<OWLClass>();
		for (String varName : varNames) {
			IRI varIRI = IRI.create(varName);
			OWLClass varClass = ontologyManager.getOWLDataFactory()
					.getOWLClass(varIRI);
			variables.add(varClass);
		}

		AlternativeUelStarter starter = new AlternativeUelStarter(owlOntology);

		Iterator<Set<OWLUelClassDefinition>> iterator = starter
				.modifyOntologyAndSolve(goalSubsumptions, goalDissubsumptions,
						variables);

		AtomManager atomManager = ((UnifierIterator) iterator).getAtomManager();
		Set<Equation> definitions = ((UnifierIterator) iterator).getProcessor()
				.getInput().getDefinitions();
		String krssDefinitions = PluginGoal.toString(atomManager, definitions);

		int actualNumberOfUnifiers = 0;
		while (iterator.hasNext()) {
			Set<OWLUelClassDefinition> unifier = iterator.next();
			actualNumberOfUnifiers++;

//			System.out.println();
//			System.out.println("--- " + actualNumberOfUnifiers);
			OWLOntology auxOntology = ontologyManager
					.loadOntologyFromOntologyDocument(new ByteArrayInputStream(
							krssDefinitions.getBytes()));
			for (OWLUelClassDefinition def : unifier) {
				ontologyManager.addAxiom(auxOntology,
						def.asOWLEquivalentClassesAxiom());
			}
//			try {
//				ontologyManager.saveOntology(auxOntology,
//						new KRSS2OntologyFormat(), System.out);
//			} catch (OWLOntologyStorageException e) {
//				e.printStackTrace();
//			}

			OWLReasoner reasoner = createReasoner(auxOntology);
			
			for (OWLSubClassOfAxiom sub : goalSubsumptions) {
				assertTrue(reasoner.isEntailed(sub));
			}
			for (OWLSubClassOfAxiom dissub : goalDissubsumptions) {
				assertTrue(!reasoner.isEntailed(dissub));
			}

		}

		assertEquals(expectedNumberOfUnifiers, (Integer) actualNumberOfUnifiers);
	}

}
