package de.tudresden.inf.lat.uel.plugin.main;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import de.tudresden.inf.lat.uel.plugin.type.OWLUelClassDefinition;
import de.tudresden.inf.lat.uel.plugin.type.UnifierTranslator;
import de.tudresden.inf.lat.uel.type.api.UelOutput;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;

public class UnifierIterator implements Iterator<Set<OWLUelClassDefinition>> {

	private boolean hasNext = false;
	private boolean isComputed = false;
	private UelProcessor processor;
	private UnifierTranslator translator;
	private UelOutput unifier;

	public UnifierIterator(UelProcessor proc, UnifierTranslator translator) {
		this.processor = proc;
		this.translator = translator;
	}

	private void compute() {
		if (!isComputed) {
			hasNext = processor.computeNextUnifier();
			if (hasNext) {
				unifier = processor.getUnifier();
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

		return translator.createOWLUelClassDefinition(unifier.getEquations());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
