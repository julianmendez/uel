package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;

import de.tudresden.inf.lat.uel.core.processor.UelModel;

/**
 * This is the controller for the panel that shows the unifiers.
 * 
 * @author Julian Mendez
 */
class UnifierController {

	private final UelModel model;
	private final UnifierView view;

	public UnifierController(UnifierView view, UelModel model) {
		this.view = view;
		this.model = model;
		init();
	}

	public void addRefineListener(ActionListener listener) {
		view.addRefineListener(listener);
	}

	public void addUndoRefineListener(ActionListener listener) {
		view.addUndoRefineListener(listener);
	}

	public void close() {
		view.close();
	}

	private void executeFirst() {
		model.setCurrentUnifierIndex(0);
		updateUnifierView();
	}

	private void executeLast() {
		while (!model.allUnifiersFound()) {
			try {
				model.computeNextUnifier();
			} catch (InterruptedException ex) {
			}
		}
		model.setCurrentUnifierIndex(model.getUnifierList().size() - 1);
		updateUnifierView();
	}

	private void executeNext() {
		if (model.getCurrentUnifierIndex() == model.getUnifierList().size() - 1) {
			try {
				model.computeNextUnifier();
			} catch (InterruptedException ex) {
			}
		}
		model.setCurrentUnifierIndex(model.getCurrentUnifierIndex() + 1);
		updateUnifierView();
	}

	private void executePrevious() {
		model.setCurrentUnifierIndex(model.getCurrentUnifierIndex() - 1);
		updateUnifierView();
	}

	private void executeSave() {
		File file = UelUI.showSaveFileDialog(view);
		if (file == null) {
			return;
		}

		if (FileUtils.isTextFile(file)) {
			FileUtils.saveToFile(file, model.printCurrentUnifier());
		} else {
			FileUtils.saveToFile(file, model.renderCurrentUnifier());
		}
	}

	private void executeShowStatInfo() {
		new StatInfoController(new StatInfoView(view), model).open();
	}

	Component getView() {
		return view;
	}

	private void init() {
		view.addFirstListener(e -> executeFirst());
		view.addPreviousListener(e -> executePrevious());
		view.addNextListener(e -> executeNext());
		view.addLastListener(e -> executeLast());
		view.addSaveListener(e -> executeSave());
		view.addShowStatInfoListener(e -> executeShowStatInfo());
	}

	public void open() {
		view.initializeButtons();
		view.open();
	}

	public void setUndoRefineButtonEnabled(boolean state) {
		view.setUndoRefineButtonEnabled(state);
	}

	private void updateUnifierView() {
		int index = model.getCurrentUnifierIndex();
		if (index > -1) {
			view.setUnifier(model.printCurrentUnifier());
			view.setSaveRefineButtonsEnabled(true);
		} else {
			view.setUnifier("[not unifiable]");
			view.setSaveRefineButtonsEnabled(false);
		}
		view.setUnifierId(" " + (index + 1) + " ");
		if (index == 0) {
			view.setFirstPreviousButtonsEnabled(false);
		} else {
			view.setFirstPreviousButtonsEnabled(true);
		}
		if (model.allUnifiersFound() && index >= model.getUnifierList().size() - 1) {
			view.setNextLastButtonsEnabled(false);
		} else {
			view.setNextLastButtonsEnabled(true);
		}
	}

}
