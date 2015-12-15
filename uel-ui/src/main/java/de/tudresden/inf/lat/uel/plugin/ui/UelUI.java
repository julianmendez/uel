package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * This manages the icons.
 * 
 * @author Julian Mendez
 *
 */
public class UelUI {

	static class ComboBoxRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -2411864526023749022L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value instanceof OWLOntology) {
				OWLOntologyID id = ((OWLOntology) value).getOntologyID();
				if (id.isAnonymous()) {
					this.setText(id.toString());
				} else {
					this.setText(id.getOntologyIRI().toString());
				}
			}

			return this;
		}

		@Override
		public Dimension getMaximumSize() {
			Dimension d = super.getMaximumSize();
			d.height *= 1.4;
			return d;
		}

		@Override
		public Dimension getMinimumSize() {
			Dimension d = super.getMinimumSize();
			d.height *= 1.4;
			return d;
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			d.height *= 1.4;
			return d;
		}
	}

	public static final int DEFAULT_ICON_SIZE = 18;
	public static final int GAP_SIZE = 6;

	public static final String PATH_BACK = "icons/back.png";
	public static final String PATH_FAST_FORWARD = "icons/fastforward.png";
	public static final String PATH_FORWARD = "icons/forward.png";
	public static final String PATH_OPEN = "icons/openfolder.png";
	public static final String PATH_REFINE = "icons/refine.png";
	public static final String PATH_REWIND = "icons/rewind.png";
	public static final String PATH_SAVE = "icons/floppydisk.png";
	public static final String PATH_STATISTICS = "icons/statistics.png";
	public static final String PATH_STEP_BACK = "icons/stepback.png";
	public static final String PATH_STEP_FORWARD = "icons/stepforward.png";

	public static final ImageIcon ICON_BACK = createIcon(PATH_BACK);
	public static final ImageIcon ICON_FAST_FORWARD = createIcon(PATH_FAST_FORWARD);
	public static final ImageIcon ICON_FORWARD = createIcon(PATH_FORWARD);
	public static final ImageIcon ICON_OPEN = createIcon(PATH_OPEN);
	public static final ImageIcon ICON_REFINE = createIcon(PATH_REFINE);
	public static final ImageIcon ICON_REWIND = createIcon(PATH_REWIND);
	public static final ImageIcon ICON_SAVE = createIcon(PATH_SAVE);
	public static final ImageIcon ICON_STATISTICS = createIcon(PATH_STATISTICS);
	public static final ImageIcon ICON_STEP_BACK = createIcon(PATH_STEP_BACK);
	public static final ImageIcon ICON_STEP_FORWARD = createIcon(PATH_STEP_FORWARD);

	public static void addLabel(Container parent, String text) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		parent.add(label);
	}

	/**
	 * Returns an icon created with the default size for the given path.
	 * 
	 * @param path
	 *            of icon
	 * @return an icon, or <code>null</code> if the path is invalid
	 */
	public static ImageIcon createIcon(String path) {
		return createIcon(path, DEFAULT_ICON_SIZE);
	}

	/**
	 * Creates an icon with the given size. If the path is invalid, this method
	 * returns <code>null</code>.
	 * 
	 * @param path
	 *            path of icon
	 * @param size
	 *            size of icon
	 * @return an icon with the given size, or <code>null</code> if the path is
	 *         invalid
	 */
	public static ImageIcon createIcon(String path, int size) {
		ImageIcon ret = null;
		try {
			URL url = UelUI.class.getClassLoader().getResource(path);
			if (url == null) {
				try {
					throw new IllegalArgumentException("Icon has an invalid path: '" + path + "'.");
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}

			} else {
				BufferedImage img = ImageIO.read(url);
				ret = new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static void setupWindow(JDialog window, Component child) {
		window.setLocation(200, 200);
		window.setSize(new Dimension(800, 600));
		window.setMinimumSize(new Dimension(200, 200));
		window.setLayout(new GridBagLayout());
		window.add(child);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public static void setupButton(Container parent, JButton button, ImageIcon icon, String toolTipText) {
		button.setIcon(icon);
		button.setToolTipText(toolTipText);
		Border line = new LineBorder(new Color(150, 150, 150));
		Border margin = new EmptyBorder(0, 4, 0, 4);
		Border compound = new CompoundBorder(line, margin);
		button.setBorder(compound);
		parent.add(button);
	}

	public static <E> void setupComboBox(Container parent, JComboBox<E> comboBox, String tooltipText) {
		comboBox.setRenderer(new ComboBoxRenderer());
		comboBox.setToolTipText(tooltipText);
		comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		comboBox.setEditable(false);
		parent.add(comboBox);
	}

	public static void setupLabel(Container parent, JLabel label, String tooltipText, Dimension preferredSize) {
		label.setToolTipText(tooltipText);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		if (preferredSize != null) {
			label.setPreferredSize(preferredSize);
		}
		parent.add(label);
	}

	public static void setupScrollPane(Container parent, Component child, String tooltipText, Dimension preferredSize) {
		if (!tooltipText.equals("")) {
			((JComponent) child).setToolTipText(tooltipText);
		}
		JScrollPane scrollPane = new JScrollPane(child);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		if (preferredSize != null) {
			scrollPane.setPreferredSize(preferredSize);
		}
		parent.add(scrollPane);
	}

	public static void setupScrollTextArea(Container parent, JTextArea textArea, String tooltipText,
			Dimension preferredSize) {
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		setupScrollPane(parent, textArea, tooltipText, preferredSize);
	}

	public static File showSaveFileDialog(Component parent) {
		JFileChooser fileChooser = new JFileChooser();
		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else {
			return null;
		}
	}

	public static File showOpenFileDialog(Component parent) {
		JFileChooser fileChooser = new JFileChooser();
		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else {
			return null;
		}
	}

}
