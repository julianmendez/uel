package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.Set;
import java.util.TreeSet;

class VarSelectionModel {

	private Set<String> constants = new TreeSet<String>();
	private Set<String> variables = new TreeSet<String>();

	public VarSelectionModel() {
	}

	public Set<String> getConstants() {
		return this.constants;
	}

	public Set<String> getVariables() {
		return this.variables;
	}

}
