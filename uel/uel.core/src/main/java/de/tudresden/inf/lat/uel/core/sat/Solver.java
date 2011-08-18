package de.tudresden.inf.lat.uel.core.sat;

import java.io.IOException;

/**
 * An object implementing this interface can solve a SAT problem.
 * 
 * @author Julian Mendez
 */
public interface Solver {

	/**
	 * First string returned by the solver when the SAT problem is satisfiable.
	 */
	public static final String msgSat = "SAT";

	/**
	 * First string returned by the solver when the SAT problem is not
	 * satisfiable.
	 */
	public static final String msgUnsat = "UNSAT";

	/**
	 * Solves a SAT problem. The input must be in the DIMACS CNF format.
	 * 
	 * @param input
	 *            SAT problem to solve
	 * @return a line containing either SAT (satisfiable) or UNSAT
	 *         (unsatisfiable), and, if satisfiable, a model.
	 * @throws IOException
	 */
	public String solve(SatInput input) throws IOException;

}
