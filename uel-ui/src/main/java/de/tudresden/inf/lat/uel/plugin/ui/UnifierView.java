package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * This is the panel that shows the unifiers.
 * 
 * @author Julian Mendez
 */
class UnifierView extends UelDialog {

	private static final long serialVersionUID = 7965907233259580732L;

	private final JButton buttonFirst = new JButton();
	private final JButton buttonLast = new JButton();
	private final JButton buttonNext = new JButton();
	private final JButton buttonPrevious = new JButton();
	private final JButton buttonSave = new JButton();
	private final JButton buttonShowStatInfo = new JButton();
	private final JButton buttonRefine = new JButton();
	private final JButton buttonUndoRefine = new JButton();
	private final JTextArea textUnifier = new JTextArea();
	private final JLabel labelUnifierId = new JLabel();

	public UnifierView(Component parent) {
		setup(parent, "Unifiers");
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

	public void addUndoRefineListener(ActionListener listener) {
		buttonUndoRefine.addActionListener(listener);
	}

	@Override
	protected JComponent createMainPanel() {
		JComponent mainPanel = UelUI.createVerticalPanel();

		mainPanel.add(createNavigateButtons(), BorderLayout.NORTH);

		mainPanel.add(UelUI.createScrollableTextArea(textUnifier, Message.tooltipUnifier), BorderLayout.CENTER);

		mainPanel.add(createUnifierButtons(), BorderLayout.SOUTH);

		return mainPanel;
	}

	private JComponent createUnifierButtons() {
		JComponent unifierButtons = UelUI.createButtonPanel();

		unifierButtons.add(UelUI.setupButton(buttonSave, UelUI.ICON_SAVE, Message.tooltipSave));

		unifierButtons.add(UelUI.setupButton(buttonRefine, UelUI.ICON_REFINE, Message.tooltipRefine));

		unifierButtons.add(UelUI.setupButton(buttonUndoRefine, UelUI.ICON_UNDO, Message.tooltipUndoRefine));

		return unifierButtons;
	}

	private JComponent createNavigateButtons() {
		JComponent navigateButtons = UelUI.createButtonPanel();

		navigateButtons.add(UelUI.setupButton(buttonFirst, UelUI.ICON_REWIND, Message.tooltipFirst));

		navigateButtons.add(UelUI.setupButton(buttonPrevious, UelUI.ICON_BACK, Message.tooltipPrevious));

		labelUnifierId.setToolTipText(Message.tooltipUnifierId);
		labelUnifierId.setHorizontalAlignment(SwingConstants.CENTER);
		navigateButtons.add(labelUnifierId);

		navigateButtons.add(UelUI.setupButton(buttonNext, UelUI.ICON_FORWARD, Message.tooltipNext));

		navigateButtons.add(UelUI.setupButton(buttonLast, UelUI.ICON_FAST_FORWARD, Message.tooltipLast));

		navigateButtons.add(UelUI.createStrut());

		navigateButtons.add(UelUI.setupButton(buttonShowStatInfo, UelUI.ICON_STATISTICS, Message.tooltipShowStatInfo));

		return navigateButtons;
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

	public void setFirstPreviousButtonsEnabled(boolean state) {
		buttonFirst.setEnabled(state);
		buttonPrevious.setEnabled(state);
	}

	public void setNextLastButtonsEnabled(boolean state) {
		buttonNext.setEnabled(state);
		buttonLast.setEnabled(state);
	}

	public void setSaveRefineButtonsEnabled(boolean state) {
		buttonSave.setEnabled(state);
		buttonRefine.setEnabled(state);
	}

	public void setUndoRefineButtonEnabled(boolean state) {
		buttonUndoRefine.setEnabled(state);
	}

}
