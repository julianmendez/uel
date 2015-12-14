package de.tudresden.inf.lat.uel.type.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.SmallEquation;
import de.tudresden.inf.lat.uel.type.api.UelInput;

/**
 * This class implements an extended UEL input. This extension is used to
 * classify the atoms contained in the UEL input.
 * 
 * @author Barbara Morawska
 * @author Julian Mendez
 * @see UelInput
 */
public class ExtendedUelInput implements UelInput {

	private final Set<Integer> constants = new HashSet<Integer>();
	private final Set<Integer> eatoms = new HashSet<Integer>();
	private final UelInput uelInput;
	private final Set<Integer> usedAtomIds = new HashSet<Integer>();
	private final Set<Integer> variables = new HashSet<Integer>();

	/**
	 * Construct an extended UEL input from a standard UEL input.
	 * 
	 * @param input
	 *            the input
	 */
	public ExtendedUelInput(UelInput input) {
		this.uelInput = input;
		configure();
	}

	private void configure() {
		Set<Integer> usedAtomsIds = new TreeSet<Integer>();

		for (Equation eq : getEquations()) {
			usedAtomsIds.add(eq.getLeft());
			usedAtomsIds.addAll(eq.getRight());
		}
		for (SmallEquation eq : getGoalDisequations()) {
			usedAtomsIds.add(eq.getLeft());
			usedAtomsIds.add(eq.getRight());
		}

		{
			Set<Integer> conceptNameIds = new HashSet<Integer>();
			for (Integer index : usedAtomsIds) {
				Atom atom = getAtoms().get(index);
				if (atom.isExistentialRestriction()) {
					this.eatoms.add(index);
					ConceptName child = atom.getConceptName();
					conceptNameIds.add(getAtoms().getIndex(child));
				}
			}
			usedAtomsIds.addAll(conceptNameIds);
		}

		for (Integer index : usedAtomsIds) {
			this.usedAtomIds.add(index);
			Atom atom = getAtoms().get(index);
			if (atom.isConceptName()) {
				if (atom.isVariable()) {
					this.variables.add(index);
				} else {
					this.constants.add(index);
				}
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = (this == o);
		if (!ret && o instanceof ExtendedUelInput) {
			ExtendedUelInput other = (ExtendedUelInput) o;
			ret = this.constants.equals(other.constants)
					&& this.eatoms.equals(other.eatoms)
					&& this.variables.equals(other.variables)
					&& this.usedAtomIds.equals(other.usedAtomIds);
		}
		return ret;
	}

	@Override
	public IndexedSet<Atom> getAtoms() {
		return this.uelInput.getAtoms();
	}

	/**
	 * Retrieve all constants occurring in the UEL input.
	 * 
	 * @return a set containing the identifiers of the constants
	 */
	public Set<Integer> getConstants() {
		return Collections.unmodifiableSet(constants);
	}

	/**
	 * Retrieve all existential restrictions occurring in the UEL input.
	 * 
	 * @return a set containing the identifiers of the existential restrictions
	 */
	public Set<Integer> getEAtoms() {
		return Collections.unmodifiableSet(eatoms);
	}

	@Override
	public Set<Equation> getDefinitions() {
		return this.uelInput.getDefinitions();
	}

	@Override
	public Set<Equation> getGoalEquations() {
		return this.uelInput.getGoalEquations();
	}

	@Override
	public Set<SmallEquation> getGoalDisequations() {
		return this.uelInput.getGoalDisequations();
	}

	@Override
	public Set<Equation> getEquations() {
		return this.uelInput.getEquations();
	}

	/**
	 * Determine which identifiers are currently used for atoms.
	 * 
	 * @return a set containing all used identifiers
	 */
	public Set<Integer> getUsedAtomIds() {
		return Collections.unmodifiableSet(this.usedAtomIds);
	}

	@Override
	public Set<Integer> getUserVariables() {
		return this.uelInput.getUserVariables();
	}

	/**
	 * Retrieve all variables occurring in the UEL input.
	 * 
	 * @return a set containing the identifiers of the variables
	 */
	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(variables);
	}

	@Override
	public int hashCode() {
		return this.constants.hashCode();
	}

}
