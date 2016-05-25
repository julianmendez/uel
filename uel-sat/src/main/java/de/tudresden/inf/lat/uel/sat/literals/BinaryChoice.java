/**
 * 
 */
package de.tudresden.inf.lat.uel.sat.literals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.sat.type.SatInput;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;

/**
 * This class represents a choice in a SAT instance using a binary encoding ('ld
 * n' literals for 'n' options).
 * 
 * @author Stefan Borgwardt
 */
public class BinaryChoice extends Choice {

	private Integer[] choiceLiterals;
	private int log;

	/**
	 * Create a new binary choice.
	 * 
	 * @param input
	 *            the SAT input
	 * @param literalManager
	 *            the literal manager
	 * @param numberOfChoices
	 *            the required number of choices
	 */
	public BinaryChoice(SatInput input, IndexedSet<Literal> literalManager, int numberOfChoices) {
		super(literalManager, numberOfChoices);
		this.log = (int) Math.ceil(Math.log(numberOfChoices) / Math.log(2));
		choiceLiterals = new Integer[log];
		for (int i = 0; i < log; i++) {
			choiceLiterals[i] = getFreshChoiceLiteral();
		}

		// TODO do this w.r.t. to any previous choice literals?
		// rule out unused choices
		for (int j = numberOfChoices; j < Math.pow(2, log); j++) {
			// TODO negate choice literals?
			input.add(addChoiceLiterals(Collections.<Integer> emptySet(), j));
		}
	}

	public Set<Integer> getChoiceLiterals(int j) {
		Set<Integer> literals = new HashSet<Integer>();
		for (int i = 0; i < log; i++) {
			int digitMask = (int) Math.pow(2, i);
			boolean digitIsOne = (j & digitMask) == digitMask;
			Integer choiceLiteralId = digitIsOne ? choiceLiterals[i] : ((-1) * choiceLiterals[i]);
			literals.add(choiceLiteralId);
		}
		return literals;
	}

}
