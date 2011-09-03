package de.tudresden.inf.lat.uel.core.type;

/**
 * An object implementing this class is a dis-subsumption.
 * 
 * @author Barbara Morawska
 */
public class DissubsumptionLiteral implements Literal {

	private final Integer first;
	private int hashCode = 0;
	private final Integer second;

	/**
	 * Constructs a dissubsumption literal given two names.
	 * 
	 * @param one
	 *            first component
	 * @param two
	 *            second component
	 */
	public DissubsumptionLiteral(Integer one, Integer two) {
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
		boolean ret = false;
		if (o instanceof DissubsumptionLiteral) {
			DissubsumptionLiteral other = (DissubsumptionLiteral) o;
			ret = this.first.equals(other.first)
					&& this.second.equals(other.second);
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
	public boolean isDissubsumption() {
		return true;
	}

	@Override
	public boolean isOrder() {
		return false;
	}

	@Override
	public boolean isSubsumption() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sbuf = new StringBuilder("(");
		sbuf.append(first);
		sbuf.append(" dissub ");
		sbuf.append(second);
		sbuf.append(")");
		return sbuf.toString();
	}

}
