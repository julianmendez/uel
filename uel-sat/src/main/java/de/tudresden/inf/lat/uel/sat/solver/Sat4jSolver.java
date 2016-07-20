package de.tudresden.inf.lat.uel.sat.solver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import de.tudresden.inf.lat.uel.sat.type.SatInput;
import de.tudresden.inf.lat.uel.sat.type.SatOutput;
import de.tudresden.inf.lat.uel.sat.type.SatSolver;

/**
 * An object of this class uses the Sat4j solver to solve a SAT problem.
 * 
 * @author Julian Mendez
 */
public class Sat4jSolver implements SatSolver {

	private ISolver solver;
	private boolean cleanedUp = false;

	/**
	 * Constructs a new solver.
	 */
	public Sat4jSolver() {
	}

	public void cleanup() {
		if ((solver != null) && !cleanedUp) {
			solver.reset();
			// we only need to reset the solver once
			cleanedUp = true;
		}
	}

	private SatOutput getSatOutput() {
		IProblem problem = solver;
		Set<Integer> model = new HashSet<Integer>();
		boolean satisfiable;
		try {
			satisfiable = problem.isSatisfiable();
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}
		if (satisfiable) {
			for (Integer e : problem.model()) {
				model.add(e);
			}
		} else {
			// TODO: unsat proof?
		}

		return new SatOutput(satisfiable, model);
	}

	@Override
	public SatOutput solve(SatInput input) throws InterruptedException {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		solver = SolverFactory.newDefault();
		solver.newVar(input.getLastId());
		for (Set<Integer> clause : input.getClauses()) {
			try {
				solver.addClause(new VecInt(SatInput.toArray(clause)));
			} catch (ContradictionException e) {
				return new SatOutput(false, Collections.<Integer> emptySet());
			}

			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
		}
		return getSatOutput();
	}

	public SatOutput update(Set<Integer> clause) {
		try {
			solver.addClause(new VecInt(SatInput.toArray(clause)));
		} catch (ContradictionException e) {
			return new SatOutput(false, Collections.<Integer> emptySet());
		}
		return getSatOutput();
	}
}
