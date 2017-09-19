/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.Navigation;
import org.semanticweb.owlapi.search.EntitySearcher;

import com.google.common.collect.Sets;

import de.tudresden.inf.lat.uel.core.main.AlternativeUelStarter;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDModuleExtractor {
	private static OWLOntologyManager manager;
	private static OWLOntology snomed;

	public static void main(String[] args)
			throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
		manager = OWLManager.createOWLOntologyManager();
		snomed = AlternativeUelStarter.loadOntology(SNOMEDEvaluation.SNOMED_PATH, manager);

		for (String rootName : SNOMEDEvaluation.PARENT_CLASSES) {
			System.out.println(rootName);
			OWLClass root = findClass(rootName);

			Set<OWLClass> hierarchy = getDescendants(root);
			Files.write(Paths.get(SNOMEDEvaluation.SNOMED_MODULE_PATH + rootName + ".txt"),
					hierarchy.stream().map(OWLClass::toString).collect(Collectors.toList()));
			System.out.println("Saved classes list (" + hierarchy.size() + " classes).");

			OWLOntology module = manager.createOntology(getDefinitions(hierarchy));
			manager.addAxioms(module, getAnnotations(module));

			manager.saveOntology(module, new OWLXMLDocumentFormat(),
					Files.newOutputStream(Paths.get(SNOMEDEvaluation.SNOMED_MODULE_PATH + rootName + ".owl")));
			System.out.println("Saved ontology module (" + module.getLogicalAxiomCount() + " axioms).");
		}
	}

	private static Set<OWLAnnotationAssertionAxiom> getAnnotations(OWLOntology ontology) {
		return ontology.getSignature().stream()
				.flatMap(entity -> EntitySearcher.getAnnotationAssertionAxioms(entity, snomed).stream()
						.filter(ax -> ax.getProperty().equals(manager.getOWLDataFactory().getRDFSLabel())))
				.collect(Collectors.toSet());
	}

	private static String getLabel(OWLEntity entity) {
		return EntitySearcher.getAnnotations(entity, snomed, manager.getOWLDataFactory().getRDFSLabel()).iterator()
				.next().getValue().asLiteral().get().getLiteral();
	}

	private static OWLClass findClass(String label) {
		for (OWLClass cls : snomed.getClassesInSignature()) {
			if (getLabel(cls).equals(label)) {
				return cls;
			}
		}
		throw new RuntimeException("No class named '" + label + "' found.");
	}

	private static Set<OWLClass> getDescendants(OWLClass root) {
		Set<OWLClass> descendants = new HashSet<>();
		Set<OWLClass> toVisit = new HashSet<>();
		toVisit.add(root);

		while (!toVisit.isEmpty()) {
			OWLClass cls = toVisit.iterator().next();
			// System.out.println(getLabel(cls));
			for (OWLClass newCls : getChildren(cls)) {
				if (!descendants.contains(newCls)) {
					toVisit.add(newCls);
				}
			}
			toVisit.remove(cls);
			descendants.add(cls);
		}

		return descendants;
	}

	private static Set<OWLClass> getChildren(OWLClass parent) {
		return Sets
				.union(snomed
						.getAxioms(OWLEquivalentClassesAxiom.class, parent, Imports.EXCLUDED,
								Navigation.IN_SUPER_POSITION)
						.stream()
						.filter(ax -> ax.getClassExpressions().stream()
								.anyMatch(expr -> expr.asConjunctSet().contains(parent)))
						.flatMap(ax -> ax.getNamedClasses().stream()).collect(Collectors.toSet()),
						snomed
								.getAxioms(OWLSubClassOfAxiom.class, parent, Imports.EXCLUDED,
										Navigation.IN_SUPER_POSITION)
								.stream().filter(ax -> ax.getSuperClass().asConjunctSet().contains(parent))
								.map(ax -> ax.getSubClass()).filter(cls -> !cls.isAnonymous())
								.map(cls -> cls.asOWLClass()).collect(Collectors.toSet()));
	}

	private static Set<OWLAxiom> getDefinitions(Set<OWLClass> toVisit) {
		Set<OWLAxiom> definitions = new HashSet<>();
		Set<OWLClass> visited = new HashSet<>();

		while (!toVisit.isEmpty()) {
			OWLClass cls = toVisit.iterator().next();

			OWLAxiom def = getDefinition(cls);
			if (def != null) {
				definitions.add(def);
				for (OWLClass newCls : def.getClassesInSignature()) {
					if (!visited.contains(newCls)) {
						toVisit.add(newCls);
					}
				}
			}

			toVisit.remove(cls);
			visited.add(cls);
		}

		return definitions;
	}

	private static OWLAxiom getDefinition(OWLClass cls) {
		Set<OWLAxiom> axioms = new HashSet<>();
		axioms.addAll(snomed.getEquivalentClassesAxioms(cls));
		axioms.addAll(snomed.getSubClassAxiomsForSubClass(cls));
		if (axioms.size() > 1) {
			throw new RuntimeException("More than one definition for " + cls + "!");
		}
		if (axioms.size() == 1) {
			return axioms.iterator().next();
		}
		return null;
	}
}
