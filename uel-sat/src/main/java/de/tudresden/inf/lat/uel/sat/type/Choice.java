/**
 * 
 */
package de.tudresden.inf.lat.uel.sat.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.sat.solver.SatInput;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;

/**
 * @author Stefan Borgwardt
 *
 */
public class Choice {

	private static int choiceLiteralCount = 0;

	private Integer[] choiceLiterals;
	private IndexedSet<Literal> literalManager;
	private int log;
	private int numberOfChoices;

	public Choice(IndexedSet<Literal> literalManager, int numberOfChoices) {
		this.literalManager = literalManager;
		this.numberOfChoices = numberOfChoices;
		this.log = (int) Math.ceil(Math.log(numberOfChoices) / Math.log(2));
		choiceLiterals = new Integer[log];
		for (int i = 0; i < log; i++) {
			choiceLiterals[i] = getFreshChoiceLiteral();
		}
	}

	public Set<Integer> addChoiceLiterals(Set<Integer> previousChoiceLiterals, int j) {
		Set<Integer> literals = getChoiceLiterals(j);
		literals.addAll(previousChoiceLiterals);
		return literals;
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

	private Integer getFreshChoiceLiteral() {
		Literal literal = new ChoiceLiteral(choiceLiteralCount);
		choiceLiteralCount++;
		return literalManager.addAndGetIndex(literal);
	}

	public void ruleOutOtherChoices(SatInput input) {
		for (int j = numberOfChoices; j < Math.pow(2, log); j++) {
			input.add(addChoiceLiterals(Collections.<Integer> emptySet(), j));
		}
	}

}
