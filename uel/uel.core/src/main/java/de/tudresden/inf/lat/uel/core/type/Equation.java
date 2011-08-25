package de.tudresden.inf.lat.uel.core.type;

import java.util.HashSet;
import java.util.Set;

/**
 * An object implementing this class is an equation.
 * 
 * @author Julian Mendez
 */
public class Equation {

	private Integer left = null;
	private boolean primitive = false;
	private Set<Integer> right = null;

	/**
	 * Constructs a new equation given two atoms.
	 * 
	 * @param leftAtom
	 *            an atom identifier
	 * @param rightAtom
	 *            an atom identifier
	 * @param prim
	 *            whether this is a primitive equation
	 */
	public Equation(Integer leftAtom, Integer rightAtom, boolean prim) {
		if (leftAtom == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (rightAtom == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.left = leftAtom;
		this.right = new HashSet<Integer>();
		this.right.add(rightAtom);
		this.primitive = prim;
	}

	/**
	 * Constructs a new equation.
	 * 
	 * @param leftAtom
	 *            an atom identifier
	 * @param rightPart
	 *            a conjunction of atoms identifiers
	 * @param prim
	 *            whether this is a primitive equation
	 */
	public Equation(Integer leftAtom, Set<Integer> rightPart, boolean prim) {
		if (leftAtom == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (rightPart == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.left = leftAtom;
		this.right = rightPart;
		this.primitive = prim;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Equation) {
			Equation other = (Equation) o;
			ret = this.primitive == other.primitive
					&& this.left.equals(other.left)
					&& this.right.equals(other.right);
		}
		return ret;
	}

	/**
	 * Returns the left-hand side of the equation, which is an atom identifier.
	 * 
	 * @return the left-hand side of the equation
	 */
	public Integer getLeft() {
		return this.left;
	}

	/**
	 * Returns the right-hand side of the equation as a set of atom identifiers.
	 * 
	 * @return the right-hand side of the equation
	 */
	public Set<Integer> getRight() {
		return this.right;
	}

	@Override
	public int hashCode() {
		return this.left.hashCode() + 31 * this.right.hashCode();
	}

	/**
	 * Tells whether this equation is primitive.
	 * 
	 * @return <code>true</code> if and only if this equation if primitive
	 */
	public boolean isPrimitive() {
		return this.primitive;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(KRSSKeyword.open);
		if (isPrimitive()) {
			sbuf.append(KRSSKeyword.define_primitive_concept);
		} else {
			sbuf.append(KRSSKeyword.define_concept);
		}
		sbuf.append(KRSSKeyword.space);
		sbuf.append(getLeft());
		if (getRight().size() > 1) {
			sbuf.append(KRSSKeyword.space);
			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			for (Integer conceptId : getRight()) {
				sbuf.append(KRSSKeyword.space);
				sbuf.append(conceptId);
			}
			sbuf.append(KRSSKeyword.close);
		} else if (getRight().size() == 1) {
			sbuf.append(KRSSKeyword.space);
			sbuf.append(getRight().iterator().next());
		}
		sbuf.append(KRSSKeyword.close);
		return sbuf.toString();
	}

}
