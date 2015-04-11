package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.processor.PluginGoal;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UelProcessorFactory;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;

/**
 * This class is a controller for the main panel of the UEL system.
 * 
 * @author Julian Mendez
 */
public class UelController implements ActionListener {

	private static final String actionAcceptVar = "accept var";
	private static final String actionOntologyBg00Selected = "ontology Bg00 selected";
	private static final String actionOntologyBg01Selected = "ontology Bg01 selected";
	private static final String actionOntologyPosSelected = "ontology Pos selected";
	private static final String actionOntologyNegSelected = "ontology Neg selected";
	private static final String actionOpen = "open";
	private static final String actionRejectVar = "reject var";
	private static final String actionSelectVariables = "get var candidate";

	public static String getId(OWLEntity entity) {
		return entity.getIRI().toURI().toString();
	}

	private Map<String, String> mapIdLabel = new HashMap<String, String>();
	private List<String> ontologyList = new ArrayList<String>();
	private OWLOntology owlOntologyBg00 = null;
	private OWLOntology owlOntologyBg01 = null;
	private OWLOntology owlOntologyPos = null;
	private OWLOntology owlOntologyNeg = null;
	private final OWLOntologyManager owlOntologyManager;
	private Map<String, OWLOntology> owlOntologyMap = new HashMap<String, OWLOntology>();
	private Map<OWLEntity, String> shortFormMap = new HashMap<OWLEntity, String>();
	private UnifierController unifierController;
	private VarSelectionController varWindow = null;
	private final UelView view;

	/**
	 * Constructs a new controller.
	 * 
	 * @param view
	 *            panel to be controlled
	 * @param ontologyManager
	 *            an OWL ontology manager
	 */
	public UelController(UelView view, OWLOntologyManager ontologyManager) {
		this.view = view;
		this.owlOntologyManager = ontologyManager;
		init();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String cmd = e.getActionCommand();
		if (cmd.equals(actionOpen)) {
			executeActionOpen();
		} else if (cmd.equals(actionOntologyBg00Selected)) {
			executeActionOntologyBg00Selected();
		} else if (cmd.equals(actionOntologyBg01Selected)) {
			executeActionOntologyBg01Selected();
		} else if (cmd.equals(actionOntologyPosSelected)) {
			executeActionOntologyPosSelected();
		} else if (cmd.equals(actionOntologyNegSelected)) {
			executeActionOntologyNegSelected();
		} else if (cmd.equals(actionSelectVariables)) {
			executeActionSelectVariables();
		} else if (cmd.equals(actionAcceptVar)) {
			executeActionAcceptVar();
		} else if (cmd.equals(actionRejectVar)) {
			executeActionRejectVar();
		} else {
			throw new IllegalStateException();
		}
	}

	private void executeActionAcceptVar() {
		resetUnifierController();

		if (getModel().getUnifierList().isEmpty()) {
			PluginGoal g = this.varWindow.getView().getModel().getPluginGoal();

			this.varWindow.close();

			UelProcessor processor = UelProcessorFactory.createProcessor(
					getView().getSelectedProcessor(), g.getUelInput());
			getModel().setUelProcessor(processor);
			getUnifier().setStatInfo(
					new StatInfo(g, processor.getInfo(), this.mapIdLabel));
		}

		getUnifier().getView().setUnifierButtons(false);
		getUnifier().getView().setButtonNextEnabled(true);
		getUnifier().getView().setButtonShowStatInfoEnabled(true);
		getUnifier().getView().setVisible(true);
	}

	private void executeActionOntologyBg00Selected() {
		executeActionOntologyBg00Selected(getView()
				.getSelectedOntologyNameBg00());
	}

