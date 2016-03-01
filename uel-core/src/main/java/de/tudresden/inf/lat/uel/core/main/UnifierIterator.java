package de.tudresden.inf.lat.uel.core.main;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

public class UnifierIterator implements Iterator<Set<OWLEquivalentClassesAxiom>> {

	private boolean hasNext = false;
	private boolean isComputed = false;
	private UelModel uelModel;
	private Unifier unifier;

	public UnifierIterator(UelModel uelModel) {
		this.uelModel = uelModel;
	}

	public void cleanup() {
		uelModel.getUnificationAlgorithm().cleanup();
	}

	private void compute() {
		if (!isComputed) {
			try {
				hasNext = uelModel.computeNextUnifier();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				cleanup();
				hasNext = false;
			}
			if (hasNext) {
				uelModel.setCurrentUnifierIndex(uelModel.getCurrentUnifierIndex() + 1);
				unifier = uelModel.getCurrentUnifier();
			}
			isComputed = true;
		}
	}

	UelModel getUelModel() {
		return uelModel;
	}

	@Override
	public boolean hasNext() {
		compute();
		return hasNext;
	}

	@Override
	public Set<OWLEquivalentClassesAxiom> next() {
		compute();
		if (!hasNext) {
			throw new NoSuchElementException();
		}

		isComputed = false;

		Set<OWLAxiom> axioms = uelModel.renderUnifier(unifier);
		return axioms.stream().map(axiom -> {
			if (axiom instanceof OWLEquivalentClassesAxiom)
				return (OWLEquivalentClassesAxiom) axiom;
			throw new IllegalStateException("Expected OWLEquivalentClassesAxiom");
		}).collect(Collectors.toSet());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
