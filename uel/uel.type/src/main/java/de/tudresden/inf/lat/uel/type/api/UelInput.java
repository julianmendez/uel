package de.tudresden.inf.lat.uel.type.api;

import java.util.Set;

/**
 * An object implementing this interface is an input for the UEL system.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public interface UelInput {

	/**
	 * Returns the atom manager.
	 * 
	 * @return the atom manager
	 */
	public IndexedSet<Atom> getAtomManager();

	/**
	 * Returns the set of flattened equations.
	 * 
	 * @return the set of equations
	 */
	public Set<Equation> getEquations();

	/**
	 * Returns the set of user variables.
	 * 
	 * @return the set of user variables
	 */
	public Set<Integer> getUserVariables();

}
