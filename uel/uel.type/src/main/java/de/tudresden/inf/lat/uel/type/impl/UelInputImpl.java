package de.tudresden.inf.lat.uel.type.impl;

import java.util.HashSet;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.SmallEquation;
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
	private final Set<Equation> definitions;
	private final Set<Equation> goalEquations;
	private final Set<SmallEquation> goalDisequations;
	private final Set<Equation> equations;
	private final Set<Integer> userVariables;

	/**
	 * Constructs a new UEL input based on a specified ontology.
	 * 
	 * @param manager
	 *            atom manager
	 * @param defs
	 *            set of definitions
	 * @param goalEqs
	 *            set of goal equations
	 * @param goalDiseqs
	 *            set of goal disequations
	 * @param vars
	 *            set of user variables
	 */
	public UelInputImpl(IndexedSet<Atom> manager, Set<Equation> defs,
			Set<Equation> goalEqs, Set<SmallEquation> goalDiseqs,
			Set<Integer> vars) {
		this.atomManager = manager;
		this.definitions = defs;
		this.goalEquations = goalEqs;
		this.goalDisequations = goalDiseqs;
		this.equations = new HashSet<Equation>();
		equations.addAll(defs);
		equations.addAll(goalEqs);
		this.userVariables = vars;
	}

	@Override
	public IndexedSet<Atom> getAtomManager() {
		return this.atomManager;
	}

	@Override
	public Set<Equation> getDefinitions() {
		return this.definitions;
	}

	@Override
	public Set<Equation> getGoalEquations() {
		return this.goalEquations;
	}

	@Override
	public Set<SmallEquation> getGoalDisequations() {
		return this.goalDisequations;
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
