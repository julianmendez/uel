/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.google.common.base.Stopwatch;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import de.tudresden.inf.lat.uel.core.main.AlternativeUelStarter;
import de.tudresden.inf.lat.uel.core.main.UnifierIterator;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDEvaluation {

	// private static final String WORK_DIR = "C:\\Users\\Stefan\\Work\\";
	private static final String WORK_DIR = "/Users/stefborg/Documents/";
	private static final String SNOMED_PATH = WORK_DIR + "Ontologies/snomed-english-rdf.owl";
	private static final String SNOMED_RESTR_PATH = WORK_DIR + "Ontologies/snomed-restrictions.owl";
	private static final String POS_PATH = WORK_DIR + "Projects/uel-snomed/uel-snomed-pos.owl";
	private static final String NEG_PATH = WORK_DIR + "Projects/uel-snomed/uel-snomed-neg.owl";
	private static final String CONSTRAINTS_PATH = WORK_DIR + "Projects/uel-snomed/constraints_const.owl";

	private static OWLClass cls(String name) {
		return OWLManager.getOWLDataFactory().getOWLClass(IRI.create("http://www.ihtsdo.org/" + name));
	}

	private static OWLObjectProperty prp(String name) {
		return OWLManager.getOWLDataFactory().getOWLObjectProperty(IRI.create("http://www.ihtsdo.org/" + name));
	}

	private static int definitions = 0;
	private static int occurrencesWithoutRoleGroup = 0;
	private static int siblingsOfRolesWithoutRoleGroup = 0;
	private static Set<OWLObjectProperty> rolesWithoutRoleGroup = new HashSet<OWLObjectProperty>();
	private static int maxRoleGroups = 0;
	private static OWLClass maxRoleGroupsClass = null;
	private static OWLClassExpression maxRoleGroupsDef = null;
	private static Map<Integer, Integer> roleGroups = new HashMap<Integer, Integer>();
	private static Map<OWLObjectProperty, Map<Integer, Integer>> otherRoles = new HashMap<OWLObjectProperty, Map<Integer, Integer>>();
	private static int maxOtherRoles = 0;
	private static OWLClass maxOtherRolesClass = null;
	private static OWLClassExpression maxOtherRolesDef = null;
	private static OWLObjectProperty roleGroup = prp("RoleGroup");

	/**
	 * Entry point for tests.
	 * 
	 * @param args
	 *            arguments (ignored)
	 */
	public static void main(String[] args) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology snomed = AlternativeUelStarter.loadOntology(SNOMED_PATH, manager);

		for (OWLSubClassOfAxiom a : snomed.getAxioms(AxiomType.SUBCLASS_OF)) {
			checkDefinition((OWLClass) a.getSubClass(), a.getSuperClass());
		}
		for (OWLEquivalentClassesAxiom a : snomed.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
			for (OWLClassExpression e : a.getClassExpressions()) {
				if (!e.isAnonymous()) {
					checkDefinition((OWLClass) e,
							manager.getOWLDataFactory().getOWLObjectIntersectionOf(a.getClassExpressionsMinus(e)));
				}
			}
		}
		System.out.println();
		{
			System.out.println("Checked " + definitions + " definitions.");
			System.out.println("Occurrences of roles without RoleGroup: " + occurrencesWithoutRoleGroup);
			System.out.println("Average number of existential restrictions in such definitions: "
					+ (((float) siblingsOfRolesWithoutRoleGroup) / ((float) occurrencesWithoutRoleGroup)));
			System.out.println("Roles that occur without RoleGroup:");
			for (OWLObjectProperty prop : rolesWithoutRoleGroup) {
				System.out.println(prop);
			}
		}
		System.out.println();
		{
			System.out.println("Maximum number of RoleGroups in one definition: " + maxRoleGroups);
			System.out.println("Definition of " + maxRoleGroupsClass + ": " + maxRoleGroupsDef);
			int sum = 0;
			int num = 0;
			for (Entry<Integer, Integer> e : roleGroups.entrySet()) {
				num += e.getValue();
				sum += e.getKey() * e.getValue();
				System.out.println("Definitions with " + e.getKey() + " RoleGroups: " + e.getValue());
			}
			System.out.println("Average number of RoleGroups: " + (((float) sum) / ((float) num)));
		}
		System.out.println();
		{
			System.out.println("Maximum number of occurrences of the same role within one RoleGroup: " + maxOtherRoles);
			System.out.println("Definition of " + maxOtherRolesClass + ": " + maxOtherRolesDef);
			int sum = 0;
			int num = 0;
			for (Entry<OWLObjectProperty, Map<Integer, Integer>> e1 : otherRoles.entrySet()) {
				for (Entry<Integer, Integer> e2 : e1.getValue().entrySet()) {
					num += e2.getValue();
					sum += e2.getKey() * e2.getValue();
					System.out.println(
							"RoleGroups with " + e2.getKey() + " occurrences of " + e1.getKey() + ": " + e2.getValue());
				}
			}
			System.out.println("Average number of occurrences of a single role in a RoleGroup: "
					+ (((float) sum) / ((float) num)));
		}
		System.out.println();
	}

	private static void checkDefinition(OWLClass definiendum, OWLClassExpression definiens) {
		definitions++;
		Set<OWLObjectSomeValuesFrom> restrictions = getRestrictions(definiens);

		long c = restrictions.stream().filter(r -> !r.getProperty().equals(roleGroup))
				.map(r -> r.getProperty().asOWLObjectProperty()).peek(rolesWithoutRoleGroup::add).count();
		if (c > 0) {
			if (c > 2) {
				System.out.println("The definition of " + definiendum + " does not use RoleGroup:"
						+ System.lineSeparator() + definiens);
			}
			occurrencesWithoutRoleGroup++;
			siblingsOfRolesWithoutRoleGroup += restrictions.size();
		} else {
			addToMap(roleGroups, restrictions.size());
			if (restrictions.size() > maxRoleGroups) {
				maxRoleGroups = restrictions.size();
				maxRoleGroupsClass = definiendum;
				maxRoleGroupsDef = definiens;
			}
			restrictions.stream().forEach(r -> checkInnerRestrictions(definiendum, definiens, r.getFiller()));
		}
	}

	private static <T> void addToMap(Map<T, Integer> map, T key) {
		Integer value = map.get(key);
		if (value == null) {
			value = 0;
		}
		map.put(key, value + 1);
	}

	private static void checkInnerRestrictions(OWLClass definiendum, OWLClassExpression definiens,
			OWLClassExpression expr) {
		Set<OWLObjectSomeValuesFrom> restrictions = getRestrictions(expr);
		Map<OWLObjectProperty, Integer> count = new HashMap<OWLObjectProperty, Integer>();

		for (OWLObjectSomeValuesFrom r : restrictions) {
			OWLObjectProperty prop = r.getProperty().asOWLObjectProperty();
			Integer c = count.get(prop);
			if (c == null) {
				c = 0;
			}
			count.put(prop, c + 1);
		}

		for (Entry<OWLObjectProperty, Integer> e : count.entrySet()) {
			Map<Integer, Integer> map = otherRoles.get(e.getKey());
			if (map == null) {
				otherRoles.put(e.getKey(), new HashMap<Integer, Integer>());
			}
			addToMap(otherRoles.get(e.getKey()), e.getValue());
			if (e.getValue() > maxOtherRoles) {
				maxOtherRoles = e.getValue();
				maxOtherRolesClass = definiendum;
				maxOtherRolesDef = definiens;
			}
		}
	}

	private static Set<OWLObjectSomeValuesFrom> getRestrictions(OWLClassExpression expr) {
		return expr.asConjunctSet().stream().filter(e -> e instanceof OWLObjectSomeValuesFrom)
				.map(OWLObjectSomeValuesFrom.class::cast).collect(Collectors.toSet());
	}

	private static void test() {
		Stopwatch timer = Stopwatch.createStarted();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology snomed = AlternativeUelStarter.loadOntology(SNOMED_PATH, manager);
		OWLOntology snomedRestrictions = AlternativeUelStarter.loadOntology(SNOMED_RESTR_PATH, manager);
		AlternativeUelStarter starter = new AlternativeUelStarter(
				new HashSet<OWLOntology>(Arrays.asList(snomed, snomedRestrictions)));
		starter.setVerbose(true);
		starter.markUndefAsVariables(false);
		// starter.markUndefAsAuxVariables(true);
		starter.setSnomedMode(true);

		OWLOntology pos = AlternativeUelStarter.loadOntology(POS_PATH, manager);
		OWLOntology neg = AlternativeUelStarter.loadOntology(NEG_PATH, manager);
		OWLOntology constraints = AlternativeUelStarter.loadOntology(CONSTRAINTS_PATH, manager);
		// OWLOntology neg = UelModel.EMPTY_ONTOLOGY;
		// String[] varNames = { "X" };
		String[] varNames = { "X" };
		UnifierIterator iterator = (UnifierIterator) starter.modifyOntologyAndSolve(pos, neg, null,
				Arrays.asList(varNames).stream().map(SNOMEDEvaluation::cls).collect(Collectors.toSet()),
				UnificationAlgorithmFactory.SAT_BASED_ALGORITHM, true);

		Set<OWLAxiom> background = iterator.getUelModel().renderDefinitions();
		UelModel model = iterator.getUelModel();

		OWLDataFactory fac = manager.getOWLDataFactory();
		OWLAxiom goalAxiom = fac
				.getOWLEquivalentClassesAxiom(cls("X"),
						fac.getOWLObjectIntersectionOf(cls("SCT_106133000"), cls("SCT_365781004"),
								fac.getOWLObjectSomeValuesFrom(prp("RoleGroup"),
										fac.getOWLObjectIntersectionOf(fac.getOWLObjectSomeValuesFrom(
												prp("SCT_363713009"), cls("SCT_371157007")),
										fac.getOWLObjectSomeValuesFrom(prp("SCT_363714003"), cls("SCT_307124006"))))));

		output(timer, "Building the unification problem", true);

		System.out.println("Unifiers:");

		try {
			Scanner in = new Scanner(System.in);
			int i = 0;
			boolean skip = true;

			if (!skip) {
				System.out.println("Press RETURN to start computing the next unifier (input 'a' for all unifiers) ...");
			}
			while (skip || in.hasNextLine()) {
				if (!skip) {
					if (in.nextLine().equals("a")) {
						skip = true;
					}
				}
				// if (i == 0) {
				// skip = false;
				// }

				i++;
				System.out.println();
				System.out.println("--- " + i);

				if (iterator.hasNext()) {

					// TODO compute unifiers modulo equivalence?

					System.out.println(model.printCurrentUnifier());
					// System.out.println(
					// model.getStringRenderer(null).renderUnifier(model.getCurrentUnifier(),
					// false, false, true));

					Set<OWLEquivalentClassesAxiom> unifier = iterator.next();
					OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
					OWLOntology extendedOntology = ontologyManager.createOntology();
					ontologyManager.addAxioms(extendedOntology, background);
					ontologyManager.addAxioms(extendedOntology, unifier);

					// ontologyManager.saveOntology(extendedOntology, new
					// FunctionalSyntaxDocumentFormat(), System.out);

					OWLReasoner reasoner = new JcelReasonerFactory().createNonBufferingReasoner(extendedOntology);
					reasoner.precomputeInferences();

					boolean solution = true;
					for (OWLAxiom a : pos.getAxioms(AxiomType.SUBCLASS_OF)) {
						// System.out.println(a + " (pos): " +
						// reasoner.isEntailed(a));
						if (!reasoner.isEntailed(a)) {
							solution = false;
						}
						// Assert.assertTrue(reasoner.isEntailed(a));
					}
					for (OWLAxiom a : neg.getAxioms(AxiomType.SUBCLASS_OF)) {
						// System.out.println(a + " (neg): " +
						// reasoner.isEntailed(a));
						if (reasoner.isEntailed(a)) {
							solution = false;
						}
						// Assert.assertTrue(!reasoner.isEntailed(a));
					}
					if (!solution) {
						System.out.println("This is not a real solution (due to the replacement of UNDEF names)!");
					}

					// System.out.println(goalAxiom + " (goal): " +
					// reasoner.isEntailed(goalAxiom));
					if (reasoner.isEntailed(goalAxiom)) {
						System.out.println("This is the wanted solution!");
						output(timer, "Time to compute the wanted solution", false);
						// break;
					} else {
						if (i == 1) {
							output(timer, "Time to compute first solution", false);
						}
					}

					reasoner.dispose();

					System.out.println();

					System.out.flush();
				} else {
					System.out.println("No more unifiers.");
					output(timer, "Time to compute all solutions", false);
					break;
				}
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void output(Stopwatch timer, String description, boolean reset) {
		System.out.println(description + ": " + timer);
		if (reset) {
			timer.reset();
			timer.start();
		}
	}

}
