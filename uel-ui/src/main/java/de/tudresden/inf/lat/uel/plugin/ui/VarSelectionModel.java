package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.tudresden.inf.lat.uel.core.processor.PluginGoal;
import de.tudresden.inf.lat.uel.core.type.AtomManager;

/**
 * An object of this class can manage which concept names are considered by the
 * user as variables and which ones as constants.
 * 
 * @author Julian Mendez
 */
class VarSelectionModel {

	private final Map<String, String> idLabelMap;
	private final PluginGoal pluginGoal;

	public VarSelectionModel(Map<String, String> labels, PluginGoal g) {
		if (labels == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (g == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.idLabelMap = labels;
		this.pluginGoal = g;
	}

	public List<LabelId> getConstants() {
		List<LabelId> ret = new ArrayList<LabelId>();
		for (Integer atomId : getPluginGoal().getConstants()) {
			Integer id = getPluginGoal().getAtomManager().getAtoms().get(atomId).getConceptNameId();
			ret.add(new LabelId(getLabel(id), id));
		}
		return Collections.unmodifiableList(ret);
	}

	public PluginGoal getPluginGoal() {
		return this.pluginGoal;
	}

	private String getLabel(Integer id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String name = getPluginGoal().getAtomManager().getConceptName(id);
		String ret = this.idLabelMap.get(name);

		if (ret == null) {
			if (name.endsWith(AtomManager.UNDEF_SUFFIX)) {
				String origId = name.substring(0, name.length() - AtomManager.UNDEF_SUFFIX.length());
				ret = this.idLabelMap.get(origId);
				if (ret != null) {
					ret += AtomManager.UNDEF_SUFFIX;
				}
			}
		}

		if (ret == null) {
			int p = name.indexOf("#");
			if (p != -1) {
				ret = name.substring(p + 1);
			} else {
				ret = name;
			}
		}

		return ret;
	}

	public List<LabelId> getVariables() {
		List<LabelId> ret = new ArrayList<LabelId>();
		for (Integer id : getPluginGoal().getUelInput().getUserVariables()) {
			ret.add(new LabelId(getLabel(id), id));
		}
		return Collections.unmodifiableList(ret);
	}

	public void makeConstants(Collection<LabelId> lids) {
		for (LabelId lid : lids) {
			getPluginGoal().makeConstant(lid.getId());
		}
	}

	public void makeVariables(Collection<LabelId> lids) {
		for (LabelId lid : lids) {
			getPluginGoal().makeUserVariable(lid.getId());
		}
	}

}
