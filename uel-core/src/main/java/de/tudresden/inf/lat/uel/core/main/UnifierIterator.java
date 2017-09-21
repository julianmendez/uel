package de.tudresden.inf.lat.uel.core.main;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

/**
 * An iterator for unifiers, in the form of sets of OWLEquivalentClassesAxioms.
 * 
 * @author Stefan Borgwardt
 *
 */
public class UnifierIterator implements Iterator<Set<OWLEquivalentClassesAxiom>> {

	private boolean hasNext = false;
	private boolean isComputed = false;
	private boolean cleaned = false;
	private UelModel uelModel;
	private Unifier unifier;

	/**
	 * Initialize a new iterator based on a UEL model.
	 * 
	 * @param uelModel
	 *            the UEL model.
	 */
	public UnifierIterator(UelModel uelModel) {
		this.uelModel = uelModel;
	}

	/**
	 * Clean up the resources used by UEL once they are no longer needed.
	 */
	public synchronized void cleanup() {
		if (!cleaned) {
			cleaned = true;
			// System.out.println("Thread '" + Thread.currentThread().getName()
			// + "' started executing 'cleanup()' on "
			// + this.toString() + " at "
			// + new SimpleDateFormat("dd.MM.yy
			// HH:mm:ss").format(Calendar.getInstance().getTime()));
			uelModel.cleanupUnificationAlgorithm();
			// System.out.println("'cleaned' is now true ("
			// + new SimpleDateFormat("dd.MM.yy
			// HH:mm:ss").format(Calendar.getInstance().getTime()) + ").");
		}
	}

	private void compute() {
		if (cleaned) {
			isComputed = true;
			hasNext = false;
			return;
		}

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

	/**
	 * Obtain the UEL model that coordinates the unification process.
	 * 
	 * @return the UEL model
	 */
	public UelModel getUelModel() {
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

	public UnifierIterator resetModel() {
		cleanup();
		uelModel.initializeUnificationAlgorithm();
		return new UnifierIterator(uelModel);
	}

}
