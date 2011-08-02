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
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasoner;
import de.tudresden.inf.lat.uel.main.Goal;

public class ProcessorTest extends TestCase {

	private static final String ontology01 = "src/test/resources/testOntology-01.krss";
	private static final String ontology02 = "src/test/resources/testOntology-02.krss";

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
		tryOntology(ontology01);
	}

	public void test02() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology02);
	}

	private void tryOntology(String ontologyName)
			throws OWLOntologyCreationException, IOException {
		Map<String, OWLClass> idClassMap = new HashMap<String, OWLClass>();
		UelProcessor processor = new UelProcessor();
		{
			OWLOntology ontology = createOntology(new FileInputStream(
					ontologyName));
			processor.loadOntology(processor.createOntology(ontology));

			Set<OWLClass> clsSet = ontology.getClassesInSignature();
			for (OWLClass cls : clsSet) {
				idClassMap.put(cls.getIRI().getFragment(), cls);
			}
		}
		Set<String> variables = new HashSet<String>();
		variables.add(idClassMap.get("A1").toStringID());
		variables.add(idClassMap.get("A4").toStringID());
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
		String goalStr = goal.toString();

		for (String unifier : unifiers) {
			String extendedOntology = goalStr + unifier;
			OWLReasoner reasoner = createReasoner(extendedOntology);
			assertTrue(reasoner.getEquivalentClasses(idClassMap.get("C"))
					.contains(idClassMap.get("D")));
		}
	}

}
