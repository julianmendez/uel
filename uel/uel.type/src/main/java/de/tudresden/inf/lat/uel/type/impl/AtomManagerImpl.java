package de.tudresden.inf.lat.uel.type.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomChangeEvent;
import de.tudresden.inf.lat.uel.type.api.AtomChangeType;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;

/**
 * 
 * @author Julian Mendez
 */
public class AtomManagerImpl implements AtomManager {

	private final IndexedSet<Atom> atomManager = new IndexedSetImpl<Atom>();

	private Set<Integer> constants = new HashSet<Integer>();

	private Set<Integer> eatoms = new HashSet<Integer>();

	private Set<Integer> variables = new HashSet<Integer>();

	@Override
	public boolean add(Atom e) {
		boolean ret = this.atomManager.add(e);
		updateAfterAddition(e);
		return ret;
	}

	@Override
	public boolean add(Atom element, Integer index) {
		boolean ret = this.atomManager.add(element, index);
		updateAfterAddition(element);
		return ret;
	}

	@Override
	public boolean addAll(Collection<? extends Atom> c) {
		boolean ret = false;
		for (Atom atom : c) {
			boolean changed = add(atom);
			ret = ret || changed;
		}
		return ret;
	}

	@Override
	public int addAndGetIndex(Atom element) {
		int ret = this.atomManager.addAndGetIndex(element);
		updateAfterAddition(element);
		return ret;
	}

	@Override
	public void atomTypeChanged(AtomChangeEvent e) {
		Atom atom = e.getAtom();
		int index = this.atomManager.getIndex(atom);
		if (e.getChangeType() == AtomChangeType.FROM_CONSTANT_TO_VARIABLE) {
			this.constants.remove(index);
			this.variables.add(index);
		} else if (e.getChangeType() == AtomChangeType.FROM_VARIABLE_TO_CONSTANT) {
			this.variables.remove(index);
			this.constants.add(index);
		} else {
			throw new IllegalStateException("Atom type is unknown.");
		}
	}

	@Override
	public void clear() {
		this.atomManager.clear();
	}

	@Override
	public boolean contains(Object o) {
		return this.atomManager.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.atomManager.containsAll(c);
	}

	@Override
	public Atom get(int id) {
		return this.atomManager.get(id);
	}

	@Override
	public Set<Integer> getConstants() {
		return Collections.unmodifiableSet(this.constants);
	}

	@Override
	public Set<Integer> getEAtoms() {
		return Collections.unmodifiableSet(this.eatoms);
	}

	@Override
	public int getIndex(Atom atom) {
		return this.atomManager.getIndex(atom);
	}

	@Override
	public Collection<Integer> getIndices() {
		return this.atomManager.getIndices();
	}

	@Override
	public Integer getMaxIndex() {
		return this.atomManager.getMaxIndex();
	}

	@Override
	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(this.variables);
	}

	@Override
	public boolean isEmpty() {
		return this.atomManager.isEmpty();
	}

	@Override
	public Iterator<Atom> iterator() {
		return this.atomManager.iterator();
	}

	@Override
	public boolean remove(Object element) {
		if (element == null) {
			throw new NullPointerException();
		}

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> elements) {
		if (elements == null) {
			throw new NullPointerException();
		}

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> elements) {
		if (elements == null) {
			throw new NullPointerException();
		}

		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return this.atomManager.size();
	}

	@Override
	public Object[] toArray() {
		return this.atomManager.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.atomManager.toArray(a);
	}

	private void updateAfterAddition(Atom atom) {
		int index = this.atomManager.getIndex(atom);
		if (atom.isExistentialRestriction()) {
			this.eatoms.add(index);
		} else if (atom.isConceptName()) {
			if (atom.isConstant()) {
				this.constants.add(index);
			} else if (atom.isVariable()) {
				this.variables.add(index);
			} else {
				throw new IllegalStateException();
			}
		} else {
			throw new IllegalStateException();
		}

		if (!atom.getAtomChangeListeners().contains(this)) {
			atom.addAtomChangeListener(this);
		}
	}

}
