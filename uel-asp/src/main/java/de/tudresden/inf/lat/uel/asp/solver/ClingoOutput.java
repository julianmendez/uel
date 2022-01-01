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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

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

	private class MyInputStream extends InputStream {
		InputStream inner;
		int i = 0;

		MyInputStream(InputStream inner) {
			this.inner = inner;
		}

		@Override
		public int read() throws IOException {
			i++;
			if ((i % 4000) == 0) {
				System.out.print("#");
			}
			int ret = inner.read();
			// System.out.print((char) ret);
			return ret;
		}

	}

	private final AtomManager atomManager;
	private Map<Integer, Set<Integer>> currentAssignment = null;
	private boolean hasNext = false;
	private final List<Entry<String, String>> info;
	private boolean isComputed = false;
	private JsonParser parser = null;
	private final InputStream jsonStream;
	private final ClingoSolver solver;

	public ClingoOutput(InputStream jsonStream, AtomManager atomManager, ClingoSolver solver) throws IOException {
		this.jsonStream = jsonStream;
		this.solver = solver;
		this.atomManager = atomManager;
		this.info = new ArrayList<Entry<String, String>>();
	}

	private boolean addInfo(String key, String value) {
		return info.add(new AbstractMap.SimpleEntry<String, String>(key, value));
	}

	@Override
	public void cleanup() {
		solver.cleanup();
	}

	private void compute() throws IOException, InterruptedException {
		if (!isComputed) {
			if (parser == null) {
				// initialize the parser and parse the initial portion of the
				// stream
				parser = new JsonFactory().createJsonParser(jsonStream);
				parseInfo();
			}

			// try to parse the next assignment from the input stream
			hasNext = parseNextAssignment();

			if (Thread.interrupted()) {
				hasNext = false;
				throw new InterruptedException();
			}

			if (!hasNext) {
				// we just finished -> parse the remainder of the stream for
				// additional information
				parseInfo();
			}

			isComputed = true;
		}
	}

	private void expected(JsonToken token, JsonToken type, String descr) throws IOException, JsonParseException {
		if (token != type) {
			expected(descr);
		}
	}

	private void expected(String descr) throws JsonParseException {
		throw new JsonParseException("Expected " + descr + ".", parser.getCurrentLocation());
	}

	private void extendAssignment(Map<Integer, Set<Integer>> assignment, String subsumption) throws IOException {
		int parenthesisIndex = subsumption.indexOf(')');
		Integer varId = Integer.parseInt(subsumption.substring(13, parenthesisIndex));
		Atom nonVarAtom = parseAtom(subsumption.substring(parenthesisIndex + 2, subsumption.length() - 1));
		Integer atomId = atomManager.getIndex(nonVarAtom);

		Set<Integer> subsumers = assignment.get(varId);
		if (subsumers == null) {
			subsumers = new HashSet<>();
			assignment.put(varId, subsumers);
		}
		subsumers.add(atomId);
	}

	public List<Entry<String, String>> getInfo() {
		return info;
	}

	@Override
	public boolean hasNext() throws IOException, InterruptedException {
		compute();
		return hasNext;
	}

	@Override
	public Map<Integer, Set<Integer>> next() throws IOException, InterruptedException {
		compute();
		if (!hasNext) {
			throw new NoSuchElementException();
		}
		isComputed = false;
		return currentAssignment;
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

	private void parseInfo() throws IOException {
		// parse information from the stream, stop when 'Witnesses' entry is
		// reached
		while (parser.nextToken() != null) {
			if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
				switch (parser.getCurrentName()) {
				case "Witnesses":
					// advance to the first witness and wait until assignments
					// are requested
					expected(parser.nextToken(), JsonToken.START_ARRAY, "Expected an array of witnesses.");
					return;
				case "Solver":
					expected(parser.nextToken(), JsonToken.VALUE_STRING, "Expected the solver name and version.");
					addInfo("ASP solver", parser.getText());
					break;
				case "Time":
					parseTimeInfo();
					break;
				}
			}
		}
		// end of stream
		solver.cleanup();
	}

	private boolean parseNextAssignment() throws IOException {
		if (parser.nextToken() == null) {
			// stream has already finished -> there are no models at all
			return false;
		}
		if (parser.getCurrentToken() == JsonToken.END_ARRAY) {
			// end of 'Witnesses' array reached
			return false;
		}

		expected(parser.getCurrentToken(), JsonToken.START_OBJECT, "start of new model");
		expected(parser.nextToken(), JsonToken.FIELD_NAME, "model entry");
		if (!parser.getCurrentName().equals("Value")) {
			expected("'Value' entry");
		}
		expected(parser.nextToken(), JsonToken.START_ARRAY, "start of atom list");

		currentAssignment = new HashMap<Integer, Set<Integer>>();
		while (parser.nextToken() != JsonToken.END_ARRAY) {
			expected(parser.getCurrentToken(), JsonToken.VALUE_STRING, "atom");
			if (!parser.getText().startsWith("relsubs")) {
				expected("'relsubs' atom");
			}
			extendAssignment(currentAssignment, parser.getText());
		}

		expected(parser.nextToken(), JsonToken.END_OBJECT, "end of model");
		return true;
	}

	private void parseTimeInfo() throws IOException {
		expected(parser.nextToken(), JsonToken.START_OBJECT, "object with timing information");
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			expected(parser.getCurrentToken(), JsonToken.FIELD_NAME, "timing entry");
			expected(parser.nextToken(), JsonToken.VALUE_NUMBER_FLOAT, "time value");
			addInfo(parser.getCurrentName() + " time (s)", String.format("%.3f", parser.getFloatValue()));
		}
	}

}
