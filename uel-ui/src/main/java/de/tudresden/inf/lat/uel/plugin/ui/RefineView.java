/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.tudresden.inf.lat.uel.core.renderer.StringRenderer;

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
	private final JPanel selectionPanel = new JPanel(new GridBagLayout());

	/**
	 * Construct a new unifier refinement view.
	 * 
	 * @param parent
	 *            the parent Component
	 */
	public RefineView(Component parent) {
		setup(parent, "Refine unifier");
	}

	private Container createButtonPanel() {
		Container buttonPanel = UelUI.createButtonPanel();

		buttonPanel.add(UelUI.setupButton(buttonRecompute, UelUI.ICON_FORWARD, Message.tooltipRecompute));

		buttonPanel.add(UelUI.setupButton(buttonSave, UelUI.ICON_SAVE, Message.tooltipSaveDissub));

		return buttonPanel;
	}

	private void addLabel(int rowCount, LabelId var) {
		JLabel varLabel = UelUI.createLabel(var.getLabel() + " ⋢", Message.tooltipVariableName);
		varLabel.setBorder(new EmptyBorder(UelUI.GAP_SIZE, UelUI.GAP_SIZE, UelUI.GAP_SIZE, UelUI.GAP_SIZE));
		varLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		varLabel.setVerticalAlignment(SwingConstants.TOP);

		GridBagConstraints varConstraints = new GridBagConstraints();
		varConstraints.gridx = 0;
		varConstraints.gridy = rowCount;
		varConstraints.weightx = 0.1;
		varConstraints.weighty = 0;
		varConstraints.anchor = GridBagConstraints.FIRST_LINE_END;

		selectionPanel.add(varLabel, varConstraints);
	}

	private void addList(int rowCount, LabelId var, List<LabelId> atoms) {
		GridBagConstraints atomsConstraints = new GridBagConstraints();
		atomsConstraints.gridx = 1;
		atomsConstraints.gridy = rowCount;
		atomsConstraints.weightx = 0.5;
		atomsConstraints.weighty = 0;
		atomsConstraints.fill = GridBagConstraints.HORIZONTAL;
		atomsConstraints.anchor = GridBagConstraints.FIRST_LINE_START;

		Component comp;
		if (atoms.isEmpty()) {
			JLabel label = UelUI.createLabel(StringRenderer.createInstance(null, null, null).renderTop(),
					Message.tooltipSubsumedByTop);
			label.setBorder(new EmptyBorder(UelUI.GAP_SIZE, 0, UelUI.GAP_SIZE, UelUI.GAP_SIZE));
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setVerticalAlignment(SwingConstants.TOP);
			comp = label;
		} else {
			JList<LabelId> list = UelUI.createList(atoms, Message.tooltipRefineAtoms, true);
			content.put(var, list);
			comp = list;
			atomsConstraints.insets = new Insets(0, 0, UelUI.GAP_SIZE, 0);
		}

		selectionPanel.add(comp, atomsConstraints);
	}

	@Override
	protected JComponent createMainPanel() {
		JComponent mainPanel = UelUI.createVerticalPanel();

		mainPanel.add(createButtonPanel(), BorderLayout.NORTH);

		Container auxPanel = UelUI.createVerticalPanel();
		mainPanel.add(auxPanel, BorderLayout.CENTER);

		auxPanel.add(new JLabel(Message.textRefineExplanation), BorderLayout.NORTH);

		UelUI.setMargin(selectionPanel);
		auxPanel.add(UelUI.createScrollPane(selectionPanel, false), BorderLayout.CENTER);

		return mainPanel;
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
		content = new HashMap<>();

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
