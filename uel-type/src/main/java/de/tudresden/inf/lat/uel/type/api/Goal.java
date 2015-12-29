package de.tudresden.inf.lat.uel.type.api;

import java.util.Set;

/**
 * An object implementing this interface is an input for the UEL system.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public interface Goal {

	/**
	 * Returns the atom manager.
	 * 
	 * @return the atom manager
	 */
	AtomManager getAtomManager();

	/**
	 * Returns the set of flattened definitions.
	 * 
	 * @return the set of definitions
	 */
	Set<Definition> getDefinitions();

	/**
	 * Returns the set of flattened goal equations.
	 * 
	 * @return the set of goal equations
	 */
	Set<Equation> getEquations();

	/**
	 * Returns the set of flattened and small goal disequations.
	 * 
	 * @return the set of goal disequations
	 */
	Set<Disequation> getDisequations();

	/**
	 * Returns the set of all flattened equations (definitions and goal).
	 * 
	 * @return the set of equations
	 */
	Set<Subsumption> getSubsumptions();

	/**
	 * Returns the set of user variables.
	 * 
	 * @return the set of user variables
	 */
	Set<Dissubsumption> getDissubsumptions();

	boolean hasNegativePart();

}
