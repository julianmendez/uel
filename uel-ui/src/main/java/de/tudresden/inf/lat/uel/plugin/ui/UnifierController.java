package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.type.api.Equation;

/**
 * This is the controller for the panel that shows the unifiers.
 * 
 * @author Julian Mendez
 */
public class UnifierController implements ActionListener {

	private static final String actionFirst = "first";
	private static final String actionLast = "last";
	private static final String actionNext = "next";
	private static final String actionPrevious = "previous";
	private static final String actionSave = "save";
	private static final String actionShowStatInfo = "show statistic info";

	private boolean allUnifiersFound = false;
	private int unifierIndex = -1;
	private UnifierView view;
	private UelModel model;

	public UnifierController(UnifierView view, UelModel model) {
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
		case actionFirst:
			executeActionFirst();
			break;
		case actionPrevious:
			executeActionPrevious();
			break;
		case actionNext:
			executeActionNext();
			break;
		case actionLast:
			executeActionLast();
			break;
		case actionSave:
			executeActionSave();
			break;
		case actionShowStatInfo:
			executeActionShowStatInfo();
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void executeActionFirst() {
		this.unifierIndex = 0;
		updateUnifierView();
	}

	private void executeActionLast() {
		while (!this.allUnifiersFound) {
			int previousSize = getModel().getUnifierList().size();
			try {
				getModel().computeNextUnifier();
			} catch (InterruptedException ex) {
			}
			if (getModel().getUnifierList().size() == previousSize) {
				this.allUnifiersFound = true;
			}
		}
		this.unifierIndex = getModel().getUnifierList().size() - 1;
		updateUnifierView();
	}

	public void showView() {
		getView().initializeButtons();
		getView().setVisible(true);
	}

	private void executeActionNext() {
		this.unifierIndex++;
		if (this.unifierIndex >= getModel().getUnifierList().size()) {
			int previousSize = getModel().getUnifierList().size();
			try {
				getModel().computeNextUnifier();
			} catch (InterruptedException ex) {
			}
			if (getModel().getUnifierList().size() == previousSize) {
				this.allUnifiersFound = true;
				this.unifierIndex = getModel().getUnifierList().size() - 1;
			}
		}
		updateUnifierView();
	}

	private void executeActionPrevious() {
		this.unifierIndex--;
		if (this.unifierIndex < 0) {
			this.unifierIndex = 0;
		}
		updateUnifierView();
	}

	private void executeActionSave() {
		File file = UelController.showSaveFileDialog(getView());
		if (file == null) {
			return;
		}

		String unifier = printCurrentUnifier();
		OntologyRenderer.saveToOntologyFile(unifier, file);
	}

	private void executeActionShowStatInfo() {
		StatInfoController statInfoWindow = new StatInfoController(new StatInfoView(), getModel());
		statInfoWindow.open();
	}

	private UnifierView getView() {
		return view;
	}

	private UelModel getModel() {
		return model;
	}

	private void init() {
		getView().addButtonFirstListener(this, actionFirst);
		getView().addButtonPreviousListener(this, actionPrevious);
		getView().addButtonNextListener(this, actionNext);
		getView().addButtonLastListener(this, actionLast);
		getView().addButtonSaveListener(this, actionSave);
		getView().addButtonShowStatInfoListener(this, actionShowStatInfo);
	}

	public void addButtonRefineListener(ActionListener listener, String actionCommand) {
		getView().addButtonRefineListener(listener, actionCommand);
	}

	public Set<Equation> getCurrentUnifier() {
		if (getModel().getUnifierList().size() == 0) {
			return null;
		}
		return getModel().getUnifierList().get(this.unifierIndex);
	}

	private String printCurrentUnifier() {
		Set<Equation> unifier = getCurrentUnifier();
		if (unifier == null) {
			return "";
		}
		return getModel().getRenderer().printUnifier(getCurrentUnifier());
	}

	private void updateUnifierView() {
		if (getModel().getUnifierList().size() > 0) {
			getView().setUnifier(printCurrentUnifier());
			getView().setSaveRefineButtons(true);
		} else {
			getView().setUnifier("[not unifiable]");
			getView().setSaveRefineButtons(false);
		}
		getView().setUnifierId(" " + (getModel().getUnifierList().isEmpty() ? 0 : (this.unifierIndex + 1)) + " ");
		if (this.unifierIndex == 0) {
			getView().setFirstPreviousButtons(false);
		} else {
			getView().setFirstPreviousButtons(true);
		}
		if (this.allUnifiersFound && this.unifierIndex >= getModel().getUnifierList().size() - 1) {
			getView().setNextLastButtons(false);
		} else {
			getView().setNextLastButtons(true);
		}
	}

}
