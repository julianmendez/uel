package de.tudresden.inf.lat.uel.core.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.krss2.parser.KRSS2OWLParser;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.google.common.base.Stopwatch;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import de.tudresden.inf.lat.uel.core.processor.BasicOntologyProvider;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UelOptions;
import de.tudresden.inf.lat.uel.core.processor.UelOptions.UndefBehavior;
import de.tudresden.inf.lat.uel.core.processor.UelOptions.Verbosity;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

public class ProcessorTest {

	static final char COMMENT_CHAR = '#';
	private static final String apath = "src/test/resources/";
	private static final String conceptC = "C";
	private static final String conceptD = "D";
	private static final String krss = ".krss";
	private static final int maxTest = 17;
	private static final String prefix = "testOntology-";
	private static final String test = ".test";
	private static final OWLClass c = toOWLClass(conceptC);
	private static final OWLClass d = toOWLClass(conceptD);

	static OWLOntology clearManagerAndCreateOntology(OWLOntologyManager manager, Set<? extends OWLAxiom> background,
			Set<? extends OWLAxiom> unifier) throws OWLOntologyCreationException {
		for (OWLOntology o : manager.getOntologies()) {
			manager.removeOntology(o);
		}
		OWLOntology ontology = manager.createOntology();
		manager.addAxioms(ontology, background);
		manager.addAxioms(ontology, unifier);
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

	static OWLOntology loadKRSSOntology(String input) throws OWLOntologyCreationException, IOException {
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = ontologyManager.createOntology();
		KRSS2OWLParser parser = new KRSS2OWLParser();
		parser.parse(new StreamDocumentSource(new FileInputStream(input)), ontology,
				new OWLOntologyLoaderConfiguration());
		return ontology;
	}

	private static Set<String> parseSet(String input) {
		Set<String> ret = new HashSet<String>(Arrays.asList(input.split(",")));
		ret.remove("");
		return ret;
	}

	private static OWLClass toOWLClass(String name) {
		return OWLManager.getOWLDataFactory().getOWLClass(IRI.create("x#" + name));
	}

	private String algorithmName;

	private Integer numberOfUnifiers;

	private String ontologyName;

	private Set<String> undefVarNames;

	private Set<String> varNames;

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

	public void tryOntology() throws OWLOntologyCreationException, IOException, InterruptedException {
		OWLOntology owlOntology = loadKRSSOntology(ontologyName);
		OWLOntologyManager manager = owlOntology.getOWLOntologyManager();
		UelOptions options = new UelOptions();
		options.undefBehavior = UndefBehavior.CONSTANTS;
		options.unificationAlgorithmName = algorithmName;
		options.verbosity = Verbosity.SILENT;
		UelModel uelModel = new UelModel(new BasicOntologyProvider(manager), options);

		OWLOntology positiveProblem = manager.createOntology();
		manager.addAxiom(positiveProblem, manager.getOWLDataFactory().getOWLEquivalentClassesAxiom(c, d));
		OWLOntology negativeProblem = manager.createOntology();
		uelModel.setupGoal(Collections.singleton(owlOntology), positiveProblem, negativeProblem, null,
				Stream.concat(varNames.stream(), undefVarNames.stream().map(s -> s + AtomManager.UNDEF_SUFFIX))
						.map(ProcessorTest::toOWLClass).collect(Collectors.toSet()),
				true);
		uelModel.initializeUnificationAlgorithm();

		// System.out.println(uelModel.getStringRenderer(null).renderGoal(uelModel.getGoal()));

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
			Set<OWLAxiom> s = uelModel.renderUnifier(unifier);
			OWLOntology extendedOntology = clearManagerAndCreateOntology(manager, background, s);
			// try {
			// extendedOntology.saveOntology(new
			// FunctionalSyntaxDocumentFormat(), System.out);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			OWLReasoner reasoner = createReasoner(extendedOntology);
			Node<OWLClass> node = reasoner.getEquivalentClasses(c);
			OWLClass elem = d;
			Assertions.assertTrue(node.contains(elem));
			reasoner.dispose();
		}

		Assertions.assertEquals(numberOfUnifiers, (Integer) uelModel.getUnifierList().size());
		System.out.println("Test OK " + getMemoryUsage() + ".");
	}

	private static void tick(Stopwatch timer) {
		System.out.print(timer.toString() + " ");
		timer.reset();
		timer.start();
	}

}
