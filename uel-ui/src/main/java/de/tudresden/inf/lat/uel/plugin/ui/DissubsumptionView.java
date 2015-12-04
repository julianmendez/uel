/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.tudresden.inf.lat.uel.core.type.KRSSRenderer;
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

	private KRSSRenderer renderer;
	private Set<Equation> unifier;
	private JList[] lists;

	public DissubsumptionView(Set<Equation> unifier, KRSSRenderer renderer) {
		this.unifier = unifier;
		this.renderer = renderer;
		n = unifier.size();
		init();
	}

	public void addRecomputeButtonListener(ActionListener listener, String actionCommand) {
		buttonRecompute.addActionListener(listener);
		buttonRecompute.setActionCommand(actionCommand);
	}

	public void addSaveButtonListener(ActionListener listener, String actionCommand) {
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
		return renderer.printUnifier(dissubsumptions);
	}

	private void init() {
		setSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(200, 200));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		getContentPane().add(createButtonPanel());
		getContentPane().add(createSelectionPanels());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		// TODO two buttons
		return buttonPanel;
	}

	private JPanel createSelectionPanels() {
		JPanel panel = new JPanel();
		// TODO jlists for each equation, everything in a scrollpane
		return panel;
	}

}
