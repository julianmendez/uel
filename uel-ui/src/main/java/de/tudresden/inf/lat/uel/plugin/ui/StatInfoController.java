package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import de.tudresden.inf.lat.uel.core.processor.UelModel;

/**
 * This is the controller of the panel that shows statistical information.
 * 
 * @author Julian Mendez
 */
class StatInfoController implements ActionListener {

	private static final String colon = ": ";
	private static final String newLine = "\n";
	private static final String actionSaveButton = "save goal";

	private StatInfoView view;
	private UelModel model;

	public StatInfoController(StatInfoView view, UelModel model) {
		this.view = view;
		this.model = model;
		init();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String cmd = e.getActionCommand();
		if (cmd.equals(actionSaveButton)) {
			executeSaveGoal();
		} else {
			throw new IllegalStateException();
		}
	}

	private void executeSaveGoal() {
		File file = UelController.showSaveFileDialog(view);
		if (file == null) {
			return;
		}

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(model.printPluginGoal());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void init() {
		view.addSaveButtonListener(this, actionSaveButton);
	}

	public void open() {
		updateView();
		view.setVisible(true);
	}

	public void updateView() {
		view.setGoalText(model.printPluginGoal());
		StringBuffer info = new StringBuffer();
		for (Map.Entry<String, String> pair : model.getUelProcessor().getInfo()) {
			info.append(pair.getKey());
			info.append(colon);
			info.append(pair.getValue());
			info.append(newLine);
		}
		view.setInfoText(info.toString());
	}

}
