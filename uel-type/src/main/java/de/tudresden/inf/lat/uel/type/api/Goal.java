package de.tudresden.inf.lat.uel.type.api;

import java.util.Map;
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

	Set<Integer> getTypes();

	Integer getDirectSupertype(Integer type);

	default boolean subtypeOrEquals(Integer type1, Integer type2) {
		while (type1 != null) {
			if (type1.equals(type2)) {
				return true;
			}
			type1 = getDirectSupertype(type1);
		}
		return false;
	}

	default boolean areDisjoint(Integer type1, Integer type2) {
		return !subtypeOrEquals(type1, type2) && !subtypeOrEquals(type2, type1);
	}

	Map<Integer, Set<Integer>> getDomains();

	Map<Integer, Set<Integer>> getRanges();

	Set<Integer> getTransparentRoles();

	boolean hasNegativePart();

}
