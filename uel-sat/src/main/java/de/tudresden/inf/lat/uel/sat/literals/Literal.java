package de.tudresden.inf.lat.uel.sat.literals;

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
	Integer getFirst();

	/**
	 * Returns the second component.
	 * 
	 * @return the second component
	 */
	Integer getSecond();

	/**
	 * Tells whether this literal is a subsumption literal.
	 * 
	 * @return <code>true</code> if and only if this is a subsumption literal
	 */
	boolean isSubsumption();

}
