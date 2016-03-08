package de.tudresden.inf.lat.uel.asp.solver;

import java.io.IOException;

/**
 * An abstract interface to an ASP solver.
 * 
 * @author stefborg
 * 
 */
public interface AspSolver {

	AspOutput solve(AspInput input) throws IOException;

	void cleanup();

}
