package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
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

	private JComboBox classNameList00 = null;
	private JComboBox classNameList01 = null;
	private JButton getVarButton = null;
	private DefaultListModel listmodel = new DefaultListModel();
	private UelProcessor model = null;
	private JButton unifyButton = null;

	public UelView(UelProcessor processor) {
		if (processor == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.model = processor;
		init();
	}

	public void addGetVarButtonListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.getVarButton.addActionListener(listener);
		this.getVarButton.setActionCommand(actionCommand);
	}

	public void addUnifyButtonListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.unifyButton.addActionListener(listener);
		this.unifyButton.setActionCommand(actionCommand);
	}

	public JButton getGetVarButton() {
		return this.getVarButton;
	}

	public DefaultListModel getListModel() {
		return this.listmodel;
	}

	public UelProcessor getModel() {
		return this.model;
	}

	public int getSelectedIndex00() {
		return this.classNameList00.getSelectedIndex();
	}

	public int getSelectedIndex01() {
		return this.classNameList01.getSelectedIndex();
	}

	public JButton getUnifyButton() {
		return this.unifyButton;
	}

	public void init() {
		this.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(initControlPanel(), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel initControlPanel() {
		JPanel ret = new JPanel(new FlowLayout());
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		JPanel textPanel = new JPanel();
		textPanel.setMinimumSize(new Dimension(280, 28));
		JLabel label00 = new JLabel(Message.uelLabel);
		label00.setPreferredSize(new Dimension(280, 28));
		textPanel.add(label00);

		JPanel selectionPanel = new JPanel();
		selectionPanel
				.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));

		this.classNameList00 = new JComboBox();
		this.classNameList00.setPreferredSize(new Dimension(112, 28));
		this.classNameList00.setMinimumSize(new Dimension(56, 28));
		this.classNameList00.setMaximumSize(new Dimension(280, 28));
		selectionPanel.add(this.classNameList00);
		this.classNameList01 = new JComboBox();
		this.classNameList01.setPreferredSize(new Dimension(112, 28));
		this.classNameList01.setMinimumSize(new Dimension(56, 28));
		this.classNameList01.setMaximumSize(new Dimension(280, 28));
		selectionPanel.add(this.classNameList01);

		JPanel computePanel = new JPanel(new FlowLayout());
		this.getVarButton = new JButton(Message.getVarButton);
		this.getVarButton.setToolTipText(Message.getVarTooltip);
		computePanel.add(this.getVarButton);
		this.unifyButton = new JButton(Message.unifyButton);
		this.unifyButton.setToolTipText(Message.unifyTooltip);
		computePanel.add(this.unifyButton);

		ret.add(textPanel);
		ret.add(selectionPanel);
		ret.add(computePanel);

		return ret;
	}

	public void refresh(List<String> classNameSet) {
		if (classNameSet == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		for (String cls : classNameSet) {
			this.classNameList00.addItem(cls);
			this.classNameList01.addItem(cls);
		}
	}

}
