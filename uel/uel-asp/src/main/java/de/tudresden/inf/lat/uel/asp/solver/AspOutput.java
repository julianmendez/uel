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
	 * Returns several mappings from the variables to their subsuming
	 * non-variable atoms.
	 */
	List<Map<Integer, Set<Integer>>> getAssignments();

	/**
	 * Returns a value indicating whether the ASP solver output "satisfiable".
	 */
	boolean isSatisfiable();

	/**
	 * Returns a list of stats provided by the ASP solver.
	 */
	List<Entry<String, String>> getStats();

}
