package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.renderer.StringRenderer;

/**
 * This is the controller of the panel to select the variables in a given
 * unification problem.
 * 
 * @author Julian Mendez
 */
class VarSelectionController {

	private final UelModel model;
	private final VarSelectionView view;

	public VarSelectionController(UelModel model) {
		this.view = new VarSelectionView();
		this.model = model;
		init();
	}

	public void addAcceptVarListener(ActionListener listener) {
		view.addAcceptVarListener(listener);
	}

	public void close() {
		view.setVisible(false);
		view.dispose();
	}

	private void executeMakeCons() {
		for (LabelId variable : view.getSelectedVariables()) {
			model.getGoal().getAtomManager().makeConstant(variable.getId());
		}
		updateLists();
	}

	private void executeMakeVar() {
		for (LabelId constant : view.getSelectedConstants()) {
			model.getGoal().getAtomManager().makeUserVariable(constant.getId());
		}
		updateLists();
	}

	private void init() {
		view.addMakeConsListener(e -> executeMakeCons());
		view.addMakeVarListener(e -> executeMakeVar());
		updateLists();
	}

	public void open() {
		view.pack();
		view.setVisible(true);
	}

	private void updateLists() {
		StringRenderer renderer = model.getStringRenderer(true, null);

		List<LabelId> constants = new ArrayList<LabelId>();
		for (Integer id : model.getGoal().getAtomManager().getConstants()) {
			constants.add(new LabelId(renderer.renderNameWithoutQuotes(id), id));
		}
		view.setConstants(constants);

		List<LabelId> variables = new ArrayList<LabelId>();
		for (Integer id : model.getGoal().getAtomManager().getUserVariables()) {
			variables.add(new LabelId(renderer.renderNameWithoutQuotes(id), id));
		}
		view.setVariables(variables);
	}

}
