package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * This dialog allows to select the variables for the unification problem.
 * 
 * @author Julian Mendez
 */
class VarSelectionView extends UelDialog {

	private static final long serialVersionUID = 1742164773153032359L;

	private final JButton acceptVarButton = new JButton();
	private final JList<LabelId> listConstants = new JList<LabelId>();
	private final JList<LabelId> listVariables = new JList<LabelId>();
	private final JButton makeConsButton = new JButton();
	private final JButton makeVarButton = new JButton();

	/**
	 * Construct a new variable selection dialog.
	 * 
	 * @param parent
	 *            parent
	 */
	public VarSelectionView(Component parent) {
		setup(parent, "Variable selection");
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

	private JComponent createButtonPanel() {
		JComponent buttonPanel = UelUI.createButtonPanel();

		buttonPanel.add(UelUI.setupButton(makeConsButton, UelUI.ICON_STEP_BACK, Message.tooltipMakeCons));

		buttonPanel.add(UelUI.setupButton(makeVarButton, UelUI.ICON_STEP_FORWARD, Message.tooltipMakeVar));

		buttonPanel.add(UelUI.createStrut());

		buttonPanel.add(UelUI.setupButton(acceptVarButton, UelUI.ICON_FORWARD, Message.tooltipAcceptVar));

		return buttonPanel;
	}

	@Override
	protected JComponent createMainPanel() {
		JComponent mainPanel = UelUI.createVerticalPanel();

		mainPanel.add(createButtonPanel(), BorderLayout.NORTH);

		mainPanel.add(createVarSelectionPanel(), BorderLayout.CENTER);

		return mainPanel;
	}

	private JComponent createVarSelectionPanel() {
		JComponent varSelectionPanel = new JPanel(new GridLayout(1, 2, UelUI.GAP_SIZE, 0));

		JComponent leftPanel = UelUI.createVerticalPanel();
		varSelectionPanel.add(leftPanel);

		leftPanel.add(new JLabel(Message.textConstants), BorderLayout.NORTH);

		leftPanel.add(UelUI.createScrollPane(UelUI.setupList(listConstants, Message.tooltipConstants, false), true),
				BorderLayout.CENTER);

		JComponent rightPanel = UelUI.createVerticalPanel();
		varSelectionPanel.add(rightPanel);

		rightPanel.add(new JLabel(Message.textVariables), BorderLayout.NORTH);

		rightPanel.add(UelUI.createScrollPane(UelUI.setupList(listVariables, Message.tooltipVariables, false), true),
				BorderLayout.CENTER);

		return varSelectionPanel;
	}

	public Collection<LabelId> getSelectedConstants() {
		return this.listConstants.getSelectedValuesList();
	}

	public Collection<LabelId> getSelectedVariables() {
		return this.listVariables.getSelectedValuesList();
	}

	public void setConstants(List<LabelId> constants) {
		this.listConstants.setListData(new Vector<>(constants));
		this.listConstants.clearSelection();
	}

	public void setVariables(List<LabelId> variables) {
		this.listVariables.setListData(new Vector<>(variables));
		this.listVariables.clearSelection();
	}

}
