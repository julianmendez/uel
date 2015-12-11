package de.tudresden.inf.lat.uel.plugin.protege;

import java.awt.BorderLayout;

import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * This is the Protege view component of UEL.
 * 
 * @author Julian Mendez
 */
public class Main extends AbstractOWLClassViewComponent {

	private static final long serialVersionUID = -5363687740449453246L;

	private UelProtegeStarter uelStarter = null;

	@Override
	public void disposeView() {
		this.uelStarter.removeListeners();
	}

	/**
	 * Initializes the data and GUI. This method is called when the view is
	 * initialized.
	 */
	@Override
	public void initialiseClassView() {
		this.uelStarter = new UelProtegeStarter(getOWLModelManager());
		this.setLayout(new BorderLayout());
		add(this.uelStarter.getView(), BorderLayout.CENTER);
	}

	@Override
	protected OWLClass updateView(OWLClass selectedClass) {
		return selectedClass;
	}

}
