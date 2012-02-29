package de.tudresden.inf.lat.uel.type.impl;

import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.UelInput;

/**
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public class UelInputImpl implements UelInput {

	private final IndexedSet<Atom> atomManager;
	private final Set<Equation> equations;
	private final Set<Integer> userVariables;

	/**
	 * Constructs a new UEL input based on a specified ontology.
	 * 
	 * @param manager
	 *            atom manager
	 */
	public UelInputImpl(IndexedSet<Atom> manager, Set<Equation> eq,
			Set<Integer> set, Equation mainEquation) {
		this.atomManager = manager;
		this.equations = eq;
		this.userVariables = set;
	}

	@Override
	public IndexedSet<Atom> getAtomManager() {
		return this.atomManager;
	}

	@Override
	public Set<Equation> getEquations() {
		return this.equations;
	}

	@Override
	public Set<Integer> getUserVariables() {
		return this.userVariables;
	}

}
