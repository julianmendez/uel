package de.tudresden.inf.lat.uel.type.impl;

import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.UelOutput;

/**
 * This is the default implementation of a UEL output.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 * @see UelOutput
 */
public class UelOutputImpl implements UelOutput {

	private final IndexedSet<Atom> atomManager;
	private final Set<Equation> equations;

	/**
	 * Constructs a new UEL input based on a specified ontology.
	 * 
	 * @param manager
	 *            atom manager
	 * @param eq
	 *            set of output definitions representing the unifier
	 */
	public UelOutputImpl(IndexedSet<Atom> manager, Set<Equation> eq) {
		this.atomManager = manager;
		this.equations = eq;
	}

	@Override
	public IndexedSet<Atom> getAtomManager() {
		return this.atomManager;
	}

	@Override
	public Set<Equation> getEquations() {
		return this.equations;
	}

}
