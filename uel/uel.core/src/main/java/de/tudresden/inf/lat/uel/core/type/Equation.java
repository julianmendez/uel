package de.tudresden.inf.lat.uel.core.type;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements an equation as two hash maps of atoms.
 * 
 * @author Barbara Morawska
 */
public class Equation {

	/**
	 * Left side of equation. Hash map with keys names of the atoms and values
	 * the atoms.
	 */
	private Map<String, Atom> left;

	private boolean primitive = false;

	/**
	 * Right side of equation Hash map with keys names of the atoms and values
	 * the atoms.
	 */
	private Map<String, Atom> right;

	/**
	 * Constructs an equation given two atoms.
	 * 
	 * @param leftAtom
	 *            atom on the left-hand side
	 * @param rightAtom
	 *            atom on the right-hand side
	 * @param prim
	 *            whether this is primitive
	 */
	public Equation(Atom leftAtom, Atom rightAtom, boolean prim) {
		left = new HashMap<String, Atom>();
		left.put(leftAtom.getName(), leftAtom);
		right = new HashMap<String, Atom>();
		right.put(rightAtom.getName(), rightAtom);
		primitive = prim;
	}

	/**
	 * Constructs an equation given two hash maps of atoms.
	 * 
	 * @param leftPart
	 *            hash map with keys names and values atoms
	 * @param rightPart
	 *            hash map with keys names and values atoms
	 * @param prim
	 *            whether this is primitive
	 */
	public Equation(Map<String, Atom> leftPart, Map<String, Atom> rightPart,
			boolean prim) {
		left = leftPart;
		right = rightPart;
		primitive = prim;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Equation) {
			Equation other = (Equation) o;
			ret = this.left.equals(other.left)
					&& this.right.equals(other.right)
					&& this.primitive == other.primitive;
		}
		return ret;
	}

	/**
	 * Returns the left side of the equation as the hash map of names and atoms.
	 * 
	 * @return the left side of the equation
	 */
	public Map<String, Atom> getLeft() {
		return left;
	}

	/**
	 * Returns the right side of the equation as the hash map of names and
	 * atoms.
	 * 
	 * @return the right side of the equation
	 */
	public Map<String, Atom> getRight() {
		return right;
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
		return primitive;
	}

	/**
	 * This method is defined for testing purposes only. It is used by printing
	 * method of Goal.
	 */
	public String printEquation() {
		StringBuffer sbuf = new StringBuffer();
		if (left != null && right != null) {
			if (primitive) {
				sbuf.append("(primitive) ");
			}
			sbuf.append("left side: ");
			for (String concept : left.keySet()) {
				sbuf.append(left.get(concept));
				sbuf.append(" | ");
			}
			sbuf.append("\n");
			sbuf.append("right side: ");
			for (String concept : right.keySet()) {
				sbuf.append(right.get(concept));
				sbuf.append(" | ");
			}
			sbuf.append("\n");
		} else {
			throw new RuntimeException("Error: equation is empty");
		}
		return sbuf.toString();
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(KRSSKeyword.open);
		if (primitive) {
			sbuf.append(KRSSKeyword.define_primitive_concept);
		} else {
			sbuf.append(KRSSKeyword.define_concept);
		}
		sbuf.append(KRSSKeyword.space);
		sbuf.append(left.keySet().iterator().next());
		if (right.keySet().size() > 1) {
			sbuf.append(KRSSKeyword.space);
			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			for (String concept : right.keySet()) {
				sbuf.append(KRSSKeyword.space);
				sbuf.append(right.get(concept));
			}
			sbuf.append(KRSSKeyword.close);
		} else if (right.keySet().size() == 1) {
			sbuf.append(KRSSKeyword.space);
			sbuf.append(right.get(right.keySet().iterator().next()));
		}
		sbuf.append(KRSSKeyword.close);
		return sbuf.toString();
	}

}
