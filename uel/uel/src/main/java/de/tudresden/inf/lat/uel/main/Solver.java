package de.tudresden.inf.lat.uel.main;

import java.io.IOException;

public interface Solver {

	public static final String msgSat = "SAT";
	public static final String msgUnsat = "UNSAT";

	public String solve(String input) throws IOException;

}
