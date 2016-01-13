package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Container;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;

/**
 * This is the main panel of the UEL system.
 * 
 * @author Julian Mendez
 * @author Stefan Borgwardt
 */
class UelView extends JPanel {

	private static final long serialVersionUID = 9096602357606632334L;

	@SuppressWarnings("unchecked")
	private static <T> void resetModelAndRestoreSelection(JComboBox<T> comboBox, T[] data) {
		T selection = (T) comboBox.getSelectedItem();
		comboBox.setModel(new DefaultComboBoxModel<T>(data));
		if (selection != null) {
			comboBox.setSelectedItem(selection);
		}
	}

	// TODO change this back to JComboBox<OWLOntology>, then ComboBoxRenderer
	// takes care of the rest

	private final JButton buttonOpen = new JButton();
	private final JButton buttonSelectVariables = new JButton();
	private final JComboBox<String> listAlgorithm = new JComboBox<String>();
	private final List<OWLOntology> listOfOntologies = new ArrayList<OWLOntology>();
	private final JComboBox<String> listOntologyBg00 = new JComboBox<String>();
	private final JComboBox<String> listOntologyBg01 = new JComboBox<String>();
	private final JComboBox<String> listOntologyNeg = new JComboBox<String>();

	private final JComboBox<String> listOntologyPos = new JComboBox<String>();

	/**
	 * Construct the main view of UEL.
	 */
	public UelView() {
		addMainPanel(this);
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

	/**
	 * Add a listener to the button for opening a new ontology.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addOpenListener(ActionListener listener) {
		buttonOpen.addActionListener(listener);
	}

	/**
	 * Add a listener to the button for starting the unification process by
	 * selecting the variables.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addSelectVariablesListener(ActionListener listener) {
		buttonSelectVariables.addActionListener(listener);
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

	String getName(OWLOntology ontology) {
		OWLOntologyID id = ontology.getOntologyID();
		if (id.getOntologyIRI().isPresent()) {
			return id.getOntologyIRI().get().toString();
		} else {
			return id.toString();
		}
	}

	String[] getNames(List<OWLOntology> list) {
		String[] ret = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ret[i] = getName(list.get(i));
		}
		return ret;
	}

	/**
	 * Return the currently selected unification algorithm.
	 * 
	 * @return a string identifier for the selected algorithm
	 */
	public String getSelectedAlgorithm() {
		return (String) listAlgorithm.getSelectedItem();
	}

	/**
	 * Return the currently selected 1st background ontology.
	 * 
	 * @return the selected OWLOntology object
	 */
	public OWLOntology getSelectedOntologyBg00() {
		return listOfOntologies.get(listOntologyBg00.getSelectedIndex());
	}

	/**
	 * Return the currently selected 2nd background ontology.
	 * 
	 * @return the selected OWLOntology object
	 */
	public OWLOntology getSelectedOntologyBg01() {
		return listOfOntologies.get(listOntologyBg01.getSelectedIndex());
	}

	/**
	 * Return the currently selected ontology containing the negative part of
	 * the goal.
	 * 
	 * @return the selected OWLOntology object
	 */
	public OWLOntology getSelectedOntologyNeg() {
		return listOfOntologies.get(listOntologyNeg.getSelectedIndex());
	}

	/**
	 * Return the currently selected ontology containing the positive part of
	 * the goal.
	 * 
	 * @return the selected OWLOntology object
	 */
	public OWLOntology getSelectedOntologyPos() {
		return listOfOntologies.get(listOntologyPos.getSelectedIndex());
	}

	public void reloadOntologies(List<OWLOntology> list) {
		this.listOfOntologies.clear();
		this.listOfOntologies.addAll(list);
		String[] ontologyNames = getNames(this.listOfOntologies);
		resetModelAndRestoreSelection(listOntologyBg00, ontologyNames);
		resetModelAndRestoreSelection(listOntologyBg01, ontologyNames);
		resetModelAndRestoreSelection(listOntologyPos, ontologyNames);
		resetModelAndRestoreSelection(listOntologyNeg, ontologyNames);
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

	public void setComboBoxOntologyNegEnabled(boolean b) {
		this.listOntologyNeg.setEnabled(b);
	}

	public void setComboBoxOntologyPosEnabled(boolean b) {
		this.listOntologyPos.setEnabled(b);
	}

	public void setSelectedOntologyNeg(OWLOntology ontology) {
		listOntologyNeg.setSelectedIndex(listOfOntologies.indexOf(ontology));
	}

}
