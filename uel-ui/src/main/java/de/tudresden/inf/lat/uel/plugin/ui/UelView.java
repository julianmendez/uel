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
 * @author Stefan Borgwardt
 */
class UelView extends JPanel {

	private static final long serialVersionUID = 9096602357606632334L;

	private static <T> void resetModelAndRestoreSelection(JComboBox<T> comboBox, T[] data) {
		Object selection = comboBox.getSelectedItem();
		comboBox.setModel(new DefaultComboBoxModel<T>(data));
		if (selection != null) {
			comboBox.setSelectedItem(selection);
		}
	}

	private final JButton buttonOpen = new JButton();
	private final JButton buttonSelectVariables = new JButton();
	private final JComboBox<String> listAlgorithm = new JComboBox<String>();
	private final JComboBox<OWLOntology> listOntologyBg00 = new JComboBox<OWLOntology>();
	private final JComboBox<OWLOntology> listOntologyBg01 = new JComboBox<OWLOntology>();
	private final JComboBox<OWLOntology> listOntologyNeg = new JComboBox<OWLOntology>();
	private final JComboBox<OWLOntology> listOntologyPos = new JComboBox<OWLOntology>();

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
		return (OWLOntology) listOntologyBg00.getSelectedItem();
	}

	/**
	 * Return the currently selected 2nd background ontology.
	 * 
	 * @return the selected OWLOntology object
	 */
	public OWLOntology getSelectedOntologyBg01() {
		return (OWLOntology) listOntologyBg01.getSelectedItem();
	}

	/**
	 * Return the currently selected ontology containing the negative part of
	 * the goal.
	 * 
	 * @return the selected OWLOntology object
	 */
	public OWLOntology getSelectedOntologyNeg() {
		return (OWLOntology) listOntologyNeg.getSelectedItem();
	}

	/**
	 * Return the currently selected ontology containing the positive part of
	 * the goal.
	 * 
	 * @return the selected OWLOntology object
	 */
	public OWLOntology getSelectedOntologyPos() {
		return (OWLOntology) listOntologyPos.getSelectedItem();
	}

	/**
	 * Refresh the list of ontologies shown for selection. This method restores
	 * the previously selected ontologies in the combo boxes, if they are still
	 * present in the new list.
	 * 
	 * @param list
	 *            the new list of ontologies
	 */
	public void reloadOntologies(List<OWLOntology> list) {
		OWLOntology[] ontologies = list.toArray(new OWLOntology[list.size()]);
		resetModelAndRestoreSelection(listOntologyBg00, ontologies);
		resetModelAndRestoreSelection(listOntologyBg01, ontologies);
		resetModelAndRestoreSelection(listOntologyPos, ontologies);
		resetModelAndRestoreSelection(listOntologyNeg, ontologies);
	}

	/**
	 * Sets the 'enabled' state of the button that allows to open a new
	 * ontology.
	 * 
	 * @param b
	 *            the new state
	 */
	public void setButtonLoadEnabled(boolean b) {
		this.buttonOpen.setEnabled(b);
	}

	/**
	 * Sets the 'enabled' state of the button that allows to start the variable
	 * selection process.
	 * 
	 * @param b
	 *            the new state
	 */
	public void setButtonSelectVariablesEnabled(boolean b) {
		this.buttonSelectVariables.setEnabled(b);
	}

	/**
	 * Sets the 'enabled' state of the combo box for selecting the first
	 * background ontology.
	 * 
	 * @param b
	 *            the new state
	 */
	public void setComboBoxOntologyBg00Enabled(boolean b) {
		this.listOntologyBg00.setEnabled(b);
	}

	/**
	 * Sets the 'enabled' state of the combo box for selecting the second
	 * background ontology.
	 * 
	 * @param b
	 *            the new state
	 */
	public void setComboBoxOntologyBg01Enabled(boolean b) {
		this.listOntologyBg01.setEnabled(b);
	}

	/**
	 * Sets the 'enabled' state of the combo box for selecting the negative goal
	 * ontology.
	 * 
	 * @param b
	 *            the new state
	 */
	public void setComboBoxOntologyNegEnabled(boolean b) {
		this.listOntologyNeg.setEnabled(b);
	}

	/**
	 * Sets the 'enabled' state of the combo box for selecting the positive goal
	 * ontology.
	 * 
	 * @param b
	 *            the new state
	 */
	public void setComboBoxOntologyPosEnabled(boolean b) {
		this.listOntologyPos.setEnabled(b);
	}

	/**
	 * Sets the currently selected negative goal ontology, if this ontology is
	 * present in the list used in the last call of 'reloadOntologies'.
	 * 
	 * @param ontology
	 *            the ontology that is to be selected
	 */
	public void setSelectedOntologyNeg(OWLOntology ontology) {
		listOntologyNeg.setSelectedItem(ontology);
	}

}
