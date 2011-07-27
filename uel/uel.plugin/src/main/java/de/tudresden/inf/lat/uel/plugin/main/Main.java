package de.tudresden.inf.lat.uel.plugin.main;

import java.awt.BorderLayout;

import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * Protege view component for UEL.
 * 
 * @author Julian Mendez
 */
public class Main extends AbstractOWLClassViewComponent {

	private static final long serialVersionUID = -5363687740449453246L;

	private UelStarter uelStarter = null;

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
		this.uelStarter = new UelStarter(getOWLModelManager());
		this.setLayout(new BorderLayout());
		add(this.uelStarter.getPanel().getView(), BorderLayout.CENTER);
	}

	@Override
	protected OWLClass updateView(OWLClass selectedClass) {
		return selectedClass;
	}

}
