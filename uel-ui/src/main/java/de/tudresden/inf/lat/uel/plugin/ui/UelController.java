package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.core.processor.UelModel;

/**
 * This class is a controller for the main panel of the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelController {

	private VarSelectionController varSelectionController = null;
	private UnifierController unifierController = null;
	private RefineController refineController = null;
	private final UelView view;
	private final UelModel model;

	public UelController(UelView view, UelModel model) {
		this.view = view;
		this.model = model;
		init();
	}

	private void executeRecompute() {
		addNewDissubsumptions();
		recomputeUnifiers();
	}

	private void executeSaveDissubsumptions() {
		File file = refineController.showSaveFileDialog();
		if (file == null) {
			return;
		}

		addNewDissubsumptions();
		OntologyRenderer.saveToOntologyFile(view.getSelectedOntologyNeg(), file);
		recomputeUnifiers();
	}

	private void addNewDissubsumptions() {
		// TODO add dissubsumptions from dview to owlOntologyNeg
		// TODO if this ontology does not yet exist, create it, add it to the
		// ontologyManager, and update the selection in the uelView
		String dissubsumptions = refineController.getDissubsumptions();
	}

	private void recomputeUnifiers() {
		// TODO close dview and restart computation, see executeSelectVariables
		refineController.close();
	}

	private void executeRefine() {
		if (model.getUnifierList().size() == 0) {
			return;
		}

		refineController = new RefineController(model);
		refineController.addSaveListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeSaveDissubsumptions();
			}
		});
		refineController.addRecomputeListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeRecompute();
			}
		});
		refineController.open();
	}

	private void executeAcceptVar() {
		varSelectionController.close();

		model.createUelProcessor(view.getSelectedProcessor());

		unifierController = new UnifierController(model);
		unifierController.addRefineListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeRefine();
			}
		});
		unifierController.open();
	}

	private void executeOpen() {
		File file = UelUI.showOpenFileDialog(view);
		if (file == null) {
			return;
		}
		model.loadOntology(file);
		updateView();
	}

	private void executeSelectVariables() {
		model.reset();

		Set<OWLOntology> bgOntologies = new HashSet<OWLOntology>();
		bgOntologies.add(view.getSelectedOntologyBg00());
		bgOntologies.add(view.getSelectedOntologyBg01());

		model.setupPluginGoal(bgOntologies, view.getSelectedOntologyPos(), view.getSelectedOntologyNeg(), null);

		this.varSelectionController = new VarSelectionController(model);
		this.varSelectionController.addAcceptVarListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeAcceptVar();
			}
		});
		this.varSelectionController.open();
	}

	private void init() {
		view.addOpenListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeOpen();
			}
		});
		view.addSelectVariablesListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeSelectVariables();
			}
		});
		updateView();
	}

	public void updateView() {
		view.reloadOntologies(model.getOntologyList());
	}
	
	public void reload() {
		model.recomputeShortFormMap();
		updateView();
	}

}
