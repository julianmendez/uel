/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.type.api.Equation;

/**
 * @author Stefan Borgwardt
 *
 */
public class RefineController {

	private RefineView view;
	private UelModel model;

	public RefineController(RefineView view, UelModel model) {
		this.view = view;
		this.model = model;
	}

	public void addButtonRecomputeListener(ActionListener listener, String actionCommand) {
		getView().addButtonRecomputeListener(listener, actionCommand);
	}

	public void addButtonSaveListener(ActionListener listener, String actionCommand) {
		getView().addButtonSaveListener(listener, actionCommand);
	}

	public RefineView getView() {
		return view;
	}

	private UelModel getModel() {
		return model;
	}

	public String getDissubsumptions() {
		Set<Equation> dissubsumptions = new HashSet<Equation>();
		// TODO transform each marked atom into a dissubsumption
		return getModel().getRenderer(false).printUnifier(dissubsumptions);
	}

	public void open() {
		updateView();
		getView().setVisible(true);
	}

	private void updateView() {
		// TODO translate current unifier to LabelIds
	}

	public void close() {
		getView().setVisible(false);
		getView().dispose();
	}

}
