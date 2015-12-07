package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * This is the panel that shows the unifiers.
 * 
 * @author Julian Mendez
 */
public class UnifierView extends JDialog {

	private static final long serialVersionUID = 7965907233259580732L;

	private JButton buttonFirst = new JButton(UelIcon.ICON_REWIND);
	private JButton buttonLast = new JButton(UelIcon.ICON_FAST_FORWARD);
	private JButton buttonNext = new JButton(UelIcon.ICON_FORWARD);
	private JButton buttonPrevious = new JButton(UelIcon.ICON_BACK);
	private JButton buttonSave = new JButton(UelIcon.ICON_SAVE);
	private JButton buttonShowStatInfo = new JButton(UelIcon.ICON_STATISTICS);
	private JButton buttonRefine = new JButton(UelIcon.ICON_REFINE);
	private JTextArea textUnifier = new JTextArea();
	private JTextArea textUnifierId = new JTextArea();

	public UnifierView() {
		super((Frame) null, "Unifier", true);
		initFrame();
	}

	public void addButtonFirstListener(ActionListener listener, String actionCommand) {
		this.buttonFirst.addActionListener(listener);
		this.buttonFirst.setActionCommand(actionCommand);
	}

	public void addButtonLastListener(ActionListener listener, String actionCommand) {
		this.buttonLast.addActionListener(listener);
		this.buttonLast.setActionCommand(actionCommand);
	}

	public void addButtonNextListener(ActionListener listener, String actionCommand) {
		this.buttonNext.addActionListener(listener);
		this.buttonNext.setActionCommand(actionCommand);
	}

	public void addButtonPreviousListener(ActionListener listener, String actionCommand) {
		this.buttonPrevious.addActionListener(listener);
		this.buttonPrevious.setActionCommand(actionCommand);
	}

	public void addButtonSaveListener(ActionListener listener, String actionCommand) {
		this.buttonSave.addActionListener(listener);
		this.buttonSave.setActionCommand(actionCommand);
	}

	public void addButtonShowStatInfoListener(ActionListener listener, String actionCommand) {
		this.buttonShowStatInfo.addActionListener(listener);
		this.buttonShowStatInfo.setActionCommand(actionCommand);
	}

	public void addButtonRefineListener(ActionListener listener, String actionCommand) {
		this.buttonRefine.addActionListener(listener);
		this.buttonRefine.setActionCommand(actionCommand);
	}

	private Container createUnifierPanel() {
		Container ret = new Box(BoxLayout.Y_AXIS);

		this.textUnifier.setToolTipText(Message.tooltipUnifier);
		this.textUnifier.setWrapStyleWord(true);
		this.textUnifier.setLineWrap(true);

		Container navigateButtons = new Box(BoxLayout.X_AXIS);

		this.buttonFirst.setToolTipText(Message.tooltipFirst);
		UelIcon.setBorder(buttonFirst);
		navigateButtons.add(this.buttonFirst);

		this.buttonPrevious.setToolTipText(Message.tooltipPrevious);
		UelIcon.setBorder(buttonPrevious);
		navigateButtons.add(this.buttonPrevious);

		this.textUnifierId.setToolTipText(Message.tooltipUnifierId);
		this.textUnifierId.setEditable(false);
		navigateButtons.add(this.textUnifierId);

		this.buttonNext.setToolTipText(Message.tooltipNext);
		UelIcon.setBorder(buttonNext);
		navigateButtons.add(this.buttonNext);

		this.buttonLast.setToolTipText(Message.tooltipLast);
		UelIcon.setBorder(buttonLast);
		navigateButtons.add(this.buttonLast);

		navigateButtons.add(Box.createHorizontalStrut(40));

		this.buttonShowStatInfo.setToolTipText(Message.tooltipShowStatInfo);
		UelIcon.setBorder(buttonShowStatInfo);
		navigateButtons.add(this.buttonShowStatInfo);

		ret.add(navigateButtons);

		JScrollPane scrollPane = new JScrollPane(this.textUnifier);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(640, 480));

		ret.add(scrollPane);

		Container unifierButtons = new Box(BoxLayout.X_AXIS);

		this.buttonSave.setToolTipText(Message.tooltipSave);
		UelIcon.setBorder(buttonSave);
		unifierButtons.add(this.buttonSave);

		this.buttonRefine.setToolTipText(Message.tooltipRefine);
		UelIcon.setBorder(buttonRefine);
		unifierButtons.add(this.buttonRefine);

		ret.add(unifierButtons);

		return ret;
	}

	public void setUnifier(String text) {
		textUnifier.setText(text);
	}

	public void setUnifierId(String text) {
		textUnifierId.setText(text);
	}

	private void initFrame() {
		setLocation(400, 400);
		setSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(200, 200));
		setLayout(new GridBagLayout());
		getContentPane().add(createUnifierPanel());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
