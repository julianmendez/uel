package de.tudresden.inf.lat.uel.core.main;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import de.tudresden.inf.lat.uel.core.type.OWLUelClassDefinition;
import de.tudresden.inf.lat.uel.core.type.UnifierTranslator;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.UnificationAlgorithm;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

public class UnifierIterator implements Iterator<Set<OWLUelClassDefinition>> {

	private boolean hasNext = false;
	private boolean isComputed = false;
	private UnificationAlgorithm algorithm;
	private UnifierTranslator translator;
	private Unifier unifier;

	public UnifierIterator(UnificationAlgorithm algorithm, UnifierTranslator translator) {
		this.algorithm = algorithm;
		this.translator = translator;
	}

	protected UnificationAlgorithm getAlgorithm() {
		return this.algorithm;
	}

	protected AtomManager getAtomManager() {
		return this.translator.getAtomManager();
	}

	private void compute() {
		if (!isComputed) {
			try {
				hasNext = algorithm.computeNextUnifier();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				algorithm.cleanup();
				hasNext = false;
			}
			if (hasNext) {
				unifier = algorithm.getUnifier();
			}
			isComputed = true;
		}
	}

	@Override
	public boolean hasNext() {
		compute();
		return hasNext;
	}

	@Override
	public Set<OWLUelClassDefinition> next() {
		compute();
		if (!hasNext) {
			throw new NoSuchElementException();
		}

		isComputed = false;

		return translator.createOWLUelClassDefinitions(unifier.getDefinitions());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
