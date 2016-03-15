/**
 * 
 */
package de.tudresden.inf.lat.uel.core.processor;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * @author Stefan Borgwardt
 *
 */
public class BasicOntologyProvider extends OntologyProvider {

	private OWLOntologyManager manager;

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
	public void loadOntology(File file) {
		try {
			manager.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Set<OWLOntology> getOntologies() {
		return manager.getOntologies();
	}

	@Override
	public String getShortForm(OWLEntity entity) {
		for (OWLAnnotation annotation : EntitySearcher.getAnnotations(entity, manager.getOntologies(),
				OWLManager.getOWLDataFactory().getRDFSLabel())) {
			return annotation.getValue().toString();
		}

		return null;
	}
}
