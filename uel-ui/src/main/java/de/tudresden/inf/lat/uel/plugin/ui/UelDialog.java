/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;

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

	private static Point location = null;
	private static Dimension size = null;

	private Component parent;

	/**
	 * Constructs a new dialog with standard behavior for UEL.
	 */
	public UelDialog() {
		super((Frame) null, true);
	}

	/**
	 * Close and dispose of the dialog.
	 */
	public void close() {
		size = getSize();
		location = getLocation();
		setVisible(false);
		dispose();
	}

	/**
	 * Constructs the main panel of this UelDialog.
	 * 
	 * @return a JComponent constituting the main panel
	 */
	protected abstract JComponent createMainPanel();

	@Override
	public Dimension getPreferredSize() {
		if (size != null) {
			return size;
		} else {
			return new Dimension(640, 480);
		}
	}

	/**
	 * Open this dialog, centered on the position of the parent view.
	 */
	public void open() {
		pack();
		if (location != null) {
			setLocation(location);
		} else {
			setLocationRelativeTo(parent);
		}
		setVisible(true);
	}

	/**
	 * Set up the dialog. This method should be called in the constructor of an
	 * implementing class.
	 * 
	 * @param parent
	 *            the parent view
	 * @param title
	 *            the window title
	 */
	protected void setup(Component parent, String title) {
		this.parent = parent;
		if (parent instanceof UelDialog) {
			size = parent.getSize();
			location = parent.getLocation();
		}
		setResizable(true);
		setTitle(title);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JComponent mainPanel = createMainPanel();
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(mainPanel);
	}

}
