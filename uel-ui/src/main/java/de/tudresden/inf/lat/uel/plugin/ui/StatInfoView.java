package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
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

	public StatInfoView() {
		setup("Additional information");
	}

	public void addSaveListener(ActionListener listener) {
		saveButton.addActionListener(listener);
	}

	@Override
	protected void addMainPanel(Container parent) {
		Container mainPanel = UelUI.addVerticalPanel(parent);

		Container buttonPanel = UelUI.addButtonPanel(mainPanel);

		UelUI.setupButton(buttonPanel, saveButton, UelUI.ICON_SAVE, Message.tooltipSaveGoal);

		UelUI.addStrut(mainPanel);

		UelUI.addScrollableTextArea(mainPanel, textGoal, Message.tooltipGoal, new Dimension(640, 240));

		UelUI.addStrut(mainPanel);

		UelUI.addScrollableTextArea(mainPanel, textInfo, Message.tooltipTextInfo, new Dimension(640, 120));
	}

	public void setGoalText(String text) {
		textGoal.setText(text);
	}

	public void setInfoText(String text) {
		textInfo.setText(text);
	}

}
