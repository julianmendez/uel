package de.tudresden.inf.lat.uel.type.api;

import java.util.Set;

/**
 * An object implementing this interface is an output of the UEL system, i.e., a
 * unifier.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */

public interface UelOutput {

	/**
	 * Returns the set of equations.
	 * 
	 * @return the set of equations
	 */
	Set<Equation> getEquations();

}
