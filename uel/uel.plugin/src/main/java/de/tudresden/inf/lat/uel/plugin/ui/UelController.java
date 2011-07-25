package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;

import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
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

	private List<LabelId> classList00 = null;
	private List<LabelId> classList01 = null;
	private Map<String, OWLClass> mapIdClass = new HashMap<String, OWLClass>();
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

		processMapIdClass(this.ontology00);
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
			classSet.add(this.classList00.get(
					getView().getSelectedClassName00()).getId());
			classSet.add(this.classList01.get(
					getView().getSelectedClassName01()).getId());
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
		classSet.add(this.classList00.get(getView().getSelectedClassName00())
				.getId());
		classSet.add(this.classList01.get(getView().getSelectedClassName01())
				.getId());
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

	private List<LabelId> getClassNames(OWLOntology ontology) {
		OWLClass nothing = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLNothing();
		OWLClass thing = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLThing();
		OWLModelManagerEntityRenderer renderer = this.owlWorkspace
				.getOWLModelManager().getOWLEntityRenderer();

		Set<OWLClass> set = new TreeSet<OWLClass>();
		set.addAll(ontology.getClassesInSignature());
		set.remove(nothing);
		set.remove(thing);

		List<LabelId> ret = new ArrayList<LabelId>();
		ret.add(new LabelId(renderer.getShortForm(nothing), nothing
				.toStringID()));
		ret.add(new LabelId(renderer.getShortForm(thing), thing.toStringID()));
		for (OWLClass cls : set) {
			ret.add(new LabelId(renderer.getShortForm(cls), getId(cls)));
		}
		return ret;
	}

	private String getId(OWLClass cls) {
		return cls.getIRI().toURI().toString();
	}

	private String getLabel(String candidateId) {
		String ret = candidateId;
		OWLClass cls = this.mapIdClass.get(candidateId);
		if (cls != null) {
			ret = this.owlWorkspace.getOWLModelManager().getOWLEntityRenderer()
					.getShortForm(cls);
		}
		return ret;
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
		Map<String, String> map = new HashMap<String, String>();
		Set<String> candidates = getModel().getCandidates();
		for (String candidateId : candidates) {
			map.put(candidateId, getLabel(candidateId));
		}
		VarSelectionController ret = new VarSelectionController(
				new VarSelectionView(new VarSelectionModel(candidates, map)));
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

		OWLClass nothing = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLNothing();
		this.mapIdClass.put(getId(nothing), nothing);

		OWLClass thing = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLThing();
		this.mapIdClass.put(getId(thing), thing);
	}

	private void reloadClassNames() {
		this.classList00 = getClassNames(this.ontology00);
		List<String> classNameList00 = new ArrayList<String>();
		for (LabelId className : this.classList00) {
			classNameList00.add(className.getLabel());
		}

		this.classList01 = getClassNames(this.ontology01);
		List<String> classNameList01 = new ArrayList<String>();
		for (LabelId className : this.classList01) {
			classNameList01.add(className.getLabel());
		}

		getView().reloadClassNames(classNameList00, classNameList01);
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

	private String showUnifier(String str) {
		StringBuffer ret = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new StringReader(str));
			String line = "";
			while (line != null) {
				line = reader.readLine();
				if (line != null) {
					StringTokenizer stok = new StringTokenizer(line);
					while (stok.hasMoreTokens()) {
						String token = stok.nextToken();
						OWLClass cls = this.mapIdClass.get(token);
						if (cls != null) {
							ret.append(getLabel(token));
						} else {
							ret.append(token);
						}
						if (stok.hasMoreTokens()) {
							ret.append(" ");
						}
					}
				}
				ret.append("\n");
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return ret.toString();
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
					showUnifier(getModel().getUnifierList().get(
							this.unifierIndex)));
		} else {
			getView().getUnifier().setText("[not unifiable]");
		}
	}

}
