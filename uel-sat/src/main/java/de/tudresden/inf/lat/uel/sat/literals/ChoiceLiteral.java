/**
 * 
 */
package de.tudresden.inf.lat.uel.sat.literals;

/**
 * @author Stefan Borgwardt
 *
 */
public class ChoiceLiteral implements Literal {

	private final int index;

	public ChoiceLiteral(int index) {
		this.index = index;
	}

	@Override
	public Integer getFirst() {
		return null;
	}

	@Override
	public Integer getSecond() {
		return null;
	}

	@Override
	public boolean isSubsumption() {
		return false;
	}

	@Override
	public int hashCode() {
		return index;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = (this == o);
		if (!ret && o instanceof ChoiceLiteral) {
			ChoiceLiteral other = (ChoiceLiteral) o;
			ret = this.index == other.index;
		}
		return ret;
	}

}
