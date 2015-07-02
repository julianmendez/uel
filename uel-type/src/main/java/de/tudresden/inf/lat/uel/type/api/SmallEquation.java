package de.tudresden.inf.lat.uel.type.api;

/**
 * This interface represents a small equation between two atoms.
 * 
 * @author Stefan Borgwardt
 * 
 */
public interface SmallEquation {

	/**
	 * Returns the left-hand side of the equation, which is an atom identifier.
	 * 
	 * @return the left-hand side of the equation
	 */
	Integer getLeft();

	/**
	 * Returns the right-hand side of the equation, which is an atom identifier.
	 * 
	 * @return the right-hand side of the equation
	 */
	Integer getRight();

}
