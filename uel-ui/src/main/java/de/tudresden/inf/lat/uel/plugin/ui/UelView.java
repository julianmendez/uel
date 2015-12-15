package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.core.processor.UelProcessorFactory;

/**
 * This is the main panel of the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelView extends JPanel {

	private static final long serialVersionUID = 9096602357606632334L;

	private final JButton buttonOpen = new JButton();
	private final JButton buttonSelectVariables = new JButton();
	private final JComboBox<OWLOntology> listOntologyBg00 = new JComboBox<OWLOntology>();
	private final JComboBox<OWLOntology> listOntologyBg01 = new JComboBox<OWLOntology>();
	private final JComboBox<OWLOntology> listOntologyPos = new JComboBox<OWLOntology>();
	private final JComboBox<OWLOntology> listOntologyNeg = new JComboBox<OWLOntology>();
	private final JComboBox<String> listProcessor = new JComboBox<String>();

	public UelView() {
		add(createSelectionPanel());
	}

	public void addOpenListener(ActionListener listener) {
		buttonOpen.addActionListener(listener);
	}

	public void addSelectVariablesListener(ActionListener listener) {
		buttonSelectVariables.addActionListener(listener);
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

		UelUI.addLabel(largePanel, Message.textOntologyBg00);

		UelUI.setupComboBox(largePanel, listOntologyBg00, Message.tooltipComboBoxOntologyBg00);

		largePanel.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		UelUI.addLabel(largePanel, Message.textOntologyBg01);

		UelUI.setupComboBox(largePanel, listOntologyBg01, Message.tooltipComboBoxOntologyBg01);

		largePanel.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		UelUI.addLabel(largePanel, Message.textOntologyPos);

		UelUI.setupComboBox(largePanel, listOntologyPos, Message.tooltipComboBoxOntologyPos);

		largePanel.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		UelUI.addLabel(largePanel, Message.textOntologyNeg);

		UelUI.setupComboBox(largePanel, listOntologyNeg, Message.tooltipComboBoxOntologyNeg);

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

	public OWLOntology getSelectedOntologyBg00() {
		return (OWLOntology) listOntologyBg00.getSelectedItem();
	}

	public OWLOntology getSelectedOntologyBg01() {
		return (OWLOntology) listOntologyBg01.getSelectedItem();
	}

	public OWLOntology getSelectedOntologyPos() {
		return (OWLOntology) listOntologyPos.getSelectedItem();
	}

	public OWLOntology getSelectedOntologyNeg() {
		return (OWLOntology) listOntologyNeg.getSelectedItem();
	}

	public String getSelectedProcessor() {
		return (String) listProcessor.getSelectedItem();
	}

	public void reloadOntologies(List<OWLOntology> list) {
		OWLOntology[] ontologies = list.toArray(new OWLOntology[list.size()]);
		resetModelAndRestoreSelection(listOntologyBg00, ontologies);
		resetModelAndRestoreSelection(listOntologyBg01, ontologies);
		resetModelAndRestoreSelection(listOntologyPos, ontologies);
		resetModelAndRestoreSelection(listOntologyNeg, ontologies);
	}

	@SuppressWarnings("unchecked")
	public static <T> void resetModelAndRestoreSelection(JComboBox<T> comboBox, T[] data) {
		T selection = (T) comboBox.getSelectedItem();
		comboBox.setModel(new DefaultComboBoxModel<T>(data));
		if (selection != null) {
			comboBox.setSelectedItem(selection);
		}
	}

	public void setButtonLoadEnabled(boolean b) {
		this.buttonOpen.setEnabled(b);
	}

	public void setButtonSelectVariablesEnabled(boolean b) {
		this.buttonSelectVariables.setEnabled(b);
	}

	public void setComboBoxOntologyBg00Enabled(boolean b) {
		this.listOntologyBg00.setEnabled(b);
	}

	public void setComboBoxOntologyBg01Enabled(boolean b) {
		this.listOntologyBg01.setEnabled(b);
	}

	public void setComboBoxOntologyPosEnabled(boolean b) {
		this.listOntologyPos.setEnabled(b);
	}

	public void setComboBoxOntologyNegEnabled(boolean b) {
		this.listOntologyNeg.setEnabled(b);
	}

}
