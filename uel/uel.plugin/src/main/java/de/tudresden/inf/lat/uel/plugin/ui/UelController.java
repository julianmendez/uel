package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;

import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener;

import de.tudresden.inf.lat.uel.plugin.processor.UelProcessor;

/**
 * This class is a controller for UEL.
 * 
 * @author Julian Mendez
 */
public class UelController implements ActionListener,
		OWLOntologyChangeListener, OWLOntologyLoaderListener {

	private static final String actionAcceptVar = "accept var";
	private static final String actionGetConceptNames = "get classes";
	private static final String actionNext = "next";
	private static final String actionPrevious = "previous";
	private static final String actionRejectVar = "reject var";
	private static final String actionReset = "reset";
	private static final String actionSave = "save";
	private static final String actionSelectVariables = "get var candidate";
	private static final Logger logger = Logger.getLogger(UelController.class
			.getName());
	private static final OWLAnnotationProperty RDFS_LABEL = OWLManager
			.getOWLDataFactory().getRDFSLabel();

	private List<String> classList00 = null;
	private List<String> classList01 = null;
	private Map<String, OWLClass> mapIdClass = new HashMap<String, OWLClass>();
	private Map<String, String> mapIdLabel = new HashMap<String, String>();
	private OWLOntology ontology00 = null;
	private OWLOntology ontology01 = null;
	private boolean ontologyChanged = true;
	private List<String> ontologyList = new ArrayList<String>();
	private Map<String, OWLOntology> ontologyMap = new HashMap<String, OWLOntology>();
	private OWLWorkspace owlWorkspace = null;
	private int unifierIndex = -1;
	private VarSelectionController varWindow = null;
	private UelView view = null;

	public UelController(UelView panel, OWLWorkspace workspace) {
		if (panel == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (workspace == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.view = panel;
		this.owlWorkspace = workspace;
		init();
	}

	/**
	 * Action handler.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String cmd = e.getActionCommand();
		if (cmd.equals(actionSelectVariables)) {
			executeActionSelectVariables();
		} else if (cmd.equals(actionReset)) {
			executeActionReset();
		} else if (cmd.equals(actionGetConceptNames)) {
			executeActionGetConceptNames();
		} else if (cmd.equals(actionAcceptVar)) {
			executeActionAcceptVar();
		} else if (cmd.equals(actionRejectVar)) {
			executeActionRejectVar();
		} else if (cmd.equals(actionPrevious)) {
			executeActionPrevious();
		} else if (cmd.equals(actionNext)) {
			executeActionNext();
		} else if (cmd.equals(actionSave)) {
			executeActionSave();
		} else {
			throw new IllegalStateException();
		}
	}

	private void executeActionAcceptVar() {
		getModel().clearCandidates();
		getModel().addAll(this.varWindow.getView().getModel().getVariables());
		this.varWindow.close();
		getView().setButtonPreviousEnabled(false);
		getView().setButtonNextEnabled(true);
		getView().setButtonSaveEnabled(false);
	}

	private void executeActionGetConceptNames() {
		getView().setButtonPreviousEnabled(false);
		getView().setButtonNextEnabled(false);
		getView().setButtonSaveEnabled(false);

		this.ontology00 = this.ontologyMap.get(this.ontologyList.get(getView()
				.getSelectedOntologyName00()));
		this.ontology01 = this.ontologyMap.get(this.ontologyList.get(getView()
				.getSelectedOntologyName01()));

		keepOntologyUpdated();

		processMapIdLabel(this.ontology00);
		processMapIdClass(this.ontology00);
		processMapIdLabel(this.ontology01);
		processMapIdClass(this.ontology01);

		reloadClassNames();

		getView().setButtonSelectVariablesEnabled(true);
		getView().setComboBoxClassName00Enabled(true);
		getView().setComboBoxClassName01Enabled(true);
	}

	private void executeActionNext() {
		getView().setButtonPreviousEnabled(true);
		getView().setButtonNextEnabled(true);
		getView().setButtonSaveEnabled(true);

		if (getModel().getUnifierList().isEmpty()) {
			Set<String> classSet = new HashSet<String>();
			classSet.add(this.classList00.get(getView()
					.getSelectedClassName00()));
			classSet.add(this.classList01.get(getView()
					.getSelectedClassName01()));
			getModel().configure(classSet);
		}
		this.unifierIndex++;
		if (this.unifierIndex >= getModel().getUnifierList().size()) {
			boolean unifiable = getModel().computeNextUnifier();
			if (!unifiable && this.unifierIndex > 0) {
				this.unifierIndex--;
			}
		}
		updateUnifier();
	}

	private void executeActionPrevious() {
		getView().setButtonPreviousEnabled(true);
		getView().setButtonNextEnabled(true);
		getView().setButtonSaveEnabled(true);

		this.unifierIndex--;
		if (this.unifierIndex < 0) {
			this.unifierIndex = 0;
		}
		updateUnifier();
	}

	private void executeActionRejectVar() {
		this.varWindow.close();
		getView().setButtonPreviousEnabled(false);
		getView().setButtonNextEnabled(false);
		getView().setButtonSaveEnabled(false);
	}

	private void executeActionReset() {
		getView().setButtonPreviousEnabled(false);
		getView().setButtonNextEnabled(false);
		getView().setButtonSaveEnabled(false);
		getView().setComboBoxClassName00Enabled(false);
		getView().setComboBoxClassName01Enabled(false);
		reloadOntologyNames();
		getView().getUnifier().setText("");
		getView().setButtonSelectVariablesEnabled(true);
	}

	private void executeActionSave() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showSaveDialog(getView());
		File file = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		}
		if (file != null) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				if (getModel().getUnifierList().size() > 0) {
					writer.write(getModel().getUnifierList().get(
							this.unifierIndex));
				}
				writer.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void executeActionSelectVariables() {
		getView().setButtonPreviousEnabled(false);
		getView().setButtonNextEnabled(false);
		getView().setButtonSaveEnabled(false);
		keepOntologyUpdated();
		Set<String> classSet = new HashSet<String>();
		classSet.add(this.classList00.get(getView().getSelectedClassName00()));
		classSet.add(this.classList01.get(getView().getSelectedClassName01()));
		getModel().recalculateCandidates(classSet);
		this.varWindow = initVarWindow();
		this.varWindow.open();
		this.unifierIndex = -1;
	}

	@Override
	public void finishedLoadingOntology(LoadingFinishedEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		executeActionReset();
	}

	private List<String> getClassNames(OWLOntology ontology) {
		OWLClass nothing = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLNothing();
		OWLClass thing = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLThing();

		Set<OWLClass> set = new TreeSet<OWLClass>();
		set.addAll(ontology.getClassesInSignature());
		set.remove(nothing);
		set.remove(thing);

		List<String> ret = new ArrayList<String>();
		ret.add(nothing.toStringID());
		ret.add(thing.toStringID());
		for (OWLClass cls : set) {
			ret.add(getId(cls));
		}
		return ret;
	}

	private String getId(OWLClass cls) {
		return cls.getIRI().toURI().toString();
	}

	private OWLAnnotationValue getLabel(OWLClass cls, OWLOntology ontology) {
		OWLAnnotationValue ret = null;
		Set<OWLAnnotation> annotationSet = cls.getAnnotations(ontology);
		for (OWLAnnotation annotation : annotationSet) {
			if (annotation.getProperty().equals(RDFS_LABEL)) {
				ret = annotation.getValue();
			}
		}
		return ret;
	}

	private String getLabel(String id) {
		return this.mapIdLabel.get(id);
	}

	public DefaultListModel getListModel() {
		return getView().getListModel();
	}

	public UelProcessor getModel() {
		return getView().getModel();
	}

	public OWLWorkspace getOWLWorkspace() {
		return this.owlWorkspace;
	}

	public UelView getView() {
		return this.view;
	}

	/**
	 * Initializes the data and GUI. This method is called when the view is
	 * initialized.
	 */
	private void init() {
		getOWLWorkspace().getOWLModelManager().getOWLOntologyManager()
				.addOntologyLoaderListener(this);
		getOWLWorkspace().getOWLModelManager().getOWLOntologyManager()
				.addOntologyChangeListener(this);
		getView().addButtonResetListener(this, actionReset);
		getView().addButtonGetConceptNamesListener(this, actionGetConceptNames);
		getView().addButtonSelectVariablesListener(this, actionSelectVariables);
		getView().addButtonPreviousListener(this, actionPrevious);
		getView().addButtonNextListener(this, actionNext);
		getView().addButtonSaveListener(this, actionSave);
		executeActionReset();
	}

	private VarSelectionController initVarWindow() {
		VarSelectionController ret = new VarSelectionController(
				new VarSelectionView(new VarSelectionModel(getModel()
						.getCandidates())));
		ret.addAcceptVarButtonListener(this, actionAcceptVar);
		ret.addRejectVarButtonListener(this, actionRejectVar);
		return ret;
	}

	private void keepOntologyUpdated() {
		if (this.ontologyChanged) {
			this.ontologyChanged = false;
			getModel().clearOntology();
			getModel().loadOntology(
					getOWLWorkspace().getOWLModelManager()
							.getOWLOntologyManager(), this.ontology00);
			getModel().loadOntology(
					getOWLWorkspace().getOWLModelManager()
							.getOWLOntologyManager(), this.ontology01);
		}
	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> change)
			throws OWLException {
		if (change == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		logger.info("The ontology has changed.");
		this.ontologyChanged = true;
	}

	private void processMapIdClass(OWLOntology ontology) {
		Set<OWLClass> set = ontology.getClassesInSignature();
		for (OWLClass cls : set) {
			this.mapIdClass.put(getId(cls), cls);
		}
	}

	private void processMapIdLabel(OWLOntology ontology) {
		Set<OWLClass> set = ontology.getClassesInSignature();
		for (OWLClass cls : set) {
			OWLAnnotationValue value = getLabel(cls, ontology);
			this.mapIdLabel.put(getId(cls), value == null ? cls.getIRI()
					.toURI().getFragment() : value.toString());
		}
	}

	private void reloadClassNames() {
		this.classList00 = getClassNames(this.ontology00);
		List<String> classNameList00 = new ArrayList<String>();
		for (String className : this.classList00) {
			classNameList00.add(getLabel(className));
		}

		this.classList01 = getClassNames(this.ontology01);
		List<String> classNameList01 = new ArrayList<String>();
		for (String className : this.classList01) {
			classNameList01.add(getLabel(className));
		}
		getView().reloadClassNames(this.classList00, this.classList01);
	}

	public void reloadOntologyNames() {
		Set<OWLOntology> ontologies = this.owlWorkspace.getOWLModelManager()
				.getOntologies();
		this.ontologyMap.clear();
		for (OWLOntology ontology : ontologies) {
			this.ontologyMap.put(ontology.getOntologyID().toString(), ontology);
		}
		Set<String> set = new TreeSet<String>();
		set.addAll(this.ontologyMap.keySet());
		this.ontologyList.clear();
		this.ontologyList.addAll(set);
		getView().reloadOntologies(this.ontologyList);
	}

	public void removeListeners() {
		getOWLWorkspace().getOWLModelManager().removeOntologyChangeListener(
				this);
	}

	@Override
	public void startedLoadingOntology(LoadingStartedEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("Null argument.");
		}
	}

	private void updateUnifier() {
		if (getModel().getUnifierList().size() > 0) {
			getView().getUnifier().setText(
					getModel().getUnifierList().get(this.unifierIndex));
		} else {
			getView().getUnifier().setText("[not unifiable]");
		}
	}

}
