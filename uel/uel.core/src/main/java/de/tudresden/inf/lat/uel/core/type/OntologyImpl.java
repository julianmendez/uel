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
	private Map<Integer, Equation> definitions = new HashMap<Integer, Equation>();

	private Map<Integer, Equation> primitiveDefinitions = new HashMap<Integer, Equation>();

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
	public boolean containsDefinition(Integer id) {
		return this.definitions.containsKey(id);
	}

	@Override
	public boolean containsPrimitiveDefinition(Integer id) {
		return this.primitiveDefinitions.containsKey(id);
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
	public Equation getDefinition(Integer id) {
		return definitions.get(id);
	}

	@Override
	public Set<Integer> getDefinitionIds() {
		return Collections.unmodifiableSet(this.definitions.keySet());
	}

	@Override
	public Equation getPrimitiveDefinition(Integer id) {
		return primitiveDefinitions.get(id);
	}

	public Set<Integer> getPrimitiveDefinitionIds() {
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

	public void putDefinition(Integer name, Equation equation) {
		this.definitions.put(name, equation);
	}

	public void putPrimitiveDefinition(Integer name, Equation equation) {
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
