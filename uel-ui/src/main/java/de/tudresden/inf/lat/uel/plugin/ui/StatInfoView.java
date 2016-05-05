package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextArea;

/**
 * This is the panel that shows statistical information.
 * 
 * @author Julian Mendez
 */
class StatInfoView extends UelDialog {

	private static final long serialVersionUID = -4153981096827550491L;

	private final JButton saveButton = new JButton();
	private final JTextArea textGoal = new JTextArea();
	private final JTextArea textInfo = new JTextArea();

	public StatInfoView(Component parent) {
		setup(parent, "Additional information");
	}

	public void addSaveListener(ActionListener listener) {
		saveButton.addActionListener(listener);
	}

	private JComponent createButtonPanel() {
		JComponent buttonPanel = UelUI.createButtonPanel();

		buttonPanel.add(UelUI.setupButton(saveButton, UelUI.ICON_SAVE, Message.tooltipSaveGoal));

		return buttonPanel;
	}

	@Override
	protected JComponent createMainPanel() {
		JComponent mainPanel = UelUI.createVerticalPanel();

		mainPanel.add(createButtonPanel(), BorderLayout.NORTH);

		mainPanel.add(UelUI.createScrollableTextArea(textGoal, Message.tooltipGoal), BorderLayout.CENTER);

		mainPanel.add(UelUI.createScrollableTextArea(textInfo, Message.tooltipTextInfo), BorderLayout.SOUTH);

		return mainPanel;
	}

	public void setGoalText(String text) {
		textGoal.setText(text);
	}

	public void setInfoText(String text) {
		textInfo.setText(text);
	}

}
