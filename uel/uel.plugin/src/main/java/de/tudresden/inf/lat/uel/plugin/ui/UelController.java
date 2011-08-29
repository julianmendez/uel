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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;

import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.sat.Translator;
import de.tudresden.inf.lat.uel.core.type.Atom;
import de.tudresden.inf.lat.uel.core.type.Equation;
import de.tudresden.inf.lat.uel.core.type.FAtom;
import de.tudresden.inf.lat.uel.core.type.Goal;
import de.tudresden.inf.lat.uel.core.type.KRSSKeyword;
import de.tudresden.inf.lat.uel.plugin.processor.UelProcessor;

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
	private static final String actionFirst = "first";
	private static final String actionLast = "last";
	private static final String actionNext = "next";
	private static final String actionOntology00Selected = "ontology 00 selected";
	private static final String actionOntology01Selected = "ontology 01 selected";
	private static final String actionOpen = "open";
	private static final String actionPrevious = "previous";
	private static final String actionRejectVar = "reject var";
	private static final String actionSave = "save";
	private static final String actionSelectVariables = "get var candidate";
	private static final String actionShowStatInfo = "show statistic info";
	private static final String initialUnifierIdText = " 0 ";

	private boolean allUnifiersFound = false;
	private List<LabelId> classList00 = null;
	private List<LabelId> classList01 = null;
	private Map<String, OWLClass> mapIdClass = new HashMap<String, OWLClass>();
	private Map<String, String> mapIdLabel = new HashMap<String, String>();
	private List<String> ontologyList = new ArrayList<String>();
	private OWLOntology owlOntology00 = null;
	private OWLOntology owlOntology01 = null;
	private OWLOntologyManager owlOntologyManager = null;
	private Map<String, OWLOntology> owlOntologyMap = new HashMap<String, OWLOntology>();
	private Map<OWLClass, String> shortFormMap = new HashMap<OWLClass, String>();
	private StatInfo statInfo = null;
	private int unifierIndex = -1;
	private VarSelectionController varWindow = null;
	private UelView view = null;

	/**
	 * Constructs a new controller.
	 * 
	 * @param panel
	 *            panel to be controlled
	 * @param ontologyManager
	 *            an OWL ontology manager
	 */
	public UelController(UelView panel, OWLOntologyManager ontologyManager) {
		if (panel == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (ontologyManager == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.view = panel;
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
		} else if (cmd.equals(actionFirst)) {
			executeActionFirst();
		} else if (cmd.equals(actionPrevious)) {
			executeActionPrevious();
		} else if (cmd.equals(actionNext)) {
			executeActionNext();
		} else if (cmd.equals(actionLast)) {
			executeActionLast();
		} else if (cmd.equals(actionSave)) {
			executeActionSave();
		} else if (cmd.equals(actionCheckBoxClassName00)) {
			executeActionOntology00Selected();
		} else if (cmd.equals(actionCheckBoxClassName01)) {
			executeActionOntology01Selected();
		} else if (cmd.equals(actionShowStatInfo)) {
			executeActionShowStatInfo();
		} else {
			throw new IllegalStateException();
		}
	}

	private void executeActionAcceptVar() {
		if (getModel().getUnifierList().isEmpty()) {
			Goal g = this.varWindow.getView().getModel().getGoal();
			this.varWindow.close();

			getModel().createTranslator(g);
			Translator translator = getModel().getTranslator();
			getModel().computeSatInput();
			this.statInfo = new StatInfo(g, translator.getLiterals().size(),
					getModel().getSatInput().getClauses().size(),
					this.mapIdLabel);
		}

		setUnifierButtons(false);
		getView().setButtonNextEnabled(true);
		getView().setButtonShowStatInfoEnabled(true);

	}

	private void executeActionClass00Selected() {
		setUnifierButtons(false);
		getView().getUnifier().setText("");
	}

	private void executeActionClass01Selected() {
		setUnifierButtons(false);
		getView().getUnifier().setText("");
	}

	private void executeActionFirst() {
		setUnifierButtons(true);

		this.unifierIndex = 0;
		updateUnifier();
	}

	private void executeActionLast() {
		while (!this.allUnifiersFound) {
			int previousSize = getModel().getUnifierList().size();
			getModel().computeNextUnifier();
			if (getModel().getUnifierList().size() == previousSize) {
				this.allUnifiersFound = true;
				this.unifierIndex = getModel().getUnifierList().size() - 1;
			}
		}
		this.unifierIndex = getModel().getUnifierList().size() - 1;
		updateUnifier();
	}

	private void executeActionNext() {
		setUnifierButtons(true);

		this.unifierIndex++;
		if (this.unifierIndex >= getModel().getUnifierList().size()) {
			int previousSize = getModel().getUnifierList().size();
			getModel().computeNextUnifier();
			if (getModel().getUnifierList().size() == previousSize) {
				this.allUnifiersFound = true;
				this.unifierIndex = getModel().getUnifierList().size() - 1;
			}
		}
		updateUnifier();
	}

	private void executeActionOntology00Selected() {
		executeActionOntology00Selected(getView().getSelectedOntologyName00());
	}

	private void executeActionOntology00Selected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {
			setUnifierButtons(false);

			String ontologyId = this.ontologyList.get(ontologyIndex);
			this.owlOntology00 = this.owlOntologyMap.get(ontologyId);

			this.classList00 = getClassNames(
					this.owlOntologyMap.get(ontologyId), getView()
							.isClassName00Primitive());
			List<String> classNameList = new ArrayList<String>();
			for (LabelId className : this.classList00) {
				classNameList.add(className.getLabel());
			}

			getView().reloadClassNames00(classNameList);
			getView().setComboBoxClassName00Enabled(true);
			getView().setButtonSelectVariablesEnabled(true);
		}
	}

	private void executeActionOntology01Selected() {
		executeActionOntology01Selected(getView().getSelectedOntologyName01());
	}

	private void executeActionOntology01Selected(int ontologyIndex) {
		if (0 <= ontologyIndex && ontologyIndex < this.ontologyList.size()) {

			setUnifierButtons(false);

			String ontologyId = this.ontologyList.get(ontologyIndex);
			this.owlOntology01 = this.owlOntologyMap.get(ontologyId);

			this.classList01 = getClassNames(
					this.owlOntologyMap.get(ontologyId), getView()
							.isClassName01Primitive());
			List<String> classNameList = new ArrayList<String>();
			for (LabelId className : this.classList01) {
				classNameList.add(className.getLabel());
			}

			getView().reloadClassNames01(classNameList);
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

	private void executeActionPrevious() {
		setUnifierButtons(true);

		this.unifierIndex--;
		if (this.unifierIndex < 0) {
			this.unifierIndex = 0;
		}
		updateUnifier();
	}

	private void executeActionRejectVar() {
		this.varWindow.close();
		setUnifierButtons(false);
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
				String unifier = toKRSS(getModel().getUnifierList().get(
						this.unifierIndex));
				OntologyRenderer renderer = new OntologyRenderer();
				OWLOntology owlOntology = renderer.parseKRSS(unifier);
				if (file.getName().endsWith(OntologyRenderer.EXTENSION_RDF)) {
					unifier = renderer.renderRDF(owlOntology);
				} else if (file.getName().endsWith(
						OntologyRenderer.EXTENSION_OWL)) {
					unifier = renderer.renderOWL(owlOntology);
				} else if (file.getName().endsWith(
						OntologyRenderer.EXTENSION_KRSS)) {
					unifier = renderer.renderKRSS(owlOntology);
				}

				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				if (getModel().getUnifierList().size() > 0) {
					writer.write(unifier);
				}
				writer.flush();
			} catch (OWLRendererException e) {
				throw new RuntimeException(e);
			} catch (OWLOntologyCreationException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void executeActionSelectVariables() {
		setUnifierButtons(false);

		getModel().clearOntology();
		getModel().loadOntology(this.owlOntology00);
		getModel().loadOntology(this.owlOntology01);

		Set<String> classSet = new HashSet<String>();
		classSet.add(this.classList00.get(getView().getSelectedClassName00())
				.getId());
		classSet.add(this.classList01.get(getView().getSelectedClassName01())
				.getId());

		Goal goal = getModel().configure(classSet);

		try {
			this.varWindow = initVarWindow(classSet, goal);
			this.varWindow.open();
		} catch (RuntimeException e) {
		}
		this.unifierIndex = -1;
		this.allUnifiersFound = false;
		getView().getUnifierId().setText(initialUnifierIdText);
	}

	private void executeActionShowStatInfo() {
		StatInfoController statInfoWindow = new StatInfoController(
				new StatInfoView(this.statInfo));
		statInfoWindow.open();
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

	private String getId(OWLClass cls) {
		return cls.getIRI().toURI().toString();
	}

	private String getLabel(String candidateId) {
		String ret = candidateId;
		OWLClass cls = this.mapIdClass.get(candidateId);
		if (cls != null) {
			ret = getShortForm(cls);
		}
		return ret;
	}

	public DefaultListModel getListModel() {
		return getView().getListModel();
	}

	public UelProcessor getModel() {
		return getView().getModel();
	}

	public OWLOntologyManager getOWLOntologyManager() {
		return this.owlOntologyManager;
	}

	private String getShortForm(OWLClass cls) {
		String ret = this.shortFormMap.get(cls);
		if (ret == null) {
			IRI iri = cls.getIRI();
			ret = iri.getFragment();
		}
		return ret;
	}

	public Map<OWLClass, String> getShortFormMap() {
		return Collections.unmodifiableMap(this.shortFormMap);
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
		getView().addButtonFirstListener(this, actionFirst);
		getView().addButtonPreviousListener(this, actionPrevious);
		getView().addButtonNextListener(this, actionNext);
		getView().addButtonLastListener(this, actionLast);
		getView().addButtonSaveListener(this, actionSave);
		getView().addComboBoxClass00Listener(this, actionClass00Selected);
		getView().addComboBoxClass01Listener(this, actionClass01Selected);
		getView().addComboBoxOntology00Listener(this, actionOntology00Selected);
		getView().addComboBoxOntology01Listener(this, actionOntology01Selected);
		getView().addCheckBoxClassName00Listener(this,
				actionCheckBoxClassName00);
		getView().addCheckBoxClassName01Listener(this,
				actionCheckBoxClassName01);
		getView().addButtonShowStatInfoListener(this, actionShowStatInfo);

		reset();
		reloadOntologies();

		executeActionOntology00Selected(0);
		executeActionOntology01Selected(0);
	}

	private VarSelectionController initVarWindow(Set<String> originalVariables,
			Goal goal) {
		VarSelectionController ret = new VarSelectionController(
				new VarSelectionView(new VarSelectionModel(originalVariables,
						this.mapIdLabel, goal)));
		ret.addAcceptVarButtonListener(this, actionAcceptVar);
		ret.addRejectVarButtonListener(this, actionRejectVar);
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

	/**
	 * Prints a substitution set (i.e. a set of atoms) as a conjunction of atoms
	 * in the krss format. Used in Translator.
	 * 
	 * @return the string representation of a substitution set
	 */
	public String printSetOfSubsumers(Collection<FAtom> setOfSubsumers) {

		StringBuffer sbuf = new StringBuffer();

		if (setOfSubsumers.isEmpty()) {

			sbuf.append(KRSSKeyword.top);
			sbuf.append(KRSSKeyword.space);

		} else if (setOfSubsumers.size() == 1) {

			FAtom atom = setOfSubsumers.iterator().next();
			sbuf.append(printSubstitution(atom));

		} else {

			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			sbuf.append(KRSSKeyword.space);

			for (FAtom atom : setOfSubsumers) {
				sbuf.append(KRSSKeyword.space);
				sbuf.append(printSubstitution(atom));
				sbuf.append(KRSSKeyword.space);
			}

			sbuf.append(KRSSKeyword.space);
			sbuf.append(KRSSKeyword.close);
		}
		return sbuf.toString();
	}

	private String printSubstitution(FAtom atom) {
		StringBuffer sbuf = new StringBuffer();
		if (atom.isRoot() && !atom.getChild().isUserVariable()) {
			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.some);
			sbuf.append(KRSSKeyword.space);
			sbuf.append(atom.getName());
			sbuf.append(KRSSKeyword.space);
			FAtom child = atom.getChild();
			if (child.isVariable() && !child.isUserVariable()) {
				sbuf.append(printSetOfSubsumers(child.getSetOfSubsumers()));
			} else {
				sbuf.append(child.getName());
			}
			sbuf.append(KRSSKeyword.close);
		} else {
			sbuf.append(atom.toString());
		}
		return sbuf.toString();
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
		for (OWLClass cls : this.shortFormMap.keySet()) {
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
		setUnifierButtons(false);

		getView().setComboBoxClassName00Enabled(false);
		getView().setComboBoxClassName01Enabled(false);
		getView().setButtonSelectVariablesEnabled(false);
		this.unifierIndex = -1;
		this.allUnifiersFound = false;
		getView().getUnifier().setText("");
		getView().getUnifierId().setText(initialUnifierIdText);
	}

	public void setShortFormMap(Map<OWLClass, String> map) {
		if (map == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.shortFormMap = map;
		recomputeShortForm();
	}

	private void setUnifierButtons(boolean b) {
		getView().setButtonFirstEnabled(b);
		getView().setButtonPreviousEnabled(b);
		getView().setButtonNextEnabled(b);
		getView().setButtonLastEnabled(b);
		getView().setButtonSaveEnabled(b);
		getView().setButtonShowStatInfoEnabled(b);
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
						ret.append(getLabel(token));
						if (stok.hasMoreTokens()) {
							ret.append(KRSSKeyword.space);
						}
					}
				}
				ret.append(KRSSKeyword.newLine);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return ret.toString();
	}

	public String toKRSS(Set<Equation> set) {
		Set<Equation> unif = new HashSet<Equation>();
		unif.addAll(set);
		StringBuffer sbuf = new StringBuffer();
		for (Equation eq : set) {
			Atom leftPart = getModel().getAtomManager().get(eq.getLeft());

			Set<FAtom> right = new HashSet<FAtom>();
			for (Integer atomId : eq.getRight()) {
				right.add((FAtom) getModel().getAtomManager().get(atomId));
			}

			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.define_concept);
			sbuf.append(KRSSKeyword.space);
			sbuf.append(leftPart.getId());
			sbuf.append(KRSSKeyword.space);

			sbuf.append(printSetOfSubsumers(right));
			sbuf.append(KRSSKeyword.space);
			sbuf.append(KRSSKeyword.close);
			sbuf.append(KRSSKeyword.space);
			sbuf.append(KRSSKeyword.newLine);
		}
		return sbuf.toString();

	}

	private void updateUnifier() {
		if (getModel().getUnifierList().size() > 0) {
			getView().getUnifier().setText(
					showUnifier(toKRSS(getModel().getUnifierList().get(
							this.unifierIndex))));
		} else {
			getView().getUnifier().setText("[not unifiable]");
		}
		getView().getUnifierId().setText(
				" "
						+ (getModel().getUnifierList().isEmpty() ? 0
								: (this.unifierIndex + 1)) + " ");
		if (this.unifierIndex == 0) {
			getView().setButtonPreviousEnabled(false);
			getView().setButtonFirstEnabled(false);
		} else {
			getView().setButtonPreviousEnabled(true);
			getView().setButtonFirstEnabled(true);
		}
		if (this.allUnifiersFound
				&& this.unifierIndex >= getModel().getUnifierList().size() - 1) {
			getView().setButtonNextEnabled(false);
			getView().setButtonLastEnabled(false);
		} else {
			getView().setButtonNextEnabled(true);
			getView().setButtonLastEnabled(true);
		}
	}

}
