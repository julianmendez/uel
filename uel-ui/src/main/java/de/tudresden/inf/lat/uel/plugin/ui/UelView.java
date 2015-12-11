package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.core.processor.UelProcessorFactory;
import de.tudresden.inf.lat.uel.plugin.ui.UelUI.ComboBoxRenderer;

/**
 * This is the main panel of the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelView extends JPanel {

	private static final long serialVersionUID = 9096602357606632334L;

	private final JButton buttonOpen = new JButton();
	private final JButton buttonSelectVariables = new JButton(UelUI.ICON_FORWARD);
	private final JComboBox<OWLOntology> listOntologyNameBg00 = new JComboBox<OWLOntology>();
	private final JComboBox<OWLOntology> listOntologyNameBg01 = new JComboBox<OWLOntology>();
	private final JComboBox<OWLOntology> listOntologyNamePos = new JComboBox<OWLOntology>();
	private final JComboBox<OWLOntology> listOntologyNameNeg = new JComboBox<OWLOntology>();
	private final JComboBox<String> listProcessor = new JComboBox<String>();

	public UelView() {
		add(createSelectionPanel());
	}

	public void addButtonOpenListener(ActionListener listener, String actionCommand) {
		this.buttonOpen.addActionListener(listener);
		this.buttonOpen.setActionCommand(actionCommand);
	}

	public void addButtonSelectVariablesListener(ActionListener listener, String actionCommand) {
		this.buttonSelectVariables.addActionListener(listener);
		this.buttonSelectVariables.setActionCommand(actionCommand);
	}

	public void addComboBoxOntologyBg00Listener(ActionListener listener, String actionCommand) {
		this.listOntologyNameBg00.addActionListener(listener);
		this.listOntologyNameBg00.setActionCommand(actionCommand);
	}

	public void addComboBoxOntologyBg01Listener(ActionListener listener, String actionCommand) {
		this.listOntologyNameBg01.addActionListener(listener);
		this.listOntologyNameBg01.setActionCommand(actionCommand);
	}

	public void addComboBoxOntologyPosListener(ActionListener listener, String actionCommand) {
		this.listOntologyNamePos.addActionListener(listener);
		this.listOntologyNamePos.setActionCommand(actionCommand);
	}

	public void addComboBoxOntologyNegListener(ActionListener listener, String actionCommand) {
		this.listOntologyNameNeg.addActionListener(listener);
		this.listOntologyNameNeg.setActionCommand(actionCommand);
	}

	private Component createSelectionPanel() {
		Container ret = new Box(BoxLayout.Y_AXIS);

		ret.add(createTopPanel());

		ret.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		ret.add(createOntologyPanel());

		return ret;
	}

	private Component createOntologyPanel() {
		JComponent largePanel = new Box(BoxLayout.Y_AXIS);
		largePanel.setAlignmentX(CENTER_ALIGNMENT);

		// TODO clean up

		JLabel labelOntologyNameBg00 = new JLabel(Message.textOntologyBg00);
		labelOntologyNameBg00.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(labelOntologyNameBg00);

		this.listOntologyNameBg00.setRenderer(new ComboBoxRenderer());
		this.listOntologyNameBg00.setToolTipText(Message.tooltipComboBoxOntologyBg00);
		this.listOntologyNameBg00.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(this.listOntologyNameBg00);

		largePanel.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		JLabel labelOntologyNameBg01 = new JLabel(Message.textOntologyBg01);
		labelOntologyNameBg01.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(labelOntologyNameBg01);

		this.listOntologyNameBg01.setRenderer(new ComboBoxRenderer());
		this.listOntologyNameBg01.setToolTipText(Message.tooltipComboBoxOntologyBg01);
		this.listOntologyNameBg01.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(this.listOntologyNameBg01);

		largePanel.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		JLabel labelOntologyNamePos = new JLabel(Message.textOntologyPos);
		labelOntologyNamePos.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(labelOntologyNamePos);

		this.listOntologyNamePos.setRenderer(new ComboBoxRenderer());
		this.listOntologyNamePos.setToolTipText(Message.tooltipComboBoxOntologyPos);
		this.listOntologyNamePos.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(this.listOntologyNamePos);

		largePanel.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		JLabel labelOntologyNameNeg = new JLabel(Message.textOntologyNeg);
		labelOntologyNameNeg.setHorizontalAlignment(SwingConstants.CENTER);
		largePanel.add(labelOntologyNameNeg);

		UelUI.setupComboBox(largePanel, listOntologyNameNeg, Message.tooltipComboBoxOntologyNeg);

		return largePanel;
	}

	private Component createTopPanel() {
		JComponent topPanel = new JPanel();
		topPanel.setAlignmentX(CENTER_ALIGNMENT);

		UelUI.setupComboBox(topPanel, listProcessor, Message.tooltipSelectProcessor);
		for (String processorName : UelProcessorFactory.getProcessorNames()) {
			listProcessor.addItem(processorName);
		}

		UelUI.setupButton(topPanel, buttonOpen, UelUI.ICON_OPEN, Message.tooltipOpen);

		UelUI.setupButton(topPanel, buttonSelectVariables, UelUI.ICON_FORWARD, Message.tooltipSelectVariables);

		return topPanel;
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

	public void reloadOntologies(List<OWLOntology> listOfOntologies) {
		if (listOfOntologies == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyNameBg00.removeAllItems();
		this.listOntologyNameBg01.removeAllItems();
		this.listOntologyNamePos.removeAllItems();
		this.listOntologyNameNeg.removeAllItems();
		for (OWLOntology ontology : listOfOntologies) {
			this.listOntologyNameBg00.addItem(ontology);
			this.listOntologyNameBg01.addItem(ontology);
			this.listOntologyNamePos.addItem(ontology);
			this.listOntologyNameNeg.addItem(ontology);
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
