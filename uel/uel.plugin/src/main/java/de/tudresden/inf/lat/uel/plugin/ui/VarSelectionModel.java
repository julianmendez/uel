package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

class VarSelectionModel {

	private Map<String, String> idLabelMap = new HashMap<String, String>();
	private Set<String> setOfConstants = new TreeSet<String>();
	private Set<String> setOfVariables = new TreeSet<String>();

	public VarSelectionModel(Set<String> allElements, Map<String, String> labels) {
		if (allElements == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (labels == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.setOfConstants.addAll(allElements);
		this.idLabelMap = labels;
	}

	public Set<String> getConstants() {
		return Collections.unmodifiableSet(this.setOfConstants);
	}

	public String getLabel(String id) {
		String ret = this.idLabelMap.get(id);
		if (ret == null) {
			ret = id;
		}
		return ret;
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
