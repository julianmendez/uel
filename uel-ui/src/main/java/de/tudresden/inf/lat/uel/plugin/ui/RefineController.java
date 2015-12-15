/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.type.api.Equation;

/**
 * @author Stefan Borgwardt
 *
 */
public class RefineController {

	private final RefineView view;
	private final UelModel model;

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

	public String getDissubsumptions() {
		Set<Equation> dissubsumptions = new HashSet<Equation>();
		// TODO transform each marked atom into a dissubsumption
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
		// TODO translate current unifier to LabelIds
	}

	public void close() {
		view.setVisible(false);
		view.dispose();
	}

}
