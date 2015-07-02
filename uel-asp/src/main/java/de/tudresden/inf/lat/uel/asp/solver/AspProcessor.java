package de.tudresden.inf.lat.uel.asp.solver;

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
	private boolean initialized;
	private Set<Equation> currentUnifier;
	private boolean isSynchronized;
	private boolean minimize;

	public AspProcessor(UelInput input, boolean minimize) {
		this.uelInput = input;
		this.aspInput = new AspInput(input.getEquations(),
				input.getGoalDisequations(), input.getAtomManager(),
				input.getUserVariables());
		this.initialized = false;
		this.currentUnifier = null;
		this.isSynchronized = false;
		this.minimize = minimize;
	}

	@Override
	public void cleanup() {
		if (aspOutput != null) {
			// the AspOutput may have a reference to an asp solver that needs to
			// be stopped
			aspOutput.cleanup();
		}
	}

	@Override
	public boolean computeNextUnifier() throws InterruptedException {
		// TODO: implement asynchronous execution of ClingoSolver
		if (!initialized) {
			AspSolver solver = new ClingoSolver(!uelInput.getGoalDisequations()
					.isEmpty(), false, minimize);
			try {
				aspOutput = solver.solve(aspInput);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			initialized = true;
		}

		isSynchronized = false;
		boolean hasNext = aspOutput.hasNext();
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		return hasNext;
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
		if (!aspOutput.hasNext()) {
			throw new IllegalStateException("There are no more unifiers.");
		}

		if (!isSynchronized) {
			currentUnifier = toUnifier(aspOutput.next());
			isSynchronized = true;
		}
		return new UelOutputImpl(uelInput.getAtomManager(), currentUnifier);
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
