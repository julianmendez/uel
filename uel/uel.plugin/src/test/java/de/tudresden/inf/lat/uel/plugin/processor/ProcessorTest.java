package de.tudresden.inf.lat.uel.plugin.processor;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasoner;
import de.tudresden.inf.lat.uel.core.type.Goal;

public class ProcessorTest extends TestCase {

	private static final String apath = "src/test/resources/";

	// private static final Logger logger = Logger.getLogger(ProcessorTest.class
	// .getName());

	private static final String ontology01 = apath + "testOntology-01.krss";
	private static final String ontology02 = apath + "testOntology-02.krss";
	private static final String ontology03 = apath + "testOntology-03.krss";
	private static final String ontology04 = apath + "testOntology-04.krss";
	private static final String ontology05 = apath + "testOntology-05.krss";
	private static final String ontology06 = apath + "testOntology-06.krss";
	private static final String ontology07 = apath + "testOntology-07.krss";
	private static final String ontology08 = apath + "testOntology-08.krss";
	private static final String ontology09 = apath + "testOntology-09.krss";
	private static final String ontology10 = apath + "testOntology-10.krss";
	private static final String ontology11 = apath + "testOntology-11.krss";
	private static final String ontology12 = apath + "testOntology-12.krss";

	private OWLOntology createOntology(InputStream input)
			throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = OWLManager
				.createOWLOntologyManager();
		ontologyManager.loadOntologyFromOntologyDocument(input);
		return ontologyManager.getOntologies().iterator().next();
	}

	private OWLReasoner createReasoner(String ontologyStr)
			throws OWLOntologyCreationException {
		JcelReasoner reasoner = new JcelReasoner(
				createOntology(new ByteArrayInputStream(ontologyStr.getBytes())));
		reasoner.precomputeInferences();
		return reasoner;
	}

	public void test01() throws OWLOntologyCreationException, IOException {
		Set<String> varNames = new HashSet<String>();
		varNames.add("A1");
		varNames.add("A4");
		tryOntology(ontology01, varNames);
	}

	public void test02() throws OWLOntologyCreationException, IOException {
		Set<String> varNames = new HashSet<String>();
		varNames.add("A1");
		varNames.add("A4");
		tryOntology(ontology02, varNames);
	}

	public void test03() throws OWLOntologyCreationException, IOException {
		Set<String> varNames = new HashSet<String>();
		varNames.add("Z");
		tryOntology(ontology03, varNames);
	}

	public void test04() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology04, new HashSet<String>());
	}

	public void test05() throws OWLOntologyCreationException, IOException {
		Set<String> varNames = new HashSet<String>();
		varNames.add("A");
		varNames.add("A1");
		varNames.add("A2");
		tryOntology(ontology05, varNames);
	}

	public void test06() throws OWLOntologyCreationException, IOException {
		Set<String> varNames = new HashSet<String>();
		varNames.add("A1");
		varNames.add("A2");
		tryOntology(ontology06, varNames);
	}

	public void test07() throws OWLOntologyCreationException, IOException {
		Set<String> varNames = new HashSet<String>();
		varNames.add("A1");
		varNames.add("A2");
		tryOntology(ontology07, varNames);
	}

	public void test08() throws OWLOntologyCreationException, IOException {
		Set<String> varNames = new HashSet<String>();
		varNames.add("A1");
		varNames.add("A2");
		varNames.add("A3");
		varNames.add("A4");
		varNames.add("A5");
		tryOntology(ontology08, varNames);
	}

	public void test09() throws OWLOntologyCreationException, IOException {
		Set<String> varNames = new HashSet<String>();
		varNames.add("A1");
		varNames.add("A2");
		tryOntology(ontology09, varNames);
	}

	public void test10() throws OWLOntologyCreationException, IOException {
		Set<String> varNames = new HashSet<String>();
		varNames.add("A1");
		varNames.add("A3");
		tryOntology(ontology10, varNames);
	}

	public void test11() throws OWLOntologyCreationException, IOException {
		Set<String> varNames = new HashSet<String>();
		tryOntology(ontology11, varNames);
	}

	public void test12() throws OWLOntologyCreationException, IOException {
		Set<String> varNames = new HashSet<String>();
		varNames.add("A2");
		tryOntology(ontology12, varNames);
	}

	private void tryOntology(String ontologyName, Set<String> varNames)
			throws OWLOntologyCreationException, IOException {
		Map<String, OWLClass> idClassMap = new HashMap<String, OWLClass>();
		UelProcessor processor = new UelProcessor();
		{
			OWLOntology owlOntology = createOntology(new FileInputStream(
					ontologyName));
			processor.loadOntology(owlOntology);
			Set<OWLClass> clsSet = owlOntology.getClassesInSignature();
			for (OWLClass cls : clsSet) {
				idClassMap.put(cls.getIRI().getFragment(), cls);
			}
		}
		Set<String> variables = new HashSet<String>();
		for (String var : varNames) {
			variables.add(idClassMap.get(var).toStringID());
		}
		processor.addAll(variables);

		Set<String> input = new HashSet<String>();
		input.add(idClassMap.get("C").toStringID());
		input.add(idClassMap.get("D").toStringID());
		Goal goal = processor.configure(input);

		boolean hasUnifiers = true;
		while (hasUnifiers) {
			hasUnifiers = processor.computeNextUnifier();
		}

		List<String> unifiers = processor.getUnifierList();
		String goalStr = goal.getGoalEquations();

		for (String unifier : unifiers) {
			String extendedOntology = goalStr + unifier;
			OWLReasoner reasoner = createReasoner(extendedOntology);
			Node<OWLClass> node = reasoner.getEquivalentClasses(idClassMap
					.get("C"));
			OWLClass elem = idClassMap.get("D");
			assertTrue(node.contains(elem));
		}
	}

}
