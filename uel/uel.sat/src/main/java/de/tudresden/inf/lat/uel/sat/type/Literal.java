package de.tudresden.inf.lat.uel.sat.type;

/**
 * A literal is defined by two strings and it can be a dis-subsumption or an
 * order literal. A literal has a value which by default is false, which in the
 * case of subsumptions means that the subsumption is false, hence the
 * dis-subsumption is true. A valuation obtained from a SAT solver can change
 * this value.
 * 
 * @author Barbara Morawska
 */
public interface Literal {

	/**
	 * Returns the first component.
	 * 
	 * @return the first component
	 */
	public Integer getFirst();

	/**
	 * Returns the second component.
	 * 
	 * @return the second component
	 */
	public Integer getSecond();

	/**
	 * Tells whether this literal is a dissubsumption literal.
	 * 
	 * @return <code>true</code> if and only if this is an dissubsumption
	 *         literal
	 */
	public boolean isDissubsumption();

	/**
	 * Tells whether this literal is an order literal.
	 * 
	 * @return <code>true</code> if and only if this is an order literal
	 */
	public boolean isOrder();

	/**
	 * Tells whether this literal is a subsumption literal.
	 * 
	 * @return <code>true</code> if and only if this is an subsumption literal
	 */
	public boolean isSubsumption();

}
