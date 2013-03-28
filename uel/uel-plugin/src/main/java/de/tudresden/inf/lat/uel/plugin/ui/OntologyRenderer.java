package de.tudresden.inf.lat.uel.plugin.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.coode.owlapi.owlxml.renderer.OWLXMLRenderer;
import org.coode.owlapi.rdf.rdfxml.RDFXMLRenderer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.uulm.ecs.ai.owlapi.krssrenderer.KRSS2OWLSyntaxRenderer;

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

	public OWLOntology parseKRSS(String krss)
			throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = OWLManager
				.createOWLOntologyManager();
		ByteArrayInputStream input = new ByteArrayInputStream(krss.getBytes());
		return ontologyManager.loadOntologyFromOntologyDocument(input);
	}

	public String renderKRSS(OWLOntology owlOntology)
			throws OWLRendererException {
		StringWriter writer = new StringWriter();
		KRSS2OWLSyntaxRenderer renderer = new KRSS2OWLSyntaxRenderer(
				owlOntology.getOWLOntologyManager());
		renderer.render(owlOntology, writer);
		writer.flush();
		return writer.toString();
	}

	public String renderOWL(OWLOntology owlOntology)
			throws OWLRendererException {
		StringWriter writer = new StringWriter();
		OWLXMLRenderer renderer = new OWLXMLRenderer(
				owlOntology.getOWLOntologyManager());
		renderer.render(owlOntology, writer);
		writer.flush();
		return writer.toString();
	}

	public String renderRDF(OWLOntology owlOntology) throws IOException {
		StringWriter writer = new StringWriter();
		RDFXMLRenderer renderer = new RDFXMLRenderer(
				owlOntology.getOWLOntologyManager(), owlOntology, writer);
		renderer.render();
		writer.flush();
		return writer.toString();
	}

}
