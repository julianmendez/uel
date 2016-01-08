package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JList;

/**
 * This dialog allows to select the variables for the unification problem.
 * 
 * @author Julian Mendez
 */
public final class VarSelectionView extends UelDialog {

	private static final long serialVersionUID = 1742164773153032359L;

	private final JButton acceptVarButton = new JButton();
	private final JList<LabelId> listConstants = new JList<LabelId>();
	private final JList<LabelId> listVariables = new JList<LabelId>();
	private final JButton makeConsButton = new JButton();
	private final JButton makeVarButton = new JButton();

	/**
	 * Construct a new variable selection dialog.
	 */
	public VarSelectionView() {
		setup("Variable selection");
	}

	public void addAcceptVarListener(ActionListener listener) {
		acceptVarButton.addActionListener(listener);
	}

	public void addMakeConsListener(ActionListener listener) {
		makeConsButton.addActionListener(listener);
	}

	public void addMakeVarListener(ActionListener listener) {
		makeVarButton.addActionListener(listener);
	}

	private void addButtonPanel(Container parent) {
		Container buttonPanel = UelUI.addButtonPanel(parent);

		UelUI.setupButton(buttonPanel, makeConsButton, UelUI.ICON_STEP_BACK, Message.tooltipMakeCons);

		UelUI.setupButton(buttonPanel, makeVarButton, UelUI.ICON_STEP_FORWARD, Message.tooltipMakeVar);

		UelUI.addStrut(buttonPanel);

		UelUI.setupButton(buttonPanel, acceptVarButton, UelUI.ICON_FORWARD, Message.tooltipAcceptVar);
	}

	@Override
	protected void addMainPanel(Container parent) {
		Container mainPanel = UelUI.addVerticalPanel(parent);

		addButtonPanel(mainPanel);

		UelUI.addStrut(mainPanel);

		addVarSelectionPanel(mainPanel);
	}

	private void addVarSelectionPanel(Container parent) {
		Container varSelectionPanel = UelUI.addHorizontalPanel(parent);

		UelUI.setupList(listConstants, Message.tooltipConstants);
		UelUI.addScrollPane(varSelectionPanel, listConstants, "", new Dimension(360, 480));

		UelUI.setupList(listVariables, Message.tooltipVariables);
		UelUI.addScrollPane(varSelectionPanel, listVariables, "", new Dimension(360, 480));
	}

	public Collection<LabelId> getSelectedConstants() {
		return this.listConstants.getSelectedValuesList();
	}

	public Collection<LabelId> getSelectedVariables() {
		return this.listVariables.getSelectedValuesList();
	}

	public void setConstants(List<LabelId> constants) {
		this.listConstants.setListData(new Vector<LabelId>(constants));
		this.listConstants.clearSelection();
	}

	public void setVariables(List<LabelId> variables) {
		this.listVariables.setListData(new Vector<LabelId>(variables));
		this.listVariables.clearSelection();
	}

}
