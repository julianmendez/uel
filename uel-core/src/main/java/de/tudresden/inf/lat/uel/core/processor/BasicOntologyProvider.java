/**
 * 
 */
package de.tudresden.inf.lat.uel.core.processor;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * This class provides the basic functionality for managing ontologies and
 * extracting entity labels that is exposed by an OWLOntologyManager.
 * 
 * @author Stefan Borgwardt
 */
public class BasicOntologyProvider extends OntologyProvider {

	private OWLOntologyManager manager;

	/**
	 * Construct a new ontology provider based on an OWL ontology manager.
	 * 
	 * @param manager
	 *            the OWL ontology manager
	 */
	public BasicOntologyProvider(OWLOntologyManager manager) {
		this.manager = manager;
	}

	@Override
	public OWLOntology createOntology() {
		try {
			return manager.createOntology();
		} catch (OWLOntologyCreationException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public OWLOntology loadOntology(File file) {
		try {
			return manager.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Set<OWLOntology> getOntologies() {
		return manager.getOntologies();
	}

	@Override
	public OWLDataFactory getOWLDataFactory() {
		return manager.getOWLDataFactory();
	}

	@Override
	public String getShortForm(OWLEntity entity) {
		for (OWLAnnotation annotation : EntitySearcher.getAnnotations(entity, manager.getOntologies(),
				getOWLDataFactory().getRDFSLabel())) {
			return annotation.getValue().asLiteral().get().getLiteral();
		}

		return null;
	}
}
