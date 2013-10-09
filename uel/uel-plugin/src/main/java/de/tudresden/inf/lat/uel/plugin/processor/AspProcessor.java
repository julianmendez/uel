package de.tudresden.inf.lat.uel.plugin.processor;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.api.UelOutput;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.EquationImpl;
import de.tudresden.inf.lat.uel.type.impl.UelOutputImpl;

public class AspProcessor implements UelProcessor {

	private UelInput uelInput;
	private AspInput aspInput;
	private AspOutput aspOutput;
	private boolean computed;
	private int currentUnifierIndex;

	public AspProcessor(UelInput input) {
		this.uelInput = input;
		this.aspInput = new AspInput(input.getEquations(),
				input.getAtomManager(), input.getUserVariables());
		this.computed = false;
		this.currentUnifierIndex = -1;
	}

	@Override
	public boolean computeNextUnifier() {
		if (!computed) {
			AspSolver solver = new ClaspSolver();
			try {
				aspOutput = solver.solve(aspInput);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			currentUnifierIndex = 0;
			computed = true;
			return aspOutput.isSatisfiable();
		} else {
			currentUnifierIndex++;
			return currentUnifierIndex < aspOutput.getAssignments().size();
		}
	}

	@Override
	public List<Entry<String, String>> getInfo() {
		Entry<String, String> e = new AbstractMap.SimpleEntry<String, String>(
				"ASP encoding", aspInput.getProgram());
		List<Entry<String, String>> res = new ArrayList<Entry<String, String>>();
		res.add(e);
		return res;
	}

	@Override
	public UelInput getInput() {
		return this.uelInput;
	}

	@Override
	public UelOutput getUnifier() {
		if (aspOutput == null) {
			throw new IllegalStateException(
					"The unifiers have not been computed yet.");
		}
		if (!aspOutput.isSatisfiable()) {
			throw new IllegalStateException("There are no unifiers.");
		}
		if (currentUnifierIndex >= aspOutput.getAssignments().size()) {
			throw new IllegalStateException("There are no more unifiers.");
		}

		Map<Integer, Set<Integer>> assignment = aspOutput.getAssignments().get(
				currentUnifierIndex);
		return new UelOutputImpl(uelInput.getAtomManager(),
				toUnifier(assignment));
	}

	private Set<Equation> toUnifier(Map<Integer, Set<Integer>> assignment) {
		Set<Equation> equations = new HashSet<Equation>();
		for (Atom at : uelInput.getAtomManager()) {
			if (at.isConceptName()) {
				ConceptName name = (ConceptName) at;
				if (name.isVariable()) {
					Integer nameId = name.getConceptNameId();
					Set<Integer> body = assignment.get(nameId);
					if (body == null) {
						body = Collections.emptySet();
					}
					equations.add(new EquationImpl(nameId, body, false));
				}
			}
		}
		return equations;
	}
}
