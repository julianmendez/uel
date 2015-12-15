package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.type.KRSSRenderer;

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
			model.getPluginGoal().makeConstant(variable.getId());
		}
		updateLists();
	}

	private void executeMakeVar() {
		for (LabelId constant : view.getSelectedConstants()) {
			model.getPluginGoal().makeUserVariable(constant.getId());
		}
		updateLists();
	}

	private void init() {
		view.addMakeConsListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeMakeCons();
			}
		});
		view.addMakeVarListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeMakeVar();
			}
		});
		updateLists();
	}

	public void open() {
		view.setVisible(true);
	}

	private void updateLists() {
		KRSSRenderer renderer = model.getRenderer(true);

		List<LabelId> constants = new ArrayList<LabelId>();
		for (Integer id : model.getPluginGoal().getConstants()) {
			constants.add(new LabelId(renderer.getName(id, false), id));
		}
		view.setConstants(constants);

		List<LabelId> variables = new ArrayList<LabelId>();
		for (Integer id : model.getPluginGoal().getUserVariables()) {
			variables.add(new LabelId(renderer.getName(id, false), id));
		}
		view.setVariables(variables);
	}

}
