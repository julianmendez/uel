package de.tudresden.inf.lat.uel.plugin.protege;

import java.awt.Container;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;

import de.tudresden.inf.lat.uel.plugin.main.UelStarter;

/**
 * This is used to start the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelProtegeStarter extends UelStarter implements OWLModelManagerListener {

	static final long serialVersionUID = -8760277761148468455L;

	private final OWLModelManager modelManager;

	/**
	 * Constructs a new UEL starter.
	 * 
	 * @param modelManager
	 *            OWL model manager
	 */
	public UelProtegeStarter(Container parent, OWLModelManager modelManager) {
		super(parent, modelManager.getOWLOntologyManager(), modelManager.getOWLEntityRenderer());
		this.modelManager = modelManager;
		modelManager.addListener(this);
		reset();
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
