package de.tudresden.inf.lat.uel.plugin.protege;

import java.awt.Container;
import java.io.File;
import java.util.List;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;

import de.tudresden.inf.lat.uel.core.processor.BasicOntologyProvider;
import de.tudresden.inf.lat.uel.plugin.main.UelStarter;

/**
 * This is used to start UEL as a plug-in in Protégé.
 * 
 * @author Julian Mendez
 */
public class UelProtegeStarter extends UelStarter implements OWLModelManagerListener, OWLOntologyChangeListener {

	private static final class ProtegeOntologyProvider extends BasicOntologyProvider {

		private final OWLModelManager modelManager;

		private ProtegeOntologyProvider(OWLModelManager modelManager) {
			super(modelManager.getOWLOntologyManager());
			this.modelManager = modelManager;
		}

		@Override
		public OWLOntology createOntology() {
			try {
				OWLOntology activeOntology = modelManager.getActiveOntology();
				OWLOntology newOntology = modelManager.createNewOntology(new OWLOntologyID(), null);
				modelManager.setActiveOntology(activeOntology);
				return newOntology;
			} catch (OWLOntologyCreationException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public OWLOntology loadOntology(File file) {
			modelManager.setActiveOntology(super.loadOntology(file));
			return modelManager.getActiveOntology();
		}
	}

	/**
	 * Serial version UID
	 */
	static final long serialVersionUID = -8760277761148468455L;

	private final OWLModelManager modelManager;

	/**
	 * Start an instance of UEL for the Protégé plug-in and listen for changes
	 * from the modelManager.
	 * 
	 * @param parent
	 *            the parent Swing component
	 * @param modelManager
	 *            the OWL model manager from Protégé
	 */
	UelProtegeStarter(Container parent, OWLModelManager modelManager) {
		super(parent, new ProtegeOntologyProvider(modelManager));
		this.modelManager = modelManager;
		modelManager.addListener(this);
		modelManager.addOntologyChangeListener(this);
	}

	/**
	 * Remove this object as a listener for changes from the OWL model manager.
	 */
	public void removeListeners() {
		modelManager.removeListener(this);
		modelManager.removeOntologyChangeListener(this);
	}

	@Override
	public void handleChange(OWLModelManagerChangeEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("Null argument");
		}

		EventType type = event.getType();
		if ((type == EventType.ONTOLOGY_CREATED) || (type == EventType.ONTOLOGY_LOADED)
				|| (type == EventType.ONTOLOGY_RELOADED) || (type == EventType.ENTITY_RENDERER_CHANGED)
				|| (type == EventType.ENTITY_RENDERING_CHANGED)) {
			reset();
		}
	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
		if (changes == null) {
			throw new IllegalArgumentException("Null argument");
		}

		reset();
	}

}
