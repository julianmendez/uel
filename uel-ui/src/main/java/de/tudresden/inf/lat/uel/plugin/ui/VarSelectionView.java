package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

/**
 * This is the panel to select the variables in a given unification problem.
 * 
 * @author Julian Mendez
 */
class VarSelectionView extends JDialog {

	private static final long serialVersionUID = 1742164773153032359L;

	private final JButton acceptVarButton = new JButton(UelIcon.ICON_FORWARD);
	private JList<LabelId> listConstants = null;
	private JList<LabelId> listVariables = null;
	private final JButton makeConsButton = new JButton(UelIcon.ICON_STEP_BACK);
	private final JButton makeVarButton = new JButton(UelIcon.ICON_STEP_FORWARD);
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

	public void addAcceptVarButtonListener(ActionListener listener, String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.acceptVarButton.addActionListener(listener);
		this.acceptVarButton.setActionCommand(actionCommand);
	}

	public void addMakeConsButtonListener(ActionListener listener, String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.makeConsButton.addActionListener(listener);
		this.makeConsButton.setActionCommand(actionCommand);
	}

	public void addMakeVarButtonListener(ActionListener listener, String actionCommand) {
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

		this.listConstants = new JList<LabelId>();
		this.listConstants.setToolTipText("constants");
		JScrollPane scrollPaneCons = new JScrollPane(this.listConstants);
		scrollPaneCons.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPaneCons.setPreferredSize(new Dimension(360, 480));

		this.listVariables = new JList<LabelId>();
		this.listVariables.setToolTipText("variables");
		JScrollPane scrollPaneVars = new JScrollPane(this.listVariables);
		scrollPaneVars.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPaneVars.setPreferredSize(new Dimension(360, 480));

		JPanel varSelPanel = new JPanel();
		varSelPanel.setLayout(new BoxLayout(varSelPanel, BoxLayout.X_AXIS));
		varSelPanel.add(scrollPaneCons);
		varSelPanel.add(scrollPaneVars);

		JPanel buttonPanel = new JPanel();
		this.makeConsButton.setToolTipText(Message.tooltipMakeCons);
		this.makeConsButton.setMinimumSize(new Dimension(56, 28));
		this.makeConsButton.setMaximumSize(new Dimension(74, 28));
		UelIcon.setBorder(makeConsButton);
		buttonPanel.add(this.makeConsButton);

		this.makeVarButton.setToolTipText(Message.tooltipMakeVar);
		this.makeVarButton.setMinimumSize(new Dimension(56, 28));
		this.makeVarButton.setMaximumSize(new Dimension(74, 28));
		UelIcon.setBorder(makeVarButton);
		buttonPanel.add(this.makeVarButton);

		this.acceptVarButton.setToolTipText(Message.tooltipAcceptVar);
		this.acceptVarButton.setMinimumSize(new Dimension(56, 28));
		this.acceptVarButton.setMaximumSize(new Dimension(74, 28));
		UelIcon.setBorder(acceptVarButton);
		buttonPanel.add(this.acceptVarButton);

		ret.add(buttonPanel);
		ret.add(varSelPanel);

		return ret;
	}

	public VarSelectionModel getModel() {
		return this.model;
	}

	public Collection<LabelId> getSelectedConstants() {
		return this.listConstants.getSelectedValuesList();
	}

	public Collection<LabelId> getSelectedVariables() {
		return this.listVariables.getSelectedValuesList();
	}

	private void initVarFrame() {
		setLocation(400, 400);
		setSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(200, 200));
		setLayout(new GridBagLayout());
		getContentPane().add(createMainPanel());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private void updateListOfConstants() {
		List<LabelId> list = getModel().getConstants();
		LabelId[] array = list.toArray(new LabelId[list.size()]);
		this.listConstants.setListData(array);
		this.listConstants.clearSelection();
	}

	private void updateListOfVariables() {
		List<LabelId> list = getModel().getVariables();
		LabelId[] array = list.toArray(new LabelId[list.size()]);
		this.listVariables.setListData(array);
		this.listVariables.clearSelection();
	}

	public void updateLists() {
		updateListOfConstants();
		updateListOfVariables();
	}

}
