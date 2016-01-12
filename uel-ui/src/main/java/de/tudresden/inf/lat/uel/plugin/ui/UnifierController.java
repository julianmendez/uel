package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.tudresden.inf.lat.uel.core.processor.UelModel;

/**
 * This is the controller for the panel that shows the unifiers.
 * 
 * @author Julian Mendez
 */
class UnifierController {

	private final UelModel model;
	private final UnifierView view;

	public UnifierController(UelModel model) {
		this.view = new UnifierView();
		this.model = model;
		init();
	}

	public void addRefineListener(ActionListener listener) {
		view.addRefineListener(listener);
	}

	public void close() {
		view.setVisible(false);
		view.dispose();
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

		Set<OWLAxiom> unifier = model.renderCurrentUnifier();
		OWLUtils.saveToOntologyFile(unifier, file);
	}

	private void executeShowStatInfo() {
		new StatInfoController(model).open();
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
		view.pack();
		view.setVisible(true);
	}

	private void updateUnifierView() {
		int index = model.getCurrentUnifierIndex();
		if (index > -1) {
			view.setUnifier(model.printCurrentUnifier());
			view.setSaveRefineButtons(true);
		} else {
			view.setUnifier("[not unifiable]");
			view.setSaveRefineButtons(false);
		}
		view.setUnifierId(" " + (index + 1) + " ");
		if (index == 0) {
			view.setFirstPreviousButtons(false);
		} else {
			view.setFirstPreviousButtons(true);
		}
		if (model.allUnifiersFound() && index >= model.getUnifierList().size() - 1) {
			view.setNextLastButtons(false);
		} else {
			view.setNextLastButtons(true);
		}
	}

}
