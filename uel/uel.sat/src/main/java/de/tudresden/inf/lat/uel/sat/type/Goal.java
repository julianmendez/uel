package de.tudresden.inf.lat.uel.sat.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

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
public class Goal {

	private final IndexedSet<Atom> atomManager;

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

	private final UelInput input;

	private Set<Integer> usedAtomIds = new HashSet<Integer>();

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
	 */
	public Goal(UelInput input) {
		this.atomManager = input.getAtomManager();
		this.input = input;
		configureGoal(input);
	}

	private boolean addConstant(Integer atomId) {
		return this.constants.add(atomId);
	}

	private boolean addEAtom(Integer atomId) {
		return this.eatoms.add(atomId);
	}

	private boolean addUsedAtomId(Integer atomId) {
		return this.usedAtomIds.add(atomId);
	}

	private boolean addVariable(Integer atomId) {
		return this.variables.add(atomId);
	}

	private void configureGoal(UelInput input) {

		Set<Integer> usedAtomsIds = new TreeSet<Integer>();

		for (Equation eq : input.getEquations()) {
			usedAtomsIds.add(eq.getLeft());
			usedAtomsIds.addAll(eq.getRight());
		}

		{
			Set<Integer> conceptNameIds = new HashSet<Integer>();
			for (Integer index : usedAtomsIds) {
				Atom atom = atomManager.get(index);
				if (atom.isExistentialRestriction()) {
					addEAtom(index);
					ConceptName child = ((ExistentialRestriction) atom)
							.getChild();
					Integer childId = atomManager.addAndGetIndex(child);
					conceptNameIds.add(childId);
				}
			}
			usedAtomsIds.addAll(conceptNameIds);
		}

		for (Integer index : usedAtomsIds) {
			addUsedAtomId(index);
			Atom atom = atomManager.get(index);
			if (atom.isConceptName()) {
				if (atom.isVariable()) {
					addVariable(index);
				} else {
					addConstant(index);
				}
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = (o == this);
		if (!ret && o instanceof Goal) {
			Goal other = (Goal) o;
			ret = this.constants.equals(other.constants)
					&& this.eatoms.equals(other.eatoms)
					&& this.variables.equals(other.variables)
					&& this.input.equals(other.input);
		}
		return ret;
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
		return this.input.getEquations();
	}

	public IndexedSet<Atom> getSatAtomManager() {
		return this.atomManager;
	}

	public UelInput getUelInput() {
		return this.input;
	}

	public Set<Integer> getUsedAtomIds() {
		return Collections.unmodifiableSet(this.usedAtomIds);
	}

	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(variables);
	}

	@Override
	public int hashCode() {
		return this.input.hashCode();
	}

}
