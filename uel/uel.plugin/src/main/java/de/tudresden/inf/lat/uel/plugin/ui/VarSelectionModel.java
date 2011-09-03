package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.lat.uel.core.type.Atom;
import de.tudresden.inf.lat.uel.core.type.Goal;

/**
 * 
 * @author Julian Mendez
 */
class VarSelectionModel {

	private Goal goal = null;
	private Map<String, String> idLabelMap = new HashMap<String, String>();
	private Set<String> setOfOriginalVariables = new TreeSet<String>();

	public VarSelectionModel(Set<String> originalVariables,
			Map<String, String> labels, Goal g) {
		if (originalVariables == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (labels == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (g == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.setOfOriginalVariables.addAll(originalVariables);
		this.idLabelMap = labels;
		this.goal = g;
	}

	public Set<String> getConstants() {
		Set<String> ret = new HashSet<String>();
		for (Integer atomId : this.goal.getConstants()) {
			ret.add(this.goal.getAtomManager().get(atomId).getId());
		}
		return Collections.unmodifiableSet(ret);
	}

	public Goal getGoal() {
		return this.goal;
	}

	public String getLabel(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String ret = this.idLabelMap.get(id);
		if (ret == null) {
			int p = id.indexOf("#");
			if (p != -1) {
				ret = id.substring(p + 1);
			} else {
				ret = id;
			}
		}
		return ret;
	}

	public Set<String> getOriginalVariables() {
		return Collections.unmodifiableSet(this.setOfOriginalVariables);
	}

	public Set<String> getVariables() {
		Set<String> ret = new HashSet<String>();
		for (Integer atomId : getGoal().getVariables()) {
			Atom atom = getGoal().getAtomManager().get(atomId);
			if (atom.isUserVariable()) {
				ret.add(atom.getId());
			}
		}
		return Collections.unmodifiableSet(ret);
	}

	public void makeConstant(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.goal.makeConstant(id);
	}

	public void makeVariable(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.goal.makeVariable(id);
	}

}
