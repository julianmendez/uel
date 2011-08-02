package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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

	private JButton buttonFirst = new JButton(Message.buttonFirst);
	private JButton buttonLast = new JButton(Message.buttonLast);
	private JButton buttonNext = new JButton(Message.buttonNext);
	private JButton buttonOpen = new JButton(Message.buttonOpen);
	private JButton buttonPrevious = new JButton(Message.buttonPrevious);
	private JButton buttonSave = new JButton(Message.buttonSave);
	private JButton buttonSelectVariables = new JButton(
			Message.buttonSelectVariables);
	private JComboBox listClassName00 = new JComboBox();
	private JComboBox listClassName01 = new JComboBox();
	private DefaultListModel listmodel = new DefaultListModel();
	private JComboBox listOntologyName00 = new JComboBox();
	private JComboBox listOntologyName01 = new JComboBox();
	private UelProcessor model = null;
	private JTextArea textUnifier = new JTextArea();
	private JTextArea textUnifierId = new JTextArea();

	public UelView(UelProcessor processor) {
		if (processor == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.model = processor;
		init();
	}

	public void addButtonFirstListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonFirst.addActionListener(listener);
		this.buttonFirst.setActionCommand(actionCommand);
	}

	public void addButtonLastListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonLast.addActionListener(listener);
		this.buttonLast.setActionCommand(actionCommand);
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

	public void addButtonOpenListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonOpen.addActionListener(listener);
		this.buttonOpen.setActionCommand(actionCommand);
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

	public void addButtonSelectVariablesListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonSelectVariables.addActionListener(listener);
		this.buttonSelectVariables.setActionCommand(actionCommand);
	}

	public void addComboBoxClass00Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listClassName00.addActionListener(listener);
		this.listClassName00.setActionCommand(actionCommand);
	}

	public void addComboBoxClass01Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listClassName00.addActionListener(listener);
		this.listClassName00.setActionCommand(actionCommand);
	}

	public void addComboBoxOntology00Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyName00.addActionListener(listener);
		this.listOntologyName00.setActionCommand(actionCommand);
	}

	public void addComboBoxOntology01Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyName01.addActionListener(listener);
		this.listOntologyName01.setActionCommand(actionCommand);
	}

	private JPanel createControlPanel() {
		JPanel ret = new JPanel(new GridLayout());
		ret.add(createSelectionPanel());
		ret.add(createUnifierPanel());
		return ret;
	}

	private JPanel createSelectionPanel() {
		JPanel ret = new JPanel(new GridBagLayout());
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		JPanel smallPanel = new JPanel();
		this.buttonOpen.setToolTipText(Message.tooltipOpen);
		smallPanel.add(this.buttonOpen);
		this.buttonSelectVariables
				.setToolTipText(Message.tooltipSelectVariables);
		smallPanel.add(this.buttonSelectVariables);
		ret.add(smallPanel);

		this.listOntologyName00.setPreferredSize(new Dimension(280, 28));
		this.listOntologyName00.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listOntologyName00);

		this.listClassName00.setPreferredSize(new Dimension(280, 28));
		this.listClassName00.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listClassName00);

		JLabel gap = new JLabel();
		gap.setPreferredSize(new Dimension(280, 28));
		gap.setMinimumSize(new Dimension(112, 28));
		ret.add(gap);

		this.listOntologyName01.setPreferredSize(new Dimension(280, 28));
		this.listOntologyName01.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listOntologyName01);

		this.listClassName01.setPreferredSize(new Dimension(280, 28));
		this.listClassName01.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listClassName01);

		return ret;
	}

	private JPanel createUnifierPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		this.textUnifier.setToolTipText(Message.tooltipUnifier);
		this.textUnifier.setWrapStyleWord(true);
		this.textUnifier.setLineWrap(true);

		JScrollPane scrollPane = new JScrollPane(this.textUnifier);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(400, 200));
		ret.add(scrollPane);

		JPanel smallPanel = new JPanel();

		this.buttonFirst.setToolTipText(Message.tooltipFirst);
		smallPanel.add(this.buttonFirst);

		this.buttonPrevious.setToolTipText(Message.tooltipPrevious);
		smallPanel.add(this.buttonPrevious);

		this.textUnifierId.setToolTipText(Message.tooltipUnifierId);
		this.textUnifierId.setEditable(false);
		smallPanel.add(this.textUnifierId);

		this.buttonNext.setToolTipText(Message.tooltipNext);
		smallPanel.add(this.buttonNext);

		this.buttonLast.setToolTipText(Message.tooltipLast);
		smallPanel.add(this.buttonLast);

		this.buttonSave.setToolTipText(Message.tooltipSave);
		smallPanel.add(this.buttonSave);

		ret.add(smallPanel);
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

	public JTextArea getUnifierId() {
		return this.textUnifierId;
	}

	public void init() {
		setLayout(new GridBagLayout());
		add(createControlPanel());
	}

	public void reloadClassNames00(List<String> list) {
		if (list == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listClassName00.removeAllItems();
		for (String className : list) {
			this.listClassName00.addItem(className);
		}
	}

	public void reloadClassNames01(List<String> list) {
		if (list == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listClassName01.removeAllItems();
		for (String className : list) {
			this.listClassName01.addItem(className);
		}
	}

	public void reloadOntologies(List<String> listOfOntologyNames) {
		if (listOfOntologyNames == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyName00.removeAllItems();
		this.listOntologyName01.removeAllItems();
		for (String ontologyName : listOfOntologyNames) {
			this.listOntologyName00.addItem(ontologyName);
			this.listOntologyName01.addItem(ontologyName);
		}
		this.listClassName00.removeAllItems();
		this.listClassName01.removeAllItems();
	}

	public void setButtonFirstEnabled(boolean b) {
		this.buttonFirst.setEnabled(b);
	}

	public void setButtonLastEnabled(boolean b) {
		this.buttonLast.setEnabled(b);
	}

	public void setButtonLoadEnabled(boolean b) {
		this.buttonOpen.setEnabled(b);
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

	public void setButtonSelectVariablesEnabled(boolean b) {
		this.buttonSelectVariables.setEnabled(b);
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
