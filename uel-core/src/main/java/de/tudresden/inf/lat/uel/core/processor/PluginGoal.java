package de.tudresden.inf.lat.uel.core.processor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.core.type.KRSSRenderer;
import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.SmallEquation;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;
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
		for (Atom atom : atomManager.getAtoms()) {
			if (atom.isConceptName()) {
				((ConceptName) atom).setVariable(false);
			}
		}
	}

	public void addGoalDisequation(Equation disequation) {
		Set<Equation> equationSet = extractModules(disequation.getLeft(), disequation.getRight());

		Integer rightId;
		if (disequation.getRight().size() == 1) {
			rightId = disequation.getRight().iterator().next();
		} else {
			ConceptName abbrev = ((DynamicOntology) this.ontology).getOntologyBuilder().createNewAtom();
			abbrev.setAuxiliaryVariable(true);
			rightId = atomManager.getAtoms().addAndGetIndex(abbrev);
			makeAuxiliaryVariable(rightId);

			Equation newDef = new EquationImpl(rightId, disequation.getRight(), false);
			equationSet.add(newDef);
			this.definitions.add(newDef);
		}

		SmallEquation newGoalDisequation = new SmallEquationImpl(disequation.getLeft(), rightId);

		this.goalDisequations.add(newGoalDisequation);
		updateIndexSets(equationSet);
		updateIndexSets(newGoalDisequation);
	}

	public void addGoalEquation(Integer leftId, Integer rightId) {
		Set<Equation> equationSet = extractModules(leftId, rightId);

		Equation newGoalEquation = new EquationImpl(leftId, rightId, false);

		equationSet.add(newGoalEquation);
		this.goalEquations.add(newGoalEquation);
		updateIndexSets(equationSet);
	}

	public void addGoalSubsumption(Integer leftId, Integer rightId) {
		Set<Equation> equationSet = extractModules(leftId, rightId);

		Set<Integer> rightIds = new HashSet<Integer>();
		rightIds.add(leftId);
		rightIds.add(rightId);
		Equation newGoalEquation = new EquationImpl(leftId, rightIds, false);

		equationSet.add(newGoalEquation);
		this.goalEquations.add(newGoalEquation);
		updateIndexSets(equationSet);
	}

	private void addModule(Set<Equation> equationSet, Integer atomId) {
		ConceptName conceptName = atomManager.getAtom(atomId).getConceptName();
		Integer conceptNameId = atomManager.getAtoms().getIndex(conceptName);
		Set<Equation> module = this.ontology.getModule(conceptNameId);
		// System.out.println("module size: " + module.size());

		for (Equation eq : module) {
			if (eq.isPrimitive()) {
				equationSet.add(processPrimitiveDefinition(eq));
			} else {
				equationSet.add(eq);
			}
		}
	}

	private Set<Equation> extractModules(Integer leftId, Integer rightId) {
		Set<Integer> atomIds = new HashSet<Integer>();
		atomIds.add(leftId);
		atomIds.add(rightId);
		return extractModules(atomIds);
	}

	private Set<Equation> extractModules(Integer leftId, Set<Integer> rightIds) {
		Set<Integer> atomIds = new HashSet<Integer>();
		atomIds.add(leftId);
		atomIds.addAll(rightIds);
		return extractModules(atomIds);
	}

	private Set<Equation> extractModules(Set<Integer> atomIds) {
		Set<Equation> equationSet = new HashSet<Equation>();
		for (Integer atomId : atomIds) {
			addModule(equationSet, atomId);
		}
		markDefinedConceptNamesAsVariables(equationSet);
		this.definitions.addAll(equationSet);
		return equationSet;
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
		if (getVariables().contains(atomId)) {
			this.auxiliaryVariables.add(atomId);
		}
	}

	public void makeConstant(Integer atomId) {
		if (getVariables().contains(atomId)) {
			this.variables.remove(atomId);
			this.constants.add(atomId);
			this.userVariables.remove(atomId);
			atomManager.getConceptName(atomId).setVariable(false);
		}
	}

	public void makeUserVariable(Integer atomId) {
		if (getConstants().contains(atomId)) {
			this.constants.remove(atomId);
			this.variables.add(atomId);
			this.userVariables.add(atomId);
			atomManager.getConceptName(atomId).setVariable(true);
		}
	}

	private void markDefinedConceptNamesAsVariables(Set<Equation> equationSet) {
		for (Equation eq : equationSet) {
			Integer atomId = eq.getLeft();
			this.variables.add(atomId);
			this.userVariables.remove(atomId);
			atomManager.getConceptName(atomId).setVariable(true);
		}
	}

	public String print(KRSSRenderer renderer) {
		StringBuffer sbuf = new StringBuffer();
		renderer.appendCustomEquations(sbuf, definitions, " ≡ ");
		renderer.appendCustomEquations(sbuf, goalEquations, " ≡ ");
		renderer.appendCustomSmallEquations(sbuf, goalDisequations, " ≢ ");
		return sbuf.toString();
	}

	private Equation processPrimitiveDefinition(Equation e) {
		Atom leftAtom = atomManager.getAtoms().get(e.getLeft());
		ConceptName var = atomManager.createUndefConceptName((ConceptName) leftAtom, false);
		Integer varId = atomManager.getAtoms().getIndex(var);
		this.userVariables.remove(varId);

		Set<Integer> newRightSet = new HashSet<Integer>();
		newRightSet.addAll(e.getRight());
		newRightSet.add(varId);
		return new EquationImpl(e.getLeft(), newRightSet, false);
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
			Atom atom = atomManager.getAtoms().get(usedAtomId);
			if (atom.isConceptName()) {
				conceptNameIds.add(usedAtomId);
			} else {
				ConceptName child = atom.getConceptName();
				Integer childId = atomManager.getAtoms().getIndex(child);
				conceptNameIds.add(childId);
			}
		}

		for (Integer atomId : conceptNameIds) {
			Atom atom = atomManager.getAtoms().get(atomId);
			if (atom.isVariable()) {
				this.variables.add(atomId);
				if (((ConceptName) atom).isAuxiliaryVariable()) {
					this.auxiliaryVariables.add(atomId);
				}
			} else {
				this.constants.add(atomId);
			}
		}

	}

	public void updateUelInput() {
		this.uelInput = new UelInputImpl(atomManager.getAtoms(), this.definitions, this.goalEquations,
				this.goalDisequations, this.userVariables);
	}

}