	private void executeActionOntologyBg00Selected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {
			getUnifier().getView().setUnifierButtons(false);

			String ontologyId = this.ontologyList.get(ontologyIndex);
			this.owlOntologyBg00 = this.owlOntologyMap.get(ontologyId);

			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOntologyBg01Selected() {
		executeActionOntologyBg01Selected(getView()
				.getSelectedOntologyNameBg01());
	}

	private void executeActionOntologyBg01Selected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {

			getUnifier().getView().setUnifierButtons(false);

			String ontologyId = this.ontologyList.get(ontologyIndex);
			this.owlOntologyBg01 = this.owlOntologyMap.get(ontologyId);

			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOntologyPosSelected() {
		executeActionOntologyPosSelected(getView().getSelectedOntologyNamePos());
	}

	private void executeActionOntologyPosSelected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {

			getUnifier().getView().setUnifierButtons(false);

			String ontologyId = this.ontologyList.get(ontologyIndex);
			this.owlOntologyPos = this.owlOntologyMap.get(ontologyId);

			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOntologyNegSelected() {
		executeActionOntologyNegSelected(getView().getSelectedOntologyNameNeg());
	}

	private void executeActionOntologyNegSelected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {

			getUnifier().getView().setUnifierButtons(false);

			String ontologyId = this.ontologyList.get(ontologyIndex);
			this.owlOntologyNeg = this.owlOntologyMap.get(ontologyId);

			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOpen() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(getView());
		File file = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		}
		if (file != null) {
			try {
				OWLOntology owlOntology = this.owlOntologyManager
						.loadOntologyFromOntologyDocument(file);
				loadOntology(owlOntology);
				reset();
				updateOntologySelection();
			} catch (OWLOntologyCreationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void executeActionRejectVar() {
		this.varWindow.close();
		getUnifier().getView().setUnifierButtons(false);
	}

	private void executeActionSelectVariables() {
		getUnifier().getView().setUnifierButtons(false);

		getModel().reset();

		Set<OWLOntology> bgOntologies = new HashSet<OWLOntology>();
		bgOntologies.add(this.owlOntologyBg00);
		bgOntologies.add(this.owlOntologyBg01);

		getModel().configure(owlOntologyManager, bgOntologies, owlOntologyPos,
				owlOntologyNeg, null);

		this.varWindow = initVarWindow(getModel().getPluginGoal());
		this.varWindow.open();
	}

	public UelModel getModel() {
		return getView().getModel();
	}

	public OWLOntologyManager getOWLOntologyManager() {
		return this.owlOntologyManager;
	}

	private String getShortForm(OWLEntity entity) {
		String ret = this.shortFormMap.get(entity);
		if (ret == null) {
			IRI iri = entity.getIRI();
			ret = iri.getFragment();
		}
		return ret;
	}

	public Map<OWLEntity, String> getShortFormMap() {
		return Collections.unmodifiableMap(this.shortFormMap);
	}

	public UnifierController getUnifier() {
		return this.unifierController;
	}

	public UelView getView() {
		return this.view;
	}

	/**
	 * Initializes the data and GUI. This method is called when the view is
	 * initialized.
	 */
	private void init() {
		getView().addButtonOpenListener(this, actionOpen);
		getView().addButtonSelectVariablesListener(this, actionSelectVariables);
		getView().addComboBoxOntologyBg00Listener(this,
				actionOntologyBg00Selected);
		getView().addComboBoxOntologyBg01Listener(this,
				actionOntologyBg01Selected);
		getView().addComboBoxOntologyPosListener(this,
				actionOntologyPosSelected);
		getView().addComboBoxOntologyNegListener(this,
				actionOntologyNegSelected);

		reset();
		reloadOntologies();

		updateOntologySelection(0);
	}

	private VarSelectionController initVarWindow(PluginGoal goal) {
		VarSelectionController ret = new VarSelectionController(
				new VarSelectionView(new VarSelectionModel(this.mapIdLabel,
						goal)));
		ret.addAcceptVarButtonListener(this, actionAcceptVar);
		return ret;
	}

	private void loadOntology(OWLOntology owlOntology) {
		String ontologyId = owlOntology.getOntologyID().toString();
		if (!this.owlOntologyMap.containsKey(ontologyId)) {
			this.owlOntologyMap.put(ontologyId, owlOntology);
			this.ontologyList.add(ontologyId);
			getView().reloadOntologies(this.ontologyList);
		}
	}

	private void recomputeShortForm() {
		this.mapIdLabel.clear();
		for (OWLEntity cls : this.shortFormMap.keySet()) {
			this.mapIdLabel.put(getId(cls), getShortForm(cls));
		}
	}

	public void reloadOntologies() {
		Set<OWLOntology> owlOntologies = getOWLOntologyManager()
				.getOntologies();
		for (OWLOntology owlOntology : owlOntologies) {
			loadOntology(owlOntology);
		}
		updateOntologySelection();
	}

	private void updateOntologySelection() {
		executeActionOntologyBg00Selected();
		executeActionOntologyBg01Selected();
		executeActionOntologyPosSelected();
		executeActionOntologyNegSelected();
	}

	private void updateOntologySelection(int ontologyIndex) {
		executeActionOntologyBg00Selected(ontologyIndex);
		executeActionOntologyBg01Selected(ontologyIndex);
		executeActionOntologyPosSelected(ontologyIndex);
		executeActionOntologyNegSelected(ontologyIndex);
	}

	public void reset() {
		resetUnifierController();
		getView().setButtonSelectVariablesEnabled(false);
	}

	private void resetUnifierController() {
		this.unifierController = new UnifierController(new UnifierView(
				view.getModel()), this.mapIdLabel);
	}

	public void setShortFormMap(Map<OWLEntity, String> map) {
		if (map == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.shortFormMap = map;
		recomputeShortForm();
	}

}
