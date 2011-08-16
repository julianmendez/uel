package de.tudresden.inf.lat.uel.core.type;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements equation as two hash maps of atoms. It is used by Goal
 * and FAtom.
 * 
 * @author Barbara Morawska
 */
public class Equation {

	/**
	 * Left side of equation. Hash map with keys names of the atoms and values
	 * the atoms.
	 */
	private Map<String, Atom> left;
	/**
	 * Right side of equation Hash map with keys names of the atoms and values
	 * the atoms.
	 */
	private Map<String, Atom> right;

	/**
	 * Constructor initializes two hash maps for the left and right side of
	 * equation.
	 */
	public Equation() {
		left = new HashMap<String, Atom>();
		right = new HashMap<String, Atom>();
	}

	/**
	 * Constructor defines an equation given two hash maps of atoms.
	 * 
	 * @param list1
	 *            hash map with keys names and values atoms
	 * @param list2
	 *            hash map with keys names and values atoms
	 */
	public Equation(Map<String, Atom> list1, Map<String, Atom> list2) {
		left = list1;
		right = list2;
	}

	public void addToLeft(Atom a) {
		this.left.put(a.getName(), a);
	}

	public void addToRight(Atom a) {
		this.right.put(a.getName(), a);
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Equation) {
			Equation other = (Equation) o;
			ret = this.left.equals(other.left)
					&& this.right.equals(other.right);
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
	 * This method is defined for testing purposes only. It is used by printing
	 * method of Goal.
	 */
	public String printEquation() {

		StringBuffer sbuf = new StringBuffer();
		if (left != null && right != null) {

			sbuf.append("From Equation: left side is -- ");

			for (String concept : left.keySet()) {

				sbuf.append(left.get(concept));
				sbuf.append(" | ");
			}

			sbuf.append("\n");
			sbuf.append("From Equation: right side is -- ");

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

	/**
	 * Sets the left side of the equation to the hash map <code>list</code> with
	 * keys names and values atoms.
	 * 
	 * Not used in UEL.
	 * 
	 * @param list
	 */
	public void setLeft(Map<String, Atom> list) {

		left = list;
	}

	/**
	 * Sets the right side of the equation to the hash map <code>list</code>
	 * with keys names and values atoms.
	 * 
	 * Used in FAtom
	 * 
	 * @param list
	 */
	public void setRight(Map<String, Atom> list) {

		right = list;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(KRSSKeyword.open);
		sbuf.append(KRSSKeyword.define_concept);
		sbuf.append(KRSSKeyword.blank);
		sbuf.append(left.keySet().iterator().next());
		if (right.keySet().size() > 1) {
			sbuf.append(KRSSKeyword.blank);
			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			for (String concept : right.keySet()) {
				sbuf.append(KRSSKeyword.blank);
				sbuf.append(right.get(concept));
			}
			sbuf.append(KRSSKeyword.close);
		} else if (right.keySet().size() == 1) {
			sbuf.append(KRSSKeyword.blank);
			sbuf.append(right.get(right.keySet().iterator().next()));
		}
		sbuf.append(KRSSKeyword.close);
		return sbuf.toString();
	}

}
