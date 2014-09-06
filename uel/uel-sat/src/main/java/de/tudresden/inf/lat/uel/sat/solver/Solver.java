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
	Integer END_OF_CLAUSE = 0;

	/**
	 * A string representing a line break.
	 */
	String NEWLINE = System.getProperty("line.separator");

	/**
	 * The string that signals the start of a DIMACS CNF file.
	 */
	String P_CNF = "p cnf";

	/**
	 * The string that signals the start of a WCNF file.
	 */
	String P_WCNF = "p wcnf";

	/**
	 * First string returned by the solver when the SAT problem is satisfiable.
	 */
	String SAT = "SAT";

	/**
	 * The string ' '.
	 */
	String SPACE = " ";

	/**
	 * First string returned by the solver when the SAT problem is not
	 * satisfiable.
	 */
	String UNSAT = "UNSAT";

	/**
	 * Solves a SAT problem. The input must be in the DIMACS CNF format.
	 *
	 * @param input
	 *            SAT problem to solve
	 * @return the output of the SAT solver
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	SatOutput solve(SatInput input) throws IOException;

	/**
	 * Updates the last solved SAT instances by adding one clause and solves the
	 * resulting problem.
	 *
	 * @param clause
	 *            the clause to be added
	 * @return the output of the SAT solver
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	SatOutput update(Set<Integer> clause) throws IOException;

}
