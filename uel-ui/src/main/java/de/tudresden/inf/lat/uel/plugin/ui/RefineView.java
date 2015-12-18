/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author Stefan Borgwardt
 *
 */
public final class RefineView extends UelDialog {

	private static final long serialVersionUID = 6093665334232206919L;

	private final JButton buttonRecompute = new JButton();
	private final JButton buttonSave = new JButton();
	private final GridBagLayout layout = new GridBagLayout();
	private final JPanel selectionPanel = new JPanel(layout);
	private Map<LabelId, JList<LabelId>> content = null;

	public RefineView() {
		setup("Refine unifier");
	}

	public void addRecomputeListener(ActionListener listener) {
		buttonRecompute.addActionListener(listener);
	}

	public void addSaveListener(ActionListener listener) {
		buttonSave.addActionListener(listener);

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

	private void addButtonPanel(Container parent) {
		Container buttonPanel = UelUI.addButtonPanel(parent);

		UelUI.setupButton(buttonPanel, buttonRecompute, UelUI.ICON_FORWARD, Message.tooltipRecompute);

		UelUI.setupButton(buttonPanel, buttonSave, UelUI.ICON_SAVE, Message.tooltipSaveDissub);
	}

	public Map<LabelId, List<LabelId>> getSelectedAtoms() {
		Map<LabelId, List<LabelId>> map = new HashMap<LabelId, List<LabelId>>();

		for (LabelId var : content.keySet()) {
			map.put(var, content.get(var).getSelectedValuesList());
		}

		return map;
	}

	public void updateSelectionPanel(Map<LabelId, List<LabelId>> unifier) {
		selectionPanel.removeAll();
		content = new HashMap<LabelId, JList<LabelId>>();

		int rowCount = 0;
		for (LabelId var : unifier.keySet()) {
			List<LabelId> atoms = unifier.get(var);

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

			JList<LabelId> list = UelUI.addList(selectionPanel, atoms, Message.tooltipRefineAtoms);
			GridBagConstraints atomsConstraints = new GridBagConstraints();
			atomsConstraints.gridx = 1;
			atomsConstraints.gridy = rowCount;
			atomsConstraints.weightx = 0.5;
			atomsConstraints.weighty = 0;
			atomsConstraints.fill = GridBagConstraints.HORIZONTAL;
			atomsConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
			if (rowCount > 0) {
				atomsConstraints.insets = new Insets(UelUI.GAP_SIZE, 0, 0, 0);
			}
			layout.setConstraints(list, atomsConstraints);
			content.put(var, list);

			rowCount++;
		}
		
		JPanel aux = new JPanel();
		GridBagConstraints auxConstraints = new GridBagConstraints();
		auxConstraints.gridx = 0;
		auxConstraints.gridy = rowCount;
		auxConstraints.weighty = 1;
		selectionPanel.add(aux, auxConstraints);
	}

}
