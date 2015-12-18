package de.tudresden.inf.lat.uel.core.type;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

public class OWLUelClassDefinitionImpl implements OWLUelClassDefinition {

	private final OWLEquivalentClassesAxiom axiom;
	private final OWLClass definiendum;
	private final OWLClassExpression definiens;

	public OWLUelClassDefinitionImpl(OWLClass definiendum, OWLClassExpression definiens) {
		this.definiendum = definiendum;
		this.definiens = definiens;
		this.axiom = OWLManager.getOWLDataFactory().getOWLEquivalentClassesAxiom(definiendum, definiens);
	}

	public OWLEquivalentClassesAxiom asOWLEquivalentClassesAxiom() {
		return this.axiom;
	}

	public OWLClass getDefiniendum() {
		return this.definiendum;
	}

	public OWLClassExpression getDefiniens() {
		return this.definiens;
	}
}
