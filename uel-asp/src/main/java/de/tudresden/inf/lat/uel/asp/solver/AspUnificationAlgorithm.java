package de.tudresden.inf.lat.uel.asp.solver;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
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

	public AspUnificationAlgorithm(Goal goal, boolean minimize) {
		super(goal);
		this.initialized = false;
		this.currentUnifier = null;
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
			aspInput = new AspInput(goal, this);
			addInfo("ASP encoding", aspInput.getProgram());
			AspSolver solver = new ClingoSolver(goal.hasNegativePart(), !goal.getTypes().isEmpty(), minimize, this);
			try {
				aspOutput = solver.solve(aspInput);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			initialized = true;
		}

		boolean hasNext = aspOutput.hasNext();

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		if (hasNext) {
			currentUnifier = toUnifier(aspOutput.next());
		}
		return hasNext;
	}

	@Override
	public Unifier getUnifier() {
		if (aspOutput == null) {
			throw new IllegalStateException("The unifiers have not been computed yet.");
		}
		if (!aspOutput.hasNext()) {
			throw new IllegalStateException("There are no more unifiers.");
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
	}
}
