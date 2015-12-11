package de.tudresden.inf.lat.uel.plugin.main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
	private final UelController panel;

	/**
	 * Constructs a new UEL starter.
	 * 
	 * @param manager
	 *            OWL ontology manager
	 */
	public UelStarter(OWLOntologyManager manager) {
		if (manager == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.ontologyManager = manager;
		this.panel = new UelController(new UelView(), new UelModel(), this.ontologyManager);
		ontologyManager.addOntologyLoaderListener(this);
		ontologyManager.addOntologyChangeListener(this);
		reset();
	}

	private Map<OWLEntity, String> createShortFormMap() {
		Map<OWLEntity, String> shortFormMap = new HashMap<OWLEntity, String>();
		for (OWLOntology ontology : ontologyManager.getOntologies()) {
			Set<OWLEntity> entities = new HashSet<OWLEntity>();
			entities.addAll(ontology.getClassesInSignature());
			entities.addAll(ontology.getObjectPropertiesInSignature());

			for (OWLEntity entity : entities) {
				String shortForm = getShortForm(entity, ontology);
				shortFormMap.put(entity, removeQuotes(shortForm));
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

	public UelView getView() {
		return panel.getView();
	}

	public void removeListeners() {
		ontologyManager.removeOntologyLoaderListener(this);
		ontologyManager.removeOntologyChangeListener(this);
	}

	protected String getShortForm(OWLEntity entity, OWLOntology ontology) {
		String ret = entity.getIRI().getShortForm();
		for (OWLAnnotation annotation : entity.getAnnotations(ontology)) {
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

	private String removeQuotes(String str) {
		String ret = str;
		if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
			ret = str.substring(1, str.length() - 1);
		}
		return ret;
	}

	public void reset() {
		this.panel.setShortFormMap(createShortFormMap());
		this.panel.reset();
	}

	@Override
	public void startedLoadingOntology(LoadingStartedEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("Null argument.");
		}
	}

}
