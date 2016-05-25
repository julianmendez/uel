package de.tudresden.inf.lat.uel.plugin.main;

import java.awt.BorderLayout;
import java.awt.Container;

import org.semanticweb.owlapi.apibinding.OWLManager;

import de.tudresden.inf.lat.uel.core.processor.BasicOntologyProvider;
import de.tudresden.inf.lat.uel.core.processor.OntologyProvider;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UelOptions;
import de.tudresden.inf.lat.uel.plugin.ui.UelController;

/**
 * This is used to start the UEL system.
 * 
 * @author Julian Mendez
 * @author Stefan Borgwardt
 */
public class UelStarter {

	/**
	 * The UEL controller started by this instance.
	 */
	protected final UelController uelController;

	/**
	 * Constructs a new UEL starter using the given Container for displaying its
	 * view.
	 * 
	 * @param parent
	 *            the parent container
	 */
	public UelStarter(Container parent) {
		this(parent, new BasicOntologyProvider(OWLManager.createOWLOntologyManager()));
	}

	/**
	 * Constructs a new UEL starter using the given OntologyProvider and the
	 * Container for displaying its view.
	 * 
	 * @param parent
	 *            the parent container
	 * @param provider
	 *            the OntologyProvider for UEL
	 */
	protected UelStarter(Container parent, OntologyProvider provider) {
		if (parent == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (provider == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		uelController = new UelController(new UelModel(provider, new UelOptions()));
		parent.add(uelController.getView(), BorderLayout.CENTER);
		reset();
	}

	/**
	 * Refreshes UEL with the currently loaded ontologies.
	 */
	protected void reset() {
		uelController.reload();
	}
}
