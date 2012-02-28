package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.sat.type.ConceptName;
import de.tudresden.inf.lat.uel.sat.type.Goal;
import de.tudresden.inf.lat.uel.sat.type.Ontology;
import de.tudresden.inf.lat.uel.sat.type.SatAtom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;

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

	public static final String UNDEF_SUFFIX = "_UNDEF";

	private final Goal goal;

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
	public PluginGoal(IndexedSet<SatAtom> manager, Ontology ont,
			String leftStr, String rightStr) {
		this.goal = new Goal(manager);
		initialize(ont, leftStr, rightStr);
	}

	public Goal getGoal() {
		return this.goal;
	}

	public Equation getMainEquation() {
		return getGoal().getMainEquation();
	}

	private IndexedSet<SatAtom> getSatAtomManager() {
		return getGoal().getSatAtomManager();
	}

	public Set<Integer> getUsedAtomIds() {
		return getGoal().getUsedAtomIds();
	}

	private void initialize(Ontology ontology, String leftStr, String rightStr) {

		ConceptName left = new ConceptName(leftStr, true);
		ConceptName right = new ConceptName(rightStr, true);

		Set<Equation> newEquationSet = new HashSet<Equation>();
		{
			Set<Equation> equationSet = new HashSet<Equation>();
			Integer leftId = getSatAtomManager().addAndGetIndex(left);
			Integer rightId = getSatAtomManager().addAndGetIndex(right);
			equationSet.addAll(ontology.getModule(leftId));
			equationSet.addAll(ontology.getModule(rightId));

			setMainEquation(new EquationImpl(getSatAtomManager()
					.addAndGetIndex(left), getSatAtomManager().addAndGetIndex(
					right), false));

			for (Equation eq : equationSet) {
				if (eq.isPrimitive()) {
					newEquationSet.add(processPrimitiveDefinition(eq));
				} else {
					newEquationSet.add(eq);
				}
			}
		}

		for (Equation eq : newEquationSet) {
			getGoal().addEquation(eq);
		}

		for (Equation eq : getGoal().getEquations()) {
			Integer atomId = eq.getLeft();
			getGoal().addVariable(atomId);
			ConceptName concept = getSatAtomManager().get(atomId)
					.asConceptName();
			concept.setVariable(true);
			concept.setUserVariable(false);
		}

		Set<Integer> usedAtomIds = new HashSet<Integer>();
		for (Equation eq : getGoal().getEquations()) {
			usedAtomIds.add(eq.getLeft());
			usedAtomIds.addAll(eq.getRight());
		}

		Set<Integer> conceptNameIds = new HashSet<Integer>();
		for (Integer usedAtomId : usedAtomIds) {
			SatAtom atom = getSatAtomManager().get(usedAtomId);
			if (atom.isConceptName()) {
				conceptNameIds.add(usedAtomId);
			} else if (atom.isExistentialRestriction()) {
				getGoal().addEAtom(usedAtomId);
				ConceptName child = atom.asExistentialRestriction().getChild();
				Integer childId = getSatAtomManager().addAndGetIndex(child);
				conceptNameIds.add(childId);
			}
		}
		usedAtomIds.addAll(conceptNameIds);
		for (Integer atomId : usedAtomIds) {
			getGoal().addUsedAtomId(atomId);
		}

		for (Integer atomId : conceptNameIds) {
			SatAtom atom = getSatAtomManager().get(atomId);
			if (atom.isConceptName()) {
				if (atom.asConceptName().isVariable()) {
					getGoal().addVariable(atomId);
				} else if (!atom.asConceptName().isVariable()) {
					getGoal().addConstant(atomId);
				}
			}
		}

		for (Integer atomId : goal.getVariables()) {
			SatAtom atom = getSatAtomManager().get(atomId);
			if (!atom.isConceptName()) {
				throw new IllegalStateException();
			}
			atom.asConceptName().setVariable(true);
		}

	}

	public void makeConstant(Integer atomId) {
		SatAtom atom = getSatAtomManager().get(atomId);
		if (!atom.isConceptName()) {
			throw new IllegalArgumentException(
					"Argument is not a concept name identifier: '" + atomId
							+ "'.");
		}
		ConceptName conceptName = atom.asConceptName();
		if (getGoal().getVariables().contains(atomId)) {
			getGoal().removeVariable(atomId);
			getGoal().addConstant(atomId);
			conceptName.setUserVariable(false);
			conceptName.setVariable(false);
		}
	}

	public void makeVariable(Integer atomId) {
		SatAtom atom = getSatAtomManager().get(atomId);
		if (!atom.isConceptName()) {
			throw new IllegalArgumentException(
					"Argument is not a concept name identifier: '" + atomId
							+ "'.");
		}
		ConceptName conceptName = atom.asConceptName();
		if (getGoal().getConstants().contains(atomId)) {
			getGoal().removeConstant(atomId);
			getGoal().addVariable(atomId);
			conceptName.setUserVariable(true);
			conceptName.setVariable(true);
		}
	}

	private Equation processPrimitiveDefinition(Equation e) {
		SatAtom leftAtom = getSatAtomManager().get(e.getLeft());
		ConceptName b = leftAtom.asConceptName();
		ConceptName var = new ConceptName(b.getId() + UNDEF_SUFFIX, false);
		var.setUserVariable(false);
		getSatAtomManager().add(var);
		Integer varId = getSatAtomManager().addAndGetIndex(var);

		Set<Integer> newRightSet = new HashSet<Integer>();
		newRightSet.addAll(e.getRight());
		newRightSet.add(varId);
		return new EquationImpl(e.getLeft(), newRightSet, false);
	}

	public void setMainEquation(Equation equation) {
		getGoal().setMainEquation(equation);
	}

	@Override
	public String toString() {
		return getGoal().toString();
	}

}
