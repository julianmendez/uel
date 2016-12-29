package de.tudresden.inf.lat.uel.type.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.IndexedSet;

/**
 * An object of this class is a set with indices, i.e., each element in this set
 * has a unique non-negative number that identifies it.
 * 
 * @author Julian Mendez
 * @param <T>
 *            type of the object stored in this indexed set
 */
public class IndexedSetImpl<T> implements IndexedSet<T> {

	private Map<Integer, T> invMap = new HashMap<>();
	private Map<T, Integer> map = new HashMap<>();
	private Integer maxIndex = 0;

	/**
	 * Constructs a new indexed set.
	 */
	public IndexedSetImpl() {
	}

	@Override
	public boolean add(T element) {
		if (element == null) {
			throw new NullPointerException();
		}
		return add(element, getNextIndex());
	}

	@Override
	public boolean add(T element, Integer index) {
		if (element == null) {
			throw new NullPointerException();
		}
		boolean ret = false;
		if (this.map.containsKey(element)) {
			Integer oldIndex = this.map.get(element);
			if (!oldIndex.equals(index)) {
				throw new IllegalArgumentException("Element '" + element + "' has already index " + oldIndex
						+ " but new index " + index + " was given.");
			}
		} else if (this.invMap.containsKey(index)) {
			T oldElement = this.invMap.get(index);
			if (!oldElement.equals(element)) {
				throw new IllegalArgumentException("Index " + index + " has been used for element '" + oldElement
						+ "' but new element '" + element + "' was given.");
			}
		} else {
			this.map.put(element, index);
			this.invMap.put(index, element);
			if (this.maxIndex < index) {
				this.maxIndex = index;
			}
			ret = true;
		}
		return ret;
	}

	@Override
	public boolean addAll(Collection<? extends T> elements) {
		if (elements == null) {
			throw new NullPointerException();
		}

		boolean ret = false;
		for (T elem : elements) {
			boolean changed = add(elem);
			ret = ret || changed;
		}
		return ret;
	}

	@Override
	public int addAndGetIndex(T element) {
		if (element == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		if (!contains(element)) {
			add(element);
		}
		return getIndex(element);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object element) {
		if (element == null) {
			throw new NullPointerException();
		}

		return this.map.containsKey(element);
	}

	@Override
	public boolean containsAll(Collection<?> elements) {
		return this.map.keySet().containsAll(elements);
	}

	@Override
	public T get(int id) {
		return this.invMap.get(id);
	}

	@Override
	public int getIndex(T atom) {
		Integer ret = this.map.get(atom);
		if (ret == null) {
			ret = -1;
		}
		return ret;
	}

	@Override
	public Set<Integer> getIndices() {
		return Collections.unmodifiableSet(invMap.keySet());
	}

	@Override
	public Integer getMaxIndex() {
		return this.maxIndex;
	}

	@Override
	public Integer getNextIndex() {
		return getMaxIndex() + 1;
	}

	@Override
	public boolean isEmpty() {
		return this.invMap.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return this.invMap.values().iterator();
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
		return this.invMap.size();
	}

	@Override
	public Object[] toArray() {
		return this.invMap.values().toArray();
	}

	@Override
	public <S> S[] toArray(S[] a) {
		if (a == null) {
			throw new NullPointerException();
		}

		return this.invMap.values().toArray(a);
	}

	@Override
	public String toString() {
		return this.invMap.toString();
	}

}
