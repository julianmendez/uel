package de.tudresden.inf.lat.uel.main;

/**
 * This class implements dis-subsumptions between atoms in the goal and order
 * literals between variables in the goal.
 * 
 * A literal is defined by first and second argument (strings) kind
 * (dis-subsumption or order literal). A literal has a value which by default is
 * false, which in the case of subsumptions means that the subsumption is false,
 * hence the dis-subsumption is true. A valuation obtained from a SAT solver by
 * Unifier can change this value.
 * 
 * Used in Translator.
 * 
 * 
 * 
 * @author Barbara Morawska
 * 
 */
public class Literal {

	private char kind;
	final private String first;
	final private String second;
	private boolean value = false;

	/**
	 * Constructor. Constructs a literal given two names.
	 * 
	 * Not used by Unifier (it does not indicate what kind of literal is
	 * created).
	 * 
	 * @param one
	 * @param two
	 */
	public Literal(String one, String two) {

		first = one;
		second = two;

	}

	/**
	 * Constructor. Constructs a literal given two names and the letter, which
	 * should indicate the kind of literal. It should be <s> for dis-subsumption
	 * or <o> for order literal.
	 * 
	 * @param one
	 * @param two
	 * @param letter
	 */
	public Literal(String one, String two, char letter) {

		first = one;
		second = two;
		kind = letter;

	}

	public String getFirst() {

		return first;

	}

	public String getSecond() {

		return second;
	}

	/**
	 * Sets kind of the literal to s. If s is <s> then it is dis-subsumption
	 * literal, if s is <o> it is order literal.
	 * 
	 * @param s
	 */
	public void setKind(char s) {

		kind = s;

	}

	/**
	 * Returns the kind of literal.
	 * 
	 * @return
	 */
	public char getKind() {

		return kind;

	}

	/**
	 * Returns the value of literal.
	 * 
	 * @return
	 */
	public boolean getValue() {

		return value;
	}

	/**
	 * Sets the value <t> for the literal.
	 * 
	 * @param t
	 */
	public void setValue(boolean t) {

		value = t;
	}

	@Override
	public String toString() {

		if (kind == 's') {

			StringBuilder str = new StringBuilder("(");

			str.append(first.toString());
			str.append(",");
			str.append(second.toString());
			str.append(")");

			return str.toString();

		} else if (kind == 'o') {

			StringBuilder str = new StringBuilder("(");
			str.append(first.toString());
			str.append(">");
			str.append(second.toString());
			str.append(")");

			return str.toString();

		} else {

			System.out.println("Failed to create string from literal");
			return "";
		}

	}
}
