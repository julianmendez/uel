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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;

import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.tudresden.inf.lat.uel.plugin.processor.UelProcessor;
import de.tudresden.inf.lat.uel.sat.type.Goal;
import de.tudresden.inf.lat.uel.sat.type.SatAtom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;

/**
 * 
 * @author Julian Mendez
 */
public class UnifierController implements ActionListener {

	private static final String actionFirst = "first";
	private static final String actionLast = "last";
	private static final String actionNext = "next";
	private static final String actionPrevious = "previous";
	private static final String actionSave = "save";
	private static final String actionShowStatInfo = "show statistic info";
	private static final String quotes = "\"";

	private boolean allUnifiersFound = false;
	private final Map<String, String> mapIdLabel;
	private StatInfo statInfo = null;
	private int unifierIndex = -1;
	private UnifierView view;

	public UnifierController(UnifierView view, Map<String, String> labels) {
		this.view = view;
		this.mapIdLabel = labels;
		init();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String cmd = e.getActionCommand();
		if (cmd.equals(actionFirst)) {
			executeActionFirst();
		} else if (cmd.equals(actionPrevious)) {
			executeActionPrevious();
		} else if (cmd.equals(actionNext)) {
			executeActionNext();
		} else if (cmd.equals(actionLast)) {
			executeActionLast();
		} else if (cmd.equals(actionSave)) {
			executeActionSave();
		} else if (cmd.equals(actionShowStatInfo)) {
			executeActionShowStatInfo();
		}
	}

	private void executeActionFirst() {
		getView().setUnifierButtons(true);

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
		getView().setUnifierButtons(true);

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

	private void executeActionPrevious() {
		getView().setUnifierButtons(true);

		this.unifierIndex--;
		if (this.unifierIndex < 0) {
			this.unifierIndex = 0;
		}
		updateUnifier();
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
				writer.close();
			} catch (OWLRendererException e) {
				throw new RuntimeException(e);
			} catch (OWLOntologyCreationException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void executeActionShowStatInfo() {
		StatInfoController statInfoWindow = new StatInfoController(
				new StatInfoView(this.statInfo));
		statInfoWindow.open();
	}

	private String getLabel(String candidateId) {
		String ret = candidateId;
		if (candidateId.endsWith(Goal.UNDEF_SUFFIX)) {
			ret = candidateId.substring(0, candidateId.length()
					- Goal.UNDEF_SUFFIX.length());
		}

		String str = this.mapIdLabel.get(ret);
		if (str != null) {
			ret = str;
		}
		if (candidateId.endsWith(Goal.UNDEF_SUFFIX)) {
			ret += Goal.UNDEF_SUFFIX;
		}
		return ret;
	}

	public UelProcessor getModel() {
		return getView().getModel();
	}

	private Collection<SatAtom> getSetOfSubsumers(SatAtom atom) {
		Collection<Integer> list = getModel().getTranslator()
				.getSetOfSubsumers(
						getModel().getAtomManager().addAndGetIndex(atom));
		List<SatAtom> ret = new ArrayList<SatAtom>();
		for (Integer id : list) {
			ret.add(getModel().getAtomManager().get(id));
		}
		return ret;
	}

	public UnifierView getView() {
		return this.view;
	}

	private void init() {
		getView().addButtonFirstListener(this, actionFirst);
		getView().addButtonPreviousListener(this, actionPrevious);
		getView().addButtonNextListener(this, actionNext);
		getView().addButtonLastListener(this, actionLast);
		getView().addButtonSaveListener(this, actionSave);
		getView().addButtonShowStatInfoListener(this, actionShowStatInfo);
	}

	/**
	 * Prints a substitution set (i.e. a set of atoms) as a conjunction of atoms
	 * in the krss format. Used in Translator.
	 * 
	 * @return the string representation of a substitution set
	 */
	public String printSetOfSubsumers(Collection<SatAtom> setOfSubsumers) {

		StringBuffer sbuf = new StringBuffer();

		if (setOfSubsumers.isEmpty()) {

			sbuf.append(KRSSKeyword.top);
			sbuf.append(KRSSKeyword.space);

		} else if (setOfSubsumers.size() == 1) {

			SatAtom atom = setOfSubsumers.iterator().next();
			sbuf.append(printSubstitution(atom));

		} else {

			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			sbuf.append(KRSSKeyword.space);

			for (SatAtom atom : setOfSubsumers) {
				sbuf.append(KRSSKeyword.space);
				sbuf.append(printSubstitution(atom));
				sbuf.append(KRSSKeyword.space);
			}

			sbuf.append(KRSSKeyword.space);
			sbuf.append(KRSSKeyword.close);
		}
		return sbuf.toString();
	}

	private String printSubstitution(SatAtom atom) {
		StringBuffer sbuf = new StringBuffer();
		if (atom.isExistentialRestriction()
				&& !(atom.asExistentialRestriction().getChild().isConceptName() && atom
						.asExistentialRestriction().getChild().asConceptName()
						.isUserVariable())) {
			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.some);
			sbuf.append(KRSSKeyword.space);
			sbuf.append(atom.getName());
			sbuf.append(KRSSKeyword.space);
			SatAtom child = atom.asExistentialRestriction().getChild();
			if (child.isConceptName() && child.asConceptName().isVariable()
					&& !child.asConceptName().isUserVariable()) {
				sbuf.append(printSetOfSubsumers(getSetOfSubsumers(child
						.asConceptName())));
			} else {
				sbuf.append(child.getName());
			}
			sbuf.append(KRSSKeyword.close);
		} else {
			sbuf.append(atom.getId());
		}
		return sbuf.toString();
	}

	public void setStatInfo(StatInfo info) {
		if (info == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.statInfo = info;
	}

	private String showLabels(String text) {
		StringBuffer ret = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new StringReader(
					text.replace(KRSSKeyword.close, KRSSKeyword.space
							+ KRSSKeyword.close)));
			String line = new String();
			while (line != null) {
				line = reader.readLine();
				if (line != null) {
					StringTokenizer stok = new StringTokenizer(line);
					while (stok.hasMoreTokens()) {
						String token = stok.nextToken();
						String label = getLabel(token);
						if (label.equals(token)) {
							ret.append(token);
						} else {
							ret.append(quotes);
							ret.append(label);
							ret.append(quotes);
						}
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
			SatAtom leftPart = getModel().getAtomManager().get(eq.getLeft());

			Set<SatAtom> right = new HashSet<SatAtom>();
			for (Integer atomId : eq.getRight()) {
				right.add(getModel().getAtomManager().get(atomId));
			}

			sbuf.append(KRSSKeyword.newLine);
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
					showLabels(toKRSS(getModel().getUnifierList().get(
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
