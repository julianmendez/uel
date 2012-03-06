package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.plugin.type.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;
import de.tudresden.inf.lat.uel.type.impl.UelInputImpl;

/**
 * This class implements a goal of unification, i.e., a set of equations between
 * concept terms with variables.
 * 
 * The goal is unique for the procedure, and should be accessible for most other
 * objects.
 * 
 * @author Barbara Morawska
 * @author Julian Mendez
 */
public class PluginGoal {

	private final AtomManager atomManager;
	private final Set<Integer> constants = new HashSet<Integer>();
	private final Set<Integer> eatoms = new HashSet<Integer>();
	private final Set<Equation> equations = new HashSet<Equation>();
	private final UelInput uelInput;
	private final Set<Integer> usedAtomIds = new HashSet<Integer>();
	private final Set<Integer> userVariables = new HashSet<Integer>();
	private final Set<Integer> variables = new HashSet<Integer>();

	/**
	 * Constructs a new goal based on a specified ontology.
	 * 
	 * @param manager
	 *            atom manager
	 * @param ont
	 *            ontology
	 * @param leftStr
	 *            left atom name of the main equation
	 * @param rightStr
	 *            right atom name of the main equation
	 */
	public PluginGoal(AtomManager manager, Ontology ont, String leftStr,
			String rightStr) {
		this.atomManager = manager;
		Set<Equation> equations = initialize(ont, leftStr, rightStr);
		for (Equation eq : equations) {
			this.equations.add(eq);
		}
		this.uelInput = new UelInputImpl(manager.getAtoms(), equations,
				this.userVariables);
	}

	public AtomManager getAtomManager() {
		return this.atomManager;
	}

	public Set<Integer> getConstants() {
		return Collections.unmodifiableSet(this.constants);
	}

	/**
	 * Returns a string representation of the equations, excluding the main
	 * equation.
	 * 
	 * @return a string representation of the equations, excluding the main
	 *         equation
	 */
	public String getGoalEquations() {
		StringBuffer sbuf = new StringBuffer();
		for (Equation eq : getUelInput().getEquations()) {

			sbuf.append(toString(eq));
		}
		return sbuf.toString();
	}

	public UelInput getUelInput() {
		return this.uelInput;
	}

