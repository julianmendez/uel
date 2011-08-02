package de.tudresden.inf.lat.uel.ontmanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import de.tudresden.inf.lat.uel.main.Atom;
import de.tudresden.inf.lat.uel.main.Equation;

/**
 * An ontology is a set of definitions.
 * 
 * @author Barbara Morawska
 */
public class Ontology {

	private HashMap<String, Atom> atoms = new HashMap<String, Atom>();

	/**
	 * The flattened definitions stored by the defined concepts.
	 */
	private HashMap<String, Equation> definitions = new HashMap<String, Equation>();

	private HashMap<String, Equation> primitiveDefinitions = new HashMap<String, Equation>();

	public Ontology() {
	}

	public boolean containsAtom(String name) {
		return this.atoms.containsKey(name);
	}

	public boolean containsDefinition(String name) {
		return this.definitions.containsKey(name);
	}

	public boolean containsPrimitiveDefinition(String name) {
		return this.primitiveDefinitions.containsKey(name);
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Ontology) {
			Ontology other = (Ontology) o;
			ret = this.atoms.equals(other.atoms)
					&& this.definitions.equals(other.definitions)
					&& this.primitiveDefinitions
							.equals(other.primitiveDefinitions);
		}
		return ret;
	}

	public Atom getAtom(String concept) {
		return this.atoms.get(concept);
	}

	public Set<String> getAtomIds() {
		return Collections.unmodifiableSet(this.atoms.keySet());
	}

	public Equation getDefinition(String name) {
		return definitions.get(name);
	}

	public Set<String> getDefinitionIds() {
		return Collections.unmodifiableSet(this.definitions.keySet());
	}

	public Equation getPrimitiveDefinition(String name) {
		return primitiveDefinitions.get(name);
	}

	public Set<String> getPrimitiveDefinitionIds() {
		return Collections.unmodifiableSet(this.primitiveDefinitions.keySet());
	}

	@Override
	public int hashCode() {
		return this.atoms.hashCode();
	}

	public void merge(Ontology other) {
		this.atoms.putAll(other.atoms);
		this.definitions.putAll(other.definitions);
		this.primitiveDefinitions.putAll(other.primitiveDefinitions);
	}

	public void putAtom(String concept, Atom atom) {
		this.atoms.put(concept, atom);
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
		sbuf.append("Atoms ");
		sbuf.append(this.atoms.toString());
		sbuf.append("\n");
		sbuf.append("Definitions ");
		sbuf.append(this.definitions.toString());
		sbuf.append("\n");
		sbuf.append("Primitive definitions ");
		sbuf.append(this.primitiveDefinitions.toString());
		sbuf.append("\n");
		return sbuf.toString();
	}

}
