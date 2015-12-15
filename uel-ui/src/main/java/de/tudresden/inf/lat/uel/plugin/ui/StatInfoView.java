package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * This is the panel that shows statistical information.
 * 
 * @author Julian Mendez
 */
class StatInfoView extends JDialog {

	private static final long serialVersionUID = -4153981096827550491L;

	private final JButton saveButton = new JButton();
	private final JTextArea textGoal = new JTextArea();
	private final JTextArea textInfo = new JTextArea();

	public StatInfoView() {
		super((Frame) null, "Statistical information", true);
		UelUI.setupWindow(this, createMainPanel());
	}

	public void addSaveListener(ActionListener listener) {
		saveButton.addActionListener(listener);
	}

	private Component createMainPanel() {
		Container mainPanel = new Box(BoxLayout.Y_AXIS);

		JComponent buttonPanel = new JPanel();
		buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
		UelUI.setupButton(buttonPanel, saveButton, UelUI.ICON_SAVE, Message.tooltipSaveGoal);
		mainPanel.add(buttonPanel);

		mainPanel.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		UelUI.setupScrollTextArea(mainPanel, textGoal, Message.tooltipGoal, new Dimension(640, 240));

		mainPanel.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		UelUI.setupScrollTextArea(mainPanel, textInfo, Message.tooltipTextInfo, new Dimension(640, 120));

		return mainPanel;
	}

	public void setGoalText(String text) {
		textGoal.setText(text);
	}

	public void setInfoText(String text) {
		textInfo.setText(text);
	}

}
