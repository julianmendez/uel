package de.tudresden.inf.lat.uel.type.impl;

import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;

/**
 * An object implementing this class is an equation.
 * 
 * @author Julian Mendez
 */
public class EquationImpl implements Equation {

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
	public EquationImpl(Integer leftAtom, Integer rightAtom, boolean prim) {
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
	public EquationImpl(Integer leftAtom, Set<Integer> rightPart, boolean prim) {
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
		boolean ret = (this == o);
		if (!ret && o instanceof EquationImpl) {
			EquationImpl other = (EquationImpl) o;
			ret = this.primitive == other.primitive
					&& this.left.equals(other.left)
					&& this.right.equals(other.right);
		}
		return ret;
	}

	@Override
	public Integer getLeft() {
		return this.left;
	}

	@Override
	public Set<Integer> getRight() {
		return this.right;
	}

	@Override
	public int hashCode() {
		return this.left.hashCode() + 31 * this.right.hashCode();
	}

	@Override
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
