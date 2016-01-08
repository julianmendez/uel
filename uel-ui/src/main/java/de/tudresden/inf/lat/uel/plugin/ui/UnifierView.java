package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * This is the panel that shows the unifiers.
 * 
 * @author Julian Mendez
 */
public class UnifierView extends UelDialog {

	private static final long serialVersionUID = 7965907233259580732L;

	private final JButton buttonFirst = new JButton();
	private final JButton buttonLast = new JButton();
	private final JButton buttonNext = new JButton();
	private final JButton buttonPrevious = new JButton();
	private final JButton buttonSave = new JButton();
	private final JButton buttonShowStatInfo = new JButton();
	private final JButton buttonRefine = new JButton();
	private final JTextArea textUnifier = new JTextArea();
	private final JLabel labelUnifierId = new JLabel();

	public UnifierView() {
		setup("Unifiers");
	}

	public void addFirstListener(ActionListener listener) {
		buttonFirst.addActionListener(listener);
	}

	public void addLastListener(ActionListener listener) {
		buttonLast.addActionListener(listener);
	}

	public void addNextListener(ActionListener listener) {
		buttonNext.addActionListener(listener);
	}

	public void addPreviousListener(ActionListener listener) {
		buttonPrevious.addActionListener(listener);
	}

	public void addSaveListener(ActionListener listener) {
		buttonSave.addActionListener(listener);
	}

	public void addShowStatInfoListener(ActionListener listener) {
		buttonShowStatInfo.addActionListener(listener);
	}

	public void addRefineListener(ActionListener listener) {
		buttonRefine.addActionListener(listener);
	}

	@Override
	protected void addMainPanel(Container parent) {
		Container mainPanel = UelUI.addVerticalPanel(parent);

		addNavigateButtons(mainPanel);

		UelUI.addStrut(mainPanel);

		UelUI.addScrollableTextArea(mainPanel, textUnifier, Message.tooltipUnifier, new Dimension(640, 480));

		UelUI.addStrut(mainPanel);

		addUnifierButtons(mainPanel);
	}

	private void addUnifierButtons(Container parent) {
		Container unifierButtons = UelUI.addButtonPanel(parent);

		UelUI.setupButton(unifierButtons, buttonSave, UelUI.ICON_SAVE, Message.tooltipSave);

		UelUI.setupButton(unifierButtons, buttonRefine, UelUI.ICON_REFINE, Message.tooltipRefine);
	}

	private void addNavigateButtons(Container parent) {
		Container navigateButtons = UelUI.addButtonPanel(parent);

		UelUI.setupButton(navigateButtons, buttonFirst, UelUI.ICON_REWIND, Message.tooltipFirst);

		UelUI.setupButton(navigateButtons, buttonPrevious, UelUI.ICON_BACK, Message.tooltipPrevious);

		UelUI.setupLabel(navigateButtons, labelUnifierId, Message.tooltipUnifierId, new Dimension(40, 20));
		labelUnifierId.setHorizontalAlignment(SwingConstants.CENTER);

		UelUI.setupButton(navigateButtons, buttonNext, UelUI.ICON_FORWARD, Message.tooltipNext);

		UelUI.setupButton(navigateButtons, buttonLast, UelUI.ICON_FAST_FORWARD, Message.tooltipLast);

		UelUI.addStrut(navigateButtons);

		UelUI.setupButton(navigateButtons, buttonShowStatInfo, UelUI.ICON_STATISTICS, Message.tooltipShowStatInfo);
	}

	public void setUnifier(String text) {
		textUnifier.setText(text);
	}

	public void setUnifierId(String text) {
		labelUnifierId.setText(text);
	}

	public void initializeButtons() {
		buttonFirst.setEnabled(false);
		buttonPrevious.setEnabled(false);
		buttonNext.setEnabled(true);
		buttonLast.setEnabled(true);
		buttonSave.setEnabled(false);
		buttonRefine.setEnabled(false);
		buttonShowStatInfo.setEnabled(true);
	}

	public void setFirstPreviousButtons(boolean state) {
		buttonFirst.setEnabled(state);
		buttonPrevious.setEnabled(state);
	}

	public void setNextLastButtons(boolean state) {
		buttonNext.setEnabled(state);
		buttonLast.setEnabled(state);
	}

	public void setSaveRefineButtons(boolean state) {
		buttonSave.setEnabled(state);
		buttonRefine.setEnabled(state);
	}

}
