/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;

/**
 * This dialog allows to select non-variable atoms that should not be included
 * in the unifiers.
 * 
 * @author Stefan Borgwardt
 */
class RefineView extends UelDialog {

	private static final long serialVersionUID = 6093665334232206919L;

	private final JButton buttonRecompute = new JButton();
	private final JButton buttonSave = new JButton();
	private Map<LabelId, JList<LabelId>> content = null;
	private final GridBagLayout layout = new GridBagLayout();
	private final JPanel selectionPanel = new JPanel(layout);

	/**
	 * Construct a new unifier refinement view.
	 */
	public RefineView() {
		setup("Refine unifier");
	}

	private void addButtonPanel(Container parent) {
		Container buttonPanel = UelUI.addButtonPanel(parent);

		UelUI.setupButton(buttonPanel, buttonRecompute, UelUI.ICON_FORWARD, Message.tooltipRecompute);

		UelUI.setupButton(buttonPanel, buttonSave, UelUI.ICON_SAVE, Message.tooltipSaveDissub);
	}

	private void addLabel(int rowCount, LabelId var) {
		JLabel varLabel = UelUI.addLabel(selectionPanel, var.getLabel() + " ⋢", Message.tooltipVariableName);
		UelUI.setMargin(varLabel);
		varLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		varLabel.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints varConstraints = new GridBagConstraints();
		varConstraints.gridx = 0;
		varConstraints.gridy = rowCount;
		varConstraints.weightx = 0.1;
		varConstraints.weighty = 0;
		varConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
		layout.setConstraints(varLabel, varConstraints);
	}

	private void addList(int rowCount, LabelId var, List<LabelId> atoms) {
		Component comp;
		GridBagConstraints atomsConstraints = new GridBagConstraints();

		if (atoms.isEmpty()) {
			JLabel label = UelUI.addLabel(selectionPanel, KRSSKeyword.top, Message.tooltipSubsumedByTop);
			label.setBorder(new EmptyBorder(UelUI.GAP_SIZE, 0, UelUI.GAP_SIZE, UelUI.GAP_SIZE));
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setVerticalAlignment(SwingConstants.TOP);
			comp = label;
		} else {
			JList<LabelId> list = UelUI.addList(selectionPanel, atoms, Message.tooltipRefineAtoms);
			content.put(var, list);
			comp = list;
			if (rowCount > 0) {
				atomsConstraints.insets = new Insets(UelUI.GAP_SIZE, 0, 0, 0);
			}
		}

		atomsConstraints.gridx = 1;
		atomsConstraints.gridy = rowCount;
		atomsConstraints.weightx = 0.5;
		atomsConstraints.weighty = 0;
		atomsConstraints.fill = GridBagConstraints.HORIZONTAL;
		atomsConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		layout.setConstraints(comp, atomsConstraints);
	}

	@Override
	protected void addMainPanel(Container parent) {
		Container mainPanel = UelUI.addVerticalPanel(parent);

		addButtonPanel(mainPanel);

		UelUI.addStrut(mainPanel);

		Container auxPanel = UelUI.addVerticalPanel(mainPanel);
		UelUI.addLabel(auxPanel, Message.textRefineExplanation);

		UelUI.addStrut(auxPanel);
		UelUI.addStrut(auxPanel);

		UelUI.setupContainer(selectionPanel);
		UelUI.addScrollPane(mainPanel, selectionPanel, "", new Dimension(640, 480));
	}

	/**
	 * Add a listener to the button for recomputing the unifiers.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addRecomputeListener(ActionListener listener) {
		buttonRecompute.addActionListener(listener);
	}

	/**
	 * Add a listener to the button for saving the dissubsumptions and then
	 * recomputing the unifiers.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addSaveListener(ActionListener listener) {
		buttonSave.addActionListener(listener);

	}

	/**
	 * Extract the selected non-variable atoms for each variable.
	 * 
	 * @return a map assigning to each variable a list of the selected atoms
	 */
	public Map<LabelId, List<LabelId>> getSelectedAtoms() {
		return content.keySet().stream().collect(Collectors.<LabelId, LabelId, List<LabelId>> toMap(
				Function.<LabelId> identity(), k -> content.get(k).getSelectedValuesList()));
	}

	/**
	 * Update the content of this dialog with the given variables and
	 * non-variable atoms.
	 * 
	 * @param unifier
	 *            a map assigning to each variable its non-variable atoms
	 */
	public void updateSelectionPanel(Map<LabelId, List<LabelId>> unifier) {
		selectionPanel.removeAll();
		content = new HashMap<LabelId, JList<LabelId>>();

		int rowCount = 0;
		for (LabelId var : unifier.keySet()) {
			addLabel(rowCount, var);
			addList(rowCount, var, unifier.get(var));
			rowCount++;
		}

		// add auxiliary panel to force top alignment of the rest
		JPanel aux = new JPanel();
		GridBagConstraints auxConstraints = new GridBagConstraints();
		auxConstraints.gridx = 0;
		auxConstraints.gridy = rowCount;
		auxConstraints.weighty = 1;
		selectionPanel.add(aux, auxConstraints);
	}

}
