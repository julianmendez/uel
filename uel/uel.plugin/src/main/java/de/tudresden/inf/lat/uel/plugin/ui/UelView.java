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

	private JButton buttonGetNames = new JButton(Message.buttonGetNames);;
	private JButton buttonGetVar = new JButton(Message.buttonGetVar);;
	private JButton buttonNext = new JButton(Message.buttonNext);
	private JButton buttonPrevious = new JButton(Message.buttonPrevious);
	private JButton buttonSave = new JButton(Message.buttonSave);
	private JComboBox listClassName00 = new JComboBox();;
	private JComboBox listClassName01 = new JComboBox();;
	private DefaultListModel listmodel = new DefaultListModel();
	private JComboBox listOntologyName00 = new JComboBox();;
	private JComboBox listOntologyName01 = new JComboBox();;
	private UelProcessor model = null;
	private JTextArea textUnifier = new JTextArea();;

	public UelView(UelProcessor processor) {
		if (processor == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.model = processor;
		init();
	}

	public void addButtonGetNamesListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonGetNames.addActionListener(listener);
		this.buttonGetNames.setActionCommand(actionCommand);
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

	private JPanel createControlPanel() {
		JPanel ret = new JPanel(new FlowLayout());
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
		ret.add(createTextPanel());
		ret.add(createSelectionPanel());
		ret.add(createExecutionPanel());
		ret.add(createUnifierPanel());
		return ret;
	}

	private JPanel createExecutionPanel() {
		JPanel computePanel = new JPanel(new FlowLayout());
		this.buttonGetNames.setToolTipText(Message.tooltipGetNames);
		computePanel.add(this.buttonGetNames);

		this.buttonGetVar.setToolTipText(Message.tooltipGetVar);
		computePanel.add(this.buttonGetVar);
		return computePanel;
	}

	private JPanel createSelectionPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		this.listOntologyName00.setPreferredSize(new Dimension(112, 28));
		this.listOntologyName00.setMinimumSize(new Dimension(56, 28));
		this.listOntologyName00.setMaximumSize(new Dimension(280, 28));
		ret.add(this.listOntologyName00);

		this.listOntologyName01.setPreferredSize(new Dimension(112, 28));
		this.listOntologyName01.setMinimumSize(new Dimension(56, 28));
		this.listOntologyName01.setMaximumSize(new Dimension(280, 28));
		ret.add(this.listOntologyName01);

		this.listClassName00.setPreferredSize(new Dimension(112, 28));
		this.listClassName00.setMinimumSize(new Dimension(56, 28));
		this.listClassName00.setMaximumSize(new Dimension(280, 28));
		ret.add(this.listClassName00);

		this.listClassName01.setPreferredSize(new Dimension(112, 28));
		this.listClassName01.setMinimumSize(new Dimension(56, 28));
		this.listClassName01.setMaximumSize(new Dimension(280, 28));
		ret.add(this.listClassName01);
		return ret;
	}

	private JPanel createTextPanel() {
		JPanel ret = new JPanel();
		ret.setMinimumSize(new Dimension(280, 28));
		JLabel label00 = new JLabel(Message.labelUel);
		label00.setPreferredSize(new Dimension(280, 28));
		ret.add(label00);
		return ret;
	}

	private JPanel createUnifierPanel() {
		JPanel ret = new JPanel(new FlowLayout());
		this.textUnifier.setToolTipText(Message.tooltipUnifier);
		this.textUnifier.setWrapStyleWord(true);
		this.textUnifier.setLineWrap(true);

		JScrollPane scrollPane = new JScrollPane(this.textUnifier);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(400, 200));
		ret.add(scrollPane);

		this.buttonPrevious.setToolTipText(Message.tooltipPrevious);
		ret.add(this.buttonPrevious);

		this.buttonNext.setToolTipText(Message.tooltipNext);
		ret.add(this.buttonNext);

		this.buttonSave.setToolTipText(Message.tooltipSave);
		ret.add(this.buttonSave);
		return ret;
	}

	public DefaultListModel getListModel() {
		return this.listmodel;
	}

	public UelProcessor getModel() {
		return this.model;
	}

	public int getSelectedClassName00() {
		return this.listClassName00.getSelectedIndex();
	}

	public int getSelectedClassName01() {
		return this.listClassName01.getSelectedIndex();
	}

	public int getSelectedOntologyName00() {
		return this.listOntologyName00.getSelectedIndex();
	}

	public int getSelectedOntologyName01() {
		return this.listOntologyName01.getSelectedIndex();
	}

	public JTextArea getUnifier() {
		return this.textUnifier;
	}

	public void init() {
		this.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(createControlPanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}

	public void reloadClassNames(List<String> firstList, List<String> secondList) {
		if (firstList == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (secondList == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// this.listClassName00.removeAll();
		for (String className : firstList) {
			this.listClassName00.addItem(className);
		}

		// this.listClassName01.removeAll();
		for (String className : secondList) {
			this.listClassName01.addItem(className);
		}
	}

	public void reloadOntologies(List<String> listOfOntologyNames) {
		if (listOfOntologyNames == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// this.listOntologyName00.removeAll();
		// this.listOntologyName01.removeAll();
		for (String ontologyName : listOfOntologyNames) {
			this.listOntologyName00.addItem(ontologyName);
			this.listOntologyName01.addItem(ontologyName);
		}
	}

	public void setButtonGetNameEnabled(boolean b) {
		this.buttonGetNames.setEnabled(b);
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

	public void setComboBoxClassName00Enabled(boolean b) {
		this.listClassName00.setEnabled(b);
	}

	public void setComboBoxClassName01Enabled(boolean b) {
		this.listClassName01.setEnabled(b);
	}

	public void setComboBoxOntologyName00Enabled(boolean b) {
		this.listOntologyName00.setEnabled(b);
	}

	public void setComboBoxOntologyName01Enabled(boolean b) {
		this.listOntologyName01.setEnabled(b);
	}

}
