package de.tudresden.inf.lat.uel.core.type;

/**
 * An object implementing this class is a subsumption.
 * 
 * @author Barbara Morawska
 */
public class SubsumptionLiteral implements Literal {

	private final String first;
	private int hashCode = 0;
	private String id = null;
	private final String second;

	/**
	 * Constructs a dissubsumption literal given two names.
	 * 
	 * @param one
	 *            first component
	 * @param two
	 *            second component
	 */
	public SubsumptionLiteral(String one, String two) {
		first = one;
		second = two;
		hashCode = one.hashCode() + 31 * two.hashCode();
		updateId();
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof SubsumptionLiteral) {
			SubsumptionLiteral other = (SubsumptionLiteral) o;
			ret = this.first.equals(other.first)
					&& this.second.equals(other.second);
		}
		return ret;
	}

	@Override
	public String getFirst() {
		return first;
	}

	@Override
	public String getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean isDissubsumption() {
		return false;
	}

	@Override
	public boolean isOrder() {
		return false;
	}

	@Override
	public boolean isSubsumption() {
		return true;
	}

	@Override
	public String toString() {
		return this.id;
	}

	private void updateId() {
		StringBuilder str = new StringBuilder("(");
		str.append(first);
		str.append(",");
		str.append(second);
		str.append(")");
		id = str.toString();
	}

}
