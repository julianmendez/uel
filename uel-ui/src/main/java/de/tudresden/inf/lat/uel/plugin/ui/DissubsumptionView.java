/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.type.api.Equation;

/**
 * @author Stefan Borgwardt
 *
 */
public class DissubsumptionView extends JDialog {

	private static final long serialVersionUID = 6093665334232206919L;

	private JButton buttonRecompute;
	private JButton buttonSave;
	private int n;

	private UelModel model;
	private Set<Equation> unifier;
	private JList<LabelId>[] lists;

	public DissubsumptionView(Set<Equation> unifier, UelModel model) {
		this.unifier = unifier;
		this.model = model;
		n = unifier.size();
		init();
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

	private void init() {
		setSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(200, 200));
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		getContentPane().add(createButtonPanel());
		getContentPane().add(createSelectionPanels());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private Container createButtonPanel() {
		Container buttonPanel = new Box(BoxLayout.X_AXIS);
		// TODO two buttons
		return buttonPanel;
	}

	private Container createSelectionPanels() {
		Container panel = new Box(BoxLayout.X_AXIS);
		// TODO jlists for each equation, everything in a scrollpane
		JScrollPane scrollPane = new JScrollPane(panel);
		return scrollPane;
	}

}
