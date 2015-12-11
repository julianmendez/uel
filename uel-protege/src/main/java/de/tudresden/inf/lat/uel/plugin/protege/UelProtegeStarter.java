package de.tudresden.inf.lat.uel.plugin.protege;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.plugin.main.UelStarter;

/**
 * This is used to start the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelProtegeStarter extends UelStarter implements OWLModelManagerListener {

	static final long serialVersionUID = -8760277761148468455L;

	private final OWLModelManager modelManager;
	private OWLModelManagerEntityRenderer renderer = null;

	/**
	 * Constructs a new UEL starter.
	 * 
	 * @param modelManager
	 *            OWL model manager
	 */
	public UelProtegeStarter(OWLModelManager modelManager) {
		super(modelManager.getOWLOntologyManager());
		this.modelManager = modelManager;
		this.renderer = modelManager.getOWLEntityRenderer();
		modelManager.addListener(this);
		reset();
	}

	@Override
	protected String getShortForm(OWLEntity entity, OWLOntology ontology) {
		return (renderer != null) ? renderer.getShortForm(entity) : super.getShortForm(entity,ontology);
	}

	@Override
	public void removeListeners() {
		modelManager.removeListener(this);
		super.removeListeners();
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
