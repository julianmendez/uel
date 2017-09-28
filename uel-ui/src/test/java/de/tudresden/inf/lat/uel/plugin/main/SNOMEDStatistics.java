/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.search.EntitySearcher;

import de.tudresden.inf.lat.uel.core.main.AlternativeUelStarter;
import de.tudresden.inf.lat.uel.core.processor.UelOptions;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDStatistics {

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
	private static OWLObjectProperty roleGroup = SNOMEDEvaluation.prp(new UelOptions().snomedRoleGroupUri);

	private static OWLOntologyManager manager;
	private static OWLOntology snomed;

	public static void main(String[] args) {
		manager = OWLManager.createOWLOntologyManager();
		snomed = AlternativeUelStarter.loadOntology(SNOMEDEvaluation.SNOMED_PATH, manager);

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
				System.out.println(getLabel(prop));
			}
		}
		System.out.println();
		{
			System.out.println("Maximum number of RoleGroups in one definition: " + maxRoleGroups);
			System.out.println("Definition of " + getLabel(maxRoleGroupsClass) + ": " + maxRoleGroupsDef);
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
			System.out.println("Definition of " + getLabel(maxOtherRolesClass) + ": " + maxOtherRolesDef);
			int sum = 0;
			int num = 0;
			for (Entry<OWLObjectProperty, Map<Integer, Integer>> e1 : otherRoles.entrySet()) {
				for (Entry<Integer, Integer> e2 : e1.getValue().entrySet()) {
					num += e2.getValue();
					sum += e2.getKey() * e2.getValue();
					System.out.println("RoleGroups with " + e2.getKey() + " occurrences of " + getLabel(e1.getKey())
							+ ": " + e2.getValue());
				}
			}
			System.out.println("Average number of occurrences of a single role in a RoleGroup: "
					+ (((float) sum) / ((float) num)));
		}
		System.out.println();
	}

	private static String getLabel(OWLEntity entity) {
		return EntitySearcher.getAnnotations(entity, snomed, manager.getOWLDataFactory().getRDFSLabel()).iterator()
				.next().getValue().asLiteral().get().getLiteral();
	}

	private static void checkDefinition(OWLClass definiendum, OWLClassExpression definiens) {
		definitions++;
		Set<OWLObjectSomeValuesFrom> restrictions = getRestrictions(definiens);

		long c = restrictions.stream().filter(r -> !r.getProperty().equals(roleGroup))
				.map(r -> r.getProperty().asOWLObjectProperty()).peek(rolesWithoutRoleGroup::add).count();
		if (c > 0) {
			// if (c > 2) {
			// System.out.println("The definition of " + definiendum + " does
			// not use RoleGroup:"
			// + System.lineSeparator() + definiens);
			// }
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

//		Set<OWLClass> parents = getClasses(definiens);
//		if (parents.size() > 1) {
//			Set<String> hierarchies = parents.stream().map(cls -> getLabel(cls).split("[\\(\\)]"))
//					// .peek(arr -> System.out.println(String.join(" | ", arr)))
//					.filter(arr -> arr.length > 1)
//					.map(arr -> arr[arr.length - 1].substring(0, arr[arr.length - 1].length()))
//					.collect(Collectors.toSet());
//			// System.out.println();
//			if (hierarchies.size() > 1) {
//				System.out.println(String.join(" and ", hierarchies) + "          " + getLabel(definiendum));
//			}
//		}
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

	private static Set<OWLClass> getClasses(OWLClassExpression expr) {
		return expr.asConjunctSet().stream().filter(e -> e instanceof OWLClass).map(OWLClass.class::cast)
				.collect(Collectors.toSet());
	}
}
