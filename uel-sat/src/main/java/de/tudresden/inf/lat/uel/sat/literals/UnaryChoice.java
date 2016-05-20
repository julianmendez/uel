/**
 * 
 */
package de.tudresden.inf.lat.uel.sat.literals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.tudresden.inf.lat.uel.sat.type.SatInput;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;

/**
 * @author Stefan Borgwardt
 *
 */
public class UnaryChoice extends Choice {

	private Integer[] choiceLiterals;

	public UnaryChoice(SatInput input, Set<Integer> previousChoiceLiterals, IndexedSet<Literal> literalManager,
			int numberOfChoices) {
		super(literalManager, numberOfChoices);
		choiceLiterals = new Integer[numberOfChoices];
		for (int i = 0; i < numberOfChoices; i++) {
			choiceLiterals[i] = getFreshChoiceLiteral();
		}

		// at least one of the choiceLiterals must be true
		input.add(Stream.concat(previousChoiceLiterals.stream(), Arrays.stream(choiceLiterals))
				.collect(Collectors.toSet()));
	}

	@Override
	public Set<Integer> getChoiceLiterals(int j) {
		Set<Integer> literal = new HashSet<Integer>();
		literal.add((-1) * choiceLiterals[j]);
		return literal;
	}

}
