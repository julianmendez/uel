/**
 * 
 */
package de.tudresden.inf.lat.uel.core.processor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Stefan Borgwardt
 *
 */
public abstract class OntologyProvider implements ShortFormProvider {

	/**
	 * Removes single or double quotes around the given string.
	 * 
	 * @param str
	 *            the input string, possibly with quotes
	 * @return the input string, with quotes removed
	 */
	private static String removeQuotes(String str) {
		String ret = str;
		if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
			ret = str.substring(1, str.length() - 1);
		}
		return ret;
	}

	private final Map<String, String> cache = new HashMap<>();

	public abstract OWLOntology createOntology();

	private String extractShortForm(String id) {
		IRI iri = IRI.create(id);
		String shortForm = getShortForm(getOWLDataFactory().getOWLClass(iri));
		if (shortForm == null) {
			shortForm = getShortForm(getOWLDataFactory().getOWLObjectProperty(iri));
		}
		if (shortForm == null) {
			return iri.getShortForm();
		} else {
			return removeQuotes(shortForm);
		}
	}

	public abstract Set<OWLOntology> getOntologies();

	public abstract String getShortForm(OWLEntity entity);

	@Override
	public String getShortForm(String id) {
		String shortForm = cache.get(id);
		if (shortForm == null) {
			shortForm = extractShortForm(id);
			cache.put(id, shortForm);
		} else {
		}
		return shortForm;
	}

	public abstract OWLDataFactory getOWLDataFactory();

	public abstract OWLOntology loadOntology(File file);

	@Override
	public void resetCache() {
		cache.clear();
	}

}
