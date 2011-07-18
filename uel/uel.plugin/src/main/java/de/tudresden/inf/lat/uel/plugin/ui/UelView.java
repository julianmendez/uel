package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.tudresden.inf.lat.uel.plugin.processor.UelProcessor;

/**
 * Panel for UEL.
 * 
 * @author Julian Mendez
 */
public class UelView extends JPanel {

	private static final long serialVersionUID = 9096602357606632334L;

	private JButton buttonGetVar = null;
	private JButton buttonNext = null;
	private JButton buttonPrevious = null;
	private JButton buttonSave = null;
	private JComboBox classNameList00 = null;
	private JComboBox classNameList01 = null;
	private DefaultListModel listmodel = new DefaultListModel();
	private UelProcessor model = null;
	private JTextArea unifier = null;

	public UelView(UelProcessor processor) {
		if (processor == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.model = processor;
		init();
	}

	public void addButtonGetVarListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonGetVar.addActionListener(listener);
		this.buttonGetVar.setActionCommand(actionCommand);
	}

	public void addButtonNextListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonNext.addActionListener(listener);
		this.buttonNext.setActionCommand(actionCommand);
	}

	public void addButtonPreviousListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonPrevious.addActionListener(listener);
		this.buttonPrevious.setActionCommand(actionCommand);
	}

	public void addButtonSaveListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonSave.addActionListener(listener);
		this.buttonSave.setActionCommand(actionCommand);
	}

	public DefaultListModel getListModel() {
		return this.listmodel;
	}

	public UelProcessor getModel() {
		return this.model;
	}

	public int getSelectedIndex00() {
		return this.classNameList00.getSelectedIndex();
	}

	public int getSelectedIndex01() {
		return this.classNameList01.getSelectedIndex();
	}

	public JTextArea getUnifier() {
		return this.unifier;
	}

	public void init() {
		this.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(initControlPanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel initControlPanel() {
		JPanel ret = new JPanel(new FlowLayout());
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		JPanel textPanel = new JPanel();
		textPanel.setMinimumSize(new Dimension(280, 28));
		JLabel label00 = new JLabel(Message.labelUel);
		label00.setPreferredSize(new Dimension(280, 28));
		textPanel.add(label00);

		JPanel selectionPanel = new JPanel();
		selectionPanel
				.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));

		this.classNameList00 = new JComboBox();
		this.classNameList00.setPreferredSize(new Dimension(112, 28));
		this.classNameList00.setMinimumSize(new Dimension(56, 28));
		this.classNameList00.setMaximumSize(new Dimension(280, 28));
		selectionPanel.add(this.classNameList00);
		this.classNameList01 = new JComboBox();
		this.classNameList01.setPreferredSize(new Dimension(112, 28));
		this.classNameList01.setMinimumSize(new Dimension(56, 28));
		this.classNameList01.setMaximumSize(new Dimension(280, 28));
		selectionPanel.add(this.classNameList01);

		JPanel computePanel = new JPanel(new FlowLayout());
		this.buttonGetVar = new JButton(Message.buttonGetVar);
		this.buttonGetVar.setToolTipText(Message.tooltipGetVar);
		computePanel.add(this.buttonGetVar);

		JPanel unifierPanel = new JPanel(new FlowLayout());

		this.unifier = new JTextArea();
		this.unifier.setToolTipText("unifier");
		this.unifier.setWrapStyleWord(true);
		this.unifier.setLineWrap(true);
		this.unifier.setPreferredSize(new Dimension(400, 200));
		JScrollPane scroll = new JScrollPane(this.unifier,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		unifierPanel.add(scroll);
		this.buttonPrevious = new JButton(Message.buttonPrevious);
		this.buttonPrevious.setToolTipText(Message.tooltipPrevious);
		unifierPanel.add(this.buttonPrevious);
		this.buttonNext = new JButton(Message.buttonNext);
		this.buttonNext.setToolTipText(Message.tooltipNext);
		unifierPanel.add(this.buttonNext);
		this.buttonSave = new JButton(Message.buttonSave);
		this.buttonSave.setToolTipText(Message.tooltipSave);
		unifierPanel.add(this.buttonSave);

		ret.add(textPanel);
		ret.add(selectionPanel);
		ret.add(computePanel);
		ret.add(unifierPanel);

		return ret;
	}

	public void refresh(List<String> classNameSet) {
		if (classNameSet == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		for (String cls : classNameSet) {
			this.classNameList00.addItem(cls);
			this.classNameList01.addItem(cls);
		}
	}

	public void setButtonGetVarEnabled(boolean b) {
		this.buttonGetVar.setEnabled(b);
	}

	public void setButtonNextEnabled(boolean b) {
		this.buttonNext.setEnabled(b);
	}

	public void setButtonPreviousEnabled(boolean b) {
		this.buttonPrevious.setEnabled(b);
	}

	public void setButtonSaveEnabled(boolean b) {
		this.buttonSave.setEnabled(b);
	}

}
