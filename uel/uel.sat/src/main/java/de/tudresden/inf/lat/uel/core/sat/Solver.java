package de.tudresden.inf.lat.uel.core.sat;

import java.io.IOException;

/**
 * An object implementing this interface can solve a SAT problem.
 * 
 * @author Julian Mendez
 */
public interface Solver {

	public static final Integer END_OF_CLAUSE = 0;

	public static final String NEWLINE = "\n";

	public static final String P_CNF = "p cnf";

	/**
	 * First string returned by the solver when the SAT problem is satisfiable.
	 */
	public static final String SAT = "SAT";

	public static final String SPACE = " ";
	
	/**
	 * First string returned by the solver when the SAT problem is not
	 * satisfiable.
	 */
	public static final String UNSAT = "UNSAT";

	/**
	 * Solves a SAT problem. The input must be in the DIMACS CNF format.
	 * 
	 * @param input
	 *            SAT problem to solve
	 * @return an output of a SAT solver
	 * @throws IOException
	 */
	public SatOutput solve(SatInput input) throws IOException;

}
