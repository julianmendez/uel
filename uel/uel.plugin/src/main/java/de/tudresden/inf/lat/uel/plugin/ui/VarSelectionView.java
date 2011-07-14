package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

class VarSelectionView extends JFrame {

	private static final long serialVersionUID = 1742164773153032359L;

	private JButton acceptVarButton = null;
	private Map<String, JCheckBox> mainMap = new HashMap<String, JCheckBox>();
	private Set<String> model = null;
	private JButton rejectVarButton = null;

	public VarSelectionView(Set<String> m) {
		super("Variable selection");
		this.model = m;
		initVarFrame();
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

	public Set<String> getModel() {
		return Collections.unmodifiableSet(this.model);
	}

	public Set<String> getSelectedValues() {
		Set<String> ret = new TreeSet<String>();

		for (String key : this.mainMap.keySet()) {
			if (this.mainMap.get(key).isSelected()) {
				ret.add(key);
			}
		}
		return ret;
	}

	private void initVarFrame() {
		setMinimumSize(new Dimension(200, 200));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel panel = new JPanel();
		Set<String> candidates = new TreeSet<String>();
		candidates.addAll(getModel());
		int n = getModel().size();
		panel.setLayout(new GridLayout(n, 1));
		for (String candidate : candidates) {
			JCheckBox checkBox = new JCheckBox(candidate, true);
			this.mainMap.put(candidate, checkBox);
			panel.add(checkBox);
		}

		JPanel varSelPanel = new JPanel();
		varSelPanel.setLayout(new BoxLayout(varSelPanel, BoxLayout.Y_AXIS));
		varSelPanel.setMinimumSize(new Dimension(200, 200));
		varSelPanel.setMaximumSize(new Dimension(300, 300));

		JPanel buttonPanel = new JPanel(new FlowLayout());
		this.acceptVarButton = new JButton(Message.acceptVarButton);
		this.acceptVarButton.setToolTipText(Message.acceptVarTooltip);
		this.acceptVarButton.setMinimumSize(new Dimension(56, 28));
		this.acceptVarButton.setMaximumSize(new Dimension(74, 28));
		buttonPanel.add(this.acceptVarButton);

		this.rejectVarButton = new JButton(Message.rejectVarButton);
		this.rejectVarButton.setToolTipText(Message.rejectVarTooltip);
		this.rejectVarButton.setMinimumSize(new Dimension(56, 28));
		this.rejectVarButton.setMaximumSize(new Dimension(74, 28));
		buttonPanel.add(this.rejectVarButton);

		JScrollPane scroll = new JScrollPane(panel);
		scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new JLabel("1",
				JLabel.CENTER));
		scroll.setCorner(JScrollPane.LOWER_RIGHT_CORNER, new JLabel("2",
				JLabel.CENTER));

		mainPanel.add(scroll);
		mainPanel.add(buttonPanel);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}

}
