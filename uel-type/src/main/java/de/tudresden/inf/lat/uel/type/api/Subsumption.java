/**
 * 
 */
package de.tudresden.inf.lat.uel.type.api;

import java.util.Set;

public class Subsumption extends Axiom {
	public Subsumption(Set<Integer> left, Set<Integer> right) {
		super(left, right);
	}

	@Override
	public String getConnective() {
		return "⊑";
	}
}