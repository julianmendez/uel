package de.tudresden.inf.lat.uel.sat.solver;

import java.io.IOException;
import java.util.Set;

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
	public static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * The string that signals the start of a DIMACS CNF file.
	 */
	public static final String P_CNF = "p cnf";

	/**
	 * The string that signals the start of a WCNF file.
	 */
	public static final String P_WCNF = "p wcnf";

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
	 * @return the output of the SAT solver
	 * @throws IOException
	 */
	public SatOutput solve(SatInput input) throws IOException;

	/**
	 * Updates the last solved SAT instances by adding one clause and solves the
	 * resulting problem.
	 * 
	 * @param clause
	 *            the clause to be added
	 * @return the output of the SAT solver
	 * @throws IOException
	 */
	public SatOutput update(Set<Integer> clause) throws IOException;

}