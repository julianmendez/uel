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
	 * Returns the set of flattened definitions.
	 * 
	 * @return the set of definitions
	 */
	public Set<Equation> getDefinitions();

	/**
	 * Returns the set of flattened goal equations.
	 * 
	 * @return the set of goal equations
	 */
	public Set<Equation> getGoalEquations();
	
	/**
	 * Returns the set of all flattened equations (definitions and goal).
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
