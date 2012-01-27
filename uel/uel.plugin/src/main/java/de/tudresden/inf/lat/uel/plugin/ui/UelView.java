package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.tudresden.inf.lat.uel.plugin.processor.UelProcessor;

/**
 * Panel for UEL.
 * 
 * @author Julian Mendez
 */
public class UelView extends JPanel {

	private static final long serialVersionUID = 9096602357606632334L;

	private JButton buttonOpen = new JButton(new ImageIcon(this.getClass()
			.getClassLoader().getResource(Message.iconOpen)));
	private JButton buttonSelectVariables = new JButton(new ImageIcon(this
			.getClass().getClassLoader().getResource(Message.iconForward)));
	private JComboBoxOfLabelId listClassName00 = new JComboBoxOfLabelId();
	private JComboBoxOfLabelId listClassName01 = new JComboBoxOfLabelId();
	private JComboBox listOntologyName00 = new JComboBox();
	private JComboBox listOntologyName01 = new JComboBox();
	private final UelProcessor model;

	public UelView(UelProcessor processor) {
		if (processor == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.model = processor;
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

	public void addComboBoxClass00Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listClassName00.addActionListener(listener);
		this.listClassName00.setActionCommand(actionCommand);
	}

	public void addComboBoxClass01Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listClassName01.addActionListener(listener);
		this.listClassName01.setActionCommand(actionCommand);
	}

	public void addComboBoxOntology00Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyName00.addActionListener(listener);
		this.listOntologyName00.setActionCommand(actionCommand);
	}

	public void addComboBoxOntology01Listener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyName01.addActionListener(listener);
		this.listOntologyName01.setActionCommand(actionCommand);
	}

	private JPanel createSelectionPanel() {
		JPanel ret = new JPanel(new GridBagLayout());
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		JPanel smallPanel = new JPanel();
		this.buttonOpen.setToolTipText(Message.tooltipOpen);
		smallPanel.add(this.buttonOpen);
		this.buttonSelectVariables
				.setToolTipText(Message.tooltipSelectVariables);
		smallPanel.add(this.buttonSelectVariables);
		ret.add(smallPanel);

		JLabel gap1 = new JLabel();
		gap1.setPreferredSize(new Dimension(280, 28));
		gap1.setMinimumSize(new Dimension(112, 28));
		ret.add(gap1);

		this.listOntologyName00
				.setToolTipText(Message.tooltipComboBoxOntology00);
		this.listOntologyName00.setPreferredSize(new Dimension(280, 28));
		this.listOntologyName00.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listOntologyName00);

		this.listClassName00.setToolTipText(Message.tooltipComboBoxClassName00);
		this.listClassName00.setPreferredSize(new Dimension(280, 28));
		this.listClassName00.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listClassName00);

		JLabel gap2 = new JLabel();
		gap2.setPreferredSize(new Dimension(280, 28));
		gap2.setMinimumSize(new Dimension(112, 28));
		ret.add(gap2);

		this.listOntologyName01
				.setToolTipText(Message.tooltipComboBoxOntology01);
		this.listOntologyName01.setPreferredSize(new Dimension(280, 28));
		this.listOntologyName01.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listOntologyName01);

		this.listClassName01.setToolTipText(Message.tooltipComboBoxClassName01);
		this.listClassName01.setPreferredSize(new Dimension(280, 28));
		this.listClassName01.setMinimumSize(new Dimension(112, 28));
		ret.add(this.listClassName01);

		return ret;
	}

	public UelProcessor getModel() {
		return this.model;
	}

	public LabelId getSelectedClassName00() {
		return this.listClassName00.getSelectedElement();
	}

	public LabelId getSelectedClassName01() {
		return this.listClassName01.getSelectedElement();
	}

	public int getSelectedOntologyName00() {
		return this.listOntologyName00.getSelectedIndex();
	}

	public int getSelectedOntologyName01() {
		return this.listOntologyName01.getSelectedIndex();
	}

	public void reloadClassNames00(List<LabelId> list) {
		if (list == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listClassName00.setItemList(list);
	}

	public void reloadClassNames01(List<LabelId> list) {
		if (list == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listClassName01.setItemList(list);
	}

	public void reloadOntologies(List<String> listOfOntologyNames) {
		if (listOfOntologyNames == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listOntologyName00.removeAllItems();
		this.listOntologyName01.removeAllItems();
		for (String ontologyName : listOfOntologyNames) {
			this.listOntologyName00.addItem(ontologyName);
			this.listOntologyName01.addItem(ontologyName);
		}
		this.listClassName00.removeAllItems();
		this.listClassName01.removeAllItems();
	}

	public void setButtonLoadEnabled(boolean b) {
		this.buttonOpen.setEnabled(b);
	}

	public void setButtonSelectVariablesEnabled(boolean b) {
		this.buttonSelectVariables.setEnabled(b);
	}

	public void setComboBoxClassName00Enabled(boolean b) {
		this.listClassName00.setEnabled(b);
	}

	public void setComboBoxClassName01Enabled(boolean b) {
		this.listClassName01.setEnabled(b);
	}

	public void setComboBoxOntologyName00Enabled(boolean b) {
		this.listOntologyName00.setEnabled(b);
	}

	public void setComboBoxOntologyName01Enabled(boolean b) {
		this.listOntologyName01.setEnabled(b);
	}

	public void setToolTipTextClass00(String str) {
		if (str == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listClassName00.setToolTipText(str);
	}

	public void setToolTipTextClass01(String str) {
		if (str == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.listClassName01.setToolTipText(str);
	}

}
