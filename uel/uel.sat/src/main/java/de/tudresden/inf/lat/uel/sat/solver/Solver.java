package de.tudresden.inf.lat.uel.sat.solver;

import java.io.IOException;

/**
 * An object implementing this interface can solve a SAT problem.
 * 
 * @author Julian Mendez
 */
public interface Solver {

	/**
	 * The integer 0 that signals the end of a clause.
	 */
	public static final Integer END_OF_CLAUSE = 0;

	/**
	 * A string representing a line break.
	 */
	public static final String NEWLINE = "\n";

	/**
	 * The string that signals the start of a DIMACS CNF file.
	 */
	public static final String P_CNF = "p cnf";

	/**
	 * First string returned by the solver when the SAT problem is satisfiable.
	 */
	public static final String SAT = "SAT";

	/**
	 * The string ' '.
	 */
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
