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

	private Map<String, Equation> definitionCache = new HashMap<String, Equation>();
	private Map<OWLClass, OWLClassExpression> definitions = new HashMap<OWLClass, OWLClassExpression>();
	private Map<String, OWLClass> nameMap = new HashMap<String, OWLClass>();
	private OntologyBuilder ontologyBuilder = null;
	private Map<String, Equation> primitiveDefinitionCache = new HashMap<String, Equation>();
	private Map<OWLClass, Set<OWLClassExpression>> primitiveDefinitions = new HashMap<OWLClass, Set<OWLClassExpression>>();

	/**
	 * Constructs a new dynamic ontology.
	 */
	public DynamicOntology(OntologyBuilder builder) {
		if (builder == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.ontologyBuilder = builder;
	}

	/**
	 * Clears this ontology.
	 */
	public void clear() {
		this.nameMap.clear();
		this.definitions.clear();
		this.definitionCache.clear();
		this.primitiveDefinitions.clear();
		this.primitiveDefinitionCache.clear();
	}

	@Override
	public boolean containsDefinition(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		boolean ret = true;
		Equation eq = this.definitionCache.get(name);
		if (eq == null) {
			OWLClass cls = this.nameMap.get(name);
			ret = this.definitions.get(cls) != null;
		}
		return ret;
	}

	@Override
	public boolean containsPrimitiveDefinition(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		boolean ret = true;
		Equation eq = this.primitiveDefinitionCache.get(name);
		if (eq == null) {
			OWLClass cls = this.nameMap.get(name);
			ret = this.primitiveDefinitions.get(cls) != null;
		}
		return ret;
	}

	@Override
	public Equation getDefinition(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Equation ret = this.definitionCache.get(name);
		if (ret == null) {
			OWLClass cls = this.nameMap.get(name);
			if (cls != null) {
				OWLClassExpression clExpr = this.definitions.get(cls);
				if (clExpr != null) {
					updateCache(getOntologyBuilder().processDefinition(cls,
							clExpr));
					ret = this.definitionCache.get(name);
				}
			}
		}

		return ret;
	}

	@Override
	public Set<String> getDefinitionIds() {
		return Collections.unmodifiableSet(nameMap.keySet());
	}

	public OntologyBuilder getOntologyBuilder() {
		return this.ontologyBuilder;
	}

	@Override
	public Equation getPrimitiveDefinition(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Equation ret = this.primitiveDefinitionCache.get(name);
		if (ret == null) {
			OWLClass cls = this.nameMap.get(name);
			if (cls != null) {
				Set<OWLClassExpression> clExprSet = this.primitiveDefinitions
						.get(cls);
				if (clExprSet != null) {
					updateCache(getOntologyBuilder()
							.processPrimitiveDefinition(cls, clExprSet));
					ret = this.primitiveDefinitionCache.get(name);
				}
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

		this.definitions.putAll(getOntologyBuilder()
				.getDefinitions(owlOntology));
		this.primitiveDefinitions.putAll(getOntologyBuilder()
				.getPrimitiveDefinitions(owlOntology));
		Set<OWLClass> toVisit = new HashSet<OWLClass>();
		toVisit.addAll(this.definitions.keySet());
		toVisit.addAll(this.primitiveDefinitions.keySet());
		this.nameMap.putAll(getOntologyBuilder().processNames(toVisit));
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

	private void updateCache(Set<Equation> equations) {
		for (Equation equation : equations) {
			String name = this.ontologyBuilder.getAtomManager()
					.get(equation.getLeft()).getId();
			if (equation.isPrimitive()) {
				this.primitiveDefinitionCache.put(name, equation);
			} else {
				this.definitionCache.put(name, equation);
			}
		}
	}

}
