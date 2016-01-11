package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.processor.UelModel;

/**
 * This class is a controller for the main panel of the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelController {

	private final UelModel model;
	private RefineController refineController = null;
	private UnifierController unifierController = null;
	private VarSelectionController varSelectionController = null;
	private final UelView view;

	public UelController(UelModel model) {
		this.view = new UelView();
		this.model = model;
		init();
	}

	private void addNewDissubsumptions() {
		OWLOntology negOntology = view.getSelectedOntologyNeg();
		if (negOntology.equals(UelModel.EMPTY_ONTOLOGY)) {
			negOntology = model.createOntology();
			updateView();
			view.setSelectedOntologyNeg(negOntology);
		}

		Set<OWLAxiom> newAxioms = refineController.getDissubsumptions();

		if (!newAxioms.stream().allMatch(axiom -> axiom instanceof OWLSubClassOfAxiom)) {
			throw new IllegalStateException("Expected dissubsumptions to be encoded as OWLSubClassOfAxioms.");
		}
		negOntology.getOWLOntologyManager().addAxioms(negOntology, newAxioms);
	}

	private void executeAcceptVar() {
		varSelectionController.close();

		setupComputation();
	}

	private void executeOpen() {
		File file = UelUI.showOpenFileDialog(view);
		if (file == null) {
			return;
		}
		model.loadOntology(file);
		updateView();
	}

	private void executeRecompute() {
		addNewDissubsumptions();
		recomputeUnifiers();
	}

	private void executeRefine() {
		if (model.getUnifierList().size() == 0) {
			return;
		}

		refineController = new RefineController(model);
		refineController.addSaveListener(e -> executeSaveDissubsumptions());
		refineController.addRecomputeListener(e -> executeRecompute());
		refineController.open();
	}

	private void executeSaveDissubsumptions() {
		File file = refineController.showSaveFileDialog();
		if (file == null) {
			return;
		}

		addNewDissubsumptions();
		OWLUtils.saveToOntologyFile(view.getSelectedOntologyNeg(), file);
		recomputeUnifiers();
	}

	private void executeSelectVariables() {
		setupModel();

		varSelectionController = new VarSelectionController(model);
		varSelectionController.addAcceptVarListener(e -> executeAcceptVar());
		varSelectionController.open();
	}

	public Component getView() {
		return view;
	}

	private void init() {
		view.addOpenListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeOpen();
			}
		});
		view.addSelectVariablesListener(e -> executeSelectVariables());
		updateView();
	}

	private void recomputeUnifiers() {
		refineController.close();
		unifierController.close();

		// store previously selected variables
		Set<String> userVariables = model.getUserVariableNames();

		setupModel();

		// restore user variables
		model.makeNamesUserVariables(userVariables);

		setupComputation();
	}

	public void reload() {
		model.recomputeShortFormMap();
		updateView();
	}

	public void setupComputation() {
		model.initializeUnificationAlgorithm(view.getSelectedAlgorithm());

		unifierController = new UnifierController(model);
		unifierController.addRefineListener(e -> executeRefine());
		unifierController.open();
	}

	public void setupModel() {
		Set<OWLOntology> bgOntologies = new HashSet<OWLOntology>();
		bgOntologies.add(view.getSelectedOntologyBg00());
		bgOntologies.add(view.getSelectedOntologyBg01());
		model.setupGoal(bgOntologies, view.getSelectedOntologyPos(), view.getSelectedOntologyNeg(), null);
	}

	public void updateView() {
		view.reloadOntologies(model.getOntologyList());
	}

}
