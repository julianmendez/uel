/**
 *
 */
package de.tudresden.inf.lat.uel.asp.solver;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents the output of an ASP solver.
 *
 * @author stefborg
 *
 */
public interface AspOutput {

	/**
	 * Release any resources that have been used.
	 */
	void cleanup();

	/**
	 * Returns a list of stats provided by the ASP solver.
	 *
	 * @return a list of stats provided by the ASP solver
	 */
	List<Entry<String, String>> getStats();

	boolean hasNext() throws InterruptedException;

	Map<Integer, Set<Integer>> next() throws InterruptedException;

}
