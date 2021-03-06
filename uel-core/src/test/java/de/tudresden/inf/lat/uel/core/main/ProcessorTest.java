package de.tudresden.inf.lat.uel.core.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.krss2.parser.KRSS2OWLParser;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import de.tudresden.inf.lat.uel.core.processor.BasicOntologyProvider;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

public class ProcessorTest {

	static final char COMMENT_CHAR = '#';
	private static final String apath = "src/test/resources/";
	private static final String conceptC = "C";
	private static final String conceptD = "D";
	private static final String prefix = "testOntology-";
	private static final String krss = ".krss";
	private static final String test = ".test";
	private static final int maxTest = 17;

	String getMemoryUsage() {
		long totalMemory = Runtime.getRuntime().totalMemory() / 0x100000;
		long freeMemory = Runtime.getRuntime().freeMemory() / 0x100000;
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("([X]:");
		sbuf.append("" + (totalMemory - freeMemory));
		sbuf.append(" MB, [ ]:");
		sbuf.append("" + freeMemory);
		sbuf.append(" MB)");
		return sbuf.toString();
	}

	static OWLOntology loadKRSSOntology(String input) throws OWLOntologyCreationException, IOException {
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = ontologyManager.createOntology();
		KRSS2OWLParser parser = new KRSS2OWLParser();
		parser.parse(new StreamDocumentSource(new FileInputStream(input)), ontology,
				new OWLOntologyLoaderConfiguration());
		return ontology;
	}

	static OWLOntology createOntology(Set<? extends OWLAxiom> background, Set<? extends OWLAxiom> unifier)
			throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = ontologyManager.createOntology();
		ontologyManager.addAxioms(ontology, background);
		ontologyManager.addAxioms(ontology, unifier);
		return ontology;
	}

	static OWLReasoner createReasoner(OWLOntology ontology) {
		JcelReasonerFactory factory = new JcelReasonerFactory();
		OWLReasoner reasoner = factory.createNonBufferingReasoner(ontology);
		reasoner.precomputeInferences();
		return reasoner;
	}

	/**
	 * Returns the next line read ignoring comments. A comment is a line
	 * starting with a distinguish comment character. This method does not
	 * ignore empty lines.
	 * 
	 * @param reader
	 *            reader
	 * @return the next line read ignoring comments
	 * @throws IOException
	 *             if something went wrong with I/O
	 */
	static String readNextLine(BufferedReader reader) throws IOException {
		String ret = reader.readLine();
		while (ret != null && ret.startsWith("" + COMMENT_CHAR)) {
			ret = reader.readLine();
		}
		return ret;
	}

	private static Stream<Arguments> data() {
		Collection<Arguments> data = new ArrayList<>();

		System.out.println("Preparing tests.");
		for (int i = 1; i <= maxTest; i++) {
			try {

				String fileName = apath + prefix + String.format("%02d", i);
				String ontologyName = fileName + krss;
				BufferedReader configFile = new BufferedReader(new FileReader(fileName + test));

				Set<String> varNames = parseSet(readNextLine(configFile));
				Set<String> undefVarNames = parseSet(readNextLine(configFile));

				String algorithmName = readNextLine(configFile);
				while (algorithmName != null) {
					Integer nbUnifiers = Integer.parseInt(readNextLine(configFile));
					if (!algorithmName.contains("ASP")) {
						data.add(Arguments.of(ontologyName, varNames, undefVarNames, nbUnifiers, algorithmName));
					}

					algorithmName = readNextLine(configFile);
				}
				configFile.close();

			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
		System.out.println("Tests are prepared.");

		return data.stream();
	}

	private static Set<String> parseSet(String input) {
		if (input.equals("")) {
			return Collections.emptySet();
		} else {
			return new HashSet<>(Arrays.asList(input.split(",")));
		}
	}

	@ParameterizedTest(name = "{index}: {0}, {4}")
	@MethodSource("data")
	public void tryOntology(String ontologyName, Set<String> varNames, Set<String> undefVarNames, Integer numberOfUnifiers,
							String algorithmName) throws OWLOntologyCreationException, IOException, InterruptedException {
		System.out.println("Testing " + ontologyName + " with " + algorithmName + " " + getMemoryUsage() + ".");
		Map<String, OWLClass> idClassMap = new HashMap<>();
		OWLOntology owlOntology = loadKRSSOntology(ontologyName);
		OWLOntologyManager ontologyManager = owlOntology.getOWLOntologyManager();
		UelModel uelModel = new UelModel(new BasicOntologyProvider(ontologyManager));
		Set<OWLClass> clsSet = owlOntology.getClassesInSignature();
		for (OWLClass cls : clsSet) {
			idClassMap.put(cls.getIRI().getShortForm(), cls);
		}

		OWLOntology positiveProblem = ontologyManager.createOntology();
		ontologyManager.addAxiom(positiveProblem, ontologyManager.getOWLDataFactory()
				.getOWLEquivalentClassesAxiom(idClassMap.get(conceptC), idClassMap.get(conceptD)));
		OWLOntology negativeProblem = ontologyManager.createOntology();

		uelModel.setupGoal(Collections.singleton(owlOntology), positiveProblem, negativeProblem, null, true);

		Set<OWLClass> variables = new HashSet<>();
		// variables.add(idClassMap.get(conceptC));
		// variables.add(idClassMap.get(conceptD));
		for (String var : varNames) {
			variables.add(idClassMap.get(var));
		}
		uelModel.makeClassesUserVariables(variables);
		variables.clear();
		for (String var : undefVarNames) {
			variables.add(idClassMap.get(var));
		}
		uelModel.makeUndefClassesUserVariables(variables);

		uelModel.initializeUnificationAlgorithm(algorithmName);

		while (uelModel.computeNextUnifier()) {
		}

		// cannot use 'owlOntology' here, as we may have introduced UNDEF
		// variables
		Set<OWLAxiom> background = uelModel.renderDefinitions();

		for (Unifier unifier : uelModel.getUnifierList()) {
			// for (OWLAxiom ax : uelModel.renderUnifier(unifier)) {
			// System.out.println(ax);
			// }
			// System.out.println();
			// System.out.println();
			// System.out.println();
			// System.out.println();
			OWLOntology extendedOntology = createOntology(background, uelModel.renderUnifier(unifier));
			OWLReasoner reasoner = createReasoner(extendedOntology);
			Node<OWLClass> node = reasoner.getEquivalentClasses(idClassMap.get(conceptC));
			OWLClass elem = idClassMap.get(conceptD);
			Assertions.assertTrue(node.contains(elem));
			reasoner.dispose();
		}

		Assertions.assertEquals(numberOfUnifiers, (Integer) uelModel.getUnifierList().size());
		System.out.println("Test OK " + getMemoryUsage() + ".");
	}

}
