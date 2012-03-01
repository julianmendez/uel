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

import de.tudresden.inf.lat.uel.plugin.processor.PluginGoal;
import de.tudresden.inf.lat.uel.plugin.processor.UelModel;

/**
 * This class is a controller for the main panel of UEL's graphical user
 * interface.
 * 
 * @author Julian Mendez
 */
public class UelController implements ActionListener {

	private static final String actionAcceptVar = "accept var";
	private static final String actionCheckBoxClassName00 = "class 00 primitive";
	private static final String actionCheckBoxClassName01 = "class 01 primitive";
	private static final String actionClass00Selected = "class 00 selected";
	private static final String actionClass01Selected = "class 01 selected";
	private static final String actionOntology00Selected = "ontology 00 selected";
	private static final String actionOntology01Selected = "ontology 01 selected";
	private static final String actionOpen = "open";
	private static final String actionRejectVar = "reject var";
	private static final String actionSelectVariables = "get var candidate";

	private List<LabelId> classList00 = null;
	private List<LabelId> classList01 = null;
	private Map<String, OWLClass> mapIdClass = new HashMap<String, OWLClass>();
	private Map<String, String> mapIdLabel = new HashMap<String, String>();
	private List<String> ontologyList = new ArrayList<String>();
	private OWLOntology owlOntology00 = null;
	private OWLOntology owlOntology01 = null;
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
		resetUnifierController();
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
		} else if (cmd.equals(actionOntology00Selected)) {
			executeActionOntology00Selected();
		} else if (cmd.equals(actionOntology01Selected)) {
			executeActionOntology01Selected();
		} else if (cmd.equals(actionClass00Selected)) {
			executeActionClass00Selected();
		} else if (cmd.equals(actionClass01Selected)) {
			executeActionClass01Selected();
		} else if (cmd.equals(actionSelectVariables)) {
			executeActionSelectVariables();
		} else if (cmd.equals(actionAcceptVar)) {
			executeActionAcceptVar();
		} else if (cmd.equals(actionRejectVar)) {
			executeActionRejectVar();
		} else if (cmd.equals(actionCheckBoxClassName00)) {
			executeActionOntology00Selected();
		} else if (cmd.equals(actionCheckBoxClassName01)) {
			executeActionOntology01Selected();
		} else {
			throw new IllegalStateException();
		}
	}

	private void executeActionAcceptVar() {
		resetUnifierController();

		if (getModel().getUnifierList().isEmpty()) {
			PluginGoal g = this.varWindow.getView().getModel().getPluginGoal();

			this.varWindow.close();

			getModel().configureUelProcessor(g.getUelInput());
			getUnifier().setStatInfo(
					new StatInfo(g, getLiteralSetSize(), getClauseSetSize(),
							this.mapIdLabel));
		}

		getUnifier().getView().setUnifierButtons(false);
		getUnifier().getView().setButtonNextEnabled(true);
		getUnifier().getView().setButtonShowStatInfoEnabled(true);

		this.unifierController.getView().setVisible(true);
	}

	private void executeActionClass00Selected() {
		getUnifier().getView().setUnifierButtons(false);
		getUnifier().getView().getUnifier().setText("");
		try {
			getView().setToolTipTextClass00(
					getView().getSelectedClassName00().getId());
		} catch (ArrayIndexOutOfBoundsException e) {
			getView().setToolTipTextClass00(Message.tooltipComboBoxClassName00);
		}
	}

	private void executeActionClass01Selected() {
		getUnifier().getView().setUnifierButtons(false);
		getUnifier().getView().getUnifier().setText("");
		try {
			getView().setToolTipTextClass01(
					getView().getSelectedClassName01().getId());
		} catch (ArrayIndexOutOfBoundsException e) {
			getView().setToolTipTextClass01(Message.tooltipComboBoxClassName01);
		}
	}

	private void executeActionOntology00Selected() {
		executeActionOntology00Selected(getView().getSelectedOntologyName00());
	}

	private void executeActionOntology00Selected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {
			getUnifier().getView().setUnifierButtons(false);

			String ontologyId = this.ontologyList.get(ontologyIndex);
			this.owlOntology00 = this.owlOntologyMap.get(ontologyId);

			this.classList00 = getClassNames(
					this.owlOntologyMap.get(ontologyId), true);

			getView().reloadClassNames00(this.classList00);
			getView().setComboBoxClassName00Enabled(true);
			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOntology01Selected() {
		executeActionOntology01Selected(getView().getSelectedOntologyName01());
	}

	private void executeActionOntology01Selected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {

			getUnifier().getView().setUnifierButtons(false);

			String ontologyId = this.ontologyList.get(ontologyIndex);
			this.owlOntology01 = this.owlOntologyMap.get(ontologyId);

			this.classList01 = getClassNames(
					this.owlOntologyMap.get(ontologyId), true);

			getView().reloadClassNames01(this.classList01);
			getView().setComboBoxClassName01Enabled(true);
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
				executeActionOntology00Selected();
				executeActionOntology01Selected();
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

		getModel().clearOntology();
		getModel().loadOntology(this.owlOntology00, this.owlOntology01);

		Set<String> classSet = new HashSet<String>();
		classSet.add(getView().getSelectedClassName00().getId());
		classSet.add(getView().getSelectedClassName01().getId());

		PluginGoal goal = getModel().configure(classSet);

		try {
			this.varWindow = initVarWindow(classSet, goal);
			this.varWindow.open();
		} catch (RuntimeException e) {
		}
	}

	private List<LabelId> getClassNames(OWLOntology ontology, boolean primitive) {
		OWLClass nothing = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLNothing();
		OWLClass thing = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLThing();

		Set<OWLClass> set = new TreeSet<OWLClass>();
		set.addAll(getDefinedConcepts(ontology, primitive));
		set.remove(nothing);
		set.remove(thing);

		List<LabelId> ret = new ArrayList<LabelId>();
		for (OWLClass cls : set) {
			ret.add(new LabelId(getShortForm(cls), getId(cls)));
		}
		return ret;
	}

	private int getClauseSetSize() {
		// FIXME
		return -1;
	}

	private Set<OWLClass> getDefinedConcepts(OWLOntology ontology,
			boolean primitive) {
		Set<OWLClass> ret = new HashSet<OWLClass>();
		for (OWLAxiom axiom : ontology.getAxioms()) {
			if (axiom instanceof OWLEquivalentClassesAxiom) {
				OWLEquivalentClassesAxiom equivAxiom = (OWLEquivalentClassesAxiom) axiom;
				for (OWLClassExpression classExpr : equivAxiom
						.getClassExpressions()) {
					if (classExpr instanceof OWLClass) {
						ret.add((OWLClass) classExpr);
					}
				}
			}
			if (primitive && axiom instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom subClassAxiom = (OWLSubClassOfAxiom) axiom;
				OWLClassExpression classExpr = subClassAxiom.getSubClass();
				if (classExpr instanceof OWLClass) {
					ret.add((OWLClass) classExpr);
				}
			}
		}
		return ret;
	}

	private String getId(OWLEntity entity) {
		return entity.getIRI().toURI().toString();
	}

	private int getLiteralSetSize() {
		// FIXME
		return -1;
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
		getView().addComboBoxClass00Listener(this, actionClass00Selected);
		getView().addComboBoxClass01Listener(this, actionClass01Selected);
		getView().addComboBoxOntology00Listener(this, actionOntology00Selected);
		getView().addComboBoxOntology01Listener(this, actionOntology01Selected);

		reset();
		reloadOntologies();

		executeActionOntology00Selected(0);
		executeActionOntology01Selected(0);
	}

	private VarSelectionController initVarWindow(Set<String> originalVariables,
			PluginGoal goal) {
		VarSelectionController ret = new VarSelectionController(
				new VarSelectionView(new VarSelectionModel(originalVariables,
						this.mapIdLabel, goal)));
		ret.addAcceptVarButtonListener(this, actionAcceptVar);
		return ret;
	}

	private void loadOntology(OWLOntology owlOntology) {
		String ontologyId = owlOntology.getOntologyID().toString();
		if (!this.owlOntologyMap.containsKey(ontologyId)) {
			this.owlOntologyMap.put(ontologyId, owlOntology);
			processMapIdClass(owlOntology);
			Set<String> set = new TreeSet<String>();
			set.addAll(this.owlOntologyMap.keySet());
			this.ontologyList.clear();
			this.ontologyList.addAll(set);
			getView().reloadOntologies(this.ontologyList);
		}
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
		executeActionOntology00Selected();
		executeActionOntology01Selected();
	}

	public void reset() {
		resetUnifierController();
		getView().setComboBoxClassName00Enabled(false);
		getView().setComboBoxClassName01Enabled(false);
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
