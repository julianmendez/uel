/**
 * 
 */
package de.tudresden.inf.lat.uel.core.processor;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Stefan Borgwardt
 *
 */
public interface OntologyProvider {

	OWLOntology createOntology();
	
	void loadOntology(File file);

	Set<OWLOntology> getOntologies();

	boolean providesShortForms();

	String getShortForm(OWLEntity entity);

}
