package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

class VarSelectionModel {

	private Set<String> setOfConstants = new TreeSet<String>();
	private Set<String> setOfVariables = new TreeSet<String>();

	public VarSelectionModel(Set<String> allElements) {
		if (allElements == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.setOfConstants.addAll(allElements);
	}

	public Set<String> getConstants() {
		return Collections.unmodifiableSet(this.setOfConstants);
	}

	public Set<String> getVariables() {
		return Collections.unmodifiableSet(this.setOfVariables);
	}

	public boolean makeConstant(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		boolean ret = false;
		if (this.setOfVariables.contains(id)) {
			this.setOfVariables.remove(id);
			ret = this.setOfConstants.add(id);
		}
		return ret;
	}

	public boolean makeVariable(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		boolean ret = false;
		if (this.setOfConstants.contains(id)) {
			this.setOfConstants.remove(id);
			ret = this.setOfVariables.add(id);
		}
		return ret;
	}

}
