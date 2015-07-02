package de.tudresden.inf.lat.uel.sat.type;

public class AuxiliaryLiteral implements Literal {

	private Integer c, x, d;
	private int hashCode = 0;

	public AuxiliaryLiteral(Integer c, Integer x, Integer d) {
		if ((c == null) || (x == null) || (d == null)) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.c = c;
		this.x = x;
		this.d = d;
		hashCode = c.hashCode() + 31 * x.hashCode() + 31 * 31 * d.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = (this == o);
		if (!ret && o instanceof AuxiliaryLiteral) {
			AuxiliaryLiteral other = (AuxiliaryLiteral) o;
			ret = this.c.equals(other.c) && this.x.equals(other.x)
					&& this.d.equals(other.d);
		}
		return ret;
	}

	@Override
	public Integer getFirst() {
		return c;
	}

	@Override
	public Integer getSecond() {
		return d;
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
		sbuf.append(c);
		sbuf.append(", ");
		sbuf.append(x);
		sbuf.append(", ");
		sbuf.append(d);
		sbuf.append("})");
		return sbuf.toString();
	}

}
