package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

class VarSelectionView extends JFrame {

	private static final long serialVersionUID = 1742164773153032359L;

	private JButton acceptVarButton = null;
	private JList listConstants = null;
	private List<LabelId> listOfConstants = null;
	private List<LabelId> listOfVariables = null;
	private JList listVariables = null;
	private JButton makeConsButton = null;
	private JButton makeVarButton = null;
	private VarSelectionModel model = null;
	private JButton rejectVarButton = null;

	public VarSelectionView(VarSelectionModel m) {
		super("Variable selection");
		this.model = m;
		initVarFrame();
		updateLists();
	}

	public void addAcceptVarButtonListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.acceptVarButton.addActionListener(listener);
		this.acceptVarButton.setActionCommand(actionCommand);
	}

	public void addMakeConsButtonListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.makeConsButton.addActionListener(listener);
		this.makeConsButton.setActionCommand(actionCommand);
	}

	public void addMakeVarButtonListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.makeVarButton.addActionListener(listener);
		this.makeVarButton.setActionCommand(actionCommand);
	}

	public void addRejectVarButtonListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.rejectVarButton.addActionListener(listener);
		this.rejectVarButton.setActionCommand(actionCommand);
	}

	public LabelId getConstant(int index) {
		return this.listOfConstants.get(index);
	}

	public VarSelectionModel getModel() {
		return this.model;
	}

	public int[] getSelectedConstants() {
		return this.listConstants.getSelectedIndices();
	}

	public int[] getSelectedVariables() {
		return this.listVariables.getSelectedIndices();
	}

	public LabelId getVariable(int index) {
		return this.listOfVariables.get(index);
	}

	private void initVarFrame() {
		setLocation(400, 400);
		setSize(new Dimension(300, 200));
		setMinimumSize(new Dimension(200, 200));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		this.listConstants = new JList();
		this.listConstants.setToolTipText("constants");
		JScrollPane scrollPaneCons = new JScrollPane(this.listConstants);
		scrollPaneCons
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPaneCons.setPreferredSize(new Dimension(300, 200));

		this.listVariables = new JList();
		this.listVariables.setToolTipText("variables");
		JScrollPane scrollPaneVars = new JScrollPane(this.listVariables);
		scrollPaneVars
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPaneVars.setPreferredSize(new Dimension(300, 200));

		JPanel varSelPanel = new JPanel();
		varSelPanel.setLayout(new BoxLayout(varSelPanel, BoxLayout.X_AXIS));
		varSelPanel.add(scrollPaneCons);
		varSelPanel.add(scrollPaneVars);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		this.makeConsButton = new JButton(Message.buttonMakeCons);
		this.makeConsButton.setToolTipText(Message.tooltipMakeCons);
		this.makeConsButton.setMinimumSize(new Dimension(56, 28));
		this.makeConsButton.setMaximumSize(new Dimension(74, 28));
		buttonPanel.add(this.makeConsButton);

		this.makeVarButton = new JButton(Message.buttonMakeVar);
		this.makeVarButton.setToolTipText(Message.tooltipMakeVar);
		this.makeVarButton.setMinimumSize(new Dimension(56, 28));
		this.makeVarButton.setMaximumSize(new Dimension(74, 28));
		buttonPanel.add(this.makeVarButton);

		this.acceptVarButton = new JButton(Message.buttonAcceptVar);
		this.acceptVarButton.setToolTipText(Message.tooltipAcceptVar);
		this.acceptVarButton.setMinimumSize(new Dimension(56, 28));
		this.acceptVarButton.setMaximumSize(new Dimension(74, 28));
		buttonPanel.add(this.acceptVarButton);

		this.rejectVarButton = new JButton(Message.buttonRejectVar);
		this.rejectVarButton.setToolTipText(Message.tooltipRejectVar);
		this.rejectVarButton.setMinimumSize(new Dimension(56, 28));
		this.rejectVarButton.setMaximumSize(new Dimension(74, 28));
		buttonPanel.add(this.rejectVarButton);

		mainPanel.add(varSelPanel);
		mainPanel.add(buttonPanel);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}

	public void updateLists() {
		this.listOfConstants = new ArrayList<LabelId>();
		for (String str : getModel().getConstants()) {
			this.listOfConstants
					.add(new LabelId(getModel().getLabel(str), str));
		}
		Vector<String> vectorOfLabelsOfConstants = new Vector<String>();
		for (LabelId elem : this.listOfConstants) {
			vectorOfLabelsOfConstants.add(elem.getLabel());
		}
		this.listConstants.setListData(vectorOfLabelsOfConstants);
		this.listConstants.setSelectedIndices(new int[0]);

		this.listOfVariables = new ArrayList<LabelId>();
		for (String str : getModel().getVariables()) {
			this.listOfVariables
					.add(new LabelId(getModel().getLabel(str), str));
		}
		Vector<String> vectorOfLabelsOfVariables = new Vector<String>();
		for (LabelId elem : this.listOfVariables) {
			vectorOfLabelsOfVariables.add(elem.getLabel());
		}
		this.listVariables.setListData(vectorOfLabelsOfVariables);
		this.listVariables.setSelectedIndices(new int[0]);
	}

}
