package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.core.processor.UelModel;

/**
 * This class is a controller for the main panel of the UEL system.
 * 
 * @author Julian Mendez
 * @author Stefan Borgwardt
 */
public class UelController {

	private Stack<OWLOntology> constraints = new Stack<OWLOntology>();
	private final UelModel model;
	private RefineController refineController = null;
	private UnifierController unifierController = null;
	private VarSelectionController varSelectionController = null;
	private final UelView view;

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

	private boolean addNewDissubsumptions() {
		Set<OWLAxiom> newDissubsumptions = refineController.getDissubsumptions();
		if (newDissubsumptions.isEmpty()) {
			return false;
		}

		if (!constraints.isEmpty()) {
			newDissubsumptions.addAll(constraints.peek().getAxioms());
		}

		constraints.push(FileUtils.toOWLOntology(newDissubsumptions));
		return true;
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

	/**
	 * Recompute the unifiers w.r.t. old and new dissubsumptions created by the
	 * user.
	 * 
	 * @param save
	 *            indicates whether the current constraints should be saved into
	 *            an ontology file
	 */
	private void executeRecompute(boolean save) {
		File file = null;
		if (save) {
			file = refineController.showSaveFileDialog();
			if (file == null) {
				return;
			}
		}

		boolean changed = addNewDissubsumptions();

		if (save && !constraints.isEmpty()) {
			if (FileUtils.isTextFile(file)) {
				FileUtils.saveToFile(file,
						model.getStringRenderer(null).renderAxioms(constraints.peek().getAxioms(), false));
			} else {
				FileUtils.saveToFile(file, constraints.peek());
			}
		}

		if (changed) {
			// if new dissubsumptions were created, restart the (dis)unification
			// process
			recomputeUnifiers();
		} else {
			// otherwise, only close the refinement view and continue computing
			// unifiers
			refineController.close();
		}
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
		constraints.clear();
		setupGoal(true);

		varSelectionController = new VarSelectionController(new VarSelectionView(view), model);
		varSelectionController.addAcceptVarListener(e -> executeAcceptVar());
		varSelectionController.open();
	}

	private void executeUndoRefine() {
		constraints.pop();
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
		view.addOpenListener(e -> executeOpen());
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
		model.makeNamesVariables(userVariables.stream(), true);

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
		model.getOptions().unificationAlgorithmName = view.getSelectedAlgorithm();
		model.initializeUnificationAlgorithm();

		unifierController = new UnifierController(new UnifierView(view), model);
		unifierController.addRefineListener(e -> executeRefine());
		unifierController.addUndoRefineListener(e -> executeUndoRefine());
		unifierController.setUndoRefineButtonEnabled(!constraints.empty());
		unifierController.open();
	}

	/**
	 * Uses the UEL model to initialize the goal for the unification algorithm
	 * with the currently selected ontologies.
	 * 
	 * @param resetShortFormCache
	 *            indicates whether the cache of used short forms should be
	 *            reset in order to refresh the presentation
	 */
	public void setupGoal(boolean resetShortFormCache) {
		model.getOptions().snomedMode = view.getSnomedMode();
		model.getOptions().expandPrimitiveDefinitions = view.getExpandPrimitiveDefinitions();
		Set<OWLOntology> bgOntologies = new HashSet<OWLOntology>();
		bgOntologies.add(view.getSelectedOntologyBg00());
		bgOntologies.add(view.getSelectedOntologyBg01());
		OWLOntology constraintOntology = constraints.empty() ? null : constraints.peek();
		model.setupGoal(bgOntologies, view.getSelectedOntologyPos(), view.getSelectedOntologyNeg(), constraintOntology,
				null, resetShortFormCache);
	}

	/**
	 * Updates the UEL view with the current set of loaded ontologies.
	 */
	public void updateView() {
		view.reloadOntologies(model.getOntologyList());
	}

}
