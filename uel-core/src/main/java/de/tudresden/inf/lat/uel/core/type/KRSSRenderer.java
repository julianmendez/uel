package de.tudresden.inf.lat.uel.core.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.SmallEquation;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;

public class KRSSRenderer {

	private final AtomManager atomManager;
	private final Set<Integer> auxiliaryVariables;
	private final Map<String, String> mapIdLabel;
	private final Set<Integer> userVariables;

	public KRSSRenderer(AtomManager atomManager, Set<Integer> userVariables, Set<Integer> auxiliaryVariables,
			Map<String, String> mapIdLabel) {
		this.atomManager = atomManager;
		this.userVariables = userVariables;
		if (auxiliaryVariables == null) {
			this.auxiliaryVariables = new HashSet<Integer>();
		} else {
			this.auxiliaryVariables = auxiliaryVariables;
		}
		this.mapIdLabel = mapIdLabel;
	}

	private void appendAtom(StringBuffer sbuf, Atom atom) {
		appendAtom(sbuf, atom, new HashSet<Equation>(), false);
	}

	private void appendAtom(StringBuffer sbuf, Atom atom, Set<Equation> equations, boolean restrictToUserVariables) {
		if (atom.isExistentialRestriction()) {
			ConceptName child = atom.getConceptName();
			Integer conceptId = atomManager.getAtoms().getIndex(child);

			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.some);
			sbuf.append(KRSSKeyword.space);
			String roleName = atomManager.printRoleName(atom);
			if (mapIdLabel != null) {
				roleName = getLabel(roleName);
			}
			sbuf.append(roleName);
			sbuf.append(KRSSKeyword.space);
			if (restrictToUserVariables && auxiliaryVariables.contains(conceptId)) {
				appendConjunction(sbuf, getSetOfSubsumers(child, equations), equations, restrictToUserVariables);
			} else {
				appendName(sbuf, child);
			}
			sbuf.append(KRSSKeyword.close);
		} else {
			appendName(sbuf, (ConceptName) atom);
		}
	}

	private void appendAtomId(StringBuffer sbuf, Integer atomId) {
		appendAtom(sbuf, toAtom(atomId));
	}

	private void appendAtomId(StringBuffer sbuf, Integer atomId, Set<Equation> equations,
			boolean restrictToUserVariables) {
		appendAtom(sbuf, toAtom(atomId), equations, restrictToUserVariables);
	}

	private void appendConjunction(StringBuffer sbuf, Collection<Atom> atoms) {
		appendConjunction(sbuf, atoms, new HashSet<Equation>(), false);
	}

	private void appendConjunction(StringBuffer sbuf, Collection<Atom> atoms, Set<Equation> equations,
			boolean restrictToUserVariables) {

		if (atoms.isEmpty()) {

			sbuf.append(KRSSKeyword.top);

		} else if (atoms.size() == 1) {

			Atom atom = atoms.iterator().next();
			appendAtom(sbuf, atom, equations, restrictToUserVariables);

		} else {

			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			sbuf.append(KRSSKeyword.space);

			for (Atom atom : atoms) {
				appendAtom(sbuf, atom, equations, restrictToUserVariables);
				sbuf.append(KRSSKeyword.space);
			}

			sbuf.setLength(sbuf.length() - KRSSKeyword.space.length());
			sbuf.append(KRSSKeyword.close);
		}
	}

	private void appendConjunctionIds(StringBuffer sbuf, Collection<Integer> atomIds) {
		appendConjunction(sbuf, toAtoms(atomIds), new HashSet<Equation>(), false);
	}

	private void appendConjunctionIds(StringBuffer sbuf, Collection<Integer> atomIds, Set<Equation> equations,
			boolean restrictToUserVariables) {
		appendConjunction(sbuf, toAtoms(atomIds), equations, restrictToUserVariables);
	}

	public void appendCustomEquations(StringBuffer sbuf, Collection<Equation> equations, String sep) {
		for (Equation eq : equations) {
			appendAtomId(sbuf, eq.getLeft());
			sbuf.append(sep);
			appendConjunctionIds(sbuf, eq.getRight());
			sbuf.append(System.lineSeparator());
			sbuf.append(System.lineSeparator());
		}
	}

	public void appendCustomSmallEquations(StringBuffer sbuf, Collection<SmallEquation> equations, String sep) {
		for (SmallEquation eq : equations) {
			appendAtomId(sbuf, eq.getLeft());
			sbuf.append(sep);
			appendAtomId(sbuf, eq.getRight());
			sbuf.append(System.lineSeparator());
			sbuf.append(System.lineSeparator());
		}

	}

	private void appendName(StringBuffer sbuf, ConceptName child) {
		sbuf.append(getName(child, true));
	}

	private String getLabel(String id) {
		return getLabel(id, true);
	}

	public String getLabel(String id, boolean quotes) {
		boolean alias = false;
		String label = id;
		if (id.endsWith(AtomManager.UNDEF_SUFFIX)) {
			label = id.substring(0, id.length() - AtomManager.UNDEF_SUFFIX.length());
		}

		String str = this.mapIdLabel.get(label);
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

	private String getName(Atom atom, boolean quotes) {
		String name = atomManager.printConceptName(atom);
		if (mapIdLabel != null) {
			name = getLabel(name, quotes);
		}
		return name;
	}

	public String getName(Integer id, boolean quotes) {
		return getName(atomManager.getAtom(id), quotes);
	}

	private Collection<Atom> getSetOfSubsumers(Atom atom, Set<Equation> equations) {
		Integer atomId = atomManager.getAtoms().addAndGetIndex(atom);
		for (Equation equation : equations) {
			if (equation.getLeft().equals(atomId)) {
				return toAtoms(equation.getRight());
			}
		}
		throw new IllegalArgumentException("Atom has no definition.");
	}

	public String printAtom(Integer atomId) {
		StringBuffer sbuf = new StringBuffer();
		appendAtomId(sbuf, atomId);
		return sbuf.toString();
	}

	public String printConjunction(Collection<Integer> conjuncts) {
		StringBuffer sbuf = new StringBuffer();
		appendConjunction(sbuf, toAtoms(conjuncts), new HashSet<Equation>(), false);
		return sbuf.toString();
	}

	public String printDefinitions(Set<Equation> definitions) {
		return printEquations(definitions, false);
	}

	private String printEquations(Set<Equation> equations, boolean restrictToUserVariables) {
		StringBuffer sbuf = new StringBuffer();
		for (Equation eq : equations) {
			if (!restrictToUserVariables || userVariables.contains(eq.getLeft())) {
				sbuf.append(KRSSKeyword.open);
				if (eq.isPrimitive()) {
					sbuf.append(KRSSKeyword.define_primitive_concept);
				} else {
					sbuf.append(KRSSKeyword.define_concept);
				}
				sbuf.append(KRSSKeyword.space);
				appendAtomId(sbuf, eq.getLeft(), equations, restrictToUserVariables);
				sbuf.append(KRSSKeyword.space);
				appendConjunctionIds(sbuf, eq.getRight(), equations, restrictToUserVariables);
				sbuf.append(KRSSKeyword.close);
				sbuf.append(System.lineSeparator());
				sbuf.append(System.lineSeparator());
			}
		}
		return sbuf.toString();
	}

	public String printUnifier(Set<Equation> equations) {
		return printEquations(equations, true);
	}

	private Atom toAtom(Integer atomId) {
		return atomManager.getAtoms().get(atomId);
	}

	private Collection<Atom> toAtoms(Collection<Integer> atomIds) {
		Set<Atom> ret = new HashSet<Atom>();
		for (Integer atomId : atomIds) {
			ret.add(toAtom(atomId));
		}
		return ret;
	}

}
