package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * This manages the icons.
 * 
 * @author Julian Mendez
 *
 */
class UelUI {

	/**
	 * This renderer for UEL's combo boxes makes some layout adjustments and
	 * takes care of rendering the names of OWLOntologies.
	 * 
	 * @author Stefan Borgwardt
	 */
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
					this.setText(id.getOntologyIRI().get().toString());
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

	static final int DEFAULT_ICON_SIZE = 18;
	static final int GAP_SIZE = 5;

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
	public static final String PATH_UNDO = "icons/undo.png";

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
	public static final Icon ICON_UNDO = createIcon(PATH_UNDO);

	static File previousFile = null;

	static JComponent createButtonPanel() {
		return new JPanel(new FlowLayout(FlowLayout.CENTER, UelUI.GAP_SIZE, 0));
	}

	/**
	 * Returns an icon created with the default size for the given path.
	 * 
	 * @param path
	 *            of icon
	 * @return an icon, or <code>null</code> if the path is invalid
	 */
	static Icon createIcon(String path) {
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
	static Icon createIcon(String path, int size) {
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

	static JLabel createLabel(String text, String toolTipText) {
		return setupLabel(new JLabel(text), toolTipText);
	}

	static <T> JList<T> createList(List<T> data, String toolTipText, boolean border) {
		return setupList(new JList<T>(new Vector<>(data)), toolTipText, border);
	}

	static JScrollPane createScrollableTextArea(JTextArea textArea, String toolTipText) {
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setToolTipText(toolTipText);
		((DefaultCaret) textArea.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		setMargin(textArea);
		return createScrollPane(textArea, true);
	}

	static JScrollPane createScrollPane(JComponent child, boolean border) {
		JScrollPane scrollPane = new JScrollPane(child);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		if (border) {
			setBorder(scrollPane, false);
		}
		return scrollPane;
	}

	static Component createStrut() {
		return Box.createHorizontalStrut(GAP_SIZE);
	}

	static JComponent createVerticalPanel() {
		return new JPanel(new BorderLayout(0, UelUI.GAP_SIZE));
	}

	static void mark(JComponent comp) {
		comp.setBorder(new LineBorder(Color.RED));
	}

	static void scrollTop(JComponent comp) {
		((JScrollPane) comp.getParent().getParent()).getVerticalScrollBar().setValue(0);
	}

	static void setBorder(JComponent comp, boolean insideMargin) {
		boolean button = comp instanceof JButton;
		Border line = new LineBorder(new Color(150, 150, 150));
		if (insideMargin) {
			int vertical = button ? 0 : GAP_SIZE - 1;
			int horizontal = GAP_SIZE - 1;
			Border margin = new EmptyBorder(vertical, horizontal, vertical, horizontal);
			Border compound = new CompoundBorder(line, margin);
			comp.setBorder(compound);
		} else {
			comp.setBorder(line);
		}
	}

	static void setMargin(JComponent comp) {
		comp.setBorder(new EmptyBorder(GAP_SIZE - 1, GAP_SIZE - 1, GAP_SIZE - 1, GAP_SIZE - 1));
	}

	static JButton setupButton(JButton button, Icon icon, String toolTipText) {
		button.setIcon(icon);
		button.setToolTipText(toolTipText);
		setBorder(button, true);
		return button;
	}

	static JCheckBox setupCheckBox(JCheckBox checkBox, boolean selected, String text) {
		checkBox.setSelected(selected);
		checkBox.setText(text);
		return checkBox;
	}

	static <E> JComboBox<E> setupComboBox(JComboBox<E> comboBox, String toolTipText) {
		comboBox.setRenderer(new ComboBoxRenderer());
		comboBox.setToolTipText(toolTipText);
		comboBox.setEditable(false);
		return comboBox;
	}

	static JLabel setupLabel(JLabel label, String toolTipText) {
		label.setToolTipText(toolTipText);
		return label;
	}

	static <E> JList<E> setupList(JList<E> list, String toolTipText, boolean border) {
		list.setToolTipText(toolTipText);
		list.setSelectionModel(new ToggleListSelectionModel());
		if (border) {
			setBorder(list, true);
		} else {
			setMargin(list);
		}
		return list;
	}

	static File showOpenFileDialog(Component parent) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(previousFile);
		if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			previousFile = fileChooser.getSelectedFile();
			return previousFile;
		} else {
			return null;
		}
	}

	static File showSaveFileDialog(Component parent) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(previousFile);
		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			previousFile = fileChooser.getSelectedFile();
			return previousFile;
		} else {
			return null;
		}
	}

}
