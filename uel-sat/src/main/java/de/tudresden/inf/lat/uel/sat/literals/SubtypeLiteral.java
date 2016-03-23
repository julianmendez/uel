/**
 * 
 */
package de.tudresden.inf.lat.uel.sat.literals;

/**
 * @author Stefan Borgwardt
 *
 */
public class SubtypeLiteral implements Literal {

	private final Integer atomId;
	private final int hashCode;
	private final Integer type;

	public SubtypeLiteral(Integer atomId, Integer type) {
		if (atomId == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (type == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.atomId = atomId;
		this.type = type;
		hashCode = atomId.hashCode() + 31 * type.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = (this == o);
		if (!ret && o instanceof SubtypeLiteral) {
			SubtypeLiteral other = (SubtypeLiteral) o;
			ret = this.atomId.equals(other.atomId) && this.type.equals(other.type);
		}
		return ret;
	}

	@Override
	public Integer getFirst() {
		return atomId;
	}

	@Override
	public Integer getSecond() {
		return type;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean isSubsumption() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sbuf = new StringBuilder("(");
		sbuf.append(atomId);
		sbuf.append(" ≤ ");
		sbuf.append(type);
		sbuf.append(")");
		return sbuf.toString();
	}

}