	public Set<Integer> getUsedAtomIds() {
		return Collections.unmodifiableSet(this.usedAtomIds);
	}

	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(this.variables);
	}

	private Set<Equation> initialize(Ontology ontology, String leftStr,
			String rightStr) {

		for (Atom atom : getAtomManager().getAtoms()) {
			if (atom.isConceptName()) {
				((ConceptName) atom).setVariable(false);
			}
		}

		ConceptName left = getAtomManager().createConceptName(leftStr, true);
		ConceptName right = getAtomManager().createConceptName(rightStr, true);

		Set<Equation> ret = new HashSet<Equation>();
		{
			Set<Equation> equationSet = new HashSet<Equation>();
			Integer leftId = getAtomManager().getAtoms().addAndGetIndex(left);
			Integer rightId = getAtomManager().getAtoms().addAndGetIndex(right);
			equationSet.addAll(ontology.getModule(leftId));
			equationSet.addAll(ontology.getModule(rightId));

			for (Equation eq : equationSet) {
				if (eq.isPrimitive()) {
					ret.add(processPrimitiveDefinition(eq));
				} else {
					ret.add(eq);
				}
			}
		}

		ret.add(new EquationImpl(getAtomManager().getAtoms().addAndGetIndex(
				left), getAtomManager().getAtoms().addAndGetIndex(right), false));

		for (Equation eq : ret) {
			Integer atomId = eq.getLeft();
			this.variables.add(atomId);
			ConceptName concept = (ConceptName) getAtomManager().getAtoms()
					.get(atomId);
			concept.setVariable(true);
			this.userVariables.remove(concept.getConceptNameId());
		}

		Set<Integer> usedAtomIds = new HashSet<Integer>();
		for (Equation eq : ret) {
			usedAtomIds.add(eq.getLeft());
			usedAtomIds.addAll(eq.getRight());
		}

		Set<Integer> conceptNameIds = new HashSet<Integer>();
		for (Integer usedAtomId : usedAtomIds) {
			Atom atom = getAtomManager().getAtoms().get(usedAtomId);
			if (atom.isConceptName()) {
				conceptNameIds.add(usedAtomId);
			} else if (atom.isExistentialRestriction()) {
				this.eatoms.add(usedAtomId);
				ConceptName child = ((ExistentialRestriction) atom).getChild();
				Integer childId = getAtomManager().getAtoms().addAndGetIndex(
						child);
				conceptNameIds.add(childId);
			}
		}
		usedAtomIds.addAll(conceptNameIds);
		for (Integer atomId : usedAtomIds) {
			this.usedAtomIds.add(atomId);
		}

		for (Integer atomId : conceptNameIds) {
			Atom atom = getAtomManager().getAtoms().get(atomId);
			if (atom.isConceptName()) {
				if (((ConceptName) atom).isVariable()) {
					this.variables.add(atomId);
				} else if (!((ConceptName) atom).isVariable()) {
					this.constants.add(atomId);
				}
			}
		}

		return ret;
	}

	public void makeConstant(Integer atomId) {
		Atom atom = getAtomManager().getAtoms().get(atomId);
		if (!atom.isConceptName()) {
			throw new IllegalArgumentException(
					"Argument is not a concept name identifier: '" + atomId
							+ "'.");
		}
		ConceptName conceptName = (ConceptName) atom;
		if (getVariables().contains(atomId)) {
			this.variables.remove(atomId);
			this.constants.add(atomId);
			this.userVariables.remove(conceptName.getConceptNameId());
			conceptName.setVariable(false);
		}
	}

	public void makeVariable(Integer atomId) {
		Atom atom = getAtomManager().getAtoms().get(atomId);
		if (!atom.isConceptName()) {
			throw new IllegalArgumentException(
					"Argument is not a concept name identifier: '" + atomId
							+ "'.");
		}
		ConceptName conceptName = (ConceptName) atom;
		if (getConstants().contains(atomId)) {
			this.constants.remove(atomId);
			this.variables.add(atomId);
			this.userVariables.add(conceptName.getConceptNameId());
			conceptName.setVariable(true);
		}
	}

	private Equation processPrimitiveDefinition(Equation e) {
		Atom leftAtom = getAtomManager().getAtoms().get(e.getLeft());
		ConceptName b = (ConceptName) leftAtom;
		ConceptName var = this.atomManager.createUndefConceptName(b, false);
		this.userVariables.remove(var.getConceptNameId());
		getAtomManager().getAtoms().add(var);
		Integer varId = getAtomManager().getAtoms().addAndGetIndex(var);

		Set<Integer> newRightSet = new HashSet<Integer>();
		newRightSet.addAll(e.getRight());
		newRightSet.add(varId);
		return new EquationImpl(e.getLeft(), newRightSet, false);
	}

	private String renderByAtomId(Integer atomId) {
		String ret = null;
		Atom atom = getAtomManager().getAtoms().get(atomId);
		if (atom.isExistentialRestriction()) {
			ExistentialRestriction restriction = (ExistentialRestriction) atom;
			String roleName = getAtomManager().getRoleName(
					restriction.getRoleId());
			StringBuffer sbuf = new StringBuffer();
			sbuf.append(KRSSKeyword.open);
			sbuf.append(roleName);
			sbuf.append(KRSSKeyword.space);
			sbuf.append(renderByAtomId(restriction.getChild()
					.getConceptNameId()));
			sbuf.append(KRSSKeyword.close);
		} else if (atom.isConceptName()) {
			ConceptName concept = (ConceptName) atom;
			ret = getAtomManager().getConceptName(concept.getConceptNameId());
		}
		return ret;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(getGoalEquations());
		return sbuf.toString();
	}

	private String toString(Equation eq) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(KRSSKeyword.newLine);
		sbuf.append(KRSSKeyword.open);
		if (eq.isPrimitive()) {
			sbuf.append(KRSSKeyword.define_primitive_concept);
		} else {
			sbuf.append(KRSSKeyword.define_concept);
		}
		sbuf.append(KRSSKeyword.space);
		sbuf.append(renderByAtomId(eq.getLeft()));
		if (eq.getRight().size() > 1) {
			sbuf.append(KRSSKeyword.space);
			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			for (Integer atomId : eq.getRight()) {
				sbuf.append(KRSSKeyword.space);
				sbuf.append(renderByAtomId(atomId));
			}
			sbuf.append(KRSSKeyword.close);
		} else if (eq.getRight().size() == 1) {
			sbuf.append(KRSSKeyword.space);
			Integer atomId = eq.getRight().iterator().next();
			sbuf.append(renderByAtomId(atomId));
		}
		sbuf.append(KRSSKeyword.close);
		sbuf.append("\n");
		return sbuf.toString();
	}

}
