/**
 * 
 */
package de.tudresden.inf.lat.uel.sat.literals;

/**
 * This class represents literals used for choices in a SAT encoding. Different
 * choice literals are distinguished simply by an integer id.
 * 
 * @author Stefan Borgwardt
 */
public class ChoiceLiteral implements Literal {

	private static int choiceLiteralCount = 0;

	private final int index;

	/**
	 * Create a new choice literal with a fresh id.
	 */
	public ChoiceLiteral() {
		this.index = choiceLiteralCount;
		choiceLiteralCount++;
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
