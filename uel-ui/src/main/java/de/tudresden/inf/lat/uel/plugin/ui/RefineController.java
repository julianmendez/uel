/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLAxiom;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.renderer.StringRenderer;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;

/**
 * @author Stefan Borgwardt
 *
 */
class RefineController {

	private final UelModel model;
	private final RefineView view;

	public RefineController(UelModel model) {
		this.view = new RefineView();
		this.model = model;
	}

	public void addRecomputeListener(ActionListener listener) {
		view.addRecomputeListener(listener);
	}

	public void addSaveListener(ActionListener listener) {
		view.addSaveListener(listener);
	}

	public void close() {
		view.setVisible(false);
		view.dispose();
	}

	public Set<OWLAxiom> getDissubsumptions() {
		// construct (primitive) definitions representing dissubsumptions from
		// selected atoms
		Function<Entry<LabelId, List<LabelId>>, Stream<Dissubsumption>> dissubsumptionsForEntry = entry -> entry
				.getValue().stream().map(atom -> new Dissubsumption(Collections.singleton(entry.getKey().getId()),
						Collections.singleton(atom.getId())));

		Set<Dissubsumption> dissubsumptions = view.getSelectedAtoms().entrySet().stream()
				.flatMap(dissubsumptionsForEntry).collect(Collectors.toSet());

		// render the definitions as OWLSubClassOfAxioms
		return model.getOWLRenderer(model.getCurrentUnifier().getDefinitions()).renderAxioms(dissubsumptions);
	}

	public void open() {
		updateView();
		view.pack();
		view.setVisible(true);
	}

	public File showSaveFileDialog() {
		return UelUI.showSaveFileDialog(view);
	}

	private void updateView() {
		Map<LabelId, List<LabelId>> map = new HashMap<LabelId, List<LabelId>>();
		Set<Definition> definitions = model.getCurrentUnifier().getDefinitions();
		StringRenderer renderer = model.getStringRenderer(definitions);

		// convert unifier into map between LabelIds for the view
		for (Definition definition : definitions) {
			Integer varId = definition.getDefiniendum();
			if (model.getGoal().getAtomManager().getUserVariables().contains(varId)) {
				map.put(new LabelId(renderer.renderAtom(varId), varId), definition.getRight().stream()
						.map(atomId -> new LabelId(renderer.renderAtom(atomId), atomId)).collect(Collectors.toList()));
			}
		}

		view.updateSelectionPanel(map);
	}

}
