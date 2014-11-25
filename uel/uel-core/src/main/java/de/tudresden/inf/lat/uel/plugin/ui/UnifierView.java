package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import de.tudresden.inf.lat.uel.plugin.processor.UelModel;

/**
 * This is the panel that shows the unifiers.
 * 
 * @author Julian Mendez
 */
public class UnifierView extends JDialog {

	private static final long serialVersionUID = 7965907233259580732L;

	private JButton buttonFirst = new JButton(new ImageIcon(this.getClass()
			.getClassLoader().getResource(Message.iconRewind)));
	private JButton buttonLast = new JButton(new ImageIcon(this.getClass()
			.getClassLoader().getResource(Message.iconFastForward)));
	private JButton buttonNext = new JButton(new ImageIcon(this.getClass()
			.getClassLoader().getResource(Message.iconForward)));
	private JButton buttonPrevious = new JButton(new ImageIcon(this.getClass()
			.getClassLoader().getResource(Message.iconBack)));
	private JButton buttonSave = new JButton(new ImageIcon(this.getClass()
			.getClassLoader().getResource(Message.iconSave)));
	private JButton buttonShowStatInfo = new JButton(new ImageIcon(this
			.getClass().getClassLoader().getResource(Message.iconHistory)));
	private final UelModel model;
	private JTextArea textUnifier = new JTextArea();
	private JTextArea textUnifierId = new JTextArea();

	public UnifierView(UelModel processor) {
		super((Frame) null, "Unifier", true);

		if (processor == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.model = processor;
		initFrame();
	}

	public void addButtonFirstListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonFirst.addActionListener(listener);
		this.buttonFirst.setActionCommand(actionCommand);
	}

	public void addButtonLastListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonLast.addActionListener(listener);
		this.buttonLast.setActionCommand(actionCommand);
	}

	public void addButtonNextListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonNext.addActionListener(listener);
		this.buttonNext.setActionCommand(actionCommand);
	}

	public void addButtonPreviousListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonPrevious.addActionListener(listener);
		this.buttonPrevious.setActionCommand(actionCommand);
	}

	public void addButtonSaveListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonSave.addActionListener(listener);
		this.buttonSave.setActionCommand(actionCommand);
	}

	public void addButtonShowStatInfoListener(ActionListener listener,
			String actionCommand) {
		if (listener == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (actionCommand == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.buttonShowStatInfo.addActionListener(listener);
		this.buttonShowStatInfo.setActionCommand(actionCommand);
	}

	private JPanel createUnifierPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		this.textUnifier.setToolTipText(Message.tooltipUnifier);
		this.textUnifier.setWrapStyleWord(true);
		this.textUnifier.setLineWrap(true);

		JPanel smallPanel = new JPanel();

		this.buttonFirst.setToolTipText(Message.tooltipFirst);
		smallPanel.add(this.buttonFirst);

		this.buttonPrevious.setToolTipText(Message.tooltipPrevious);
		smallPanel.add(this.buttonPrevious);

		this.textUnifierId.setToolTipText(Message.tooltipUnifierId);
		this.textUnifierId.setEditable(false);
		smallPanel.add(this.textUnifierId);

		this.buttonNext.setToolTipText(Message.tooltipNext);
		smallPanel.add(this.buttonNext);

		this.buttonLast.setToolTipText(Message.tooltipLast);
		smallPanel.add(this.buttonLast);

		this.buttonSave.setToolTipText(Message.tooltipSave);
		smallPanel.add(this.buttonSave);

		this.buttonShowStatInfo.setToolTipText(Message.tooltipShowStatInfo);
		smallPanel.add(this.buttonShowStatInfo);

		JScrollPane scrollPane = new JScrollPane(this.textUnifier);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(640, 480));

		ret.add(smallPanel);
		ret.add(scrollPane);

		return ret;
	}

	public UelModel getModel() {
		return this.model;
	}

	public JTextArea getUnifier() {
		return this.textUnifier;
	}

	public JTextArea getUnifierId() {
		return this.textUnifierId;
	}

	private void initFrame() {
		setLocation(400, 400);
		setSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(200, 200));
		setLayout(new GridBagLayout());
		getContentPane().add(createUnifierPanel());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void setButtonFirstEnabled(boolean b) {
		this.buttonFirst.setEnabled(b);
	}

	public void setButtonLastEnabled(boolean b) {
		this.buttonLast.setEnabled(b);
	}

	public void setButtonNextEnabled(boolean b) {
		this.buttonNext.setEnabled(b);
	}

	public void setButtonPreviousEnabled(boolean b) {
		this.buttonPrevious.setEnabled(b);
	}

	public void setButtonSaveEnabled(boolean b) {
		this.buttonSave.setEnabled(b);
	}

	public void setButtonShowStatInfoEnabled(boolean b) {
		this.buttonShowStatInfo.setEnabled(b);
	}

	public void setUnifierButtons(boolean b) {
		setButtonFirstEnabled(b);
		setButtonPreviousEnabled(b);
		setButtonNextEnabled(b);
		setButtonLastEnabled(b);
		setButtonSaveEnabled(b);
		setButtonShowStatInfoEnabled(b);
	}

}
