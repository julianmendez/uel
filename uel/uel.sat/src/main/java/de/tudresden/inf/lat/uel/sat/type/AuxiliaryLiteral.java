package de.tudresden.inf.lat.uel.sat.type;

public class AuxiliaryLiteral implements Literal {

	private Integer d, x, e;
	private int hashCode = 0;

	public AuxiliaryLiteral(Integer d, Integer x, Integer e) {
		if ((d == null) || (x == null) || (e == null)) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.d = d;
		this.x = x;
		this.e = e;
		hashCode = d.hashCode() + 31 * x.hashCode() + 31 * 31 * e.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = (this == o);
		if (!ret && o instanceof AuxiliaryLiteral) {
			AuxiliaryLiteral other = (AuxiliaryLiteral) o;
			ret = this.d.equals(other.d) && this.x.equals(other.x)
					&& this.e.equals(other.e);
		}
		return ret;
	}

	@Override
	public Integer getFirst() {
		return d;
	}

	@Override
	public Integer getSecond() {
		return e;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean isAuxiliary() {
		return true;
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
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sbuf = new StringBuilder("(p_{");
		sbuf.append(x);
		sbuf.append(", ");
		sbuf.append(d);
		sbuf.append(", ");
		sbuf.append(e);
		sbuf.append("})");
		return sbuf.toString();
	}

}
