/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.type.KRSSRenderer;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;

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
		Set<Equation> dissubsumptions = new HashSet<Equation>();
		Map<LabelId, List<LabelId>> map = view.getSelectedAtoms();

		// construct (primitive) equations representing dissubsumptions from
		// selected atoms
		for (LabelId var : map.keySet()) {
			for (LabelId atom : map.get(var)) {
				dissubsumptions.add(new EquationImpl(var.getId(), atom.getId(), true));
			}
		}

		return model.getRenderer(false).printUnifier(dissubsumptions);
	}

	public void open() {
		updateView();
		view.setVisible(true);
	}

	public File showSaveFileDialog() {
		return UelUI.showSaveFileDialog(view);
	}

	private void updateView() {
		Map<LabelId, List<LabelId>> map = new HashMap<LabelId, List<LabelId>>();
		KRSSRenderer renderer = model.getRenderer(true);
		Set<Equation> unifier = model.getCurrentUnifier();

		// convert unifier into map between LabelIds for the view
		for (Equation eq : unifier) {
			Integer varId = eq.getLeft();
			if (model.getPluginGoal().getUserVariables().contains(varId)) {
				LabelId var = new LabelId(renderer.getName(varId, false), varId);

				List<LabelId> atoms = new ArrayList<LabelId>();
				for (Integer atomId : eq.getRight()) {
					atoms.add(new LabelId(UelModel.removeQuotes(renderer.printAtom(atomId, unifier, true)), atomId));
				}

				map.put(var, atoms);
			}
		}

		view.updateSelectionPanels(map);
	}

}
