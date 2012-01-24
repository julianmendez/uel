package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

/**
 * 
 * @author Julian Mendez
 */
class VarSelectionView extends JDialog {

	private static final long serialVersionUID = 1742164773153032359L;

	private JButton acceptVarButton = null;
	private JList listConstants = null;
	private List<LabelId> listOfConstants = null;
	private List<LabelId> listOfVariables = null;
	private JList listVariables = null;
	private JButton makeConsButton = null;
	private JButton makeVarButton = null;
	private final VarSelectionModel model;

	public VarSelectionView(VarSelectionModel m) {
		super((Frame) null, "Variable selection", true);

		if (m == null) {
			throw new IllegalArgumentException("Null argument.");
		}

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

	private JPanel createMainPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		this.listConstants = new JList();
		this.listConstants.setToolTipText("constants");
		JScrollPane scrollPaneCons = new JScrollPane(this.listConstants);
		scrollPaneCons
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPaneCons.setPreferredSize(new Dimension(360, 480));

		this.listVariables = new JList();
		this.listVariables.setToolTipText("variables");
		JScrollPane scrollPaneVars = new JScrollPane(this.listVariables);
		scrollPaneVars
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPaneVars.setPreferredSize(new Dimension(360, 480));

		JPanel varSelPanel = new JPanel();
		varSelPanel.setLayout(new BoxLayout(varSelPanel, BoxLayout.X_AXIS));
		varSelPanel.add(scrollPaneCons);
		varSelPanel.add(scrollPaneVars);

		JPanel buttonPanel = new JPanel();
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

		this.acceptVarButton = new JButton(new ImageIcon(this.getClass()
				.getClassLoader().getResource(Message.iconForward)));
		this.acceptVarButton.setToolTipText(Message.tooltipAcceptVar);
		this.acceptVarButton.setMinimumSize(new Dimension(56, 28));
		this.acceptVarButton.setMaximumSize(new Dimension(74, 28));
		buttonPanel.add(this.acceptVarButton);

		ret.add(buttonPanel);
		ret.add(varSelPanel);

		return ret;
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
		setSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(200, 200));
		setLayout(new GridBagLayout());
		getContentPane().add(createMainPanel());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void updateLists() {
		{
			Set<LabelId> setOfConstants = new TreeSet<LabelId>();
			for (String str : getModel().getConstants()) {
				setOfConstants.add(new LabelId(getModel().getLabel(str), str));
			}
			this.listOfConstants = new ArrayList<LabelId>();
			this.listOfConstants.addAll(setOfConstants);

			Vector<String> vectorOfLabelsOfConstants = new Vector<String>();
			for (LabelId elem : this.listOfConstants) {
				vectorOfLabelsOfConstants.add(elem.getLabel());
			}
			this.listConstants.setListData(vectorOfLabelsOfConstants);
			this.listConstants.setSelectedIndices(new int[0]);
		}
		{
			Set<LabelId> setOfVariables = new TreeSet<LabelId>();
			for (String str : getModel().getVariables()) {
				if (!getModel().getOriginalVariables().contains(str)) {
					setOfVariables.add(new LabelId(getModel().getLabel(str),
							str));
				}
			}
			this.listOfVariables = new ArrayList<LabelId>();
			this.listOfVariables.addAll(setOfVariables);

			Vector<String> vectorOfLabelsOfVariables = new Vector<String>();
			for (LabelId elem : this.listOfVariables) {
				vectorOfLabelsOfVariables.add(elem.getLabel());
			}
			this.listVariables.setListData(vectorOfLabelsOfVariables);
			this.listVariables.setSelectedIndices(new int[0]);
		}
	}

}
