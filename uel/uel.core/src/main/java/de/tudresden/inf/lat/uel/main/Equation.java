package de.tudresden.inf.lat.uel.main;

import java.io.PrintWriter;
import java.util.HashMap;

/**
 * This class implements equation as two hash maps of atoms. It is used by Goal
 * and FAtom.
 * 
 * @author Barbara Morawska
 * 
 */
public class Equation {

	/**
	 * Left side of equation. Hash map with keys names of the atoms and values
	 * the atoms.
	 */
	private HashMap<String, Atom> left;
	/**
	 * Right side of equation Hash map with keys names of the atoms and values
	 * the atoms.
	 */
	private HashMap<String, Atom> right;

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
	public Equation(HashMap<String, Atom> list1, HashMap<String, Atom> list2) {

		left = list1;
		right = list2;
	}

	/**
	 * Returns the left side of the equation as the hash map of names and atoms.
	 * 
	 * @return the left side of the equation
	 */
	public HashMap<String, Atom> getLeft() {

		return left;
	}

	/**
	 * Returns the right side of the equation as the hash map of names and
	 * atoms.
	 * 
	 * @return the right side of the equation
	 */
	public HashMap<String, Atom> getRight() {

		return right;
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
	 * Prints an equation to Print Writer <code>out</code>. Used in Goal
	 * initialization, in creating TBox file.
	 * 
	 * 
	 * @param out
	 */
	public void printFDefinition(PrintWriter out) {

		if (left.size() == 1 && right != null) {

			for (String key : left.keySet()) {

				FAtom a = (FAtom) left.get(key);
				if (a.isVar()) {
					out.print("(");
					out.print("DEFINE-CONCEPT ");
					out.print(a.toString());
					out.print(" ");

				} else {
					System.out.println("Cannot find a defined concept");
				}

			}

			if (right.size() == 1) {

				for (String key : right.keySet()) {

					FAtom a = (FAtom) right.get(key);

					out.print(a.toString());

				}

			} else {

				out.print(" (AND ");

				for (String key : right.keySet()) {

					FAtom a = (FAtom) right.get(key);

					out.print(a.toString());

					out.print(" ");
				}

				out.print(") ");

			}
			out.print(" ) ");
			out.println("");
		} else {

			System.out.println("Error: equation is empty");

		}

	}

	/**
	 * Sets the left side of the equation to the hash map <code>list</code> with
	 * keys names and values atoms.
	 * 
	 * Not used in UEL.
	 * 
	 * @param list
	 */
	public void setLeft(HashMap<String, Atom> list) {

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
	public void setRight(HashMap<String, Atom> list) {

		right = list;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("(define-concept ");
		sbuf.append(left.keySet().iterator().next());
		if (right.keySet().size() > 1) {
			sbuf.append(" (and");
			for (String concept : right.keySet()) {
				sbuf.append(" ");
				sbuf.append(right.get(concept));
			}
			sbuf.append(")");
		} else if (right.keySet().size() == 1) {
			sbuf.append(" ");
			sbuf.append(right.get(right.keySet().iterator().next()));
		}
		sbuf.append(")");
		return sbuf.toString();
	}

}
