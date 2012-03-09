package de.tudresden.inf.lat.uel.type.api;

/**
 * Represents a flat EL-atom, which can be a concept name or an existential
 * restriction.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public interface Atom {

	/**
	 * Retrieve the concept name of this flat atom.
	 * 
	 * @return the concept name
	 */
	public Integer getConceptNameId();

	/**
	 * Tells whether this flat atom is a concept name.
	 * 
	 * @return <code>true</code> if and only if this atom has an associated role
	 *         name
	 */
	public boolean isConceptName();

	/**
	 * Tells whether this flat atom is a constant.
	 * 
	 * @return <code>true</code> if and only if this atom is not an existential
	 *         restriction and is ground
	 */
	public boolean isConstant();

	/**
	 * Tells whether this flat atom is an existential restriction.
	 * 
	 * @return <code>true</code> if and only if this atom has an associated role
	 *         name
	 */
	public boolean isExistentialRestriction();

	/**
	 * Check whether this flat atom is ground.
	 * 
	 * @return <code>true</code> if and only if the concept name is not a
	 *         variable
	 */
	public boolean isGround();

	/**
	 * Check whether this flat atom is a variable.
	 * 
	 * @return <code>true</code> if and only if this atom is not an existential
	 *         restriction and is not ground
	 */
	public boolean isVariable();

}
