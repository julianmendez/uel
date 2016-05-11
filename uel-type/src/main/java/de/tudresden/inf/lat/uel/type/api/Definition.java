/**
 * 
 */
package de.tudresden.inf.lat.uel.type.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Definition extends Axiom {
	private boolean primitive;

	public Definition(Set<Integer> left, Set<Integer> right) {
		super(left, right);
		if (left.size() != 1) {
			throw new IllegalArgumentException("The left-hand side of a definition must contain exactly one atom.");
		}
		this.primitive = false;
	}

	public Definition(Integer left, Set<Integer> right, boolean primitive) {
		super(Collections.singleton(left), right);
		this.primitive = primitive;
	}

	public Definition(Definition orig) {
		super(Collections.singleton(orig.getDefiniendum()), new HashSet<Integer>(orig.getRight()));
		this.primitive = orig.primitive;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o) && primitive == ((Definition) o).primitive;
	}

	@Override
	public String getConnective() {
		return primitive ? Subsumption.CONNECTIVE : Equation.CONNECTIVE;
	}

	public Integer getDefiniendum() {
		return left.iterator().next();
	}

	public Set<Integer> getDefiniens() {
		return right;
	}

	public boolean isPrimitive() {
		return primitive;
	}
}