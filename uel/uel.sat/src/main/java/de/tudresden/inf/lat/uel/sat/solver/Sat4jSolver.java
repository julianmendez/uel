package de.tudresden.inf.lat.uel.sat.solver;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * An object of this class uses the Sat4j solver to solve a SAT problem.
 * 
 * @author Julian Mendez
 */
public class Sat4jSolver implements Solver {

	/**
	 * Constructs a new solver.
	 */
	public Sat4jSolver() {
	}

	@Override
	public SatOutput solve(SatInput input) {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		ISolver solver = SolverFactory.newDefault();
		solver.newVar(input.getLastId());
		solver.setExpectedNumberOfClauses(input.getClauses().size());
		for (Collection<Integer> clause : input.getClauses()) {
			try {
				solver.addClause(new VecInt(toArray(clause)));
			} catch (ContradictionException e) {
				return new SatOutput(false, Collections.<Integer> emptySet());
			}
		}

		IProblem problem = solver;
		Set<Integer> model = new TreeSet<Integer>();
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
		}

		return new SatOutput(satisfiable, model);
	}

	private int[] toArray(Collection<Integer> clause) {
		int[] ret = new int[clause.size()];
		int index = 0;
		for (Integer var : clause) {
			ret[index] = var;
			index++;
		}
		return ret;
	}

}
