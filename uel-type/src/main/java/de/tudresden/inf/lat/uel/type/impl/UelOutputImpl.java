package de.tudresden.inf.lat.uel.type.impl;

import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.UelOutput;

/**
 * This is the default implementation of a UEL output.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 * @see UelOutput
 */
public class UelOutputImpl implements UelOutput {

	private final Set<Equation> equations;

	/**
	 * Constructs a new UEL input based on a specified ontology.
	 * 
	 * @param equations
	 *            set of output definitions representing the unifier
	 */
	public UelOutputImpl(Set<Equation> equations) {
		this.equations = equations;
	}

	@Override
	public Set<Equation> getEquations() {
		return this.equations;
	}

}
