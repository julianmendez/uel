package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import de.tudresden.inf.lat.uel.core.processor.UelModel;

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
		getModel().setCurrentUnifierIndex(0);
		updateUnifierView();
	}

	private void executeActionLast() {
		while (!getModel().allUnifiersFound()) {
			try {
				getModel().computeNextUnifier();
			} catch (InterruptedException ex) {
			}
		}
		getModel().setCurrentUnifierIndex(getModel().getUnifierList().size() - 1);
		updateUnifierView();
	}

	public void open() {
		getView().initializeButtons();
		getView().setVisible(true);
	}

	private void executeActionNext() {
		if (getModel().getCurrentUnifierIndex() == getModel().getUnifierList().size() - 1) {
			try {
				getModel().computeNextUnifier();
			} catch (InterruptedException ex) {
			}
		}
		getModel().setCurrentUnifierIndex(getModel().getCurrentUnifierIndex() + 1);
		updateUnifierView();
	}

	private void executeActionPrevious() {
		getModel().setCurrentUnifierIndex(getModel().getCurrentUnifierIndex() - 1);
		updateUnifierView();
	}

	private void executeActionSave() {
		File file = UelUI.showSaveFileDialog(getView());
		if (file == null) {
			return;
		}

		String unifier = getModel().printCurrentUnifier(false);
		OntologyRenderer.saveToOntologyFile(unifier, file);
	}

	private void executeActionShowStatInfo() {
		new StatInfoController(new StatInfoView(), getModel()).open();
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

	private void updateUnifierView() {
		int index = getModel().getCurrentUnifierIndex();
		if (index > -1) {
			getView().setUnifier(getModel().printCurrentUnifier(true));
			getView().setSaveRefineButtons(true);
		} else {
			getView().setUnifier("[not unifiable]");
			getView().setSaveRefineButtons(false);
		}
		getView().setUnifierId(" " + (index + 1) + " ");
		if (index == 0) {
			getView().setFirstPreviousButtons(false);
		} else {
			getView().setFirstPreviousButtons(true);
		}
		if (getModel().allUnifiersFound() && index >= getModel().getUnifierList().size() - 1) {
			getView().setNextLastButtons(false);
		} else {
			getView().setNextLastButtons(true);
		}
	}

}
