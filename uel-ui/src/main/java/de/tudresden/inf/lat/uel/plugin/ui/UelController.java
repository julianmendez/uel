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

import javax.swing.JFileChooser;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
	private static final String actionRefine = "refine";
	private static final String actionSaveDissubsumptions = "save dissubs";
	private static final String actionRecompute = "recompute";

	private Map<String, String> mapIdLabel = new HashMap<String, String>();
	private List<OWLOntology> ontologyList = new ArrayList<OWLOntology>();
	private OWLOntology owlOntologyBg00 = null;
	private OWLOntology owlOntologyBg01 = null;
	private OWLOntology owlOntologyPos = null;
	private OWLOntology owlOntologyNeg = null;
	private final OWLOntologyManager owlOntologyManager;
	private Map<OWLEntity, String> shortFormMap = new HashMap<OWLEntity, String>();
	private UnifierController unifierController;
	private VarSelectionController varWindow = null;
	private final UelView view;
	private DissubsumptionView dview;

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

		switch (e.getActionCommand()) {
		case actionOpen:
			executeActionOpen();
			break;
		case actionOntologyBg00Selected:
			executeActionOntologyBg00Selected();
			break;
		case actionOntologyBg01Selected:
			executeActionOntologyBg01Selected();
			break;
		case actionOntologyPosSelected:
			executeActionOntologyPosSelected();
			break;
		case actionOntologyNegSelected:
			executeActionOntologyNegSelected();
			break;
		case actionSelectVariables:
			executeActionSelectVariables();
			break;
		case actionAcceptVar:
			executeActionAcceptVar();
			break;
		case actionRejectVar:
			executeActionRejectVar();
			break;
		case actionRefine:
			executeActionRefine();
			break;
		case actionSaveDissubsumptions:
			executeActionSaveDissubsumptions();
			break;
		case actionRecompute:
			executeActionRecompute();
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void executeActionRecompute() {
		// TODO Auto-generated method stub

	}

	private void executeActionSaveDissubsumptions() {
		String allDissubsumptions = dview.getDissubsumptions();
		saveToOntologyFile(allDissubsumptions);
	}

	private void executeActionRefine() {
		if (getModel().getUnifierList().size() == 0) {
			return;
		}

		if (dview != null) {
			dview.close();
		}

		dview = new DissubsumptionView(unifierController.getCurrentUnifier(), unifierController.getKRSSRenderer());
		dview.addSaveButtonListener(this, actionSaveDissubsumptions);
		dview.addRecomputeButtonListener(this, actionRecompute);

	}

	private void executeActionAcceptVar() {
		resetUnifierController();

		if (getModel().getUnifierList().isEmpty()) {
			PluginGoal g = getModel().getPluginGoal();

			this.varWindow.close();

			UelProcessor processor = UelProcessorFactory.createProcessor(getView().getSelectedProcessor(),
					g.getUelInput());
			getModel().setUelProcessor(processor);
			getUnifier().setStatInfo(new StatInfo(g, processor.getInfo(), this.mapIdLabel));
		}

		getUnifier().getView().setUnifierButtons(false);
		getUnifier().getView().setButtonNextEnabled(true);
		getUnifier().getView().setButtonShowStatInfoEnabled(true);
		getUnifier().getView().setVisible(true);
	}

	private void executeActionOntologyBg00Selected() {
		executeActionOntologyBg00Selected(getView().getSelectedOntologyNameBg00());
	}

	private void executeActionOntologyBg00Selected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {
			getUnifier().getView().setUnifierButtons(false);

			this.owlOntologyBg00 = this.ontologyList.get(ontologyIndex);

			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOntologyBg01Selected() {
		executeActionOntologyBg01Selected(getView().getSelectedOntologyNameBg01());
	}

	private void executeActionOntologyBg01Selected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {

			getUnifier().getView().setUnifierButtons(false);

			this.owlOntologyBg01 = this.ontologyList.get(ontologyIndex);

			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOntologyPosSelected() {
		executeActionOntologyPosSelected(getView().getSelectedOntologyNamePos());
	}

	private void executeActionOntologyPosSelected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {

			getUnifier().getView().setUnifierButtons(false);

			this.owlOntologyPos = this.ontologyList.get(ontologyIndex);

			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOntologyNegSelected() {
		executeActionOntologyNegSelected(getView().getSelectedOntologyNameNeg());
	}

	private void executeActionOntologyNegSelected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {

			getUnifier().getView().setUnifierButtons(false);

			this.owlOntologyNeg = this.ontologyList.get(ontologyIndex);

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
				this.owlOntologyManager.loadOntologyFromOntologyDocument(file);
				reset();
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

		getModel().configure(bgOntologies, owlOntologyPos, owlOntologyNeg, null);

		this.varWindow = initVarWindow(getModel().getPluginGoal());
		this.varWindow.open();
	}

	public UelModel getModel() {
		return getView().getModel();
	}

	public OWLOntologyManager getOWLOntologyManager() {
		return this.owlOntologyManager;
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
		getView().addComboBoxOntologyBg00Listener(this, actionOntologyBg00Selected);
		getView().addComboBoxOntologyBg01Listener(this, actionOntologyBg01Selected);
		getView().addComboBoxOntologyPosListener(this, actionOntologyPosSelected);
		getView().addComboBoxOntologyNegListener(this, actionOntologyNegSelected);

		reset();
	}

	private VarSelectionController initVarWindow(PluginGoal goal) {
		VarSelectionController ret = new VarSelectionController(
				new VarSelectionView(new VarSelectionModel(this.mapIdLabel, goal)));
		ret.addAcceptVarButtonListener(this, actionAcceptVar);
		return ret;
	}

	private void recomputeShortForm() {
		this.mapIdLabel.clear();
		for (OWLEntity entity : this.shortFormMap.keySet()) {
			this.mapIdLabel.put(entity.getIRI().toURI().toString(), this.shortFormMap.get(entity));
		}
	}

	private void updateOntologySelection() {
		executeActionOntologyBg00Selected();
		executeActionOntologyBg01Selected();
		executeActionOntologyPosSelected();
		executeActionOntologyNegSelected();
	}

	public void reset() {
		resetUnifierController();
		getView().setButtonSelectVariablesEnabled(false);
		this.ontologyList.clear();
		try {
			this.ontologyList.add(OWLManager.createOWLOntologyManager().createOntology(IRI.create("empty")));
		} catch (OWLOntologyCreationException e) {
		}
		this.ontologyList.addAll(getOWLOntologyManager().getOntologies());

		getView().reloadOntologies(this.ontologyList);
		updateOntologySelection();
	}

	private void resetUnifierController() {
		this.unifierController = new UnifierController(new UnifierView(view.getModel()), this.mapIdLabel);
		unifierController.addRefineButtonListener(this, actionRefine);
	}

	public void setShortFormMap(Map<OWLEntity, String> map) {
		if (map == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.shortFormMap = map;
		recomputeShortForm();
	}

}
