package de.tudresden.inf.lat.uel.plugin.protege;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
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
public class UelProtegeStarter implements OWLOntologyChangeListener,
		OWLOntologyLoaderListener, OWLModelManagerListener {

	private static final long serialVersionUID = -8760277761148468455L;

	private final OWLModelManager modelManager;
	private final OWLOntologyManager ontologyManager;
	private final UelController panel;
	private OWLModelManagerEntityRenderer renderer = null;

	/**
	 * Constructs a new UEL starter.
	 * 
	 * @param modelManager
	 *            OWL model manager
	 */
	public UelProtegeStarter(OWLModelManager modelManager) {
		if (modelManager == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.modelManager = modelManager;
		this.ontologyManager = modelManager.getOWLOntologyManager();
		this.panel = new UelController(new UelView(new UelModel()),
				this.ontologyManager);
		this.renderer = modelManager.getOWLEntityRenderer();
		modelManager.addListener(this);
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
				String shortForm = this.renderer != null ? this.renderer
						.getShortForm(entity) : getShortForm(entity, ontology);
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
			if (annotationProperty.isLabel()
					&& entity.getAnnotationPropertiesInSignature().contains(
							annotationProperty)) {
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
		if (modelManager != null) {
			this.modelManager.removeListener(this);
		}
		getOWLOntologyManager().removeOntologyLoaderListener(this);
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
	}

	@Override
	public void startedLoadingOntology(LoadingStartedEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("Null argument.");
		}
	}

	@Override
	public void handleChange(OWLModelManagerChangeEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("Null argument");
		}

		EventType type = event.getType();
		if ((type == EventType.ONTOLOGY_CREATED)
				|| (type == EventType.ONTOLOGY_LOADED)
				|| (type == EventType.ONTOLOGY_RELOADED)) {
			reset();
		}
	}

}
