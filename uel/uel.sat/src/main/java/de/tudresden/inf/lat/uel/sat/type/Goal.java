package de.tudresden.inf.lat.uel.sat.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.UelInput;
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
	public Goal(IndexedSet<SatAtom> manager) {
		this.atomManager = manager;
	}

	public boolean addConstant(Integer atomId) {
		return this.constants.add(atomId);
	}

	public boolean addEAtom(Integer atomId) {
		return this.eatoms.add(atomId);
	}

	/**
	 * Method to add an equation e to the list of goal equations
	 * 
	 * @param e
	 *            equation
	 */
	public boolean addEquation(Equation e) {
		return this.equations.add(e);
	}

	public boolean addUsedAtomId(Integer atomId) {
		return this.usedAtomIds.add(atomId);
	}

	public boolean addVariable(Integer atomId) {
		return this.variables.add(atomId);
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

	@Override
	public IndexedSet<Atom> getAtomManager() {
		IndexedSet<Atom> ret = new IndexedSetImpl<Atom>();
		for (SatAtom atom : getSatAtomManager()) {
			ret.add(atom);
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
		return equations;
	}

	public Equation getMainEquation() {
		return mainEquation;
	}

	public IndexedSet<SatAtom> getSatAtomManager() {
		return this.atomManager;
	}

	public Set<Integer> getUsedAtomIds() {
		return Collections.unmodifiableSet(this.usedAtomIds);
	}

	@Override
	public Set<Integer> getUserVariables() {
		// FIXME
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(variables);
	}

	@Override
	public int hashCode() {
		return this.mainEquation.hashCode();
	}

	public boolean removeConstant(Integer atomId) {
		return this.constants.remove(atomId);
	}

	public boolean removeVariable(Integer atomId) {
		return this.variables.remove(atomId);
	}

	public void setMainEquation(Equation equation) {
		this.mainEquation = equation;
	}

}
