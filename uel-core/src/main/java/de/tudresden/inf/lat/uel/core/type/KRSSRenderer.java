package de.tudresden.inf.lat.uel.core.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;

public class KRSSRenderer {

	private final AtomManager atomManager;
	private final Map<String, String> shortFormMap;

	public KRSSRenderer(AtomManager atomManager, Map<String, String> shortFormMap) {
		this.atomManager = atomManager;
		this.shortFormMap = shortFormMap;
	}

	private void appendAtom(StringBuffer sbuf, Integer atomId, Set<Definition> background,
			boolean restrictToUserVariables) {
		if (atomManager.getExistentialRestrictions().contains(atomId)) {
			Integer childId = atomManager.getChild(atomId);

			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.some);
			sbuf.append(KRSSKeyword.space);
			String roleName = atomManager.printRoleName(atomId);
			if (shortFormMap != null) {
				roleName = getShortForm(roleName, true);
			}
			sbuf.append(roleName);
			sbuf.append(KRSSKeyword.space);
			if (restrictToUserVariables && atomManager.getFlatteningVariables().contains(childId)) {
				appendConjunction(sbuf, getDefinition(childId, background), background, restrictToUserVariables);
			} else {
				appendName(sbuf, childId, true);
			}
			sbuf.append(KRSSKeyword.close);
		} else {
			appendName(sbuf, atomId, true);
		}
	}

	private void appendAxioms(StringBuffer sbuf, Set<? extends Axiom> axioms) {
		for (Axiom axiom : axioms) {
			appendConjunction(sbuf, axiom.getLeft(), Collections.<Definition> emptySet(), false);
			sbuf.append(" ");
			sbuf.append(axiom.getConnective());
			sbuf.append(" ");
			appendConjunction(sbuf, axiom.getRight(), Collections.<Definition> emptySet(), false);
			sbuf.append(System.lineSeparator());
			sbuf.append(System.lineSeparator());
		}
	}

	private void appendConjunction(StringBuffer sbuf, Set<Integer> atomIds, Set<Definition> background,
			boolean restrictToUserVariables) {

		if (atomIds.isEmpty()) {

			sbuf.append(KRSSKeyword.top);

		} else if (atomIds.size() == 1) {

			appendAtom(sbuf, atomIds.iterator().next(), background, restrictToUserVariables);

		} else {

			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			sbuf.append(KRSSKeyword.space);

			for (Integer atomId : atomIds) {
				appendAtom(sbuf, atomId, background, restrictToUserVariables);
				sbuf.append(KRSSKeyword.space);
			}

			sbuf.setLength(sbuf.length() - KRSSKeyword.space.length());
			sbuf.append(KRSSKeyword.close);
		}
	}

	private void appendName(StringBuffer sbuf, Integer atomId, boolean quotes) {
		sbuf.append(getName(atomId, true));
	}

	private Set<Integer> getDefinition(Integer atomId, Set<Definition> background) {
		for (Definition definition : background) {
			if (definition.getDefiniendum().equals(atomId)) {
				return definition.getRight();
			}
		}
		throw new IllegalArgumentException("Atom has no definition.");
	}

	public String getName(Integer atomId, boolean quotes) {
		String name = atomManager.printConceptName(atomId);
		if (shortFormMap != null) {
			name = getShortForm(name, quotes);
		}
		return name;
	}

	private String getShortForm(String id, boolean quotes) {
		boolean alias = false;
		String label = id;
		if (id.endsWith(AtomManager.UNDEF_SUFFIX)) {
			label = id.substring(0, id.length() - AtomManager.UNDEF_SUFFIX.length());
		}

		String str = shortFormMap.get(label);
		if (str != null) {
			alias = true;
			label = str;
		}
		if (id.endsWith(AtomManager.UNDEF_SUFFIX)) {
			label += AtomManager.UNDEF_SUFFIX;
		}
		if (alias) {
			if (quotes) {
				return "\"" + label + "\"";
			} else
				return label;
		} else {
			return IRI.create(label).getShortForm();
		}
	}

	public String printAtom(Integer atomId) {
		return printAtom(atomId, new HashSet<Definition>(), false);
	}

	public String printAtom(Integer atomId, Set<Definition> background, boolean restrictToUserVariables) {
		StringBuffer sbuf = new StringBuffer();
		appendAtom(sbuf, atomId, background, restrictToUserVariables);
		return sbuf.toString();
	}

	public String printConjunction(Set<Integer> atomIds) {
		StringBuffer sbuf = new StringBuffer();
		appendConjunction(sbuf, atomIds, Collections.<Definition> emptySet(), false);
		return sbuf.toString();
	}

	public String printDefinitions(Set<Definition> definitions, boolean restrictToUserVariables) {
		return printDefinitions(definitions, definitions, restrictToUserVariables);
	}

	public String printDefinitions(Set<Definition> definitions, Set<Definition> background,
			boolean restrictToUserVariables) {
		StringBuffer sbuf = new StringBuffer();
		for (Definition definition : definitions) {
			if (!restrictToUserVariables || atomManager.getUserVariables().contains(definition.getDefiniendum())) {
				sbuf.append(KRSSKeyword.open);
				if (definition.isPrimitive()) {
					sbuf.append(KRSSKeyword.define_primitive_concept);
				} else {
					sbuf.append(KRSSKeyword.define_concept);
				}
				sbuf.append(KRSSKeyword.space);
				appendAtom(sbuf, definition.getDefiniendum(), background, restrictToUserVariables);
				sbuf.append(KRSSKeyword.space);
				appendConjunction(sbuf, definition.getRight(), background, restrictToUserVariables);
				sbuf.append(KRSSKeyword.close);
				sbuf.append(System.lineSeparator());
				sbuf.append(System.lineSeparator());
			}
		}
		return sbuf.toString();
	}

	public String printGoal(Goal input) {
		StringBuffer sbuf = new StringBuffer();
		appendAxioms(sbuf, input.getDefinitions());
		appendAxioms(sbuf, input.getEquations());
		appendAxioms(sbuf, input.getSubsumptions());
		appendAxioms(sbuf, input.getDisequations());
		appendAxioms(sbuf, input.getDissubsumptions());
		return sbuf.toString();
	}
}
