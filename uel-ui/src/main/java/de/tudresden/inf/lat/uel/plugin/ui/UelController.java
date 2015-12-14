package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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

	private List<OWLOntology> ontologyList = new ArrayList<OWLOntology>();
	private OWLOntology owlOntologyBg00 = null;
	private OWLOntology owlOntologyBg01 = null;
	private OWLOntology owlOntologyPos = null;
	private OWLOntology owlOntologyNeg = null;
	private final OWLOntologyManager owlOntologyManager;
	private VarSelectionController varSelectionController = null;
	private UnifierController unifierController;
	private RefineController refineController = null;
	private final UelView view;
	private final UelModel model;

	/**
	 * Constructs a new controller.
	 * 
	 * @param view
	 *            panel to be controlled
	 * @param ontologyManager
	 *            an OWL ontology manager
	 */
	public UelController(UelView view, UelModel model, OWLOntologyManager ontologyManager) {
		this.view = view;
		this.model = model;
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
		addNewDissubsumptions();
		recomputeUnifiers();
	}

	private void executeActionSaveDissubsumptions() {
		File file = UelUI.showSaveFileDialog(refineController.getView());
		if (file == null) {
			return;
		}

		addNewDissubsumptions();
		try {
			String dissubsumptions = OntologyRenderer.renderKRSS(owlOntologyNeg);
			OntologyRenderer.saveToOntologyFile(dissubsumptions, file);
		} catch (OWLRendererException ex) {
			JOptionPane.showMessageDialog(refineController.getView(),
					"Failed to save ontology: " + System.lineSeparator() + ex.toString());
		}
		recomputeUnifiers();
	}

	private void addNewDissubsumptions() {
		// TODO add dissubsumptions from dview to owlOntologyNeg
		String dissubsumptions = refineController.getDissubsumptions();
	}

	private void recomputeUnifiers() {
		// TODO close dview and restart computation
		refineController.close();
	}

	private void executeActionRefine() {
		if (getModel().getUnifierList().size() == 0) {
			return;
		}

		refineController = new RefineController(new RefineView(), getModel());
		refineController.addButtonSaveListener(this, actionSaveDissubsumptions);
		refineController.addButtonRecomputeListener(this, actionRecompute);
		refineController.open();
	}

	private void executeActionAcceptVar() {
		varSelectionController.close();

		if (getModel().getUnifierList().isEmpty()) {
			UelProcessor processor = UelProcessorFactory.createProcessor(getView().getSelectedProcessor(),
					getModel().getPluginGoal().getUelInput());
			getModel().setUelProcessor(processor);
		}

		unifierController = new UnifierController(new UnifierView(), getModel());
		unifierController.addButtonRefineListener(this, actionRefine);
		unifierController.open();
	}

	private void executeActionOntologyBg00Selected() {
		executeActionOntologyBg00Selected(getView().getSelectedOntologyNameBg00());
	}

	private void executeActionOntologyBg00Selected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {
			this.owlOntologyBg00 = this.ontologyList.get(ontologyIndex);

			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOntologyBg01Selected() {
		executeActionOntologyBg01Selected(getView().getSelectedOntologyNameBg01());
	}

	private void executeActionOntologyBg01Selected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {
			this.owlOntologyBg01 = this.ontologyList.get(ontologyIndex);

			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOntologyPosSelected() {
		executeActionOntologyPosSelected(getView().getSelectedOntologyNamePos());
	}

	private void executeActionOntologyPosSelected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {
			this.owlOntologyPos = this.ontologyList.get(ontologyIndex);

			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOntologyNegSelected() {
		executeActionOntologyNegSelected(getView().getSelectedOntologyNameNeg());
	}

	private void executeActionOntologyNegSelected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {
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
		this.varSelectionController.close();
	}

	private void executeActionSelectVariables() {
		getModel().reset();

		Set<OWLOntology> bgOntologies = new HashSet<OWLOntology>();
		bgOntologies.add(this.owlOntologyBg00);
		bgOntologies.add(this.owlOntologyBg01);

		getModel().configure(bgOntologies, owlOntologyPos, owlOntologyNeg, null);

		this.varSelectionController = new VarSelectionController(new VarSelectionView(), getModel());
		this.varSelectionController.addAcceptVarButtonListener(this, actionAcceptVar);
		this.varSelectionController.open();
	}

	private UelModel getModel() {
		return model;
	}

	public UelView getView() {
		return view;
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

	private void updateOntologySelection() {
		executeActionOntologyBg00Selected();
		executeActionOntologyBg01Selected();
		executeActionOntologyPosSelected();
		executeActionOntologyNegSelected();
	}

	public void reset() {
		// TODO is it always necessary to reset everything? can we keep the
		// selected ontologies (if they still exist?)
		getView().setButtonSelectVariablesEnabled(false);
		this.ontologyList.clear();
		try {
			this.ontologyList.add(OWLManager.createOWLOntologyManager().createOntology(IRI.create("empty")));
		} catch (OWLOntologyCreationException e) {
		}
		this.ontologyList.addAll(owlOntologyManager.getOntologies());

		getView().reloadOntologies(this.ontologyList);
		updateOntologySelection();
	}

	public void setShortFormMap(Map<OWLEntity, String> map) {
		if (map == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		Map<String, String> mapIdLabel = new HashMap<String, String>();
		for (OWLEntity entity : map.keySet()) {
			mapIdLabel.put(entity.getIRI().toURI().toString(), map.get(entity));
		}
		getModel().setMapIdLabel(mapIdLabel);
	}

}
