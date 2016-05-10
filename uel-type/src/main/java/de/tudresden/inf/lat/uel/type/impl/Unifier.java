package de.tudresden.inf.lat.uel.type.impl;

import java.util.Map;
import java.util.Set;

public class Unifier {
	private final DefinitionSet definitions;
	private final Map<Integer, Set<Integer>> typeAssignment;

	public Unifier(DefinitionSet definitions) {
		this.definitions = definitions;
		this.typeAssignment = null;
	}

	public Unifier(DefinitionSet definitions, Map<Integer, Set<Integer>> typeAssignment) {
		this.definitions = definitions;
		this.typeAssignment = typeAssignment;
	}

	public DefinitionSet getDefinitions() {
		return definitions;
	}

	public Map<Integer, Set<Integer>> getTypeAssignment() {
		return typeAssignment;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || !o.getClass().equals(this.getClass())) {
			return false;
		}
		return definitions.equals(((Unifier) o).definitions);
	}

	@Override
	public int hashCode() {
		return definitions.hashCode();
	}
}
