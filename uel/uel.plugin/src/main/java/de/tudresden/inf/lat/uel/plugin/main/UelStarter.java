package de.tudresden.inf.lat.uel.plugin.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tudresden.inf.lat.uel.plugin.processor.UelProcessor;
import de.tudresden.inf.lat.uel.plugin.ui.UelController;
import de.tudresden.inf.lat.uel.plugin.ui.UelView;

/**
 * 
 * @author Julian Mendez
 */
public class UelStarter implements OWLOntologyChangeListener,
		OWLOntologyLoaderListener {

	private OWLModelManager modelManager = null;
	private UelController panel = null;

	public UelStarter(OWLModelManager manager) {
		if (manager == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.modelManager = manager;
		this.panel = new UelController(new UelView(new UelProcessor()),
				this.modelManager.getOWLOntologyManager());
		getOWLOntologyManager().addOntologyLoaderListener(this);
		getOWLOntologyManager().addOntologyChangeListener(this);
		reset();
	}

	private Map<OWLClass, String> createShortFormMap() {
		Map<OWLClass, String> shortFormMap = new HashMap<OWLClass, String>();
		OWLModelManagerEntityRenderer renderer = getOWLModelManager()
				.getOWLEntityRenderer();
		shortFormMap.put(getOWLModelManager().getOWLDataFactory()
				.getOWLNothing(), renderer.getShortForm(getOWLModelManager()
				.getOWLDataFactory().getOWLNothing()));
		shortFormMap.put(
				getOWLModelManager().getOWLDataFactory().getOWLThing(),
				renderer.getShortForm(getOWLModelManager().getOWLDataFactory()
						.getOWLThing()));
		for (OWLOntology ontology : getOWLModelManager().getOntologies()) {
			for (OWLClass cls : ontology.getClassesInSignature()) {
				shortFormMap.put(cls, renderer.getShortForm(cls));
			}
		}
		return shortFormMap;
	}

	@Override
	public void finishedLoadingOntology(LoadingFinishedEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		reset();
	}

	public OWLModelManager getOWLModelManager() {
		return this.modelManager;
	}

	public OWLOntologyManager getOWLOntologyManager() {
		return getOWLModelManager().getOWLOntologyManager();
	}

	public UelController getPanel() {
		return this.panel;
	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> change)
			throws OWLException {
		if (change == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		reset();
	}

	public void removeListeners() {
		getOWLOntologyManager().removeOntologyChangeListener(this);
	}

	public void reset() {
		this.panel.setShortFormMap(createShortFormMap());
		this.panel.reset();
		this.panel.reloadOntologies();
	}

	@Override
	public void startedLoadingOntology(LoadingStartedEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("Null argument.");
		}
	}

}
