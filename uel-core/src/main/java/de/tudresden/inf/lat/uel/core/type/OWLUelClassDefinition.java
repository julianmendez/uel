package de.tudresden.inf.lat.uel.core.type;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

public interface OWLUelClassDefinition {

	OWLEquivalentClassesAxiom asOWLEquivalentClassesAxiom();

	OWLClass getDefiniendum();

	OWLClassExpression getDefiniens();

}
