package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Container;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;

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
	private final JComboBox<String> listAlgorithm = new JComboBox<String>();

	public UelView() {
		addMainPanel(this);
	}

	public void addOpenListener(ActionListener listener) {
		buttonOpen.addActionListener(listener);
	}

	public void addSelectVariablesListener(ActionListener listener) {
		buttonSelectVariables.addActionListener(listener);
	}

	private void addMainPanel(Container parent) {
		Container mainPanel = UelUI.addVerticalPanel(parent);

		addTopPanel(mainPanel);

		UelUI.addStrut(mainPanel);

		addOntologyPanel(mainPanel);
	}

	private void addOntologyPanel(Container parent) {
		Container ontologyPanel = UelUI.addVerticalPanel(parent);

		UelUI.addLabel(ontologyPanel, Message.textOntologyBg00);

		UelUI.setupComboBox(ontologyPanel, listOntologyBg00, Message.tooltipComboBoxOntologyBg00);

		UelUI.addStrut(ontologyPanel);

		UelUI.addLabel(ontologyPanel, Message.textOntologyBg01);

		UelUI.setupComboBox(ontologyPanel, listOntologyBg01, Message.tooltipComboBoxOntologyBg01);

		UelUI.addStrut(ontologyPanel);

		UelUI.addLabel(ontologyPanel, Message.textOntologyPos);

		UelUI.setupComboBox(ontologyPanel, listOntologyPos, Message.tooltipComboBoxOntologyPos);

		UelUI.addStrut(ontologyPanel);

		UelUI.addLabel(ontologyPanel, Message.textOntologyNeg);

		UelUI.setupComboBox(ontologyPanel, listOntologyNeg, Message.tooltipComboBoxOntologyNeg);
	}

	private void addTopPanel(Container parent) {
		Container topPanel = UelUI.addButtonPanel(parent);

		UelUI.setupComboBox(topPanel, listAlgorithm, Message.tooltipSelectAlgorithm);
		for (String algorithmName : UnificationAlgorithmFactory.getAlgorithmNames()) {
			listAlgorithm.addItem(algorithmName);
		}

		UelUI.setupButton(topPanel, buttonOpen, UelUI.ICON_OPEN, Message.tooltipOpen);

		UelUI.setupButton(topPanel, buttonSelectVariables, UelUI.ICON_FORWARD, Message.tooltipSelectVariables);
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

	public void setSelectedOntologyNeg(OWLOntology ontology) {
		listOntologyNeg.setSelectedItem(ontology);
	}

	public String getSelectedAlgorithm() {
		return (String) listAlgorithm.getSelectedItem();
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
