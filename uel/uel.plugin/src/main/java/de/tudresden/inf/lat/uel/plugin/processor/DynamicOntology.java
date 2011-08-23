package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.core.type.Equation;
import de.tudresden.inf.lat.uel.core.type.Ontology;

/**
 * An object of this class is a UEL ontology that reuses a previously build OWL
 * ontology.
 * 
 * @author Julian Mendez
 */
public class DynamicOntology implements Ontology {

	private Map<OWLClass, OWLClassExpression> definitions = new HashMap<OWLClass, OWLClassExpression>();
	private Map<String, OWLClass> nameMap = new HashMap<String, OWLClass>();
	private Map<OWLClass, Set<OWLClassExpression>> primitiveDefinitions = new HashMap<OWLClass, Set<OWLClassExpression>>();

	/**
	 * Constructs a new dynamic ontology.
	 */
	public DynamicOntology() {
	}

	/**
	 * Clears this ontology.
	 */
	public void clear() {
		this.nameMap.clear();
		this.definitions.clear();
		this.primitiveDefinitions.clear();
	}

	@Override
	public boolean containsDefinition(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		OWLClass cls = this.nameMap.get(name);
		return this.definitions.get(cls) != null;
	}

	@Override
	public boolean containsPrimitiveDefinition(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		OWLClass cls = this.nameMap.get(name);
		return this.primitiveDefinitions.get(cls) != null;
	}

	@Override
	public Equation getDefinition(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		OntologyBuilder adapter = new OntologyBuilder();
		Equation ret = null;
		OWLClass cls = this.nameMap.get(name);
		if (cls != null) {
			OWLClassExpression clExpr = this.definitions.get(cls);
			if (clExpr != null) {
				ret = adapter.processDefinition(cls, clExpr);
			}
		}
		return ret;
	}

	@Override
	public Set<String> getDefinitionIds() {
		return Collections.unmodifiableSet(nameMap.keySet());
	}

	@Override
	public Equation getPrimitiveDefinition(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Equation ret = null;
		OntologyBuilder adapter = new OntologyBuilder();
		OWLClass cls = this.nameMap.get(name);
		if (cls != null) {
			Set<OWLClassExpression> clExprSet = this.primitiveDefinitions
					.get(cls);
			if (clExprSet != null) {
				ret = adapter.processPrimitiveDefinition(cls, clExprSet);
			}
		}
		return ret;
	}

	/**
	 * Loads an OWL ontology.
	 * 
	 * @param owlOntology
	 *            OWL ontology
	 */
	public void load(OWLOntology owlOntology) {
		if (owlOntology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		OntologyBuilder adapter = new OntologyBuilder();
		this.definitions.putAll(adapter.getDefinitions(owlOntology));
		this.primitiveDefinitions.putAll(adapter
				.getPrimitiveDefinitions(owlOntology));
		Set<OWLClass> toVisit = new HashSet<OWLClass>();
		toVisit.addAll(this.definitions.keySet());
		toVisit.addAll(this.primitiveDefinitions.keySet());
		this.nameMap.putAll(adapter.processNames(toVisit));
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Definitions ");
		sbuf.append(this.definitions.toString());
		sbuf.append("\n");
		sbuf.append("Primitive definitions ");
		sbuf.append(this.primitiveDefinitions.toString());
		sbuf.append("\n");
		return sbuf.toString();
	}

}
