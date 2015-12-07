package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.tudresden.inf.lat.uel.core.processor.UelModel;

/**
 * An object of this class can manage which concept names are considered by the
 * user as variables and which ones as constants.
 * 
 * @author Julian Mendez
 */
class VarSelectionModel {
	
	
	// TODO merge into UelModel

	private final UelModel model;

	public VarSelectionModel(UelModel model) {
		if (model == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.model = model;
	}

	public List<LabelId> getConstants() {
		List<LabelId> ret = new ArrayList<LabelId>();
		for (Integer id : model.getPluginGoal().getConstants()) {
			ret.add(new LabelId(model.getLabel(id), id));
		}
		return Collections.unmodifiableList(ret);
	}

	public List<LabelId> getVariables() {
		List<LabelId> ret = new ArrayList<LabelId>();
		for (Integer id : model.getPluginGoal().getUserVariables()) {
			ret.add(new LabelId(model.getLabel(id), id));
		}
		return Collections.unmodifiableList(ret);
	}

	public void makeConstants(Collection<LabelId> lids) {
		for (LabelId lid : lids) {
			model.getPluginGoal().makeConstant(lid.getId());
		}
	}

	public void makeVariables(Collection<LabelId> lids) {
		for (LabelId lid : lids) {
			model.getPluginGoal().makeUserVariable(lid.getId());
		}
	}

}
