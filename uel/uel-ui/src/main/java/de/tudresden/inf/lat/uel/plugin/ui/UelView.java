package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UelProcessorFactory;

/**
 * This is the main panel of the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelView extends JPanel {

	private static final long serialVersionUID = 9096602357606632334L;

	private JButton buttonOpen = new JButton(new ImageIcon(this.getClass()
			.getClassLoader().getResource(Message.iconOpen)));
	private JButton buttonSelectVariables = new JButton(new ImageIcon(this
			.getClass().getClassLoader().getResource(Message.iconForward)));
	private JComboBox listOntologyNameBg00 = new JComboBox();
	private JComboBox listOntologyNameBg01 = new JComboBox();
	private JComboBox listOntologyNamePos = new JComboBox();
	private JComboBox listOntologyNameNeg = new JComboBox();
	private JComboBox listProcessor = new JComboBox();
	private final UelModel model;

	public UelView(UelModel processor) {
		if (processor == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.model = processor;
		add(createSelectionPanel());
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

	public void addComboBoxOntologyBg00Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyNameBg00.addActionListener(listener);
		this.listOntologyNameBg00.setActionCommand(actionCommand);
	}

	public void addComboBoxOntologyBg01Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyNameBg01.addActionListener(listener);
		this.listOntologyNameBg01.setActionCommand(actionCommand);
	}

	public void addComboBoxOntologyPosListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyNamePos.addActionListener(listener);
		this.listOntologyNamePos.setActionCommand(actionCommand);
	}

	public void addComboBoxOntologyNegListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyNameNeg.addActionListener(listener);
		this.listOntologyNameNeg.setActionCommand(actionCommand);
	}

	private JPanel createSelectionPanel() {
		JPanel ret = new JPanel(new GridBagLayout());
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		JPanel smallPanel = new JPanel();
		this.listProcessor.setToolTipText(Message.tooltipSelectProcessor);
		for (String processorName : UelProcessorFactory.getProcessorNames()) {
			this.listProcessor.addItem(processorName);
		}
		smallPanel.add(this.listProcessor);
		this.buttonOpen.setToolTipText(Message.tooltipOpen);
		smallPanel.add(this.buttonOpen);
		this.buttonSelectVariables
				.setToolTipText(Message.tooltipSelectVariables);
		smallPanel.add(this.buttonSelectVariables);
		ret.add(smallPanel);

		JLabel gap1 = new JLabel();
		gap1.setPreferredSize(new Dimension(280, 14));
		gap1.setMinimumSize(new Dimension(112, 14));
		ret.add(gap1);

		JLabel labelOntologyNameBg00 = new JLabel();
		labelOntologyNameBg00.setPreferredSize(new Dimension(280, 28));
		labelOntologyNameBg00.setMinimumSize(new Dimension(112, 28));
		labelOntologyNameBg00.setText(Message.textOntologyBg00);
		ret.add(labelOntologyNameBg00);
		this.listOntologyNameBg00
				.setToolTipText(Message.tooltipComboBoxOntologyBg00);
		this.listOntologyNameBg00.setPreferredSize(new Dimension(280, 28));
		this.listOntologyNameBg00.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listOntologyNameBg00);

		JLabel gap2 = new JLabel();
		gap2.setPreferredSize(new Dimension(280, 14));
		gap2.setMinimumSize(new Dimension(112, 14));
		ret.add(gap2);

		JLabel labelOntologyNameBg01 = new JLabel();
		labelOntologyNameBg01.setPreferredSize(new Dimension(280, 28));
		labelOntologyNameBg01.setMinimumSize(new Dimension(112, 28));
		labelOntologyNameBg01.setText(Message.textOntologyBg01);
		ret.add(labelOntologyNameBg01);
		this.listOntologyNameBg01
				.setToolTipText(Message.tooltipComboBoxOntologyBg01);
		this.listOntologyNameBg01.setPreferredSize(new Dimension(280, 28));
		this.listOntologyNameBg01.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listOntologyNameBg01);

		JLabel gap3 = new JLabel();
		gap3.setPreferredSize(new Dimension(280, 14));
		gap3.setMinimumSize(new Dimension(112, 14));
		ret.add(gap3);

		JLabel labelOntologyNamePos = new JLabel();
		labelOntologyNamePos.setPreferredSize(new Dimension(280, 28));
		labelOntologyNamePos.setMinimumSize(new Dimension(112, 28));
		labelOntologyNamePos.setText(Message.textOntologyPos);
		ret.add(labelOntologyNamePos);
		this.listOntologyNamePos
				.setToolTipText(Message.tooltipComboBoxOntologyPos);
		this.listOntologyNamePos.setPreferredSize(new Dimension(280, 28));
		this.listOntologyNamePos.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listOntologyNamePos);

		JLabel gap4 = new JLabel();
		gap4.setPreferredSize(new Dimension(280, 14));
		gap4.setMinimumSize(new Dimension(112, 14));
		ret.add(gap4);

		JLabel labelOntologyNameNeg = new JLabel();
		labelOntologyNameNeg.setPreferredSize(new Dimension(280, 28));
		labelOntologyNameNeg.setMinimumSize(new Dimension(112, 28));
		labelOntologyNameNeg.setText(Message.textOntologyNeg);
		ret.add(labelOntologyNameNeg);
		this.listOntologyNameNeg
				.setToolTipText(Message.tooltipComboBoxOntologyNeg);
		this.listOntologyNameNeg.setPreferredSize(new Dimension(280, 28));
		this.listOntologyNameNeg.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listOntologyNameNeg);

		return ret;
	}

	public UelModel getModel() {
		return this.model;
	}

	public int getSelectedOntologyNameBg00() {
		return this.listOntologyNameBg00.getSelectedIndex();
	}

	public int getSelectedOntologyNameBg01() {
		return this.listOntologyNameBg01.getSelectedIndex();
	}

	public int getSelectedOntologyNamePos() {
		return this.listOntologyNamePos.getSelectedIndex();
	}

	public int getSelectedOntologyNameNeg() {
		return this.listOntologyNameNeg.getSelectedIndex();
	}

	public String getSelectedProcessor() {
		return this.listProcessor.getSelectedItem().toString();
	}

	public void reloadOntologies(List<String> listOfOntologyNames) {
		if (listOfOntologyNames == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyNameBg00.removeAllItems();
		this.listOntologyNameBg01.removeAllItems();
		this.listOntologyNamePos.removeAllItems();
		this.listOntologyNameNeg.removeAllItems();
		for (String ontologyName : listOfOntologyNames) {
			this.listOntologyNameBg00.addItem(ontologyName);
			this.listOntologyNameBg01.addItem(ontologyName);
			this.listOntologyNamePos.addItem(ontologyName);
			this.listOntologyNameNeg.addItem(ontologyName);
		}
	}

	public void setButtonLoadEnabled(boolean b) {
		this.buttonOpen.setEnabled(b);
	}

	public void setButtonSelectVariablesEnabled(boolean b) {
		this.buttonSelectVariables.setEnabled(b);
	}

	public void setComboBoxOntologyNameBg00Enabled(boolean b) {
		this.listOntologyNameBg00.setEnabled(b);
	}

	public void setComboBoxOntologyNameBg01Enabled(boolean b) {
		this.listOntologyNameBg01.setEnabled(b);
	}

	public void setComboBoxOntologyNamePosEnabled(boolean b) {
		this.listOntologyNamePos.setEnabled(b);
	}

	public void setComboBoxOntologyNameNegEnabled(boolean b) {
		this.listOntologyNameNeg.setEnabled(b);
	}

}
