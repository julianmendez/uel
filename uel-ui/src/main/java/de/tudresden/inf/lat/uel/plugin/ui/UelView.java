package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

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
	private final JCheckBox checkSnomedMode = new JCheckBox();
	private final JCheckBox checkExpandPrimitiveDefinitions = new JCheckBox();

	/**
	 * Construct the main view of UEL.
	 */
	public UelView() {
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout(0, UelUI.GAP_SIZE));

		add(createTopPanel(), BorderLayout.NORTH);

		JPanel auxPanel = new JPanel();
		add(auxPanel, BorderLayout.CENTER);

		auxPanel.add(createOntologyPanel());
	}

	private Container createOntologyPanel() {
		Container ontologyPanel = new JPanel(new GridLayout(0, 1));

		ontologyPanel.add(new JLabel(Message.textOntologyBg00));

		ontologyPanel.add(UelUI.setupComboBox(listOntologyBg00, Message.tooltipComboBoxOntologyBg00));

		ontologyPanel.add(new JLabel(Message.textOntologyBg01));

		ontologyPanel.add(UelUI.setupComboBox(listOntologyBg01, Message.tooltipComboBoxOntologyBg01));

		ontologyPanel.add(UelUI.setupCheckBox(checkSnomedMode, false, Message.textSnomedMode));

		ontologyPanel.add(UelUI.setupCheckBox(checkExpandPrimitiveDefinitions, true, Message.textExpandPrimitiveDefinitions));

		ontologyPanel.add(new JLabel(Message.textOntologyPos));

		ontologyPanel.add(UelUI.setupComboBox(listOntologyPos, Message.tooltipComboBoxOntologyPos));

		ontologyPanel.add(new JLabel(Message.textOntologyNeg));

		ontologyPanel.add(UelUI.setupComboBox(listOntologyNeg, Message.tooltipComboBoxOntologyNeg));

		return ontologyPanel;
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

	private JComponent createTopPanel() {
		JComponent topPanel = UelUI.createButtonPanel();

		topPanel.add(UelUI.setupComboBox(listAlgorithm, Message.tooltipSelectAlgorithm));
		for (String algorithmName : UnificationAlgorithmFactory.getAlgorithmNames()) {
			listAlgorithm.addItem(algorithmName);
		}

		topPanel.add(UelUI.setupButton(buttonOpen, UelUI.ICON_OPEN, Message.tooltipOpen));

		topPanel.add(UelUI.setupButton(buttonSelectVariables, UelUI.ICON_FORWARD, Message.tooltipSelectVariables));

		return topPanel;
	}
	
	public boolean getExpandPrimitiveDefinitions() {
		return checkExpandPrimitiveDefinitions.isSelected();
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
	 * Return the state of the "SNOMED mode" checkbox.
	 * 
	 * @return 'true' iff SNOMED mode should be enabled
	 */
	public boolean getSnomedMode() {
		return checkSnomedMode.isSelected();
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

}
