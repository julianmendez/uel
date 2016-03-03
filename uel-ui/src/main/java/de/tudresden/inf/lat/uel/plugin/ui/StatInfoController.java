package de.tudresden.inf.lat.uel.plugin.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

import de.tudresden.inf.lat.uel.core.processor.UelModel;

/**
 * This is the controller of the panel that shows statistical information.
 * 
 * @author Julian Mendez
 */
class StatInfoController {

	private static final String colon = ": ";
	private static final String newLine = "\n";

	private final UelModel model;
	private final StatInfoView view;

	public StatInfoController(StatInfoView view, UelModel model) {
		this.view = view;
		this.model = model;
		init();
	}

	private void executeSaveGoal() {
		File file = UelUI.showSaveFileDialog(view);
		if (file == null) {
			return;
		}

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(model.printGoal());
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void init() {
		view.addSaveListener(e -> executeSaveGoal());
	}

	public void open() {
		updateView();
		view.open();
	}

	public void updateView() {
		view.setGoalText(model.printGoal());
		StringBuffer info = new StringBuffer();
		for (Entry<String, String> pair : model.getUnificationAlgorithm().getInfo()) {
			info.append(pair.getKey());
			info.append(colon);
			info.append(pair.getValue());
			info.append(newLine);
		}
		view.setInfoText(info.toString());
	}

}
