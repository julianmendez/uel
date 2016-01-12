/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Container;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * @author Stefan Borgwardt
 *
 */
abstract class UelDialog extends JDialog {

	private static final long serialVersionUID = -9159093716481990295L;

	public UelDialog() {
		super((Frame) null, true);
	}

	protected void setup(String title) {
		setTitle(title);
		UelUI.setupWindow(this);
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel);
		addMainPanel(panel);
	}

	protected abstract void addMainPanel(Container parent);

}
