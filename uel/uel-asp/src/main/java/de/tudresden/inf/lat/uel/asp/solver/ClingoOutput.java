package de.tudresden.inf.lat.uel.asp.solver;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

/**
 * This class parses the output of clingo.
 * 
 * @author Stefan Borgwardt
 * 
 */
public class ClingoOutput implements AspOutput {

	private static String SATISFIABLE = "SATISFIABLE";
	private static String OPTIMUM_FOUND = "OPTIMUM FOUND";

	private IndexedSet<Atom> atomManager;
	private List<Map<Integer, Set<Integer>>> assignments;
	private boolean satisfiable;
	private List<Entry<String, String>> stats;

	public ClingoOutput(String json, boolean searchCompleted,
			IndexedSet<Atom> atomManager) throws IOException {
		stats = new ArrayList<Entry<String, String>>();
		addEntry(stats, "Search Completed", searchCompleted ? "Yes" : "No");
		this.atomManager = atomManager;
		// System.out.println(json);
		parse(json);
	}

	/**
	 * Parse JSON output.
	 */
	private void parse(String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readValue(json, JsonNode.class);

		// Result -> satisfiable
		String result = root.get("Result").asText();
		satisfiable = result.equals(SATISFIABLE)
				|| result.equals(OPTIMUM_FOUND);
		if (satisfiable) {
			// Witnesses -> assignments (using atomManager)
			assignments = new ArrayList<Map<Integer, Set<Integer>>>();
			for (JsonNode witness : root.get("Call").get(0).get("Witnesses")) {
				Map<Integer, Set<Integer>> assignment = new HashMap<Integer, Set<Integer>>();
				for (JsonNode subsumption : witness.get("Value")) {
					String text = subsumption.asText();
					if (text.startsWith("relsubs")) {
						extendAssignment(assignment, subsumption.asText());
					}
				}
				assignments.add(assignment);
			}
		}

		// Solver,Time -> stats
		addEntry(stats, "ASP Solver", root.get("Solver").asText());
		JsonNode time = root.get("Time");
		addEntry(stats, "Total time (s)", time.get("Total").asText());
		addEntry(stats, "Solving time (s)", time.get("Solve").asText());
		addEntry(stats, "Model time (s)", time.get("Model").asText());
		addEntry(stats, "Unsat time (s)", time.get("Unsat").asText());
		addEntry(stats, "CPU time (s)", time.get("CPU").asText());
	}

	private void extendAssignment(Map<Integer, Set<Integer>> assignment,
			String subsumption) throws IOException {
		int parenthesisIndex = subsumption.indexOf(')');
		Integer cnameId = Integer.parseInt(subsumption.substring(13,
				parenthesisIndex));
		Integer varId = atomManager.addAndGetIndex(new ConceptName(cnameId,
				true));
		Atom nonVarAtom = parseAtom(subsumption.substring(parenthesisIndex + 2,
				subsumption.length() - 1));
		Integer atomId = atomManager.addAndGetIndex(nonVarAtom);

		Set<Integer> subsumers = assignment.get(varId);
		if (subsumers == null) {
			subsumers = new HashSet<Integer>();
			assignment.put(varId, subsumers);
		}
		subsumers.add(atomId);
	}

	private Atom parseAtom(String encoding) throws IOException {
		switch (encoding.charAt(0)) {
		case 'e':
			int commaIndex = encoding.indexOf(',');
			Integer roleId = Integer
					.parseInt(encoding.substring(8, commaIndex));
			ConceptName conceptName = (ConceptName) parseAtom(encoding
					.substring(commaIndex + 1, encoding.length() - 1));
			return new ExistentialRestriction(roleId, conceptName);
		case 'c':
			Integer cnameId = Integer.parseInt(encoding.substring(7,
					encoding.length() - 1));
			return new ConceptName(cnameId, false);
		case 'v':
			cnameId = Integer.parseInt(encoding.substring(5,
					encoding.length() - 1));
			return new ConceptName(cnameId, true);
		default:
			throw new IOException("Invalid atom encoding.");
		}
	}

	private boolean addEntry(List<Map.Entry<String, String>> list, String key,
			String value) {
		return list
				.add(new AbstractMap.SimpleEntry<String, String>(key, value));
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
