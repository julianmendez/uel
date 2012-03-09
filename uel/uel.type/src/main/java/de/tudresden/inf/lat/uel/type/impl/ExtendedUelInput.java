package de.tudresden.inf.lat.uel.type.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
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

	public ExtendedUelInput(UelInput input) {
		this.uelInput = input;
		configure(input);
	}

	private void configure(UelInput input) {
		Set<Integer> usedAtomsIds = new TreeSet<Integer>();

		for (Equation eq : input.getEquations()) {
			usedAtomsIds.add(eq.getLeft());
			usedAtomsIds.addAll(eq.getRight());
		}

		{
			Set<Integer> conceptNameIds = new HashSet<Integer>();
			for (Integer index : usedAtomsIds) {
				Atom atom = input.getAtomManager().get(index);
				if (atom.isExistentialRestriction()) {
					this.eatoms.add(index);
					ConceptName child = ((ExistentialRestriction) atom)
							.getChild();
					Integer childId = input.getAtomManager().addAndGetIndex(
							child);
					conceptNameIds.add(childId);
				}
			}
			usedAtomsIds.addAll(conceptNameIds);
		}

		for (Integer index : usedAtomsIds) {
			this.usedAtomIds.add(index);
			Atom atom = input.getAtomManager().get(index);
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
	public IndexedSet<Atom> getAtomManager() {
		return this.uelInput.getAtomManager();
	}

	public Set<Integer> getConstants() {
		return Collections.unmodifiableSet(constants);
	}

	public Set<Integer> getEAtoms() {
		return Collections.unmodifiableSet(eatoms);
	}

	@Override
	public Set<Equation> getEquations() {
		return this.uelInput.getEquations();
	}

	public Set<Integer> getUsedAtomIds() {
		return Collections.unmodifiableSet(this.usedAtomIds);
	}

	@Override
	public Set<Integer> getUserVariables() {
		return this.uelInput.getUserVariables();
	}

	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(variables);
	}

	@Override
	public int hashCode() {
		return this.constants.hashCode();
	}

}
