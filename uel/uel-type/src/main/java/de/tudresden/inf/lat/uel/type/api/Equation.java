package de.tudresden.inf.lat.uel.type.api;

import java.util.Set;

/**
 * An object implementing this interface is an equation. An equation has an
 * identifier on the left-hand side and a set of identifiers on the right-hand
 * side. The meaning is X = A<sub>1</sub> \u2293 &hellip; \u2293 A<sub>n</sub>
 * where X , A<sub>1</sub>, &hellip; A<sub>n</sub> are atoms.
 * 
 * @author Julian Mendez
 */
public interface Equation {

	/**
	 * Returns the left-hand side of the equation, which is an atom identifier.
	 * 
	 * @return the left-hand side of the equation
	 */
	Integer getLeft();

	/**
	 * Returns the right-hand side of the equation as a set of atom identifiers.
	 * 
	 * @return the right-hand side of the equation
	 */
	Set<Integer> getRight();

	/**
	 * Tells whether this equation is primitive.
	 * 
	 * @return <code>true</code> if and only if this equation if primitive
	 */
	boolean isPrimitive();

}
