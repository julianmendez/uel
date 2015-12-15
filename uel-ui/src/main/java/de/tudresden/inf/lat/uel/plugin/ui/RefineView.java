/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * @author Stefan Borgwardt
 *
 */
public class RefineView extends JDialog {

	private static final long serialVersionUID = 6093665334232206919L;

	private final JButton buttonRecompute = new JButton();
	private final JButton buttonSave = new JButton();
	private final Container mainPanel = new Box(BoxLayout.X_AXIS);
	private Map<LabelId, JList<LabelId>> content = null;

	public RefineView() {
		super((Frame) null, "Refine unifier", true);
		UelUI.setupWindow(this, createRefinePanel());
	}

	public void addRecomputeListener(ActionListener listener) {
		buttonRecompute.addActionListener(listener);
	}

	public void addSaveListener(ActionListener listener) {
		buttonSave.addActionListener(listener);

	}

	private Container createRefinePanel() {
		Container ret = new Box(BoxLayout.Y_AXIS);

		ret.add(createButtons());

		ret.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		UelUI.setupScrollPane(ret, mainPanel, "", new Dimension(640, 480));

		return ret;
	}

	private Component createButtons() {
		Container buttonPanel = new JPanel();

		UelUI.setupButton(buttonPanel, buttonRecompute, UelUI.ICON_FORWARD, Message.tooltipRecompute);

		UelUI.setupButton(buttonPanel, buttonSave, UelUI.ICON_SAVE, Message.tooltipSaveDissub);

		return buttonPanel;
	}

	public void updateSelectionPanels(Map<LabelId, List<LabelId>> unifier) {
		mainPanel.removeAll();
		content = new HashMap<LabelId, JList<LabelId>>();

		for (LabelId var : unifier.keySet()) {
			Container varPanel = new Box(BoxLayout.Y_AXIS);

			JLabel label = new JLabel(var.getLabel());
			JList<LabelId> list = new JList<LabelId>();
			label.setLabelFor(list);
			// TODO setup jlist
			content.put(var, list);

			mainPanel.add(varPanel);
			mainPanel.add(Box.createHorizontalStrut(UelUI.GAP_SIZE));
		}
	}

}
