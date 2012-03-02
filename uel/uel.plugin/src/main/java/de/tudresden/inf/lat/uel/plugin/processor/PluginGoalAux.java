package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.plugin.type.SatAtom;
import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;
import de.tudresden.inf.lat.uel.type.impl.IndexedSetImpl;

/**
 * @author Barbara Morawska
 * @author Julian Mendez
 */
class PluginGoalAux implements UelInput {

	private final IndexedSet<SatAtom> atomManager;
	private IndexedSet<String> conceptNameSet = new IndexedSetImpl<String>();
	private Set<Integer> constants = new HashSet<Integer>();
	private Set<Integer> eatoms = new HashSet<Integer>();
	private Set<Equation> equations = new HashSet<Equation>();
	private IndexedSet<String> roleNameSet = new IndexedSetImpl<String>();
	private Set<Integer> usedAtomIds = new HashSet<Integer>();
	private Set<Integer> userVariables = new HashSet<Integer>();
	private Set<Integer> variables = new HashSet<Integer>();

	public PluginGoalAux(IndexedSet<SatAtom> manager) {
		this.atomManager = manager;
	}

	public boolean addConstant(Integer atomId) {
		return this.constants.add(atomId);
	}

	public boolean addEAtom(Integer atomId) {
		return this.eatoms.add(atomId);
	}

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
		if (o instanceof PluginGoalAux) {
			PluginGoalAux other = (PluginGoalAux) o;
			ret = this.constants.equals(other.constants)
					&& this.eatoms.equals(other.eatoms)
					&& this.equations.equals(other.equations)
					&& this.variables.equals(other.variables);
		}
		return ret;
	}

	@Override
	public IndexedSet<Atom> getAtomManager() {
		return processSatAtoms(this.atomManager);
	}

	public Set<Integer> getConstants() {
		return Collections.unmodifiableSet(constants);
	}

	public Set<Integer> getEAtoms() {
		return Collections.unmodifiableSet(eatoms);
	}

	public Set<Equation> getEquations() {
		return equations;
	}

	public Set<Integer> getUsedAtomIds() {
		return Collections.unmodifiableSet(this.usedAtomIds);
	}

	@Override
	public Set<Integer> getUserVariables() {
		return Collections.unmodifiableSet(this.userVariables);
	}

	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(variables);
	}

	@Override
	public int hashCode() {
		return this.equations.hashCode();
	}

	private IndexedSet<Atom> processSatAtoms(IndexedSet<SatAtom> manager) {
		IndexedSet<Atom> ret = new IndexedSetImpl<Atom>();

		for (SatAtom atom : manager) {
			if (atom.isConceptName()) {
				Integer conceptId = manager.getIndex(atom);
				this.conceptNameSet.add(atom.getName(), conceptId);
				atom.asConceptName().setConceptNameId(conceptId);
				ConceptName cAtom = new ConceptName(atom.isVariable(),
						conceptId);
				ret.add(cAtom, conceptId);

				if (atom.asConceptName().isUserVariable()) {
					this.userVariables.add(conceptId);
				}

			}
		}

		for (SatAtom atom : manager) {
			if (atom.isExistentialRestriction()) {
				Integer index = manager.getIndex(atom);
				Integer roleId = this.roleNameSet
						.addAndGetIndex(atom.getName());
				atom.asExistentialRestriction().setRoleId(roleId);
				ConceptName concept = (ConceptName) ret.get(atom
						.asExistentialRestriction().getConceptNameId());
				ExistentialRestriction eAtom = new ExistentialRestriction(
						roleId, concept);
				ret.add(eAtom, index);
			}
		}

		return ret;
	}

	public boolean removeConstant(Integer atomId) {
		return this.constants.remove(atomId);
	}

	public boolean removeVariable(Integer atomId) {
		return this.variables.remove(atomId);
	}

}
