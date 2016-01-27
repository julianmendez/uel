package de.tudresden.inf.lat.uel.plugin.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.krss2.renderer.KRSS2OWLSyntaxRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLRenderer;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLRenderer;

/**
 * Utility methods for saving ontologies.
 * 
 * @author Julian Mendez
 * @author Stefan Borgwardt
 */
class OWLUtils {

	private static final String EXTENSION_KRSS = ".krss";
	private static final String EXTENSION_OWL = ".owl";
	private static final String EXTENSION_RDF = ".rdf";

	/**
	 * Render the given ontology as a string in KRSS2 format.
	 * 
	 * @param owlOntology
	 *            the input ontology
	 * @return the KRRS2 representation of the input ontology
	 * @throws OWLRendererException
	 *             if an error occurred during rendering
	 */
	private static String renderKRSS(OWLOntology owlOntology) throws OWLRendererException {
		StringWriter writer = new StringWriter();
		KRSS2OWLSyntaxRenderer renderer = new KRSS2OWLSyntaxRenderer();
		renderer.render(owlOntology, writer);
		writer.flush();
		return writer.toString();
	}

	/**
	 * Render the given ontology as a string in OWL XML format.
	 * 
	 * @param owlOntology
	 *            the input ontology
	 * @return the OWL XML representation of the input ontology
	 * @throws OWLRendererException
	 *             if an error occurred during rendering
	 */
	private static String renderOWL(OWLOntology owlOntology) throws OWLRendererException {
		StringWriter writer = new StringWriter();
		OWLXMLRenderer renderer = new OWLXMLRenderer();
		renderer.render(owlOntology, writer);
		writer.flush();
		return writer.toString();
	}

	/**
	 * Render the given ontology as a string in RDF XML format.
	 * 
	 * @param owlOntology
	 *            the input ontology
	 * @return the RDF XML representation of the input ontology
	 * @throws IOException
	 *             if an error occurred during rendering
	 */
	private static String renderRDF(OWLOntology owlOntology) throws IOException {
		StringWriter writer = new StringWriter();
		RDFXMLRenderer renderer = new RDFXMLRenderer(owlOntology, writer);
		renderer.render();
		writer.flush();
		return writer.toString();
	}

	/**
	 * Saves a set of axioms to an OWL ontology file.
	 * 
	 * @param axioms
	 *            a set of OWLAxioms
	 * @param file
	 *            the output destination
	 */
	public static void saveToOntologyFile(Set<OWLAxiom> axioms, File file) {
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.createOntology();
			manager.addAxioms(ontology, axioms);
			saveToOntologyFile(ontology, file);
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Saves an OWL ontology to a file.
	 * 
	 * @param owlOntology
	 *            an ontology
	 * @param file
	 *            the output destination
	 */
	public static void saveToOntologyFile(OWLOntology owlOntology, File file) {
		if (file != null) {
			try {
				String ontology = "";
				if (file.getName().endsWith(OWLUtils.EXTENSION_RDF)) {
					ontology = renderRDF(owlOntology);
				} else if (file.getName().endsWith(OWLUtils.EXTENSION_OWL)) {
					ontology = renderOWL(owlOntology);
				} else if (file.getName().endsWith(OWLUtils.EXTENSION_KRSS)) {
					ontology = renderKRSS(owlOntology);
				}

				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(ontology);
				writer.flush();
				writer.close();
			} catch (OWLRendererException | IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
