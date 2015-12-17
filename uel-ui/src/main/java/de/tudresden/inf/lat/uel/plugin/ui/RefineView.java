/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;

/**
 * @author Stefan Borgwardt
 *
 */
public final class RefineView extends UelDialog {

	private static final long serialVersionUID = 6093665334232206919L;

	private final JButton buttonRecompute = new JButton();
	private final JButton buttonSave = new JButton();
	private final Box selectionPanel = new Box(BoxLayout.X_AXIS);
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

		selectionPanel.setAlignmentX(LEFT_ALIGNMENT);
		UelUI.addScrollPane(auxPanel, selectionPanel, "", new Dimension(640, 480));
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

	public void updateSelectionPanels(Map<LabelId, List<LabelId>> unifier) {
		selectionPanel.removeAll();
		content = new HashMap<LabelId, JList<LabelId>>();

		for (LabelId var : unifier.keySet()) {
			List<LabelId> atoms = unifier.get(var);

			Container varPanel = UelUI.addVerticalPanel(selectionPanel);

			UelUI.addLabel(varPanel, var.getLabel(), Message.tooltipVariableName);

			UelUI.addStrut(varPanel);

			JList<LabelId> list = UelUI.addList(varPanel, atoms, Message.tooltipRefineAtoms);
			content.put(var, list);
		}

		UelUI.addGlue(selectionPanel);
	}

}
