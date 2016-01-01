package de.tudresden.inf.lat.uel.type.api;

import de.tudresden.inf.lat.uel.type.impl.ConceptName;

/**
 * Represents a flat EL-atom, which can be a concept name or an existential
 * restriction over a concept name (possibly the 'top concept name').
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
	ConceptName getConceptName();

	/**
	 * Tells whether this flat atom is a concept name.
	 * 
	 * @return <code>true</code> if and only if this atom has an associated role
	 *         name
	 */
	boolean isConceptName();

	/**
	 * Tells whether this flat atom is a constant.
	 * 
	 * @return <code>true</code> if and only if this atom is not an existential
	 *         restriction and is ground
	 */
	boolean isConstant();

	/**
	 * Tells whether this flat atom is an existential restriction.
	 * 
	 * @return <code>true</code> if and only if this atom has an associated role
	 *         name
	 */
	boolean isExistentialRestriction();

	/**
	 * Check whether this flat atom is ground.
	 * 
	 * @return <code>true</code> if and only if the concept name is not a
	 *         variable
	 */
	boolean isGround();

	/**
	 * Check whether this flat atom is a variable.
	 * 
	 * @return <code>true</code> if and only if this atom is not an existential
	 *         restriction and is not ground
	 */
	boolean isVariable();
}
