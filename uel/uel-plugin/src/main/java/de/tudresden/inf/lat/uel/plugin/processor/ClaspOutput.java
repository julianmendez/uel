package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;

/**
 * This class parses the output of clasp.
 * 
 * @author Stefan Borgwardt
 * 
 */
public class ClaspOutput implements AspOutput {

	private static String SATISFIABLE = "SATISFIABLE";

	private IndexedSet<Atom> atomManager;
	private List<Map<Integer, Set<Integer>>> assignments;
	private boolean satisfiable;
	private List<Entry<String, String>> stats;

	public ClaspOutput(String json, IndexedSet<Atom> atomManager) {
		this.atomManager = atomManager;
		parse(json);
	}

	private void parse(String json) {
		// TODO: parse json input
		// Witnesses -> assignments (using atomManager)
		// Result -> satisfiable
		// Solver,Stats>Time -> stats
	}

	public List<Map<Integer, Set<Integer>>> getAssignments() {
		return assignments;
	}

	public boolean isSatisfiable() {
		return satisfiable;
	}

	public List<Entry<String, String>> getStats() {
		return stats;
	}

}
