package de.tudresden.inf.lat.uel.core.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An object of this class is a set of definitions and primitive definitions.
 * 
 * @author Barbara Morawska
 */
public class OntologyImpl implements Ontology {

	/**
	 * The flattened definitions stored by the defined concepts.
	 */
	private Map<String, Equation> definitions = new HashMap<String, Equation>();

	private Map<String, Equation> primitiveDefinitions = new HashMap<String, Equation>();

	/**
	 * Constructs a new empty ontology.
	 */
	public OntologyImpl() {
	}

	/**
	 * Clears the ontology.
	 */
	public void clear() {
		this.definitions.clear();
		this.primitiveDefinitions.clear();
	}

	@Override
	public boolean containsDefinition(String name) {
		return this.definitions.containsKey(name);
	}

	@Override
	public boolean containsPrimitiveDefinition(String name) {
		return this.primitiveDefinitions.containsKey(name);
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof OntologyImpl) {
			OntologyImpl other = (OntologyImpl) o;
			ret = this.definitions.equals(other.definitions)
					&& this.primitiveDefinitions
							.equals(other.primitiveDefinitions);
		}
		return ret;
	}

	@Override
	public Equation getDefinition(String name) {
		return definitions.get(name);
	}

	@Override
	public Set<String> getDefinitionIds() {
		return Collections.unmodifiableSet(this.definitions.keySet());
	}

	@Override
	public Equation getPrimitiveDefinition(String name) {
		return primitiveDefinitions.get(name);
	}

	public Set<String> getPrimitiveDefinitionIds() {
		return Collections.unmodifiableSet(this.primitiveDefinitions.keySet());
	}

	@Override
	public int hashCode() {
		return this.definitions.hashCode();
	}

	public void merge(OntologyImpl other) {
		this.definitions.putAll(other.definitions);
		this.primitiveDefinitions.putAll(other.primitiveDefinitions);
	}

	public void putDefinition(String name, Equation equation) {
		this.definitions.put(name, equation);
	}

	public void putPrimitiveDefinition(String name, Equation equation) {
		this.primitiveDefinitions.put(name, equation);
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
