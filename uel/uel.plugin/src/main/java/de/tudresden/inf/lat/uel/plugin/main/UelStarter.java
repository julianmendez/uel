package de.tudresden.inf.lat.uel.plugin.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
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

	private final OWLOntologyManager ontologyManager;
	private final UelController panel;
	private OWLModelManagerEntityRenderer renderer = null;

	public UelStarter(OWLModelManager modelManager) {
		if (modelManager == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.ontologyManager = modelManager.getOWLOntologyManager();
		this.panel = new UelController(new UelView(new UelProcessor()),
				this.ontologyManager);
		this.renderer = modelManager.getOWLEntityRenderer();
		getOWLOntologyManager().addOntologyLoaderListener(this);
		getOWLOntologyManager().addOntologyChangeListener(this);
		reset();
	}

	public UelStarter(OWLOntologyManager manager) {
		if (manager == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.ontologyManager = manager;
		this.panel = new UelController(new UelView(new UelProcessor()),
				this.ontologyManager);
		getOWLOntologyManager().addOntologyLoaderListener(this);
		getOWLOntologyManager().addOntologyChangeListener(this);
		reset();
	}

	private Map<OWLClass, String> createShortFormMap() {
		Map<OWLClass, String> shortFormMap = new HashMap<OWLClass, String>();
		for (OWLOntology ontology : getOWLOntologyManager().getOntologies()) {
			for (OWLClass cls : ontology.getClassesInSignature()) {
				String shortForm = this.renderer != null ? this.renderer
						.getShortForm(cls) : getShortForm(cls, ontology);
				shortFormMap.put(cls, removeQuotes(shortForm));
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

	public OWLOntologyManager getOWLOntologyManager() {
		return this.ontologyManager;
	}

	public UelController getPanel() {
		return this.panel;
	}

	private String getShortForm(OWLClass owlClass, OWLOntology ontology) {
		String ret = owlClass.getIRI().getFragment();
		for (OWLAnnotation annotation : owlClass.getAnnotations(ontology)) {
			if (annotation.getProperty().isLabel()) {
				ret = annotation.getValue().toString();
			}
		}
		return ret;
	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> change) {
		if (change == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		reset();
	}

	public void removeListeners() {
		getOWLOntologyManager().removeOntologyChangeListener(this);
	}

	private String removeQuotes(String str) {
		String ret = str;
		if ((str.startsWith("\"") && str.endsWith("\""))
				|| (str.startsWith("'") && str.endsWith("'"))) {
			ret = str.substring(1, str.length() - 1);
		}
		return ret;
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
