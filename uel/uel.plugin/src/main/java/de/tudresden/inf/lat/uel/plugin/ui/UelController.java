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
import javax.swing.WindowConstants;

import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.OWLClass;
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

	private static final String action_acceptVar = "accept var";
	private static final String action_getVar = "get var candidate";
	private static final String action_rejectVar = "reject var";
	private static final String action_unify = "unify";
	private static final Logger logger = Logger.getLogger(UelController.class
			.getName());

	private List<OWLClass> classList = null;
	private boolean ontologyChanged = true;
	private VarSelectionView varFrame = null;
	private UelView view = null;

	public UelController(UelView panel) {
		if (panel == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.view = panel;
		init();
	}

	/**
	 * Action handler.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String cmd = e.getActionCommand();
		if (cmd.equals(action_getVar)) {
			getView().getUnifyButton().setEnabled(false);
			Set<OWLClass> classSet = new HashSet<OWLClass>();
			classSet.add(getSelectedClass00());
			classSet.add(getSelectedClass01());
			getModel().recalculateCandidates(classSet);
			this.varFrame = initVarFrame(getModel().getCandidates());
			getVarWindow().setVisible(true);

		} else if (cmd.equals(action_acceptVar)) {
			getVarWindow().setVisible(false);
			getModel().clearCandidates();
			getModel().addAll(getVarWindow().getSelectedValues());
			getVarWindow().dispose();
			getView().getUnifyButton().setEnabled(true);

		} else if (cmd.equals(action_rejectVar)) {
			getVarWindow().setVisible(false);
			getVarWindow().dispose();
			getView().getUnifyButton().setEnabled(false);

		} else if (cmd.equals(action_unify)) {
			getVarWindow().setVisible(false);
			getView().getUnifyButton().setEnabled(false);

			Set<OWLClass> classSet = new HashSet<OWLClass>();
			classSet.add(getSelectedClass00());
			classSet.add(getSelectedClass01());
			String res = getModel().unify(classSet);

			logger.info("Unifying " + getSelectedClass00().toStringID()
					+ " and " + getSelectedClass01().toStringID()
					+ " using variables " + getModel().getCandidates()
					+ "\nResult: " + res);

		} else {
			throw new IllegalStateException();
		}
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

	public VarSelectionView getVarWindow() {
		return this.varFrame;
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
		getView().getUnifyButton().setEnabled(false);
		getView().addGetVarButtonListener(this, action_getVar);
		getView().addUnifyButtonListener(this, action_unify);
		refresh();
		getView().getGetVarButton().setEnabled(true);
	}

	private VarSelectionView initVarFrame(Set<String> set) {
		VarSelectionView ret = new VarSelectionView(set);
		ret.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		ret.addAcceptVarButtonListener(this, action_acceptVar);
		ret.addRejectVarButtonListener(this, action_rejectVar);
		ret.setLocation(400, 400);
		ret.setSize(200, 400);
		ret.setVisible(true);
		return ret;
	}

	protected void keepUpdated() {
		if (this.ontologyChanged) {
			this.ontologyChanged = false;
		}
	}

	public void ontologiesChanged(List<? extends OWLOntologyChange> change)
			throws OWLException {
		if (change == null) {
			throw new IllegalArgumentException("Null argument.");
		}

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
