/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Container;
import java.awt.Frame;

import javax.swing.JDialog;

/**
 * @author Stefan Borgwardt
 *
 */
public abstract class UelDialog extends JDialog {

	private static final long serialVersionUID = -9159093716481990295L;

	public UelDialog() {
		super((Frame) null, true);
	}
	
	protected void setup(String title) {
		setTitle(title);
		UelUI.setupWindow(this);
		addMainPanel(getContentPane());
	}

	protected abstract void addMainPanel(Container parent);

}
