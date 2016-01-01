/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.type.KRSSRenderer;
import de.tudresden.inf.lat.uel.type.api.Definition;

/**
 * @author Stefan Borgwardt
 *
 */
public class RefineController {

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

	public String getDissubsumptions() {
		Set<Definition> dissubsumptions = new HashSet<Definition>();
		Map<LabelId, List<LabelId>> map = view.getSelectedAtoms();

		// construct (primitive) definitions representing dissubsumptions from
		// selected atoms
		for (LabelId var : map.keySet()) {
			for (LabelId atom : map.get(var)) {
				dissubsumptions.add(new Definition(var.getId(), Collections.singleton(atom.getId()), true));
			}
		}

		return model.getRenderer(false).printDefinitions(dissubsumptions, model.getCurrentUnifier().getDefinitions(),
				true);
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
		KRSSRenderer renderer = model.getRenderer(true);
		Set<Definition> definitions = model.getCurrentUnifier().getDefinitions();

		// convert unifier into map between LabelIds for the view
		for (Definition definition : definitions) {
			Integer varId = definition.getDefiniendum();
			if (model.getGoal().getAtomManager().getUserVariables().contains(varId)) {
				LabelId var = new LabelId(renderer.getName(varId, false), varId);

				List<LabelId> atoms = new ArrayList<LabelId>();
				for (Integer atomId : definition.getRight()) {
					String label = UelModel.removeQuotes(renderer.printAtom(atomId, definitions, true));
					atoms.add(new LabelId(label, atomId));
				}

				map.put(var, atoms);
			}
		}

		view.updateSelectionPanel(map);
	}

}