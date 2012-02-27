package de.tudresden.inf.lat.uel.sat.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;
import de.tudresden.inf.lat.uel.type.impl.IndexedSetImpl;

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
public class Goal implements UelInput {

	public static final String UNDEF_SUFFIX = "_UNDEF";

	private final IndexedSet<SatAtom> atomManager;

	/**
	 * constants is a hash map implementing all constant concept names in the
	 * goal. keys are names and values are flat atoms
	 */
	private Set<Integer> constants = new HashSet<Integer>();

	/**
	 * eatoms is a hash map implementing all flat existential restrictions keys
	 * are names and values are flat atoms
	 */
	private Set<Integer> eatoms = new HashSet<Integer>();

	/**
	 * equations is a list containing all goal equations
	 */
	private Set<Equation> equations = new HashSet<Equation>();

	private Equation mainEquation;

	private Set<Integer> usedAtomIds;

	/**
	 * variables is a hash map implementing all concept names which are treated
	 * as variables keys are names and values are flat atoms
	 */
	private Set<Integer> variables = new HashSet<Integer>();

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
	public Goal(IndexedSet<SatAtom> manager, Ontology ont, String leftStr,
			String rightStr) {
		this.atomManager = manager;
		initialize(ont, leftStr, rightStr);
	}

	/**
	 * Method to add an equation e to the list of goal equations
	 * 
	 * @param e
	 *            equation
	 */
	public void addEquation(Equation e) {
		equations.add(e);
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Goal) {
			Goal other = (Goal) o;
			ret = this.constants.equals(other.constants)
					&& this.eatoms.equals(other.eatoms)
					&& this.equations.equals(other.equations)
					&& this.mainEquation.equals(other.mainEquation)
					&& this.variables.equals(other.variables);
		}
		return ret;
	}

	public IndexedSet<SatAtom> getSatAtomManager() {
		return this.atomManager;
	}

	public Set<Integer> getConstants() {
		return Collections.unmodifiableSet(constants);
	}

	public Set<Integer> getEAtoms() {
		return Collections.unmodifiableSet(eatoms);
	}

	/**
	 * Method to get the list of goal equations
	 * 
	 * @return the list of goal equations
	 */
	public Set<Equation> getEquations() {
		return equations;
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
		for (Equation eq : getEquations()) {

			sbuf.append(toString(eq));
		}
		return sbuf.toString();
	}

	public Equation getMainEquation() {
		return mainEquation;
	}

	public Set<Integer> getUsedAtomIds() {
		return this.usedAtomIds;
	}

	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(variables);
	}

	@Override
	public int hashCode() {
		return this.mainEquation.hashCode();
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

		this.equations.addAll(newEquationSet);

		for (Equation eq : this.equations) {
			Integer atomId = eq.getLeft();
			variables.add(atomId);
			ConceptName concept = getSatAtomManager().get(atomId)
					.asConceptName();
			concept.setVariable(true);
			concept.setUserVariable(false);
		}

		Set<Integer> usedAtomIds = new HashSet<Integer>();
		for (Equation eq : this.equations) {
			usedAtomIds.add(eq.getLeft());
			usedAtomIds.addAll(eq.getRight());
		}

		Set<Integer> conceptNameIds = new HashSet<Integer>();
		for (Integer usedAtomId : usedAtomIds) {
			SatAtom atom = getSatAtomManager().get(usedAtomId);
			if (atom.isConceptName()) {
				conceptNameIds.add(usedAtomId);
			} else if (atom.isExistentialRestriction()) {
				eatoms.add(usedAtomId);
				ConceptName child = atom.asExistentialRestriction().getChild();
				Integer childId = getSatAtomManager().addAndGetIndex(child);
				conceptNameIds.add(childId);
			}
		}
		usedAtomIds.addAll(conceptNameIds);
		this.usedAtomIds = usedAtomIds;

		for (Integer atomId : conceptNameIds) {
			SatAtom atom = getSatAtomManager().get(atomId);
			if (atom.isConceptName()) {
				if (atom.asConceptName().isVariable()) {
					variables.add(atomId);
				} else if (!atom.asConceptName().isVariable()) {
					constants.add(atomId);
				}
			}
		}

		for (Integer atomId : variables) {
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
		if (this.variables.contains(atomId)) {
			this.variables.remove(atomId);
			this.constants.add(atomId);
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
		if (this.constants.contains(atomId)) {
			this.constants.remove(atomId);
			this.variables.add(atomId);
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
		this.mainEquation = equation;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(toString(this.mainEquation));
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
		sbuf.append(getSatAtomManager().get(eq.getLeft()));
		if (eq.getRight().size() > 1) {
			sbuf.append(KRSSKeyword.space);
			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			for (Integer conceptId : eq.getRight()) {
				sbuf.append(KRSSKeyword.space);
				sbuf.append(getSatAtomManager().get(conceptId).getId());
			}
			sbuf.append(KRSSKeyword.close);
		} else if (eq.getRight().size() == 1) {
			sbuf.append(KRSSKeyword.space);
			sbuf.append(getSatAtomManager()
					.get(eq.getRight().iterator().next()).getId());
		}
		sbuf.append(KRSSKeyword.close);
		sbuf.append("\n");
		return sbuf.toString();
	}

	@Override
	public IndexedSet<Atom> getAtomManager() {
		IndexedSet<Atom> ret = new IndexedSetImpl<Atom>();
		for (SatAtom atom : getSatAtomManager()) {
			ret.add(atom);
		}
		return ret;
	}

	@Override
	public Set<Integer> getUserVariables() {
		// FIXME
		// TODO Auto-generated method stub
		return null;
	}

}
