/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.renderer.StringRenderer;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;
import de.tudresden.inf.lat.uel.type.impl.DefinitionSet;

/**
 * An instance of this class controls the behavior of the 'Refine unifier' view.
 * 
 * @author Stefan Borgwardt
 */
class RefineController {

	private final UelModel model;
	private final RefineView view;

	/**
	 * Constructs a new Refine controller using the given model.
	 * 
	 * @param view
	 *            the Refine view
	 * @param model
	 *            the UEL model
	 */
	public RefineController(RefineView view, UelModel model) {
		this.view = view;
		this.model = model;
	}

	/**
	 * Adds a listener to the button for recomputing the unifiers.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addRecomputeListener(ActionListener listener) {
		view.addRecomputeListener(listener);
	}

	/**
	 * Adds a listener to the button for saving the current and new
	 * dissubsumptions and recomputing the unifiers.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addSaveListener(ActionListener listener) {
		view.addSaveListener(listener);
	}

	/**
	 * Closes the Refine view.
	 */
	public void close() {
		view.close();
	}

	/**
	 * Returns the currently selected dissubsumptions, rendered as
	 * OWLSubClassOfAxioms.
	 * 
	 * @return the OWL representation of the new dissubsumptions
	 */
	public Set<OWLAxiom> getDissubsumptions() {
		// construct (primitive) definitions representing dissubsumptions from
		// selected atoms
		Function<Entry<LabelId, List<LabelId>>, Stream<Dissubsumption>> dissubsumptionsForEntry = entry -> entry
				.getValue().stream().map(atom -> new Dissubsumption(Collections.singleton(entry.getKey().getId()),
						Collections.singleton(model.replaceByUndefId(atom.getId()))));

		Set<Dissubsumption> dissubsumptions = view.getSelectedAtoms().entrySet().stream()
				.flatMap(dissubsumptionsForEntry).collect(Collectors.toSet());

		// render the definitions as OWLSubClassOfAxioms
		return model.getOWLRenderer(model.getCurrentUnifier().getDefinitions()).renderAxioms(dissubsumptions);
	}

	/**
	 * Opens the Refine view.
	 */
	public void open() {
		updateView();
		view.open();
	}

	/**
	 * Shows a dialog window for saving a file.
	 * 
	 * @return the selected file, possibly null
	 */
	public File showSaveFileDialog() {
		return UelUI.showSaveFileDialog(view);
	}

	private void updateView() {
		Map<LabelId, List<LabelId>> map = new TreeMap<LabelId, List<LabelId>>();
		DefinitionSet definitions = model.getCurrentUnifier().getDefinitions();
		StringRenderer renderer = model.getStringRenderer(definitions);

		// convert unifier into map between LabelIds for the view
		for (Definition definition : definitions) {
			Integer varId = definition.getDefiniendum();
			if (model.getGoal().getAtomManager().getUserVariables().contains(varId)) {
				map.put(new LabelId(renderer.renderAtom(varId, false), varId),
						definition.getRight().stream()
								.map(atomId -> new LabelId(renderer.renderAtom(atomId, true), atomId))
								.collect(Collectors.toList()));
			}
		}

		view.updateSelectionPanel(map);
	}

}
