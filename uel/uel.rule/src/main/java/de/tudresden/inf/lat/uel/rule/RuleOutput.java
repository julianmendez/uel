package de.tudresden.inf.lat.uel.rule;

import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.UelOutput;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;

/**
 * 'UelOutput' implementation to translate from a variable assignment to a set of definitions.
 * 
 * @author Stefan Borgwardt
 */
class RuleOutput implements UelOutput {

	private Set<Equation> unifier;
	private IndexedSet<Atom> atomManager;
	
	/**
	 * Constructs a 'RuleOutput' object from a variable assignment and an atom manager.
	 * @param unifier the assignment
	 * @param atomManager the atom manager
	 */
	public RuleOutput(Assignment unifier, IndexedSet<Atom> atomManager) {
		this.atomManager = atomManager;
		this.unifier = convertAssignment(unifier);
	}
	
	private Set<Equation> convertAssignment(Assignment unifier) {
		Set<Equation> res = new HashSet<Equation>();
		for (Integer var : unifier.getKeys()) {
			Integer head = atomManager.addAndGetIndex(new FlatAtom(var, false));
			Set<Integer> body = new HashSet<Integer>();
			for (Atom at : unifier.getSubsumers(var)) {
				body.add(atomManager.addAndGetIndex(at));
			}
			res.add(new EquationImpl(head, body, false));
		}
		return res;
	}
	
	@Override
	public IndexedSet<Atom> getAtomManager() {
		return atomManager;
	}

	@Override
	public Set<Equation> getEquations() {
		return unifier;
	}
	
}
