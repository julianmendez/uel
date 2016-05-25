/**
 * 
 */
package de.tudresden.inf.lat.uel.sat.literals;

import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.IndexedSet;

/**
 * This class represents a choice between different options to be encoded in a
 * SAT instance. Subclasses can implement this behavior using different
 * encodings.
 * 
 * @author Stefan Borgwardt
 */
public abstract class Choice {

	/**
	 * The literal manager.
	 */
	protected IndexedSet<Literal> literalManager;

	/**
	 * The number of choices supported by this Choice object.
	 */
	protected int numberOfChoices;

	/**
	 * Create a new choice with a given number of choices.
	 * 
	 * @param literalManager
	 *            the literal manager
	 * @param numberOfChoices
	 *            the required number of choices
	 */
	protected Choice(IndexedSet<Literal> literalManager, int numberOfChoices) {
		this.literalManager = literalManager;
		this.numberOfChoices = numberOfChoices;
	}

	/**
	 * Add the literals representing a specific choice to a previous choice.
	 * 
	 * @param previousChoiceLiterals
	 *            the literals representing the previous choice
	 * @param j
	 *            the number of the option to choose
	 * @return the combined set of literals
	 */
	public Set<Integer> addChoiceLiterals(Set<Integer> previousChoiceLiterals, int j) {
		Set<Integer> literals = getChoiceLiterals(j);
		literals.addAll(previousChoiceLiterals);
		return literals;
	}

	/**
	 * Get the literals representing a specific choice.
	 * 
	 * @param j
	 *            the number of the option to choose (between '0' and
	 *            'numberOfChoices - 1').
	 * @return the set of literals representing the choice
	 */
	public abstract Set<Integer> getChoiceLiterals(int j);

	/**
	 * Create a fresh literal to be used in encoding a choice.
	 * 
	 * @return the literal id of the new literal
	 */
	protected Integer getFreshChoiceLiteral() {
		return literalManager.addAndGetIndex(new ChoiceLiteral());
	}

}
