package de.tudresden.inf.lat.uel.sat.solver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
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
class ProcessorAux {

	private Set<Integer> constants = new HashSet<Integer>();

	private Set<Integer> eatoms = new HashSet<Integer>();

	private Set<Integer> usedAtomIds = new HashSet<Integer>();

	private Set<Integer> variables = new HashSet<Integer>();

	public ProcessorAux(UelInput input) {
		configure(input);
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
					addEAtom(index);
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
			addUsedAtomId(index);
			Atom atom = input.getAtomManager().get(index);
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
		boolean ret = (this == o);
		if (!ret && o instanceof ProcessorAux) {
			ProcessorAux other = (ProcessorAux) o;
			ret = this.constants.equals(other.constants)
					&& this.eatoms.equals(other.eatoms)
					&& this.variables.equals(other.variables);
		}
		return ret;
	}

	public Set<Integer> getConstants() {
		return Collections.unmodifiableSet(constants);
	}

	public Set<Integer> getEAtoms() {
		return Collections.unmodifiableSet(eatoms);
	}

	public Set<Integer> getUsedAtomIds() {
		return Collections.unmodifiableSet(this.usedAtomIds);
	}

	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(variables);
	}

	@Override
	public int hashCode() {
		return this.constants.hashCode();
	}

}
