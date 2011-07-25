package de.tudresden.inf.lat.uel.plugin.main;

import java.awt.BorderLayout;

import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.OWLClass;

import de.tudresden.inf.lat.uel.plugin.processor.UelProcessor;
import de.tudresden.inf.lat.uel.plugin.ui.UelController;
import de.tudresden.inf.lat.uel.plugin.ui.UelView;

/**
 * Protege view component for UEL.
 * 
 * @author Julian Mendez
 */
public class Main extends AbstractOWLClassViewComponent {

	private static final long serialVersionUID = -5363687740449453246L;

	private UelController uelController = null;

	@Override
	public void disposeView() {
		this.uelController.removeListeners();
	}

	/**
	 * Initializes the data and GUI. This method is called when the view is
	 * initialized.
	 */
	@Override
	public void initialiseClassView() throws Exception {
		this.setLayout(new BorderLayout());
		this.uelController = new UelController(new UelView(new UelProcessor()),
				getOWLWorkspace());
		add(this.uelController.getView(), BorderLayout.CENTER);
	}

	@Override
	protected OWLClass updateView(OWLClass selectedClass) {
		return selectedClass;
	}

}
