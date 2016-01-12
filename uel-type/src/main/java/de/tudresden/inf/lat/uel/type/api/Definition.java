/**
 * 
 */
package de.tudresden.inf.lat.uel.type.api;

import java.util.Collections;
import java.util.Set;

public class Definition extends Axiom {
	private boolean primitive;

	public Definition(Integer left, Set<Integer> right, boolean primitive) {
		super(Collections.singleton(left), right);
		this.primitive = primitive;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o) && primitive == ((Definition) o).primitive;
	}

	@Override
	public String getConnective() {
		return primitive ? "⊑" : "≡";
	}

	public Integer getDefiniendum() {
		return left.iterator().next();
	}

	public boolean isPrimitive() {
		return primitive;
	}
}