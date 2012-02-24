package de.tudresden.inf.lat.uel.type.api;

import java.util.Set;

/**
 * An object implementing this interface is an equation.
 * 
 * @author Julian Mendez
 */
public interface Equation {

	/**
	 * Returns the left-hand side of the equation, which is an atom identifier.
	 * 
	 * @return the left-hand side of the equation
	 */
	public Integer getLeft();

	/**
	 * Returns the right-hand side of the equation as a set of atom identifiers.
	 * 
	 * @return the right-hand side of the equation
	 */
	public Set<Integer> getRight();

	/**
	 * Tells whether this equation is primitive.
	 * 
	 * @return <code>true</code> if and only if this equation if primitive
	 */
	public boolean isPrimitive();

}
