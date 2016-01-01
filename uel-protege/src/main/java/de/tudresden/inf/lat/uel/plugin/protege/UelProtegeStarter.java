package de.tudresden.inf.lat.uel.plugin.protege;

import java.awt.Container;
import java.io.File;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;

import de.tudresden.inf.lat.uel.core.processor.OntologyProvider;
import de.tudresden.inf.lat.uel.plugin.main.UelStarter;

/**
 * This is used to start the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelProtegeStarter extends UelStarter implements OWLModelManagerListener {

	private static final class ProtegeOntologyProvider implements OntologyProvider {

		private final OWLModelManager modelManager;

		private ProtegeOntologyProvider(OWLModelManager modelManager) {
			this.modelManager = modelManager;
		}

		@Override
		public OWLOntology createOntology() {
			try {
				return modelManager.createNewOntology(new OWLOntologyID(), null);
			} catch (OWLOntologyCreationException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public void loadOntology(File file) {
			try {
				OWLOntology ontology = modelManager.getOWLOntologyManager().loadOntologyFromOntologyDocument(file);
				modelManager.setActiveOntology(ontology);
			} catch (OWLOntologyCreationException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public Set<OWLOntology> getOntologies() {
			return modelManager.getOntologies();
		}

		@Override
		public boolean providesShortForms() {
			return true;
		}

		@Override
		public String getShortForm(OWLEntity entity) {
			return modelManager.getOWLEntityRenderer().getShortForm(entity);
		}
	}

	static final long serialVersionUID = -8760277761148468455L;

	private final OWLModelManager modelManager;

	/**
	 * Constructs a new UEL starter.
	 * 
	 * @param modelManager
	 *            OWL model manager
	 */
	public UelProtegeStarter(Container parent, OWLModelManager modelManager) {
		super(parent, new ProtegeOntologyProvider(modelManager));
		this.modelManager = modelManager;
		modelManager.addListener(this);
	}

	public void removeListeners() {
		modelManager.removeListener(this);
	}

	@Override
	public void handleChange(OWLModelManagerChangeEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("Null argument");
		}

		EventType type = event.getType();
		if ((type == EventType.ONTOLOGY_CREATED) || (type == EventType.ONTOLOGY_LOADED)
				|| (type == EventType.ONTOLOGY_RELOADED)) {
			reset();
		}
	}

}
