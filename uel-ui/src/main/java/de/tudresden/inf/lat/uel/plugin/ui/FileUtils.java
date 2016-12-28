package de.tudresden.inf.lat.uel.plugin.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
class FileUtils {

	private static final String EXTENSION_KRSS = ".krss";
	private static final String EXTENSION_OWL = ".owl";
	private static final String EXTENSION_RDF = ".rdf";
	private static final String EXTENSION_TXT = ".txt";

	/**
	 * Test whether a file has the extension of a text file.
	 * 
	 * @param file
	 *            the file description
	 * @return true iff the file has the extension '.txt'
	 */
	public static boolean isTextFile(File file) {
		return file.getName().endsWith(EXTENSION_TXT);
	}

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
		PrintWriter writer = new PrintWriter(new StringWriter());
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
		PrintWriter writer = new PrintWriter(new StringWriter());
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
		PrintWriter writer = new PrintWriter(new StringWriter());
		RDFXMLRenderer renderer = new RDFXMLRenderer(owlOntology, writer);
		renderer.render();
		writer.flush();
		return writer.toString();
	}

	/**
	 * Saves a set of axioms to an OWL ontology file.
	 * 
	 * @param file
	 *            the output destination
	 * @param axioms
	 *            a set of OWLAxioms
	 */
	public static void saveToFile(File file, Set<OWLAxiom> axioms) {
		OWLOntology ontology = toOWLOntology(axioms);
		saveToFile(file, ontology);
	}

	/**
	 * Converts a set of OWLAxioms to an OWLOntology.
	 * 
	 * @param axioms
	 *            the input axioms
	 * @return a new ontology containing the axioms
	 */
	public static OWLOntology toOWLOntology(Set<OWLAxiom> axioms) {
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.createOntology();
			manager.addAxioms(ontology, axioms);
			return ontology;
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Saves an OWL ontology to a file.
	 * 
	 * @param file
	 *            the output destination
	 * @param owlOntology
	 *            an ontology
	 */
	public static void saveToFile(File file, OWLOntology owlOntology) {
		try {
			String ontology = "";
			if (file.getName().endsWith(EXTENSION_RDF)) {
				ontology = renderRDF(owlOntology);
			} else if (file.getName().endsWith(EXTENSION_OWL)) {
				ontology = renderOWL(owlOntology);
			} else if (file.getName().endsWith(EXTENSION_KRSS)) {
				ontology = renderKRSS(owlOntology);
			} else {
				throw new RuntimeException("Unexpected file extension: " + file.getName());
			}
			saveToFile(file, ontology);
		} catch (OWLRendererException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Saves a string to a file.
	 * 
	 * @param file
	 *            the output destination
	 * @param content
	 *            the file content
	 */
	public static void saveToFile(File file, String content) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
