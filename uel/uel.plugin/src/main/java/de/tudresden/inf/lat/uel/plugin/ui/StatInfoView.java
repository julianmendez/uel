package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;

/**
 * 
 * @author Julian Mendez
 */
class StatInfoView extends JDialog {

	private static final String quotes = "\"";
	private static final long serialVersionUID = -4153981096827550491L;

	private final StatInfo model;
	private JButton saveButton = null;
	private JTextArea textClauseCount = null;
	private JTextArea textGoal = null;
	private JTextArea textLiteralCount = null;
	private JTextArea textSysVarCount = null;

	public StatInfoView(StatInfo info) {
		super((Frame) null, "Statistical information", true);

		if (info == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.model = info;
		initFrame();
	}

	public void addSaveButtonListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.saveButton.addActionListener(listener);
		this.saveButton.setActionCommand(actionCommand);
	}

	private JPanel createMainPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		this.textGoal = new JTextArea();
		this.textGoal.setToolTipText(Message.tooltipGoal);
		this.textGoal.setWrapStyleWord(true);
		this.textGoal.setLineWrap(true);
		JScrollPane scrollPaneVars = new JScrollPane(this.textGoal);
		scrollPaneVars
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPaneVars.setPreferredSize(new Dimension(640, 480));

		JPanel goalPanel = new JPanel();
		goalPanel.setLayout(new BoxLayout(goalPanel, BoxLayout.X_AXIS));
		goalPanel.add(scrollPaneVars);

		JPanel buttonPanel = new JPanel();
		this.saveButton = new JButton(new ImageIcon(this.getClass()
				.getClassLoader().getResource(Message.iconSave)));
		this.saveButton.setToolTipText(Message.tooltipSaveGoal);
		this.saveButton.setMinimumSize(new Dimension(56, 28));
		this.saveButton.setMaximumSize(new Dimension(74, 28));
		buttonPanel.add(this.saveButton);

		JLabel lblSysVarCount = new JLabel(Message.lblAllVarCount);
		this.textSysVarCount = new JTextArea();
		this.textSysVarCount.setToolTipText(Message.tooltipTextAllVarCount);
		this.textSysVarCount.setEditable(false);

		JLabel lblLabelCount = new JLabel(Message.lblPropositionCount);
		this.textLiteralCount = new JTextArea();
		this.textLiteralCount.setToolTipText(Message.tooltipTextLiteralCount);
		this.textLiteralCount.setEditable(false);

		JLabel lblClauseCount = new JLabel(Message.lblClauseCount);
		this.textClauseCount = new JTextArea();
		this.textClauseCount.setToolTipText(Message.tooltipTextClauseCount);
		this.textClauseCount.setEditable(false);

		JPanel smallPanel = new JPanel();
		smallPanel.setLayout(new GridLayout(3, 2));
		smallPanel.add(lblSysVarCount);
		smallPanel.add(this.textSysVarCount);
		smallPanel.add(lblLabelCount);
		smallPanel.add(this.textLiteralCount);
		smallPanel.add(lblClauseCount);
		smallPanel.add(this.textClauseCount);

		ret.add(buttonPanel);
		ret.add(goalPanel);
		ret.add(smallPanel);

		return ret;
	}

	public StatInfo getModel() {
		return this.model;
	}

	private void initFrame() {
		setLocation(400, 400);
		setSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(200, 200));
		setLayout(new GridBagLayout());
		getContentPane().add(createMainPanel());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private String showLabels(String text) {
		StringBuffer ret = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new StringReader(
					text.replace(KRSSKeyword.close, KRSSKeyword.space
							+ KRSSKeyword.close)));
			String line = new String();
			while (line != null) {
				line = reader.readLine();
				if (line != null) {
					StringTokenizer stok = new StringTokenizer(line);
					while (stok.hasMoreTokens()) {
						String token = stok.nextToken();
						String label = getModel().getLabel(token);
						if (label.equals(token)) {
							ret.append(token);
						} else {
							ret.append(quotes);
							ret.append(label);
							ret.append(quotes);
						}
						if (stok.hasMoreTokens()) {
							ret.append(KRSSKeyword.space);
						}
					}
				}
				ret.append(KRSSKeyword.newLine);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return ret.toString();
	}

	public void update() {
		this.textGoal.setText(showLabels(getModel().getGoal().toString()));
		this.textClauseCount.setText(getModel().getClauseCount().toString());
		this.textSysVarCount.setText(getModel().getAllVarCount().toString());
		this.textLiteralCount.setText(getModel().getLiteralCount().toString());
	}

}
