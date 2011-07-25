package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.WindowConstants;

class VarSelectionController implements ActionListener {

	private static final String BUTTON_CONS = "make constant";
	private static final String BUTTON_VAR = "make variable";

	private VarSelectionView view = null;

	public VarSelectionController(VarSelectionView v) {
		if (v == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.view = v;
		init();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String cmd = e.getActionCommand();

		if (cmd.equals(BUTTON_VAR)) {
			int[] options = getView().getSelectedConstants();
			for (int i = 0; i < options.length; i++) {
				getView().getModel().makeVariable(
						getView().getConstant(options[i]).getId());
			}
			getView().updateLists();

		} else if (cmd.equals(BUTTON_CONS)) {
			int[] options = getView().getSelectedVariables();
			for (int i = 0; i < options.length; i++) {
				getView().getModel().makeConstant(
						getView().getVariable(options[i]).getId());
			}
			getView().updateLists();

		}
	}

	public void addAcceptVarButtonListener(ActionListener listener,
			String actionCommand) {
		getView().addAcceptVarButtonListener(listener, actionCommand);

	}

	public void addRejectVarButtonListener(ActionListener listener,
			String actionCommand) {
		getView().addRejectVarButtonListener(listener, actionCommand);
	}

	public void close() {
		getView().setVisible(false);
		getView().dispose();
	}

	public VarSelectionView getView() {
		return this.view;
	}

	private void init() {
		getView().addMakeConsButtonListener(this, BUTTON_CONS);
		getView().addMakeVarButtonListener(this, BUTTON_VAR);
		getView().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void open() {
		getView().setVisible(true);
	}

}
