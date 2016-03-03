/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * @author Stefan Borgwardt
 *
 */
abstract class UelDialog extends JDialog {

	private static final long serialVersionUID = -9159093716481990295L;

	private Component parent;

	public UelDialog() {
		super((Frame) null, true);
	}

	public void close() {
		setVisible(false);
		dispose();
	}

	protected abstract JComponent createMainPanel();

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(640, 480);
	}

	public void open() {
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	protected void setup(Component parent, String title) {
		this.parent = parent;
		setResizable(true);
		setTitle(title);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JComponent mainPanel = createMainPanel();
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(mainPanel);
	}

}
