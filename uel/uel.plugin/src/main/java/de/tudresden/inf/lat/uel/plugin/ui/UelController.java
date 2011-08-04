package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
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

import org.coode.owlapi.owlxml.renderer.OWLXMLRenderer;
import org.coode.owlapi.rdf.rdfxml.RDFXMLRenderer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tudresden.inf.lat.uel.ontmanager.Ontology;
import de.tudresden.inf.lat.uel.plugin.processor.UelProcessor;
import de.uulm.ecs.ai.owlapi.krssrenderer.KRSS2OWLSyntaxRenderer;

/**
 * This class is a controller for UEL.
 * 
 * @author Julian Mendez
 */
public class UelController implements ActionListener {

	private static final String actionAcceptVar = "accept var";
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
	private static final String extension_krss = ".krss";
	private static final String extension_owl = ".owl";
	private static final String extension_rdf = ".rdf";
	private static final String initialUnifierIdText = " 0 ";

	private boolean allUnifiersFound = false;
	private List<LabelId> classList00 = null;
	private List<LabelId> classList01 = null;
	private Map<String, OWLClass> mapIdClass = new HashMap<String, OWLClass>();
	private Map<String, String> mapIdLabel = new HashMap<String, String>();
	private Ontology ontology00 = null;
	private Ontology ontology01 = null;
	private List<String> ontologyList = new ArrayList<String>();
	private Map<String, Ontology> ontologyMap = new HashMap<String, Ontology>();
	private OWLOntologyManager owlOntologyManager = null;
	private Map<String, OWLOntology> owlOntologyMap = new HashMap<String, OWLOntology>();
	private Map<OWLClass, String> shortFormMap = new HashMap<OWLClass, String>();
	private int unifierIndex = -1;
	private VarSelectionController varWindow = null;
	private UelView view = null;

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

	/**
	 * Action handler.
	 */
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
		} else {
			throw new IllegalStateException();
		}
	}

	private void executeActionAcceptVar() {
		getModel().clearCandidates();
		getModel().addAll(this.varWindow.getView().getModel().getVariables());
		this.varWindow.close();

		setUnifierButtons(false);
		getView().setButtonNextEnabled(true);
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
			this.ontology00 = this.ontologyMap.get(ontologyId);

			this.classList00 = getClassNames(this.owlOntologyMap
					.get(ontologyId));
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
			this.ontology01 = this.ontologyMap.get(ontologyId);

			this.classList01 = getClassNames(this.owlOntologyMap
					.get(ontologyId));
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
				String unifier = getModel().getUnifierList().get(
						this.unifierIndex);
				OWLOntology owlOntology = parseUnifier(unifier);
				if (file.getName().endsWith(extension_rdf)) {
					unifier = this.renderRDF(owlOntology);
				} else if (file.getName().endsWith(extension_owl)) {
					unifier = this.renderOWL(owlOntology);
				} else if (file.getName().endsWith(extension_krss)) {
					unifier = this.renderKRSS(owlOntology);
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
		getModel().loadOntology(this.ontology00);
		getModel().loadOntology(this.ontology01);

		Set<String> classSet = new HashSet<String>();
		classSet.add(this.classList00.get(getView().getSelectedClassName00())
				.getId());
		classSet.add(this.classList01.get(getView().getSelectedClassName01())
				.getId());
		try {
			this.varWindow = initVarWindow(classSet);
			this.varWindow.open();
		} catch (RuntimeException e) {
		}
		this.unifierIndex = -1;
		this.allUnifiersFound = false;
		getView().getUnifierId().setText(initialUnifierIdText);
	}

	private List<LabelId> getClassNames(OWLOntology ontology) {
		OWLClass nothing = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLNothing();
		OWLClass thing = ontology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLThing();

		Set<OWLClass> set = new TreeSet<OWLClass>();
		set.addAll(ontology.getClassesInSignature());
		set.remove(nothing);
		set.remove(thing);

		List<LabelId> ret = new ArrayList<LabelId>();
		ret.add(new LabelId(getShortForm(nothing), nothing.toStringID()));
		ret.add(new LabelId(getShortForm(thing), thing.toStringID()));
		for (OWLClass cls : set) {
			ret.add(new LabelId(getShortForm(cls), getId(cls)));
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
			ret = cls.toStringID();
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

		reset();
		reloadOntologies();

		executeActionOntology00Selected(0);
		executeActionOntology01Selected(0);
	}

	private VarSelectionController initVarWindow(Set<String> originalVariables) {
		VarSelectionController ret = new VarSelectionController(
				new VarSelectionView(new VarSelectionModel(originalVariables,
						this.mapIdLabel, getModel().getOntology())));
		ret.addAcceptVarButtonListener(this, actionAcceptVar);
		ret.addRejectVarButtonListener(this, actionRejectVar);
		return ret;
	}

	private void loadOntology(OWLOntology owlOntology) {
		String ontologyId = owlOntology.getOntologyID().toString();
		if (!this.ontologyMap.containsKey(ontologyId)) {
			this.ontologyMap.put(ontologyId,
					getModel().createOntology(owlOntology));
			this.owlOntologyMap.put(ontologyId, owlOntology);
			processMapIdClass(owlOntology);
			Set<String> set = new TreeSet<String>();
			set.addAll(this.owlOntologyMap.keySet());
			this.ontologyList.clear();
			this.ontologyList.addAll(set);
			getView().reloadOntologies(this.ontologyList);
		}
	}

	private OWLOntology parseUnifier(String krss)
			throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = OWLManager
				.createOWLOntologyManager();
		ByteArrayInputStream input = new ByteArrayInputStream(krss.getBytes());
		return ontologyManager.loadOntologyFromOntologyDocument(input);
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

	private String renderKRSS(OWLOntology owlOntology)
			throws OWLRendererException {
		StringWriter writer = new StringWriter();
		KRSS2OWLSyntaxRenderer renderer = new KRSS2OWLSyntaxRenderer(
				owlOntology.getOWLOntologyManager());
		renderer.render(owlOntology, writer);
		writer.flush();
		return writer.toString();
	}

	private String renderOWL(OWLOntology owlOntology)
			throws OWLRendererException {
		StringWriter writer = new StringWriter();
		OWLXMLRenderer renderer = new OWLXMLRenderer(
				owlOntology.getOWLOntologyManager());
		renderer.render(owlOntology, writer);
		writer.flush();
		return writer.toString();
	}

	private String renderRDF(OWLOntology owlOntology) throws IOException {
		StringWriter writer = new StringWriter();
		RDFXMLRenderer renderer = new RDFXMLRenderer(
				owlOntology.getOWLOntologyManager(), owlOntology, writer);
		renderer.render();
		writer.flush();
		return writer.toString();
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

	private void updateUnifier() {
		if (getModel().getUnifierList().size() > 0) {
			getView().getUnifier().setText(
					showUnifier(getModel().getUnifierList().get(
							this.unifierIndex)));
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
