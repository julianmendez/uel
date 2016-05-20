/**
 * 
 */
package de.tudresden.inf.lat.uel.sat.literals;

import java.util.Set;

import de.tudresden.inf.lat.uel.sat.type.SatInput;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;

/**
 * @author Stefan Borgwardt
 *
 */
public abstract class Choice {

	protected IndexedSet<Literal> literalManager;
	protected int numberOfChoices;

	protected Choice(IndexedSet<Literal> literalManager, int numberOfChoices) {
		this.literalManager = literalManager;
		this.numberOfChoices = numberOfChoices;
	}

	public Set<Integer> addChoiceLiterals(Set<Integer> previousChoiceLiterals, int j) {
		Set<Integer> literals = getChoiceLiterals(j);
		literals.addAll(previousChoiceLiterals);
		return literals;
	}

	public abstract Set<Integer> getChoiceLiterals(int j);

	protected Integer getFreshChoiceLiteral() {
		return literalManager.addAndGetIndex(new ChoiceLiteral());
	}

}
