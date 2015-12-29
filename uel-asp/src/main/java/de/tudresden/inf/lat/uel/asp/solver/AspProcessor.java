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

import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

public class AspProcessor implements UelProcessor {

	private Goal goal;
	private AspInput aspInput;
	private AspOutput aspOutput;
	private boolean initialized;
	private Unifier currentUnifier;
	private boolean isSynchronized;
	private boolean minimize;

	public AspProcessor(Goal goal, boolean minimize) {
		this.goal = goal;
		this.aspInput = new AspInput(goal);
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
			AspSolver solver = new ClingoSolver(goal.hasNegativePart(), false, minimize);
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
		Entry<String, String> e = new AbstractMap.SimpleEntry<String, String>("ASP encoding", aspInput.getProgram());
		List<Entry<String, String>> res = new ArrayList<Entry<String, String>>();
		res.add(e);
		return res;
	}

	@Override
	public Goal getGoal() {
		return this.goal;
	}

	@Override
	public Unifier getUnifier() {
		if (aspOutput == null) {
			throw new IllegalStateException("The unifiers have not been computed yet.");
		}
		if (!aspOutput.hasNext()) {
			throw new IllegalStateException("There are no more unifiers.");
		}

		if (!isSynchronized) {
			currentUnifier = toUnifier(aspOutput.next());
			isSynchronized = true;
		}
		return currentUnifier;
	}

	private Unifier toUnifier(Map<Integer, Set<Integer>> assignment) {
		Set<Definition> definitions = new HashSet<Definition>();
		for (Integer varId : goal.getAtomManager().getVariables()) {
			Set<Integer> body = assignment.get(varId);
			if (body == null) {
				body = Collections.emptySet();
			}
			definitions.add(new Definition(varId, body, false));
		}
		return new Unifier(definitions);
	}
}
