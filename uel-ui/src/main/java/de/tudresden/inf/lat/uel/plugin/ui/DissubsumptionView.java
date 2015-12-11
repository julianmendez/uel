/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.type.api.Equation;

// TODO split into view and controller?

/**
 * @author Stefan Borgwardt
 *
 */
public class DissubsumptionView extends JDialog {

	private static final long serialVersionUID = 6093665334232206919L;

	private final JButton buttonRecompute = new JButton();
	private final JButton buttonSave = new JButton();
	private int n;

	private UelModel model;
	private Set<Equation> unifier;
	private JList<LabelId>[] lists;

	public DissubsumptionView(Set<Equation> unifier, UelModel model) {
		super((Frame) null, "Refine unifier", true);
		this.unifier = unifier;
		this.model = model;
		n = unifier.size();
		UelUI.setupWindow(this, createRefinePanel());
	}

	public void addButtonRecomputeListener(ActionListener listener, String actionCommand) {
		buttonRecompute.addActionListener(listener);
		buttonRecompute.setActionCommand(actionCommand);
	}

	public void addButtonSaveListener(ActionListener listener, String actionCommand) {
		buttonSave.addActionListener(listener);
		buttonSave.setActionCommand(actionCommand);
	}

	public void close() {
		setVisible(false);
		dispose();
	}

	public String getDissubsumptions() {
		Set<Equation> dissubsumptions = new HashSet<Equation>();
		for (int i = 0; i < n; i++) {
			// TODO transform each marked atom into an equation
		}
		return model.getRenderer().printUnifier(dissubsumptions);
	}

	private Container createRefinePanel() {
		Container ret = new Box(BoxLayout.Y_AXIS);

		ret.add(createButtons());

		ret.add(Box.createVerticalStrut(UelUI.GAP_SIZE));

		UelUI.setupScrollPane(ret, createSelectionPanels(), "", new Dimension(640, 480));

		JScrollPane scrollPane = new JScrollPane(createSelectionPanels());
		ret.add(scrollPane);

		return ret;
	}

	private Component createButtons() {
		Container buttonPanel = new JPanel();

		UelUI.setupButton(buttonPanel, buttonRecompute, UelUI.ICON_FORWARD, Message.tooltipRecompute);

		UelUI.setupButton(buttonPanel, buttonSave, UelUI.ICON_SAVE, Message.tooltipSaveDissub);

		return buttonPanel;
	}

	private Component createSelectionPanels() {
		Container panel = new Box(BoxLayout.X_AXIS);
		// TODO jlists for each equation
		return panel;
	}

}
