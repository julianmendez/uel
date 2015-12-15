package de.tudresden.inf.lat.uel.core.processor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import de.tudresden.inf.lat.uel.core.type.KRSSRenderer;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;

@RunWith(value = Parameterized.class)
public class ProcessorTest {

	private static final String apath = "src/test/resources/";
	private static final String conceptC = "C";
	private static final String conceptD = "D";
	private static final String prefix = "testOntology-";
	private static final String krss = ".krss";
	private static final String test = ".test";
	private static final int maxTest = 17;

	private String ontologyName;
	private Set<String> varNames;
	private Set<String> undefVarNames;
	private Integer numberOfUnifiers;
	private String processorName;

	public ProcessorTest(String ontologyName, Set<String> varNames, Set<String> undefVarNames, Integer numberOfUnifiers,
			String processorName) {
		this.ontologyName = ontologyName;
		this.varNames = varNames;
		this.undefVarNames = undefVarNames;
		this.numberOfUnifiers = numberOfUnifiers;
		this.processorName = processorName;
	}

	private OWLOntology createOntology(InputStream input) throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		ontologyManager.loadOntologyFromOntologyDocument(input);
		return ontologyManager.getOntologies().iterator().next();
	}

	private OWLReasoner createReasoner(String ontologyStr) throws OWLOntologyCreationException {
		JcelReasonerFactory factory = new JcelReasonerFactory();
		OWLReasoner reasoner = factory
				.createNonBufferingReasoner(createOntology(new ByteArrayInputStream(ontologyStr.getBytes())));
		reasoner.precomputeInferences();
		return reasoner;
	}

	private void makeVariable(UelModel model, String atomName, boolean undef) {
		AtomManager atomManager = model.getAtomManager();
		if (undef) {
			atomName += AtomManager.UNDEF_SUFFIX;
		}
		ConceptName conceptName = atomManager.createConceptName(atomName, true);
		model.getPluginGoal().makeUserVariable(atomManager.getAtoms().getIndex(conceptName));
	}

	@Parameters(name = "{index}: {0}, {4}")
	public static Collection<Object[]> data() {
		Collection<Object[]> data = new ArrayList<Object[]>();

		for (int i = 1; i <= maxTest; i++) {
			try {

				String fileName = apath + prefix + String.format("%02d", i);
				String ontologyName = fileName + krss;
				BufferedReader configFile = new BufferedReader(new FileReader(fileName + test));

				Set<String> varNames = parseSet(configFile.readLine());
				Set<String> undefVarNames = parseSet(configFile.readLine());

				String processorName = configFile.readLine();
				while (processorName != null) {
					Integer numberOfUnifiers = Integer.parseInt(configFile.readLine());
					if (!processorName.contains("ASP")) {
						data.add(new Object[] { ontologyName, varNames, undefVarNames, numberOfUnifiers,
								processorName });
					}

					processorName = configFile.readLine();
				}
				configFile.close();

			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

		return data;
	}

	private static Set<String> parseSet(String input) {
		if (input.equals("")) {
			return Collections.emptySet();
		} else {
			return new HashSet<String>(Arrays.asList(input.split(",")));
		}
	}

	@Test
	public void tryOntology() throws OWLOntologyCreationException, IOException, InterruptedException {
		Map<String, OWLClass> idClassMap = new HashMap<String, OWLClass>();
		OWLOntology owlOntology = createOntology(new FileInputStream(ontologyName));
		OWLOntologyManager ontologyManager = owlOntology.getOWLOntologyManager();
		UelModel uelModel = new UelModel(ontologyManager, null);
		Set<OWLClass> clsSet = owlOntology.getClassesInSignature();
		for (OWLClass cls : clsSet) {
			idClassMap.put(cls.getIRI().getShortForm(), cls);
		}

		OWLOntology positiveProblem = ontologyManager.createOntology();
		ontologyManager.addAxiom(positiveProblem, ontologyManager.getOWLDataFactory()
				.getOWLEquivalentClassesAxiom(idClassMap.get(conceptC), idClassMap.get(conceptD)));
		OWLOntology negativeProblem = ontologyManager.createOntology();

		Set<OWLOntology> owlOntologies = new HashSet<OWLOntology>();
		owlOntologies.add(owlOntology);
		uelModel.setupPluginGoal(owlOntologies, positiveProblem, negativeProblem, null);

		makeVariable(uelModel, idClassMap.get(conceptC).toStringID(), false);
		makeVariable(uelModel, idClassMap.get(conceptD).toStringID(), false);
		for (String var : varNames) {
			makeVariable(uelModel, idClassMap.get(var).toStringID(), false);
		}
		for (String var : undefVarNames) {
			makeVariable(uelModel, idClassMap.get(var).toStringID(), true);
		}
		uelModel.getPluginGoal().updateUelInput();

		uelModel.createUelProcessor(processorName);

		boolean hasUnifiers = true;
		while (hasUnifiers) {
			hasUnifiers = uelModel.computeNextUnifier();
		}

		List<Set<Equation>> unifiers = uelModel.getUnifierList();
		KRSSRenderer renderer = new KRSSRenderer(uelModel.getAtomManager(), uelModel.getPluginGoal().getUserVariables(),
				uelModel.getPluginGoal().getAuxiliaryVariables(), null);
		String goalStr = renderer.printDefinitions(uelModel.getPluginGoal().getUelInput().getDefinitions());

		for (Set<Equation> unifier : unifiers) {
			String extendedOntology = goalStr + renderer.printUnifier(unifier);
			// System.out.println();
			// System.out.println("---");
			// System.out.println(extendedOntology);

			OWLReasoner reasoner = createReasoner(extendedOntology);
			Node<OWLClass> node = reasoner.getEquivalentClasses(idClassMap.get(conceptC));
			OWLClass elem = idClassMap.get(conceptD);
			Assert.assertTrue(node.contains(elem));
		}

		Assert.assertEquals(numberOfUnifiers, (Integer) unifiers.size());
	}
}
