package de.tudresden.inf.lat.uel.core.sat;

import java.io.IOException;

/**
 * 
 * @author Julian Mendez
 */
public interface Solver {

	public static final String msgSat = "SAT";
	public static final String msgUnsat = "UNSAT";

	public String solve(String input) throws IOException;

}
