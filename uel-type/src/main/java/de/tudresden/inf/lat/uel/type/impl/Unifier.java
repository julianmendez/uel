package de.tudresden.inf.lat.uel.type.impl;

import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Definition;

public class Unifier {
	private final Set<Definition> definitions;
	private final Map<Integer, Integer> typeAssignment;

	public Unifier(Set<Definition> definitions) {
		this.definitions = definitions;
		this.typeAssignment = null;
	}

	public Unifier(Set<Definition> definitions, Map<Integer, Integer> typeAssignment) {
		this.definitions = definitions;
		this.typeAssignment = typeAssignment;
	}

	public Set<Definition> getDefinitions() {
		return definitions;
	}

	public Map<Integer, Integer> getTypeAssignment() {
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
