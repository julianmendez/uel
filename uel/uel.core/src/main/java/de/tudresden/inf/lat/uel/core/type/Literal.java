package de.tudresden.inf.lat.uel.core.type;

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

	private static final char ORDER = 'o';
	private static final char SUBSUMPTION = 's';

	public static Literal newOrder(String one, String two) {
		return new Literal(one, two, ORDER);
	}

	public static Literal newSubsumption(String one, String two) {
		return new Literal(one, two, SUBSUMPTION);
	}

	private final String first;
	private char kind;
	private final String second;
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
	private Literal(String one, String two, char letter) {
		first = one;
		second = two;
		kind = letter;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Literal) {
			Literal other = (Literal) o;
			ret = this.value == other.value && this.kind == other.kind
					&& this.first.equals(other.first)
					&& this.second.equals(other.second);
		}
		return ret;
	}

	public String getFirst() {
		return first;

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

	@Override
	public int hashCode() {
		return this.first.hashCode() + 31 * this.second.hashCode();
	}

	public boolean isOrder() {
		return this.kind == ORDER;
	}

	public boolean isSubsumption() {
		return this.kind == SUBSUMPTION;
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

		if (isSubsumption()) {

			StringBuilder str = new StringBuilder("(");

			str.append(first.toString());
			str.append(",");
			str.append(second.toString());
			str.append(")");

			return str.toString();

		} else if (isOrder()) {

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
