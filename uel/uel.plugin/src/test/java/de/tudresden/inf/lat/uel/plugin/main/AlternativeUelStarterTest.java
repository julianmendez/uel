package de.tudresden.inf.lat.uel.plugin.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasoner;
import de.tudresden.inf.lat.uel.plugin.processor.UelProcessorFactory;
import de.tudresden.inf.lat.uel.plugin.type.OWLUelClassDefinition;

public class AlternativeUelStarterTest extends TestCase {

	private static final String apath = "src/test/resources/";
	private static final String conceptC = "C";
	private static final String conceptD = "D";
	private static final String ontology16 = apath + "testOntology-16.krss";

	private OWLOntology createOntology(InputStream input)
			throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = OWLManager
				.createOWLOntologyManager();
		ontologyManager.loadOntologyFromOntologyDocument(input);
		return ontologyManager.getOntologies().iterator().next();
	}

	private OWLReasoner createReasoner(OWLOntology ontology)
			throws OWLOntologyCreationException {
		JcelReasoner reasoner = new JcelReasoner(ontology, false);
		reasoner.precomputeInferences();
		return reasoner;
	}

	private Set<String> set(String a, String b) {
		Set<String> ret = new HashSet<String>();
		ret.add(a);
		ret.add(b);
		return ret;
	}

	public void test16SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology16, set("Head", "Injury"), 128,
				UelProcessorFactory.SAT_BASED_ALGORITHM);
	}

	private String toString(OWLOntology ontology) {
		StringBuffer sbuf = new StringBuffer();

		for (OWLAxiom axiom : ontology.getAxioms()) {
			sbuf.append(axiom.toString());
			sbuf.append("\n");
		}
		return sbuf.toString();
	}

	private void tryOntology(String ontologyName, Set<String> varNames,
			Integer expectedNumberOfUnifiers, String processorName)
			throws OWLOntologyCreationException, IOException {
		Map<String, OWLClass> idClassMap = new HashMap<String, OWLClass>();

		OWLOntology owlOntology = createOntology(new FileInputStream(
				ontologyName));

		AlternativeUelStarter starter = new AlternativeUelStarter(owlOntology);

		Set<OWLClass> clsSet = owlOntology.getClassesInSignature();
		for (OWLClass cls : clsSet) {
			idClassMap.put(cls.getIRI().getFragment(), cls);
		}

		OWLClass classC = idClassMap.get(conceptC);
		OWLClass classD = idClassMap.get(conceptD);

		Set<OWLSubClassOfAxiom> axioms = new HashSet<OWLSubClassOfAxiom>();
		OWLDataFactory factory = owlOntology.getOWLOntologyManager()
				.getOWLDataFactory();
		axioms.add(factory.getOWLSubClassOfAxiom(classC, classD));
		axioms.add(factory.getOWLSubClassOfAxiom(classD, classC));

		Set<OWLClass> userVariables = new HashSet<OWLClass>();
		for (String userVarName : varNames) {
			OWLClass userVar = idClassMap.get(userVarName);
			userVariables.add(userVar);
		}

		Iterator<Set<OWLUelClassDefinition>> iterator = starter
				.modifyOntologyAndSolve(axioms, userVariables);

		int actualNumberOfUnifiers = 0;
		while (iterator.hasNext()) {
			Set<OWLUelClassDefinition> unifier = iterator.next();
			actualNumberOfUnifiers++;

			Set<OWLAxiom> auxAxiomSet = new HashSet<OWLAxiom>();
			auxAxiomSet.addAll(owlOntology.getAxioms());
			for (OWLUelClassDefinition equation : unifier) {
				auxAxiomSet.add(equation.asOWLEquivalentClassesAxiom());
			}

			OWLOntology auxOntology = owlOntology.getOWLOntologyManager()
					.createOntology(auxAxiomSet);

			OWLReasoner reasoner = createReasoner(auxOntology);
			Node<OWLClass> node = reasoner.getEquivalentClasses(idClassMap
					.get(conceptC));
			OWLClass elem = idClassMap.get(conceptD);

			// assertTrue(node.contains(elem));

		}

		assertEquals(expectedNumberOfUnifiers, (Integer) actualNumberOfUnifiers);
	}

}
