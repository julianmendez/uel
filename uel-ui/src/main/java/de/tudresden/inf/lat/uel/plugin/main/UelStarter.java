package de.tudresden.inf.lat.uel.plugin.main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
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
public class UelStarter implements OWLOntologyChangeListener,
		OWLOntologyLoaderListener {

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
		this.panel = new UelController(new UelView(new UelModel()),
				this.ontologyManager);
		getOWLOntologyManager().addOntologyLoaderListener(this);
		getOWLOntologyManager().addOntologyChangeListener(this);
		reset();
	}

	private Map<OWLEntity, String> createShortFormMap() {
		Map<OWLEntity, String> shortFormMap = new HashMap<OWLEntity, String>();
		for (OWLOntology ontology : getOWLOntologyManager().getOntologies()) {
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

	public OWLOntologyManager getOWLOntologyManager() {
		return this.ontologyManager;
	}

	public UelController getPanel() {
		return this.panel;
	}

	private String getShortForm(OWLEntity entity, OWLOntology ontology) {
		String ret = entity.getIRI().getShortForm();
		Set<OWLAnnotation> annotations = ontology.getAnnotations();
		for (OWLAnnotation annotation : annotations) {
			OWLAnnotationProperty annotationProperty = annotation.getProperty();

			
			// for OWL API 3.5.1

			if (annotationProperty.isLabel()
					&& entity.getAnnotations(ontology).contains(
							annotationProperty)) {
				ret = annotation.getValue().toString();
			}

			
			// for OWL API 4.0.2

			// if (annotationProperty.isLabel()
			// && entity.getAnnotationPropertiesInSignature().contains(
			// annotationProperty)) {
			// ret = annotation.getValue().toString();
			// }

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
		if ((str.startsWith("\"") && str.endsWith("\""))
				|| (str.startsWith("'") && str.endsWith("'"))) {
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
