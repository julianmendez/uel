package de.tudresden.inf.lat.uel.plugin.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.ShortFormProvider;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.plugin.ui.UelController;
import de.tudresden.inf.lat.uel.plugin.ui.UelView;

/**
 * This is used to start the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelStarter implements OWLOntologyChangeListener, OWLOntologyLoaderListener {

	private final OWLOntologyManager ontologyManager;
	private final UelController uelController;

	public UelStarter(Container parent, OWLOntologyManager manager, ShortFormProvider shortFormProvider) {
		if (manager == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (parent == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.ontologyManager = manager;
		UelView view = new UelView();
		parent.add(view, BorderLayout.CENTER);
		UelModel model = new UelModel(ontologyManager, shortFormProvider);
		this.uelController = new UelController(view, model);
		ontologyManager.addOntologyLoaderListener(this);
		ontologyManager.addOntologyChangeListener(this);
		reset();
	}

	@Override
	public void finishedLoadingOntology(LoadingFinishedEvent event) {
		reset();
	}

	public void removeListeners() {
		ontologyManager.removeOntologyLoaderListener(this);
		ontologyManager.removeOntologyChangeListener(this);
	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> change) {
		reset();
	}

	public void reset() {
		uelController.reload();
	}

	@Override
	public void startedLoadingOntology(LoadingStartedEvent event) {
	}

}
