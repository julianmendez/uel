package de.tudresden.inf.lat.uel.asp.solver;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.impl.AbstractUnificationAlgorithm;
import de.tudresden.inf.lat.uel.type.impl.DefinitionSet;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

public class AspUnificationAlgorithm extends AbstractUnificationAlgorithm {

	private AspInput aspInput;
	private AspOutput aspOutput;
	private boolean initialized;
	private Unifier currentUnifier;
	private boolean minimize;
	private boolean hasNext;

	public AspUnificationAlgorithm(Goal goal, boolean minimize) {
		super(goal);
		this.initialized = false;
		this.currentUnifier = null;
		this.minimize = minimize;
		this.hasNext = false;
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
		try {
			if (!initialized) {
				aspInput = new AspInput(goal, this);
				AspSolver solver = new ClingoSolver(goal.hasNegativePart(), !goal.getTypes().isEmpty(), minimize, this);
				aspOutput = solver.solve(aspInput);
				callbackPreprocessing();
				initialized = true;
			}

			hasNext = aspOutput.hasNext();
			if (hasNext) {
				currentUnifier = toUnifier(aspOutput.next());
			}
			return hasNext;
		} catch (IOException e) {
			if (!e.getMessage().contains("Stream closed")) {
				// ignore 'Stream closed', because it was caused by 'cleanup'
				// being called from another thread
				cleanup();
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	@Override
	public Unifier getUnifier() {
		if (aspOutput == null) {
			throw new IllegalStateException("The unifiers have not been computed yet.");
		}
		if (!hasNext) {
			throw new IllegalStateException("There are no more unifiers.");
		}
		if (currentUnifier == null) {
			throw new IllegalStateException("Failed to compute unifier.");
		}

		return currentUnifier;
	}

	private Unifier toUnifier(Map<Integer, Set<Integer>> assignment) {
		DefinitionSet definitions = new DefinitionSet(goal.getAtomManager().getVariables().size());
		for (Integer varId : goal.getAtomManager().getVariables()) {
			Set<Integer> body = assignment.get(varId);
			if (body == null) {
				body = Collections.emptySet();
			}
			definitions.add(new Definition(varId, Collections.unmodifiableSet(body), false));
		}
		return new Unifier(definitions);
	}

	@Override
	protected void updateInfo() {
		for (Entry<String, String> e : aspOutput.getInfo()) {
			addInfo(e.getKey(), e.getValue());
		}
	}
}
