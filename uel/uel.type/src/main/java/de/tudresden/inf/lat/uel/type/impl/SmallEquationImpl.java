package de.tudresden.inf.lat.uel.type.impl;

import de.tudresden.inf.lat.uel.type.api.SmallEquation;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;

/**
 * An object implementing this class is a small equation.
 * 
 * @author Stefan Borgwardt
 */
public class SmallEquationImpl implements SmallEquation {

	private Integer left = null;
	private Integer right = null;

	/**
	 * Constructs a new small equation.
	 * 
	 * @param leftAtom
	 *            an atom identifier
	 * @param rightAtom
	 *            an atom identifier
	 */
	public SmallEquationImpl(Integer leftAtom, Integer rightAtom) {
		if (leftAtom == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (rightAtom == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.left = leftAtom;
		this.right = rightAtom;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = (this == o);
		if (!ret && o instanceof SmallEquationImpl) {
			SmallEquationImpl other = (SmallEquationImpl) o;
			ret = this.left.equals(other.left)
					&& this.right.equals(other.right);
		}
		return ret;
	}

	@Override
	public Integer getLeft() {
		return this.left;
	}

	@Override
	public Integer getRight() {
		return this.right;
	}

	@Override
	public int hashCode() {
		return this.left.hashCode() + 31 * this.right.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(KRSSKeyword.open);
		sbuf.append(KRSSKeyword.define_concept);
		sbuf.append(KRSSKeyword.space);
		sbuf.append(getLeft());
		sbuf.append(KRSSKeyword.space);
		sbuf.append(getRight());
		sbuf.append(KRSSKeyword.close);
		return sbuf.toString();
	}

}
