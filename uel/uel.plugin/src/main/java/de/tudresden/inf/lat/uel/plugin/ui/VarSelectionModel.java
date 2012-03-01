package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.lat.uel.plugin.processor.PluginGoal;
import de.tudresden.inf.lat.uel.plugin.type.SatAtom;

/**
 * 
 * @author Julian Mendez
 */
class VarSelectionModel {

	private final Map<String, String> idLabelMap;
	private final PluginGoal pluginGoal;
	private Set<String> setOfOriginalVariables = new TreeSet<String>();

	public VarSelectionModel(Set<String> originalVariables,
			Map<String, String> labels, PluginGoal g) {
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
		this.pluginGoal = g;
	}

	private Integer getAtomId(String atomName) {
		Integer ret = null;
		for (Integer currentAtomId : getGoal().getSatAtomManager().getIndices()) {
			SatAtom currentAtom = getGoal().getSatAtomManager().get(
					currentAtomId);
			if (currentAtom.getId().equals(atomName)) {
				ret = currentAtomId;
			}
		}
		return ret;
	}

	public Set<String> getConstants() {
		Set<String> ret = new HashSet<String>();
		for (Integer atomId : getGoal().getGoal().getConstants()) {
			ret.add(getGoal().getSatAtomManager().get(atomId).getId());
		}
		return Collections.unmodifiableSet(ret);
	}

	public PluginGoal getGoal() {
		return this.pluginGoal;
	}

	public String getLabel(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String ret = this.idLabelMap.get(id);

		if (ret == null) {
			if (id.endsWith(PluginGoal.UNDEF_SUFFIX)) {
				String origId = id.substring(0, id.length()
						- PluginGoal.UNDEF_SUFFIX.length());
				ret = this.idLabelMap.get(origId);
				if (ret != null) {
					ret += PluginGoal.UNDEF_SUFFIX;
				}
			}
		}

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

	public PluginGoal getPluginGoal() {
		return this.pluginGoal;
	}

	public Set<String> getVariables() {
		Set<String> ret = new HashSet<String>();
		for (Integer atomId : getGoal().getGoal().getVariables()) {
			SatAtom atom = getGoal().getSatAtomManager().get(atomId);
			if (atom.isConceptName() && atom.asConceptName().isUserVariable()) {
				ret.add(atom.getId());
			}
		}
		return Collections.unmodifiableSet(ret);
	}

	public void makeConstant(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Integer atomId = getAtomId(id);
		if (atomId == null) {
			throw new IllegalArgumentException("Unkown atom.");
		}
		this.pluginGoal.makeConstant(atomId);
	}

	public void makeVariable(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Integer atomId = getAtomId(id);
		if (atomId == null) {
			throw new IllegalArgumentException("Unkown atom.");
		}
		this.pluginGoal.makeVariable(atomId);
	}

}
