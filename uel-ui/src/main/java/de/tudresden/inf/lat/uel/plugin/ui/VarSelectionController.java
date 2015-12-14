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
class VarSelectionController implements ActionListener {

	private static final String BUTTON_CONS = "make constant";
	private static final String BUTTON_VAR = "make variable";

	private final UelModel model;
	private final VarSelectionView view;

	public VarSelectionController(VarSelectionView view, UelModel model) {
		if (view == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (model == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.view = view;
		this.model = model;
		init();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		switch (e.getActionCommand()) {
		case BUTTON_VAR:
			executeMakeVar();
			break;
		case BUTTON_CONS:
			executeMakeCons();
			break;
		default:
			throw new IllegalStateException();
		}
	}

	public void addAcceptVarButtonListener(ActionListener listener, String actionCommand) {
		view.addAcceptVarButtonListener(listener, actionCommand);
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
		view.addMakeConsButtonListener(this, BUTTON_CONS);
		view.addMakeVarButtonListener(this, BUTTON_VAR);
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
