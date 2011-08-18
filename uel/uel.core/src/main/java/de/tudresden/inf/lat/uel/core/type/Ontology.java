package de.tudresden.inf.lat.uel.core.type;

import java.util.Set;

/**
 * This interface models an ontology that has only definitions and primitive
 * definitions.
 * 
 * @author Julian Mendez
 */
public interface Ontology {

	/**
	 * Tells whether this ontology contains the specified definition.
	 * 
	 * @param name
	 *            definition identifier
	 * @return <code>true</code> if and only if this ontology contains the
	 *         specified definition
	 */
	public boolean containsDefinition(String name);

	/**
	 * Tells whether this ontology contains the specified primitive definition.
	 * 
	 * @param name
	 *            primitive definition identifier
	 * @return <code>true</code> if and only if this ontology contains the
	 *         specified primitive definition
	 */
	public boolean containsPrimitiveDefinition(String name);

	/**
	 * Returns the definition for a given concept name.
	 * 
	 * @param name
	 *            concept name
	 * @return the definitions for a given concept name
	 */
	public Equation getDefinition(String name);

	/**
	 * Returns all the concept names on the left-hand side of primitive and
	 * non-primitive definitions.
	 * 
	 * @return all the concept names on the left-hand side of primitive and
	 *         non-primitive definitions
	 */
	public Set<String> getDefinitionIds();

	/**
	 * Returns the symbols in definitions of a specified concept name.
	 * 
	 * @param name
	 *            concept name
	 * @return the symbols in definitions of a specified concept name
	 */
	public Set<String> getDefinitionSymbols(String name);

	/**
	 * Returns the primitive definition for a given concept name.
	 * 
	 * @param name
	 *            concept name
	 * @return the definitions for a given concept name
	 */
	public Equation getPrimitiveDefinition(String name);

	/**
	 * Returns the symbols in primitive definitions of a specified concept name.
	 * 
	 * @param name
	 *            concept name
	 * @return the symbols in primitive definitions of a specified concept name
	 */
	public Set<String> getPrimitiveDefinitionSymbols(String name);

}
