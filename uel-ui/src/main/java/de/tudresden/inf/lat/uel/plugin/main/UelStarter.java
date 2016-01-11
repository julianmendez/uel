package de.tudresden.inf.lat.uel.plugin.main;

import java.awt.BorderLayout;
import java.awt.Container;

import org.semanticweb.owlapi.apibinding.OWLManager;

import de.tudresden.inf.lat.uel.core.processor.BasicOntologyProvider;
import de.tudresden.inf.lat.uel.core.processor.OntologyProvider;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.plugin.ui.UelController;

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

	protected UelStarter(Container parent, OntologyProvider provider) {
		if (parent == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (provider == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		uelController = new UelController(new UelModel(provider));
		parent.add(uelController.getView(), BorderLayout.CENTER);
		reset();
	}

	protected void reset() {
		uelController.reload();
	}
}
