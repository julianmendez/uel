package de.tudresden.inf.lat.uel.core.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

public class UnifierKRSSRenderer {

	private final AtomManager atomManager;
	private final Set<Integer> userVariables;
	private final Set<Integer> auxiliaryVariables;

	public UnifierKRSSRenderer(AtomManager atomManager,
			Set<Integer> userVariables, Set<Integer> auxiliaryVariables) {
		this.atomManager = atomManager;
		this.userVariables = userVariables;
		this.auxiliaryVariables = auxiliaryVariables;
	}

	public String printUnifier(Set<Equation> equations) {
		StringBuffer sbuf = new StringBuffer();
		for (Equation eq : equations) {
			if (userVariables.contains(eq.getLeft())) {
				Atom leftPart = atomManager.getAtoms().get(eq.getLeft());

				Set<Atom> right = new HashSet<Atom>();
				for (Integer atomId : eq.getRight()) {
					right.add(atomManager.getAtoms().get(atomId));
				}

				sbuf.append(KRSSKeyword.newLine);
				sbuf.append(KRSSKeyword.open);
				sbuf.append(KRSSKeyword.define_concept);
				sbuf.append(KRSSKeyword.space);
				Integer conceptId = atomManager.getAtoms().getIndex(leftPart);
				String conceptName = atomManager.getConceptName(conceptId);
				sbuf.append(conceptName);
				sbuf.append(KRSSKeyword.space);

				toKRSS(sbuf, right, equations);
				sbuf.append(KRSSKeyword.space);
				sbuf.append(KRSSKeyword.close);
				sbuf.append(KRSSKeyword.space);
				sbuf.append(KRSSKeyword.newLine);
			}
		}
		return sbuf.toString();

	}

	/**
	 * Prints a substitution set (i.e. a set of atoms) as a conjunction of atoms
	 * in the krss format. Used in Translator.
	 * 
	 * @return the string representation of a substitution set
	 */
	private void toKRSS(StringBuffer sbuf, Collection<Atom> setOfSubsumers,
			Set<Equation> equations) {

		if (setOfSubsumers.isEmpty()) {

			sbuf.append(KRSSKeyword.top);
			sbuf.append(KRSSKeyword.space);

		} else if (setOfSubsumers.size() == 1) {

			Atom atom = setOfSubsumers.iterator().next();
			toKRSS(sbuf, atom, equations);

		} else {

			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			sbuf.append(KRSSKeyword.space);

			for (Atom atom : setOfSubsumers) {
				sbuf.append(KRSSKeyword.space);
				toKRSS(sbuf, atom, equations);
				sbuf.append(KRSSKeyword.space);
			}

			sbuf.append(KRSSKeyword.space);
			sbuf.append(KRSSKeyword.close);
		}
	}

	private void toKRSS(StringBuffer sbuf, Atom atom, Set<Equation> equations) {
		if (atom.isExistentialRestriction()) {
			ConceptName child = ((ExistentialRestriction) atom).getChild();
			Integer conceptId = atomManager.getAtoms().getIndex(child);

			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.some);
			sbuf.append(KRSSKeyword.space);
			String roleName = atomManager
					.getRoleName(((ExistentialRestriction) atom).getRoleId());
			sbuf.append(roleName);
			sbuf.append(KRSSKeyword.space);
			if (auxiliaryVariables.contains(conceptId)) {
				toKRSS(sbuf, getSetOfSubsumers(child, equations), equations);
			} else {
				String childName = atomManager.getConceptName(child
						.getConceptNameId());
				sbuf.append(childName);
			}
			sbuf.append(KRSSKeyword.close);
		} else {
			ConceptName concept = (ConceptName) atom;
			Integer conceptId = atomManager.getAtoms().getIndex(concept);
			String name = atomManager.getConceptName(conceptId);
			sbuf.append(name);
		}
	}

	private Collection<Atom> getSetOfSubsumers(Atom atom,
			Set<Equation> equations) {
		Set<Atom> ret = new HashSet<Atom>();
		for (Equation equation : equations) {
			if (equation.getLeft().equals(
					atomManager.getAtoms().addAndGetIndex(atom))) {
				for (Integer id : equation.getRight()) {
					ret.add(atomManager.getAtoms().get(id));
				}
			}
		}
		return ret;
	}

}
