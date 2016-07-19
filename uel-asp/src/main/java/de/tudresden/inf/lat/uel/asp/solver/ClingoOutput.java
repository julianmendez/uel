package de.tudresden.inf.lat.uel.asp.solver;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
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

	private ClingoSolver solver;
	private AtomManager atomManager;
	private List<Map<Integer, Set<Integer>>> assignments;
	private List<Entry<String, String>> stats;
	private int currentIndex;
	private boolean finished;

	public ClingoOutput(ClingoSolver solver, AtomManager atomManager) {
		this.solver = solver;
		this.atomManager = atomManager;
		this.assignments = new ArrayList<Map<Integer, Set<Integer>>>();
		this.currentIndex = -1;
		this.finished = false;
	}

	@Override
	public void cleanup() {
		solver.cleanup();
	}

	/**
	 * Parse JSON output.
	 */
	private void parse(InputStream jsonStream) throws IOException, InterruptedException {

		// JsonParser p = new JsonFactory().createJsonParser(jsonStream);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readValue(jsonStream, JsonNode.class);

		// Result -> satisfiable
		String result = root.get("Result").asText();
		if (result.equals(SATISFIABLE) || result.equals(OPTIMUM_FOUND)) {
			// Witnesses -> assignments (using atomManager)
			for (JsonNode witness : root.get("Call").get(0).get("Witnesses")) {
				// System.out.println();
				// System.out.println();
				// System.out.println("New Witness!");
				// System.out.println();
				// System.out.println();
				Map<Integer, Set<Integer>> assignment = new HashMap<Integer, Set<Integer>>();
				// Pattern p = Pattern.compile("var\\(x(.*?)\\)");
				for (JsonNode subsumption : witness.get("Value")) {
					String text = subsumption.asText();
					if (text.startsWith("relsubs")) {
						extendAssignment(assignment, text);
						// } else if (text.startsWith("compatible")) {
						// System.out.println(text);
						// Matcher m = p.matcher(text);
						// System.out.print("Compatible: ");
						// while (m.find()) {
						// Integer varId = Integer.parseInt(m.group(1));
						// System.out.print(solver.parent.printAtom(varId) + " /
						// ");
						// }
						// System.out.println();
					}
				}
				if (!assignments.contains(assignment)) {
					assignments.add(assignment);
				}

				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
			}
		}

		// Solver,Time -> stats
		stats = new ArrayList<Entry<String, String>>();
		addEntry(stats, "ASP Solver", root.get("Solver").asText());
		JsonNode time = root.get("Time");
		addEntry(stats, "Total time (s)", time.get("Total").asText());
		addEntry(stats, "Solving time (s)", time.get("Solve").asText());
		addEntry(stats, "Model time (s)", time.get("Model").asText());
		addEntry(stats, "Unsat time (s)", time.get("Unsat").asText());
		addEntry(stats, "CPU time (s)", time.get("CPU").asText());
	}

	private void extendAssignment(Map<Integer, Set<Integer>> assignment, String subsumption) throws IOException {
		int parenthesisIndex = subsumption.indexOf(')');
		Integer varId = Integer.parseInt(subsumption.substring(13, parenthesisIndex));
		Atom nonVarAtom = parseAtom(subsumption.substring(parenthesisIndex + 2, subsumption.length() - 1));
		Integer atomId = atomManager.getIndex(nonVarAtom);

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
			Integer roleId = Integer.parseInt(encoding.substring(8, commaIndex));
			ConceptName conceptName = (ConceptName) parseAtom(
					encoding.substring(commaIndex + 1, encoding.length() - 1));
			return new ExistentialRestriction(roleId, conceptName);
		case 'c':
			Integer atomId = Integer.parseInt(encoding.substring(7, encoding.length() - 1));
			return atomManager.getAtom(atomId);
		case 'v':
			atomId = Integer.parseInt(encoding.substring(5, encoding.length() - 1));
			return atomManager.getAtom(atomId);
		default:
			throw new IOException("Invalid atom encoding.");
		}
	}

	private boolean addEntry(List<Map.Entry<String, String>> list, String key, String value) {
		return list.add(new AbstractMap.SimpleEntry<String, String>(key, value));
	}

	public List<Entry<String, String>> getStats() {
		return stats;
	}

	@Override
	public boolean hasNext() throws InterruptedException {
		if (currentIndex + 1 < assignments.size()) {
			// we still have pre-computed assignments left
			return true;
		} else {
			// there are no more pre-computed assignments
			if (finished) {
				// the computation has finished -> all assignments have been
				// returned
				return false;
			} else {
				// try to compute more assignments until either clingo reports
				// that there are no more solutions or we have computed at least
				// one additional assignment
				do {
					finished = solver.computeMoreSolutions();
					if (Thread.currentThread().isInterrupted()) {
						return false;
					}
					try {
						parse(solver.getCurrentSolutions());
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				} while ((currentIndex + 1 >= assignments.size()) && !finished);
				// check if we now have at least one new assignment
				return currentIndex + 1 < assignments.size();
			}
		}
	}

	@Override
	public Map<Integer, Set<Integer>> next() throws InterruptedException {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		currentIndex++;
		return assignments.get(currentIndex);
	}

}
