package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;

/**
 * This is the panel that shows statistical information.
 * 
 * @author Julian Mendez
 */
class StatInfoView extends JDialog {

	private static final String colon = ": ";
	private static final String newLine = "\n";
	private static final String quotes = "\"";
	private static final long serialVersionUID = -4153981096827550491L;

	private final StatInfo model;
	private JButton saveButton = null;
	private JTextArea textGoal = null;
	private JTextArea textInfo = null;

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
		scrollPaneVars.setMinimumSize(new Dimension(640, 240));
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

		this.textInfo = new JTextArea();
		this.textInfo.setToolTipText(Message.tooltipTextInfo);
		JScrollPane scrollPaneInfo = new JScrollPane(this.textInfo);
		scrollPaneVars
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPaneInfo.setMinimumSize(new Dimension(640, 120));
		scrollPaneInfo.setPreferredSize(new Dimension(640, 240));

		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
		infoPanel.add(scrollPaneInfo);

		ret.add(buttonPanel);
		ret.add(goalPanel);
		ret.add(infoPanel);

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
		this.textGoal
				.setText(showLabels(getModel().getPluginGoal().toString()));
		StringBuffer info = new StringBuffer();
		for (Map.Entry<String, String> pair : getModel().getInfo()) {
			info.append(pair.getKey());
			info.append(colon);
			info.append(pair.getValue());
			info.append(newLine);
		}
		this.textInfo.setText(info.toString());
	}

}
