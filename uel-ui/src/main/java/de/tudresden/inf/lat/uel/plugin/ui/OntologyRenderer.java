package de.tudresden.inf.lat.uel.plugin.ui;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.krss2.renderer.KRSS2OWLSyntaxRenderer;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLRenderer;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLRenderer;

// for OWL API 4.0.2

//import org.semanticweb.owlapi.krss2.renderer.KRSS2OWLSyntaxRenderer;
//import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxRenderer;
//import org.semanticweb.owlapi.owlxml.renderer.OWLXMLRenderer;
//import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLRenderer;

/**
 * An object of this class can read OWL ontologies from strings and also write
 * these ontologies as strings.
 * 
 * @author Julian Mendez
 */
public class OntologyRenderer {

	public static final String EXTENSION_KRSS = ".krss";
	public static final String EXTENSION_OWL = ".owl";
	public static final String EXTENSION_RDF = ".rdf";

	public OntologyRenderer() {
	}

	public static OWLOntology parseOntology(String ontology) {
		try {
			OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
			ByteArrayInputStream input = new ByteArrayInputStream(ontology.getBytes());
			return ontologyManager.loadOntologyFromOntologyDocument(input);
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e);
		}
	}

	private static String renderKRSS(OWLOntology owlOntology) throws OWLRendererException {
		StringWriter writer = new StringWriter();
		KRSS2OWLSyntaxRenderer renderer = new KRSS2OWLSyntaxRenderer();
		renderer.render(owlOntology, writer);
		writer.flush();
		return writer.toString();
	}

	private static String renderOWL(OWLOntology owlOntology) throws OWLRendererException {
		StringWriter writer = new StringWriter();
		OWLXMLRenderer renderer = new OWLXMLRenderer();
		renderer.render(owlOntology, writer);
		writer.flush();
		return writer.toString();
	}

	private static String renderRDF(OWLOntology owlOntology) throws IOException {
		StringWriter writer = new StringWriter();
		RDFXMLRenderer renderer = new RDFXMLRenderer(owlOntology, writer);
		renderer.render();
		writer.flush();
		return writer.toString();
	}

	public static void saveToOntologyFile(String ontology, File file) {
		saveToOntologyFile(parseOntology(ontology), file);
	}

	public static void saveToOntologyFile(OWLOntology owlOntology, File file) {
		if (file != null) {
			try {
				String ontology = "";
				if (file.getName().endsWith(OntologyRenderer.EXTENSION_RDF)) {
					ontology = renderRDF(owlOntology);
				} else if (file.getName().endsWith(OntologyRenderer.EXTENSION_OWL)) {
					ontology = renderOWL(owlOntology);
				} else if (file.getName().endsWith(OntologyRenderer.EXTENSION_KRSS)) {
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
