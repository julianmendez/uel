package de.tudresden.inf.lat.uel.core.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

public class KRSSRenderer {

	public interface Replacer {
		String replace(String input);
	}

	private final Replacer aliasReplacer;
	private final AtomManager atomManager;
	private final Set<Integer> auxiliaryVariables;
	private final Set<Integer> userVariables;

	public KRSSRenderer(AtomManager atomManager, Set<Integer> userVariables,
			Set<Integer> auxiliaryVariables, Replacer aliasReplacer) {
		this.atomManager = atomManager;
		this.userVariables = userVariables;
		if (auxiliaryVariables == null) {
			this.auxiliaryVariables = new HashSet<Integer>();
		} else {
			this.auxiliaryVariables = auxiliaryVariables;
		}
		this.aliasReplacer = aliasReplacer;
	}

	private void appendName(StringBuffer sbuf, ConceptName child) {
		String childName = atomManager.getConceptName(child.getConceptNameId());
		if (aliasReplacer != null) {
			childName = aliasReplacer.replace(childName);
		}
		sbuf.append(childName);
	}

	private Collection<Atom> getSetOfSubsumers(Atom atom,
			Set<Equation> equations) {
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
		toKRSS(sbuf, atomManager.getAtoms().get(atomId),
				new HashSet<Equation>(), false);
		return sbuf.toString();
	}

	public String printConjunction(Collection<Integer> conjuncts) {
		StringBuffer sbuf = new StringBuffer();
		toKRSS(sbuf, toAtoms(conjuncts), new HashSet<Equation>(), false);
		return sbuf.toString();
	}

	public String printDefinitions(Set<Equation> definitions) {
		return printEquations(definitions, false);
	}

	private String printEquations(Set<Equation> equations,
			boolean restrictToUserVariables) {
		StringBuffer sbuf = new StringBuffer();
		for (Equation eq : equations) {
			if (!restrictToUserVariables
					|| userVariables.contains(eq.getLeft())) {
				Atom leftPart = atomManager.getAtoms().get(eq.getLeft());
				Collection<Atom> rightPart = toAtoms(eq.getRight());

				sbuf.append(KRSSKeyword.newLine);
				sbuf.append(KRSSKeyword.open);
				if (eq.isPrimitive()) {
					sbuf.append(KRSSKeyword.define_primitive_concept);
				} else {
					sbuf.append(KRSSKeyword.define_concept);
				}
				sbuf.append(KRSSKeyword.space);
				toKRSS(sbuf, leftPart, equations, restrictToUserVariables);
				sbuf.append(KRSSKeyword.space);
				toKRSS(sbuf, rightPart, equations, restrictToUserVariables);
				sbuf.append(KRSSKeyword.space);
				sbuf.append(KRSSKeyword.close);
				sbuf.append(KRSSKeyword.newLine);
			}
		}
		return sbuf.toString();
	}

	public String printUnifier(Set<Equation> equations) {
		return printEquations(equations, true);
	}

	private Collection<Atom> toAtoms(Collection<Integer> atomIds) {
		Set<Atom> ret = new HashSet<Atom>();
		for (Integer id : atomIds) {
			ret.add(atomManager.getAtoms().get(id));
		}
		return ret;
	}

	private void toKRSS(StringBuffer sbuf, Atom atom, Set<Equation> equations,
			boolean restrictToUserVariables) {
		if (atom.isExistentialRestriction()) {
			ConceptName child = ((ExistentialRestriction) atom).getChild();
			Integer conceptId = atomManager.getAtoms().getIndex(child);

			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.some);
			sbuf.append(KRSSKeyword.space);
			String roleName = atomManager
					.getRoleName(((ExistentialRestriction) atom).getRoleId());
			if (aliasReplacer != null) {
				roleName = aliasReplacer.replace(roleName);
			}
			sbuf.append(roleName);
			sbuf.append(KRSSKeyword.space);
			if (restrictToUserVariables
					&& auxiliaryVariables.contains(conceptId)) {
				toKRSS(sbuf, getSetOfSubsumers(child, equations), equations,
						restrictToUserVariables);
			} else {
				appendName(sbuf, child);
			}
			sbuf.append(KRSSKeyword.close);
		} else {
			appendName(sbuf, (ConceptName) atom);
		}
	}

	/**
	 * Prints a substitution set (i.e. a set of atoms) as a conjunction of atoms
	 * in the krss format. Used in Translator.
	 * 
	 * @return the string representation of a substitution set
	 */
	private void toKRSS(StringBuffer sbuf, Collection<Atom> setOfSubsumers,
			Set<Equation> equations, boolean restrictToUserVariables) {

		if (setOfSubsumers.isEmpty()) {

			sbuf.append(KRSSKeyword.top);

		} else if (setOfSubsumers.size() == 1) {

			Atom atom = setOfSubsumers.iterator().next();
			toKRSS(sbuf, atom, equations, restrictToUserVariables);

		} else {

			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			sbuf.append(KRSSKeyword.space);

			for (Atom atom : setOfSubsumers) {
				toKRSS(sbuf, atom, equations, restrictToUserVariables);
				sbuf.append(KRSSKeyword.space);
			}

			sbuf.append(KRSSKeyword.close);
		}
	}

}
