package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.processor.UelModel;

/**
 * This class is a controller for the main panel of the UEL system.
 * 
 * @author Julian Mendez
 * @author Stefan Borgwardt
 */
public class UelController {

	private final UelModel model;
	private RefineController refineController = null;
	private UnifierController unifierController = null;
	private VarSelectionController varSelectionController = null;
	private final UelView view;
	private Stack<Set<OWLAxiom>> undoStack = new Stack<Set<OWLAxiom>>();

	/**
	 * Constructs a new UEL controller using the specified model.
	 * 
	 * @param model
	 *            the UEL model
	 */
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

		for (OWLAxiom axiom : newAxioms) {
			if (!(axiom instanceof OWLSubClassOfAxiom)) {
				throw new IllegalStateException("Expected new dissubsumptions to be encoded as OWLSubClassOfAxioms.");
			}
			if (negOntology.containsAxiom(axiom)) {
				throw new IllegalStateException("The negative goal already contains the following axiom: " + axiom);
			}
			negOntology.getOWLOntologyManager().addAxiom(negOntology, axiom);
		}
		undoStack.push(newAxioms);
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

	private void executeRecompute(boolean save) {
		File file = null;
		if (save) {
			file = refineController.showSaveFileDialog();
			if (file == null) {
				return;
			}
		}

		addNewDissubsumptions();

		if (save) {
			OWLUtils.saveToOntologyFile(view.getSelectedOntologyNeg(), file);
		}

		recomputeUnifiers();
	}

	private void executeRefine() {
		if (model.getUnifierList().size() == 0) {
			return;
		}

		refineController = new RefineController(new RefineView(unifierController.getView()), model);
		refineController.addSaveListener(e -> executeRecompute(true));
		refineController.addRecomputeListener(e -> executeRecompute(false));
		refineController.open();
	}

	private void executeSelectVariables() {
		undoStack.clear();
		setupGoal(true);

		varSelectionController = new VarSelectionController(new VarSelectionView(view), model);
		varSelectionController.addAcceptVarListener(e -> executeAcceptVar());
		varSelectionController.open();
	}

	private void executeUndoRefine() {
		if (undoStack.isEmpty()) {
			throw new IllegalStateException("Expected the undo stack to be non-empty.");
		}

		Set<OWLAxiom> lastDiff = undoStack.pop();
		OWLOntology negOntology = view.getSelectedOntologyNeg();
		for (OWLAxiom axiom : lastDiff) {
			if (!negOntology.containsAxiom(axiom)) {
				throw new IllegalStateException("Expected the negative goal to contain the following axiom: " + axiom);
			}
			negOntology.getOWLOntologyManager().removeAxiom(negOntology, axiom);
		}

		recomputeUnifiers();
	}

	/**
	 * Returns the UEL view controlled by this instance.
	 * 
	 * @return the view component
	 */
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
		if (refineController != null) {
			refineController.close();
		}
		unifierController.close();

		// store previously selected variables
		Set<String> userVariables = model.getUserVariableNames();

		setupGoal(false);

		// restore user variables
		model.makeNamesUserVariables(userVariables);

		setupComputation();
	}

	/**
	 * Updates internal indices with the list of currently loaded ontologies and
	 * refreshes the view accordingly.
	 */
	public void reload() {
		model.resetShortFormCache();
		updateView();
	}

	/**
	 * Uses the UEL model to initialize the selected unification algorithm and
	 * opens the 'Unifier' view.
	 */
	public void setupComputation() {
		model.initializeUnificationAlgorithm(view.getSelectedAlgorithm());

		unifierController = new UnifierController(new UnifierView(view), model);
		unifierController.addRefineListener(e -> executeRefine());
		unifierController.addUndoRefineListener(e -> executeUndoRefine());
		unifierController.setUndoRefineButtonEnabled(!undoStack.isEmpty());
		unifierController.open();
	}

	/**
	 * Uses the UEL model to initialize the goal for the unification algorithm
	 * with the currently selected ontologies.
	 */
	public void setupGoal(boolean resetShortFormCache) {
		Set<OWLOntology> bgOntologies = new HashSet<OWLOntology>();
		bgOntologies.add(view.getSelectedOntologyBg00());
		bgOntologies.add(view.getSelectedOntologyBg01());
		model.setupGoal(bgOntologies, view.getSelectedOntologyPos(), view.getSelectedOntologyNeg(), null,
				view.getSnomedMode(), resetShortFormCache);
	}

	/**
	 * Updates the UEL view with the current set of loaded ontologies.
	 */
	public void updateView() {
		view.reloadOntologies(model.getOntologyList());
	}

}
