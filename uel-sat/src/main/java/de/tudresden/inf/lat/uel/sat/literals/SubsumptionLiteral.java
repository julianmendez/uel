package de.tudresden.inf.lat.uel.sat.literals;

/**
 * An object implementing this class is a subsumption.
 * 
 * @author Barbara Morawska
 */
public class SubsumptionLiteral implements Literal {

	private final Integer first;
	private final int hashCode;
	private final Integer second;

	/**
	 * Constructs a dissubsumption literal given two names.
	 * 
	 * @param one
	 *            first component
	 * @param two
	 *            second component
	 */
	public SubsumptionLiteral(Integer one, Integer two) {
		if (one == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (two == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		first = one;
		second = two;
		hashCode = one.hashCode() + 31 * two.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = (this == o);
		if (!ret && o instanceof SubsumptionLiteral) {
			SubsumptionLiteral other = (SubsumptionLiteral) o;
			ret = this.first.equals(other.first) && this.second.equals(other.second);
		}
		return ret;
	}

	@Override
	public Integer getFirst() {
		return first;
	}

	@Override
	public Integer getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean isSubsumption() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sbuf = new StringBuilder("(");
		sbuf.append(first);
		sbuf.append(" sub ");
		sbuf.append(second);
		sbuf.append(")");
		return sbuf.toString();
	}

}
