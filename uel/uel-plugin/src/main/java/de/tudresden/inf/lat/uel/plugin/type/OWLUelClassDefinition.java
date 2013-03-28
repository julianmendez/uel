package de.tudresden.inf.lat.uel.plugin.type;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

public interface OWLUelClassDefinition {

	public OWLEquivalentClassesAxiom asOWLEquivalentClassesAxiom();

	public OWLClass getDefiniendum();

	public OWLClassExpression getDefiniens();

}
