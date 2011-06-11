package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.Set;

import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * This class connects with UEL.
 * 
 * @author Julian Mendez
 */
public class UelProcessor {

	private OWLWorkspace owlWorkspace = null;

	public UelProcessor(OWLWorkspace workspace) {
		if (workspace == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.owlWorkspace = workspace;
	}

	public OWLClassExpression compute(Set<OWLClass> input) {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		OWLClassExpression ret = null;
		if (input.size() > 0) {
			ret = getOWLWorkspace().getOWLModelManager().getActiveOntology()
					.getOWLOntologyManager().getOWLDataFactory()
					.getOWLObjectUnionOf(input);
		} else {
			ret = getOWLWorkspace().getOWLModelManager().getActiveOntology()
					.getOWLOntologyManager().getOWLDataFactory().getOWLThing();
		}
		
		// FIXME this method does not execute the computation
	
		return ret;
	}

	public OWLWorkspace getOWLWorkspace() {
		return this.owlWorkspace;
	}

}
