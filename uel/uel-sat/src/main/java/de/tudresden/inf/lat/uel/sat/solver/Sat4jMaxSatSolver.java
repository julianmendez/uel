package de.tudresden.inf.lat.uel.sat.solver;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.TimeoutException;

/**
 * An object of this class uses the Sat4j MaxSAT solver to solve a SAT problem in which a given
 * subset of the propositional variables should be minimized (minimal number of variables set to
 * 1 (true)).
 * 
 * @author Stefan Borgwardt
 */
public class Sat4jMaxSatSolver implements Solver {

	private Integer nbVars;
	private WeightedMaxSatDecorator solver;

	/**
	 * Constructs a new solver.
	 */
	public Sat4jMaxSatSolver() {
	}

	private SatOutput getSatOutput() {
		IOptimizationProblem problem = solver;
		Set<Integer> model = new TreeSet<Integer>();
		boolean satisfiable = false;
		int counter = 0;
		try {
			while (problem.admitABetterSolution()) {
				satisfiable = true;
				counter++;
				problem.discardCurrentSolution();
			}
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		} catch (ContradictionException e) {
			// this means that the current model is optimal
		}
		if (counter > 2) {
			System.out.println(counter);
		}
		if (satisfiable) {
			for (int i = 1; i <= nbVars; i++) {
				if (problem.model(i)) {
					model.add(i);
				}
			}
		}

		return new SatOutput(satisfiable, model);
	}

	@Override
	public SatOutput solve(SatInput input) {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		
		// TODO: use own VarOrder and PhaseSelectionStrategy?

		solver = new WeightedMaxSatDecorator(SolverFactory.newDefault());
		nbVars = input.getLastId();
		solver.newVar(nbVars);
		solver.addLiteralsToMinimize(new VecInt(SatInput.toArray(input
				.getMinimizeLiterals())));
		for (Set<Integer> clause : input.getClauses()) {
			try {
				solver.addHardClause(new VecInt(SatInput.toArray(clause)));
			} catch (ContradictionException e) {
				return new SatOutput(false, Collections.<Integer> emptySet());
			}
		}
		return getSatOutput();
	}

	public SatOutput update(Set<Integer> clause) {
		try {
			solver.addHardClause(new VecInt(SatInput.toArray(clause)));
		} catch (ContradictionException e) {
			return new SatOutput(false, Collections.<Integer> emptySet());
		}
		return getSatOutput();
	}

}
