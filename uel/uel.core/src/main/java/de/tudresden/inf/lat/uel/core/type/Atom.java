package de.tudresden.inf.lat.uel.core.type;

/**
 * An atom is either a concept name or an existential restriction.
 * 
 * @author Julian Mendez
 */
public interface Atom {

	/**
	 * Returns the atom as a concept name.
	 * 
	 * @throw ClassCastException if the atom is not a concept name
	 * @return the atom as a concept name
	 */
	public ConceptName asConceptName();

	/**
	 * Returns the atom as an existential restriction.
	 * 
	 * @throw ClassCastException if the atom is not an existential restriction
	 * @return the atom as an existential restriction
	 */
	public ExistentialRestriction asExistentialRestriction();

	/**
	 * Returns a string representation of the atom.
	 * 
	 * @return a string representation of the atom
	 */
	public String getId();

	/**
	 * Returns name of the atom.
	 * 
	 * @return name of the atom
	 */
	public String getName();

	/**
	 * Is true if the atom is an existential restriction.
	 * 
	 * @return <code>true</code> if and only if the atom is a concept name
	 */
	public boolean isConceptName();

	/**
	 * Is true if the atom is an existential restriction.
	 * 
	 * @return <code>true</code> if and only if the atom is an existential
	 *         restriction
	 */
	public boolean isExistentialRestriction();

}
