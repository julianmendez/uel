package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.plugin.type.AtomManager;
import de.tudresden.inf.lat.uel.sat.type.SubsumptionLiteral;
import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.SmallEquation;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;
import de.tudresden.inf.lat.uel.type.impl.SmallEquationImpl;
import de.tudresden.inf.lat.uel.type.impl.UelInputImpl;

/**
 * This class is a goal of unification.
 * 
 * @author Barbara Morawska
 * @author Julian Mendez
 * @author Stefan Borgwardt
 */
public class PluginGoal {

	public static String toString(AtomManager atomManager,
			Collection<Equation> equations) {
		StringBuffer sbuf = new StringBuffer();
		for (Equation eq : equations) {
			sbuf.append(toString(atomManager, eq));
		}
		return sbuf.toString();
	}

	public static String toString(AtomManager atomManager, Equation eq) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(KRSSKeyword.newLine);
		sbuf.append(KRSSKeyword.open);
		if (eq.isPrimitive()) {
			sbuf.append(KRSSKeyword.define_primitive_concept);
		} else {
			sbuf.append(KRSSKeyword.define_concept);
		}
		sbuf.append(KRSSKeyword.space);
		sbuf.append(toString(atomManager, eq.getLeft()));
		if (eq.getRight().size() > 1) {
			sbuf.append(KRSSKeyword.space);
			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			for (Integer atomId : eq.getRight()) {
				sbuf.append(KRSSKeyword.space);
				sbuf.append(toString(atomManager, atomId));
			}
			sbuf.append(KRSSKeyword.close);
		} else if (eq.getRight().size() == 1) {
			sbuf.append(KRSSKeyword.space);
			Integer atomId = eq.getRight().iterator().next();
			sbuf.append(toString(atomManager, atomId));
		}
		sbuf.append(KRSSKeyword.close);
		sbuf.append("\n");
		return sbuf.toString();
	}

	public static String toString(AtomManager atomManager, Integer atomId) {
		String ret = null;
		Atom atom = atomManager.getAtoms().get(atomId);
		if (atom.isExistentialRestriction()) {
			ExistentialRestriction restriction = (ExistentialRestriction) atom;
			String roleName = atomManager.getRoleName(restriction.getRoleId());
			StringBuffer sbuf = new StringBuffer();
			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.some);
			sbuf.append(KRSSKeyword.space);
			sbuf.append(roleName);
			sbuf.append(KRSSKeyword.space);
			sbuf.append(toString(atomManager, restriction.getChild()
					.getConceptNameId()));
			sbuf.append(KRSSKeyword.close);
			ret = sbuf.toString();
		} else if (atom.isConceptName()) {
			ConceptName concept = (ConceptName) atom;
			ret = atomManager.getConceptName(concept.getConceptNameId());
		}
		return ret;
	}

	private final AtomManager atomManager;
	private final Set<Integer> auxiliaryVariables = new HashSet<Integer>();
	private final Set<Integer> constants = new HashSet<Integer>();
	private final Set<Equation> definitions = new HashSet<Equation>();
	private final Set<SmallEquation> goalDisequations = new HashSet<SmallEquation>();
	private final Set<Equation> goalEquations = new HashSet<Equation>();

	private final Ontology ontology;

	private UelInput uelInput;
	private final Set<Integer> userVariables = new HashSet<Integer>();

	private final Set<Integer> variables = new HashSet<Integer>();

	/**
	 * Constructs a new goal based on a specified ontology.
	 * 
	 * @param manager
	 *            atom manager
	 * @param ont
	 *            ontology
	 */
	public PluginGoal(AtomManager manager, Ontology ont) {
		this.atomManager = manager;
		this.ontology = ont;
		for (Atom atom : getAtomManager().getAtoms()) {
			if (atom.isConceptName()) {
				((ConceptName) atom).setVariable(false);
			}
		}
	}

	/**
	 * Constructs a new goal based on a specified ontology and the main
	 * equation.
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
		this(manager, ont);
		addGoalEquation(leftStr, rightStr);
		updateUelInput();
	}

	public void addGoalDisequation(String leftStr, String rightStr) {
		Set<Equation> equationSet = new HashSet<Equation>();
		Integer leftId = addModule(equationSet, leftStr);
		Integer rightId = addModule(equationSet, rightStr);
		markDefinedConceptNamesAsVariables(equationSet);
		this.definitions.addAll(equationSet);

		SmallEquation newGoalDisequation = new SmallEquationImpl(leftId,
				rightId);

		this.goalDisequations.add(newGoalDisequation);
		updateIndexSets(equationSet);
		updateIndexSets(newGoalDisequation);
	}

	public void addGoalEquation(String leftStr, String rightStr) {
		Set<Equation> equationSet = new HashSet<Equation>();
		Integer leftId = addModule(equationSet, leftStr);
		Integer rightId = addModule(equationSet, rightStr);
		markDefinedConceptNamesAsVariables(equationSet);
		this.definitions.addAll(equationSet);

		Equation newGoalEquation = new EquationImpl(leftId, rightId, false);

		equationSet.add(newGoalEquation);
		this.goalEquations.add(newGoalEquation);
		updateIndexSets(equationSet);
	}

	public void addGoalSubsumption(String leftStr, String rightStr) {
		Set<Equation> equationSet = new HashSet<Equation>();
		Integer leftId = addModule(equationSet, leftStr);
		Integer rightId = addModule(equationSet, rightStr);
		markDefinedConceptNamesAsVariables(equationSet);
		this.definitions.addAll(equationSet);

		Set<Integer> rightIds = new HashSet<Integer>();
		rightIds.add(leftId);
		rightIds.add(rightId);
		Equation newGoalEquation = new EquationImpl(leftId, rightIds, false);

		equationSet.add(newGoalEquation);
		this.goalEquations.add(newGoalEquation);
		updateIndexSets(equationSet);
	}

	private Integer addModule(Set<Equation> equationSet, String str) {
		ConceptName conceptName = getAtomManager()
				.createConceptName(str, false);
		Integer ret = getAtomManager().getAtoms().addAndGetIndex(conceptName);
		Set<Equation> module = this.ontology.getModule(ret);
		for (Equation eq : module) {
			if (eq.isPrimitive()) {
				equationSet.add(processPrimitiveDefinition(eq));
			} else {
				equationSet.add(eq);
			}
		}
		return ret;
	}

	public AtomManager getAtomManager() {
		return this.atomManager;
	}

	public Set<Integer> getAuxiliaryVariables() {
		return Collections.unmodifiableSet(this.auxiliaryVariables);
	}

	public Set<Integer> getConstants() {
		return Collections.unmodifiableSet(this.constants);
	}

	public UelInput getUelInput() {
		return this.uelInput;
	}

	public Set<Integer> getUserVariables() {
		return Collections.unmodifiableSet(this.userVariables);
	}

	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(this.variables);
	}

	public void makeAuxiliaryVariable(Integer atomId) {
		Atom atom = getAtomManager().getAtoms().get(atomId);
		if ((atom == null) || !atom.isConceptName()) {
			throw new IllegalArgumentException(
					"Argument is not a concept name identifier: '" + atomId
							+ "'.");
		}
		ConceptName conceptName = (ConceptName) atom;
		if (getVariables().contains(atomId)) {
			this.auxiliaryVariables.add(conceptName.getConceptNameId());
		}
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

	public void makeUserVariable(Integer atomId) {
		Atom atom = getAtomManager().getAtoms().get(atomId);
		if ((atom == null) || !atom.isConceptName()) {
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

	private void markDefinedConceptNamesAsVariables(Set<Equation> equationSet) {
		for (Equation eq : equationSet) {
			Integer atomId = eq.getLeft();
			this.variables.add(atomId);
			ConceptName concept = (ConceptName) getAtomManager().getAtoms()
					.get(atomId);
			concept.setVariable(true);
			this.userVariables.remove(concept.getConceptNameId());
		}
	}

	/**
	 * Returns a string representation of the equations, excluding the goal
	 * equations.
	 * 
	 * @return a string representation of the equations, excluding the main
	 *         equation
	 */
	public String printDefinitions() {
		return toString(getAtomManager(), this.definitions);
	}

	private Equation processPrimitiveDefinition(Equation e) {
		Atom leftAtom = getAtomManager().getAtoms().get(e.getLeft());
		ConceptName b = (ConceptName) leftAtom;
		ConceptName var = this.atomManager.createUndefConceptName(b, false);
		this.userVariables.remove(var.getConceptNameId());
		Integer varId = getAtomManager().getAtoms().addAndGetIndex(var);

		Set<Integer> newRightSet = new HashSet<Integer>();
		newRightSet.addAll(e.getRight());
		newRightSet.add(varId);
		return new EquationImpl(e.getLeft(), newRightSet, false);
	}

	@Override
	public String toString() {
		return toString(getAtomManager(), this.definitions)
				+ toString(getAtomManager(), this.goalEquations);
	}

	private void updateIndexSets(Set<Equation> equationSet) {
		Set<Integer> usedAtomIds = new HashSet<Integer>();
		for (Equation eq : equationSet) {
			usedAtomIds.add(eq.getLeft());
			usedAtomIds.addAll(eq.getRight());
		}
		updateIndexSetsWithAtoms(usedAtomIds);
	}

	private void updateIndexSets(SmallEquation eq) {
		Set<Integer> usedAtomIds = new HashSet<Integer>();
		usedAtomIds.add(eq.getLeft());
		usedAtomIds.add(eq.getRight());
		updateIndexSetsWithAtoms(usedAtomIds);
	}

	private void updateIndexSetsWithAtoms(Set<Integer> usedAtomIds) {
		Set<Integer> conceptNameIds = new HashSet<Integer>();
		for (Integer usedAtomId : usedAtomIds) {
			Atom atom = getAtomManager().getAtoms().get(usedAtomId);
			if (atom.isConceptName()) {
				conceptNameIds.add(usedAtomId);
			} else if (atom.isExistentialRestriction()) {
				ConceptName child = ((ExistentialRestriction) atom).getChild();
				Integer childId = getAtomManager().getAtoms().addAndGetIndex(
						child);
				conceptNameIds.add(childId);
			}
		}

		for (Integer atomId : conceptNameIds) {
			Atom atom = getAtomManager().getAtoms().get(atomId);
			if (atom.isConceptName()) {
				if (((ConceptName) atom).isVariable()) {
					this.variables.add(atomId);
					if (((ConceptName) atom).isAuxiliaryVariable()) {
						this.auxiliaryVariables.add(atomId);
					}
				} else {
					this.constants.add(atomId);
				}
			}
		}

	}

	public void updateUelInput() {
		this.uelInput = new UelInputImpl(getAtomManager().getAtoms(),
				this.definitions, this.goalEquations, this.goalDisequations,
				this.userVariables);
	}

}
