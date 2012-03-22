package de.tudresden.inf.lat.uel.type.impl;

import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.UelInput;

/**
 * This is the default implementation of a UEL input.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 * @see UelInput
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
	 * @param eq
	 *            set of input equations
	 * @param set
	 *            set of user variables
	 */
	public UelInputImpl(IndexedSet<Atom> manager, Set<Equation> eq,
			Set<Integer> set) {
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
