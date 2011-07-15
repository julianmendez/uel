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

	public static final char ORDER = 'o';
	public static final char SUBSUMPTION = 's';
	final private String first;
	private char kind;
	final private String second;
	private boolean value = false;

	/**
	 * Constructor. Constructs a literal given two names and the letter, which
	 * should indicate the kind of literal. It should be <code>s</code> for
	 * dis-subsumption or <code>o</code> for order literal.
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

	/**
	 * Returns the kind of literal.
	 * 
	 * @return the kind
	 */
	public char getKind() {

		return kind;

	}

	public String getSecond() {

		return second;
	}

	/**
	 * Returns the value of literal.
	 * 
	 * @return the value
	 */
	public boolean getValue() {

		return value;
	}

	/**
	 * Sets kind of the literal to s. If s is <code>s</code> then it is
	 * dis-subsumption literal, if s is <code>o</code> it is order literal.
	 * 
	 * @param s
	 */
	public void setKind(char s) {

		kind = s;

	}

	/**
	 * Sets the value <code>t</code> for the literal.
	 * 
	 * @param t
	 */
	public void setValue(boolean t) {

		value = t;
	}

	@Override
	public String toString() {

		if (kind == SUBSUMPTION) {

			StringBuilder str = new StringBuilder("(");

			str.append(first.toString());
			str.append(",");
			str.append(second.toString());
			str.append(")");

			return str.toString();

		} else if (kind == ORDER) {

			StringBuilder str = new StringBuilder("(");
			str.append(first.toString());
			str.append(">");
			str.append(second.toString());
			str.append(")");

			return str.toString();

		} else {

			throw new IllegalStateException(
					"Failed to create string from literal");
		}
	}

}
