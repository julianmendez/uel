package de.tudresden.inf.lat.uel.type.impl;

import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Definition;

public class Unifier {
	private final Set<Definition> definitions;

	public Unifier(Set<Definition> definitions) {
		this.definitions = definitions;
	}

	public Set<Definition> getDefinitions() {
		return definitions;
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
