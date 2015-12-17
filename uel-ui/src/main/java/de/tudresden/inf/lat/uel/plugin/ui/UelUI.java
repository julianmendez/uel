package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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

	static class ToggleListSelectionModel extends DefaultListSelectionModel {

		private static final long serialVersionUID = -187853559553210017L;

		boolean gestureStarted = false;

		@Override
		public void setSelectionInterval(int index0, int index1) {
			if (!gestureStarted) {
				if (isSelectedIndex(index0)) {
					super.removeSelectionInterval(index0, index1);
				} else {
					super.addSelectionInterval(index0, index1);
				}
			}
			gestureStarted = true;
		}

		@Override
		public void setValueIsAdjusting(boolean isAdjusting) {
			if (isAdjusting == false) {
				gestureStarted = false;
			}
		}
	}

	private static final int DEFAULT_ICON_SIZE = 18;
	private static final int GAP_SIZE = 6;

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

	public static final Icon ICON_BACK = createIcon(PATH_BACK);
	public static final Icon ICON_FAST_FORWARD = createIcon(PATH_FAST_FORWARD);
	public static final Icon ICON_FORWARD = createIcon(PATH_FORWARD);
	public static final Icon ICON_OPEN = createIcon(PATH_OPEN);
	public static final Icon ICON_REFINE = createIcon(PATH_REFINE);
	public static final Icon ICON_REWIND = createIcon(PATH_REWIND);
	public static final Icon ICON_SAVE = createIcon(PATH_SAVE);
	public static final Icon ICON_STATISTICS = createIcon(PATH_STATISTICS);
	public static final Icon ICON_STEP_BACK = createIcon(PATH_STEP_BACK);
	public static final Icon ICON_STEP_FORWARD = createIcon(PATH_STEP_FORWARD);

	public static void setBorder(JComponent comp) {
		Border line = new LineBorder(new Color(150, 150, 150));
		Border margin = new EmptyBorder(0, 4, 0, 4);
		Border compound = new CompoundBorder(line, margin);
		comp.setBorder(compound);
	}

	public static Container addButtonPanel(Container parent) {
		JPanel panel = new JPanel();
		setupContainer(parent, panel);
		return panel;
	}

	public static void addGlue(Container parent) {
		LayoutManager layout = parent.getLayout();
		if ((layout instanceof BoxLayout) && (((BoxLayout) layout).getAxis() == BoxLayout.Y_AXIS)) {
			parent.add(Box.createVerticalGlue());
		} else {
			parent.add(Box.createHorizontalGlue());
		}
	}

	public static Container addHorizontalPanel(Container parent) {
		Box box = Box.createHorizontalBox();
		setupContainer(parent, box);
		return box;
	}

	public static JLabel addLabel(Container parent, String text) {
		return addLabel(parent, text, "");
	}

	public static JLabel addLabel(Container parent, String text, String tooltipText) {
		JLabel label = new JLabel(text);
		setupLabel(parent, label, tooltipText, null);
		return label;
	}

	public static <T> JList<T> addList(Container parent, List<T> data, String tooltipText) {
		JList<T> list = new JList<T>(new Vector<T>(data));
		list.setToolTipText(tooltipText);
		list.setAlignmentX(Component.LEFT_ALIGNMENT);
		list.setSelectionModel(new ToggleListSelectionModel());
		setBorder(list);
		parent.add(list);
		return list;
	}

	public static JScrollPane addScrollPane(Container parent, Component child, String tooltipText,
			Dimension preferredSize) {
		if (!tooltipText.equals("")) {
			((JComponent) child).setToolTipText(tooltipText);
		}
		JScrollPane scrollPane = new JScrollPane(child);
		if (preferredSize != null) {
			scrollPane.setPreferredSize(preferredSize);
		}
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		setBorder(scrollPane);
		parent.add(scrollPane);
		return scrollPane;
	}

	public static JScrollPane addScrollTextArea(Container parent, JTextArea textArea, String tooltipText,
			Dimension preferredSize) {
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		return addScrollPane(parent, textArea, tooltipText, preferredSize);
	}

	public static void addStrut(Container parent) {
		LayoutManager layout = parent.getLayout();
		if ((layout instanceof BoxLayout) && (((BoxLayout) layout).getAxis() == BoxLayout.Y_AXIS)) {
			parent.add(Box.createVerticalStrut(GAP_SIZE));
		} else {
			parent.add(Box.createHorizontalStrut(GAP_SIZE));
		}
	}

	public static Container addVerticalPanel(Container parent) {
		Box box = Box.createVerticalBox();
		setupContainer(parent, box);
		return box;
	}

	/**
	 * Returns an icon created with the default size for the given path.
	 * 
	 * @param path
	 *            of icon
	 * @return an icon, or <code>null</code> if the path is invalid
	 */
	public static Icon createIcon(String path) {
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
	public static Icon createIcon(String path, int size) {
		try {
			URL url = UelUI.class.getClassLoader().getResource(path);
			if (url == null) {
				throw new IllegalArgumentException("Icon has an invalid path: '" + path + "'.");
			} else {
				return new ImageIcon(ImageIO.read(url).getScaledInstance(size, size, Image.SCALE_SMOOTH));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setupButton(Container parent, JButton button, Icon icon, String toolTipText) {
		button.setIcon(icon);
		button.setToolTipText(toolTipText);
		setBorder(button);
		parent.add(button);
	}

	private static void setupContainer(Container parent, JComponent comp) {
		comp.setAlignmentX(Component.CENTER_ALIGNMENT);
		comp.setAlignmentY(Component.TOP_ALIGNMENT);
		comp.setMinimumSize(new Dimension(0, 0));
		parent.add(comp);
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
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		if (preferredSize != null) {
			label.setPreferredSize(preferredSize);
		}
		parent.add(label);
	}

	public static void setupWindow(JDialog window) {
		window.setLocation(200, 200);
		window.setSize(new Dimension(800, 600));
		window.setMinimumSize(new Dimension(200, 200));
		window.setLayout(new GridBagLayout());
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public static File showOpenFileDialog(Component parent) {
		JFileChooser fileChooser = new JFileChooser();
		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else {
			return null;
		}
	}

	public static File showSaveFileDialog(Component parent) {
		JFileChooser fileChooser = new JFileChooser();
		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else {
			return null;
		}
	}

}
