package de.tudresden.inf.lat.uel.plugin.processor;

/**
 * An abstract interface to an ASP solver.
 * @author stefborg
 *
 */
public interface AspSolver {

	public AspOutput solve(AspInput input);

}
