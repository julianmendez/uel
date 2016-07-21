package de.tudresden.inf.lat.uel.sat.solver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.TimeoutException;

import de.tudresden.inf.lat.uel.sat.type.SatInput;
import de.tudresden.inf.lat.uel.sat.type.SatOutput;
import de.tudresden.inf.lat.uel.sat.type.SatSolver;

/**
 * An object of this class uses the Sat4j MaxSAT solver to solve a SAT problem
 * in which a given subset of the propositional variables should be minimized
 * (minimal number of variables set to 1 (true)).
 * 
 * @author Stefan Borgwardt
 */
public class Sat4jMaxSatSolver implements SatSolver {

	private Integer nbVars;
	private WeightedMaxSatDecorator solver;
	private boolean cleanedUp = false;

	/**
	 * Constructs a new solver.
	 */
	public Sat4jMaxSatSolver() {
	}

	@Override
	public void cleanup() {
		if ((solver != null) && !cleanedUp) {
			solver.reset();
			// we only need to reset the solver once
			cleanedUp = true;
		}
	}

	private SatOutput getSatOutput() throws InterruptedException {
		IOptimizationProblem problem = new PseudoOptDecorator(solver, false);
		Set<Integer> model = new HashSet<Integer>();
		boolean satisfiable = false;
		try {
			while (problem.admitABetterSolution()) {
				satisfiable = true;
				// counter++;
				problem.discardCurrentSolution();

				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
			}
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		} catch (ContradictionException e) {
			// this means that the current model is optimal
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
	public SatOutput solve(SatInput input) throws InterruptedException {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		// TODO: use own VarOrder and PhaseSelectionStrategy?

		solver = new WeightedMaxSatDecorator(SolverFactory.newDefault());
		nbVars = input.getLastId();
		solver.newVar(nbVars);
		solver.addLiteralsToMinimize(new VecInt(SatInput.toArray(input.getMinimizeLiterals())));
		for (Set<Integer> clause : input.getClauses()) {
			try {
				solver.addHardClause(new VecInt(SatInput.toArray(clause)));
			} catch (ContradictionException e) {
				return new SatOutput(false, Collections.emptySet());
			}

			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
		}
		for (Set<Integer> clause : input.getSoftClauses()) {
			try {
				solver.addSoftClause(new VecInt(SatInput.toArray(clause)));
			} catch (ContradictionException e) {
				return new SatOutput(false, Collections.emptySet());
			}

			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
		}

		return getSatOutput();
	}

	public SatOutput update(Set<Integer> clause) throws InterruptedException {
		try {
			solver.addHardClause(new VecInt(SatInput.toArray(clause)));
		} catch (ContradictionException e) {
			return new SatOutput(false, Collections.<Integer> emptySet());
		}
		return getSatOutput();
	}
}
