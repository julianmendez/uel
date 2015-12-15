package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * This is the panel to select the variables in a given unification problem.
 * 
 * @author Julian Mendez
 */
class VarSelectionView extends JDialog {

	private static final long serialVersionUID = 1742164773153032359L;

	private final JButton acceptVarButton = new JButton();
	private final JList<LabelId> listConstants = new JList<LabelId>();
	private final JList<LabelId> listVariables = new JList<LabelId>();
	private final JButton makeConsButton = new JButton();
	private final JButton makeVarButton = new JButton();

	public VarSelectionView() {
		super((Frame) null, "Variable selection", true);
		UelUI.setupWindow(this, createMainPanel());
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

	private Component createButtons() {
		Container buttonPanel = new JPanel();

		UelUI.setupButton(buttonPanel, makeConsButton, UelUI.ICON_STEP_BACK, Message.tooltipMakeCons);

		UelUI.setupButton(buttonPanel, makeVarButton, UelUI.ICON_STEP_FORWARD, Message.tooltipMakeVar);

		UelUI.setupButton(buttonPanel, acceptVarButton, UelUI.ICON_FORWARD, Message.tooltipAcceptVar);

		return buttonPanel;
	}

	private Component createMainPanel() {
		Container ret = new Box(BoxLayout.Y_AXIS);

		ret.add(createButtons());

		ret.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		ret.add(createVarSelectionPanel());

		return ret;
	}

	private Component createVarSelectionPanel() {
		Container varSelPanel = new Box(BoxLayout.X_AXIS);

		UelUI.setupScrollPane(varSelPanel, listConstants, Message.tooltipConstants, new Dimension(360, 480));

		UelUI.setupScrollPane(varSelPanel, listVariables, Message.tooltipVariables, new Dimension(360, 480));

		return varSelPanel;
	}

	public Collection<LabelId> getSelectedConstants() {
		return this.listConstants.getSelectedValuesList();
	}

	public Collection<LabelId> getSelectedVariables() {
		return this.listVariables.getSelectedValuesList();
	}

	public void setConstants(List<LabelId> constants) {
		LabelId[] array = constants.toArray(new LabelId[constants.size()]);
		this.listConstants.setListData(array);
		this.listConstants.clearSelection();
	}

	public void setVariables(List<LabelId> variables) {
		LabelId[] array = variables.toArray(new LabelId[variables.size()]);
		this.listVariables.setListData(array);
		this.listVariables.clearSelection();
	}

}
