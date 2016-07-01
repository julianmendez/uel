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

	/**
	 * Set up a new Unifier controller.
	 * 
	 * @param view
	 *            the Unifier view
	 * @param model
	 *            the UEL model
	 */
	public UnifierController(UnifierView view, UelModel model) {
		this.view = view;
		this.model = model;
		init();
	}

	/**
	 * Register an action listener for the 'refine' button.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addRefineListener(ActionListener listener) {
		view.addRefineListener(listener);
	}

	/**
	 * Register an action listener for the 'undo' button.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addUndoRefineListener(ActionListener listener) {
		view.addUndoRefineListener(listener);
	}

	/**
	 * Close the view.
	 */
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
				throw new RuntimeException(ex);
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
				throw new RuntimeException(ex);
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

	/**
	 * Access the unifier view Component.
	 * 
	 * @return the view
	 */
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

	/**
	 * Initialize the unifier view and open it.
	 */
	public void open() {
		view.initializeButtons();
		view.open();
	}

	/**
	 * Sets the state of the 'undo' button.
	 * 
	 * @param state
	 *            indicates whether the button should be enabled
	 */
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
