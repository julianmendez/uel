package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UelProcessorFactory;

/**
 * This is the main panel of the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelView extends JPanel {

	private static final long serialVersionUID = 9096602357606632334L;

	private JButton buttonOpen = new JButton(UelIcon.ICON_OPEN);
	private JButton buttonSelectVariables = new JButton(UelIcon.ICON_FORWARD);
	private JComboBox<OWLOntology> listOntologyNameBg00 = new JComboBox<OWLOntology>();
	private JComboBox<OWLOntology> listOntologyNameBg01 = new JComboBox<OWLOntology>();
	private JComboBox<OWLOntology> listOntologyNamePos = new JComboBox<OWLOntology>();
	private JComboBox<OWLOntology> listOntologyNameNeg = new JComboBox<OWLOntology>();
	private JComboBox<String> listProcessor = new JComboBox<String>();
	private final UelModel model;

	public UelView(UelModel model) {
		if (model == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.model = model;
		add(createSelectionPanel());
	}

	public void addButtonOpenListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonOpen.addActionListener(listener);
		this.buttonOpen.setActionCommand(actionCommand);
	}

	public void addButtonSelectVariablesListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonSelectVariables.addActionListener(listener);
		this.buttonSelectVariables.setActionCommand(actionCommand);
	}

	public void addComboBoxOntologyBg00Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyNameBg00.addActionListener(listener);
		this.listOntologyNameBg00.setActionCommand(actionCommand);
	}

	public void addComboBoxOntologyBg01Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyNameBg01.addActionListener(listener);
		this.listOntologyNameBg01.setActionCommand(actionCommand);
	}

	public void addComboBoxOntologyPosListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyNamePos.addActionListener(listener);
		this.listOntologyNamePos.setActionCommand(actionCommand);
	}

	public void addComboBoxOntologyNegListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyNameNeg.addActionListener(listener);
		this.listOntologyNameNeg.setActionCommand(actionCommand);
	}

	private JPanel createSelectionPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		int width = 280;
		int height = 28;
		int gap = 4;

		JPanel smallPanel = new JPanel();
		smallPanel.setAlignmentX(CENTER_ALIGNMENT);
		this.listProcessor.setRenderer(new ComboBoxRenderer());
		this.listProcessor.setToolTipText(Message.tooltipSelectProcessor);
		for (String processorName : UelProcessorFactory.getProcessorNames()) {
			this.listProcessor.addItem(processorName);
		}
		smallPanel.add(this.listProcessor);
		this.buttonOpen.setToolTipText(Message.tooltipOpen);
		UelIcon.setBorder(buttonOpen);
		smallPanel.add(this.buttonOpen);
		this.buttonSelectVariables
				.setToolTipText(Message.tooltipSelectVariables);
		UelIcon.setBorder(buttonSelectVariables);
		smallPanel.add(this.buttonSelectVariables);
		ret.add(smallPanel);

		ret.add(Box.createVerticalStrut(gap));

		JPanel largePanel = new JPanel();
		largePanel.setAlignmentX(CENTER_ALIGNMENT);
		// largePanel.setPreferredSize(new Dimension(0, width));
		largePanel.setLayout(new BoxLayout(largePanel, BoxLayout.Y_AXIS));

		JLabel labelOntologyNameBg00 = new JLabel(Message.textOntologyBg00);
		// labelOntologyNameBg00.setPreferredSize(new Dimension(width, height));
		labelOntologyNameBg00.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(labelOntologyNameBg00);

		this.listOntologyNameBg00.setRenderer(new ComboBoxRenderer());
		this.listOntologyNameBg00
				.setToolTipText(Message.tooltipComboBoxOntologyBg00);
		// this.listOntologyNameBg00
		// .setPreferredSize(new Dimension(width, height));
		this.listOntologyNameBg00.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(this.listOntologyNameBg00);

		largePanel.add(Box.createVerticalStrut(gap));

		JLabel labelOntologyNameBg01 = new JLabel(Message.textOntologyBg01);
		// labelOntologyNameBg01.setPreferredSize(new Dimension(width, height));
		labelOntologyNameBg01.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(labelOntologyNameBg01);

		this.listOntologyNameBg01.setRenderer(new ComboBoxRenderer());
		this.listOntologyNameBg01
				.setToolTipText(Message.tooltipComboBoxOntologyBg01);
		// this.listOntologyNameBg01
		// .setPreferredSize(new Dimension(width, height));
		this.listOntologyNameBg01.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(this.listOntologyNameBg01);

		largePanel.add(Box.createVerticalStrut(gap));

		JLabel labelOntologyNamePos = new JLabel(Message.textOntologyPos);
		// labelOntologyNamePos.setPreferredSize(new Dimension(width, height));
		labelOntologyNamePos.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(labelOntologyNamePos);

		this.listOntologyNamePos.setRenderer(new ComboBoxRenderer());
		this.listOntologyNamePos
				.setToolTipText(Message.tooltipComboBoxOntologyPos);
		// this.listOntologyNamePos.setPreferredSize(new Dimension(width,
		// height));
		this.listOntologyNamePos.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(this.listOntologyNamePos);

		largePanel.add(Box.createVerticalStrut(gap));

		JLabel labelOntologyNameNeg = new JLabel(Message.textOntologyNeg);
		// labelOntologyNameNeg.setPreferredSize(new Dimension(width, height));
		labelOntologyNameNeg.setHorizontalAlignment(SwingConstants.CENTER);
		largePanel.add(labelOntologyNameNeg);

		this.listOntologyNameNeg.setRenderer(new ComboBoxRenderer());
		this.listOntologyNameNeg
				.setToolTipText(Message.tooltipComboBoxOntologyNeg);
		this.listOntologyNameNeg.setMinimumSize(new Dimension(width, height));
		this.listOntologyNameNeg.setAlignmentX(LEFT_ALIGNMENT);
		largePanel.add(this.listOntologyNameNeg);

		ret.add(largePanel);

		return ret;
	}

	public UelModel getModel() {
		return this.model;
	}

	public int getSelectedOntologyNameBg00() {
		return this.listOntologyNameBg00.getSelectedIndex();
	}

	public int getSelectedOntologyNameBg01() {
		return this.listOntologyNameBg01.getSelectedIndex();
	}

	public int getSelectedOntologyNamePos() {
		return this.listOntologyNamePos.getSelectedIndex();
	}

	public int getSelectedOntologyNameNeg() {
		return this.listOntologyNameNeg.getSelectedIndex();
	}

	public String getSelectedProcessor() {
		return this.listProcessor.getSelectedItem().toString();
	}

	public void reloadOntologies(List<OWLOntology> listOfOntologies) {
		if (listOfOntologies == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyNameBg00.removeAllItems();
		this.listOntologyNameBg01.removeAllItems();
		this.listOntologyNamePos.removeAllItems();
		this.listOntologyNameNeg.removeAllItems();
		for (OWLOntology ontology : listOfOntologies) {
			this.listOntologyNameBg00.addItem(ontology);
			this.listOntologyNameBg01.addItem(ontology);
			this.listOntologyNamePos.addItem(ontology);
			this.listOntologyNameNeg.addItem(ontology);
		}
	}

	public void setButtonLoadEnabled(boolean b) {
		this.buttonOpen.setEnabled(b);
	}

	public void setButtonSelectVariablesEnabled(boolean b) {
		this.buttonSelectVariables.setEnabled(b);
	}

	public void setComboBoxOntologyNameBg00Enabled(boolean b) {
		this.listOntologyNameBg00.setEnabled(b);
	}

	public void setComboBoxOntologyNameBg01Enabled(boolean b) {
		this.listOntologyNameBg01.setEnabled(b);
	}

	public void setComboBoxOntologyNamePosEnabled(boolean b) {
		this.listOntologyNamePos.setEnabled(b);
	}

	public void setComboBoxOntologyNameNegEnabled(boolean b) {
		this.listOntologyNameNeg.setEnabled(b);
	}

	static class ComboBoxRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -2411864526023749022L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

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
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			d.height *= 1.4;
			return d;
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
	}
}
