package de.tudresden.inf.lat.uel.plugin.main;

import java.awt.BorderLayout;
import java.awt.Container;

import org.semanticweb.owlapi.apibinding.OWLManager;

import de.tudresden.inf.lat.uel.core.processor.BasicOntologyProvider;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UelOntologyProvider;
import de.tudresden.inf.lat.uel.plugin.ui.UelController;
import de.tudresden.inf.lat.uel.plugin.ui.UelView;

/**
 * This is used to start the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelStarter {

	protected final UelController uelController;

	public UelStarter(Container parent) {
		this(parent, new BasicOntologyProvider(OWLManager.createOWLOntologyManager()));
	}

	protected UelStarter(Container parent, UelOntologyProvider provider) {
		if (parent == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (provider == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		UelView view = new UelView();
		parent.add(view, BorderLayout.CENTER);
		UelModel model = new UelModel(provider);
		uelController = new UelController(view, model);
		reset();
	}

	protected void reset() {
		uelController.reload();
	}
}
