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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * This is the panel that shows the unifiers.
 * 
 * @author Julian Mendez
 */
public class UnifierView extends JDialog {

	private static final long serialVersionUID = 7965907233259580732L;

	private JButton buttonFirst = new JButton();
	private JButton buttonLast = new JButton();
	private JButton buttonNext = new JButton();
	private JButton buttonPrevious = new JButton();
	private JButton buttonSave = new JButton();
	private JButton buttonShowStatInfo = new JButton();
	private JButton buttonRefine = new JButton();
	private JTextArea textUnifier = new JTextArea();
	private JLabel labelUnifierId = new JLabel();

	public UnifierView() {
		super((Frame) null, "Unifier", true);
		UelUI.setupWindow(this, createUnifierPanel());
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

	private Component createUnifierPanel() {
		Container ret = new Box(BoxLayout.Y_AXIS);

		ret.add(createNavigateButtons(UelUI.GAP_SIZE));

		ret.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		UelUI.setupScrollTextArea(ret, textUnifier, Message.tooltipUnifier, new Dimension(640, 480));

		ret.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		ret.add(createUnifierButtons());

		return ret;
	}

	private Component createUnifierButtons() {
		JComponent unifierButtons = new JPanel();
		unifierButtons.setAlignmentX(CENTER_ALIGNMENT);

		UelUI.setupButton(unifierButtons, buttonSave, UelUI.ICON_SAVE, Message.tooltipSave);

		UelUI.setupButton(unifierButtons, buttonRefine, UelUI.ICON_REFINE, Message.tooltipRefine);

		return unifierButtons;
	}

	private Component createNavigateButtons(int gap) {
		JComponent navigateButtons = new JPanel();
		navigateButtons.setAlignmentX(CENTER_ALIGNMENT);

		UelUI.setupButton(navigateButtons, buttonFirst, UelUI.ICON_REWIND, Message.tooltipFirst);

		UelUI.setupButton(navigateButtons, buttonPrevious, UelUI.ICON_BACK, Message.tooltipPrevious);

		UelUI.setupLabel(navigateButtons, labelUnifierId, Message.tooltipUnifierId, new Dimension(40, 20));

		UelUI.setupButton(navigateButtons, buttonNext, UelUI.ICON_FORWARD, Message.tooltipNext);

		UelUI.setupButton(navigateButtons, buttonLast, UelUI.ICON_FAST_FORWARD, Message.tooltipLast);

		navigateButtons.add(Box.createHorizontalStrut(gap));

		UelUI.setupButton(navigateButtons, buttonShowStatInfo, UelUI.ICON_STATISTICS, Message.tooltipShowStatInfo);

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
		buttonLast.setEnabled(false);
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
