package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;

import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

import de.tudresden.inf.lat.uel.plugin.processor.UelProcessor;

/**
 * This class is a controller for UEL.
 * 
 * @author Julian Mendez
 */
public class UelController implements ActionListener, OWLOntologyChangeListener {

	private static final String computeUelAction = "compute uel";
	private static final Logger logger = Logger.getLogger(UelController.class
			.getName());
	protected static final String nameDescriptionAction = "name description";

	private List<OWLClass> classList = null;
	private boolean ontologyChanged = true;
	private UelView view = null;

	public UelController(UelView panel) {
		this.view = panel;
		init();
	}

	/**
	 * Action handler.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals(computeUelAction)) {
			compute();
		} else {
			logger.warning("Not available yet");
		}
	}

	public void compute() {
		keepUpdated();
		Set<OWLClass> classSet = new HashSet<OWLClass>();
		classSet.add(getSelectedClass00());
		classSet.add(getSelectedClass01());
		OWLClassExpression result = getModel().compute(classSet);

		// FIXME this method does not show the result
	}

	public DefaultListModel getListModel() {
		return getView().getListModel();
	}

	public UelProcessor getModel() {
		return getView().getModel();
	}

	public OWLWorkspace getOWLWorkspace() {
		return getModel().getOWLWorkspace();
	}

	public OWLClass getSelectedClass00() {
		return this.classList.get(getView().getSelectedIndex00());
	}

	public OWLClass getSelectedClass01() {
		return this.classList.get(getView().getSelectedIndex01());
	}

	public UelView getView() {
		return this.view;
	}

	/**
	 * Initializes the data and GUI. This method is called when the view is
	 * initialized.
	 */
	public void init() {
		getOWLWorkspace().getOWLModelManager().addOntologyChangeListener(this);

		getView().addComputeButtonListener(this, computeUelAction);
		refresh();

	}

	protected void keepUpdated() {
		if (this.ontologyChanged) {
			this.ontologyChanged = false;
		}
	}

	public void ontologiesChanged(List<? extends OWLOntologyChange> arg0)
			throws OWLException {
		logger.info("The ontology has changed.");
		this.ontologyChanged = true;
	}

	public void refresh() {
		OWLClass nothing = getModel().getOWLWorkspace().getOWLModelManager()
				.getOWLDataFactory().getOWLNothing();
		OWLClass thing = getModel().getOWLWorkspace().getOWLModelManager()
				.getOWLDataFactory().getOWLThing();
		Set<OWLClass> set = new TreeSet<OWLClass>();
		set.addAll(getModel().getOWLWorkspace().getOWLModelManager()
				.getActiveOntology().getClassesInSignature());
		set.remove(nothing);
		set.remove(thing);
		this.classList = new ArrayList<OWLClass>();
		this.classList.add(nothing);
		this.classList.add(thing);
		this.classList.addAll(set);
		List<String> classNameSet = new ArrayList<String>();
		for (OWLClass cls : this.classList) {
			classNameSet.add(cls.getIRI().toURI().getFragment());
		}
		getView().refresh(classNameSet);
	}

	public void removeListeners() {
		getOWLWorkspace().getOWLModelManager().removeOntologyChangeListener(
				this);
	}

	public void setSelectedClass(OWLClass selectedClass) {
	}

}
