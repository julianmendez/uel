package de.tudresden.inf.lat.uel.core.type;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An object of this class is a set with indices, i.e. , each element in this
 * set has a unique non-negative number to identify it.
 * 
 * @author Julian Mendez
 */
public class IndexedSet<T> implements Set<T> {

	private Map<Integer, T> invMap = new HashMap<Integer, T>();
	private Map<T, Integer> map = new HashMap<T, Integer>();
	private Integer maxIndex = 0;

	/**
	 * Constructs a new indexed set.
	 */
	public IndexedSet() {
	}

	@Override
	public boolean add(T element) {
		if (element == null) {
			throw new NullPointerException();
		}
		return add(element, getMaxIndex() + 1);
	}

	public boolean add(T element, Integer index) {
		if (element == null) {
			throw new NullPointerException();
		}
		boolean ret = false;
		if (!this.map.containsKey(element)) {
			this.map.put(element, index);
			this.invMap.put(index, element);
			if (maxIndex < index) {
				maxIndex = index;
			}
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

	public int addAndGetIndex(T element) {
		if (element == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		add(element);
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

		return this.map.keySet().contains(element);
	}

	@Override
	public boolean containsAll(Collection<?> elements) {
		return this.map.keySet().containsAll(elements);
	}

	public T get(int id) {
		return this.invMap.get(id);
	}

	public int getIndex(T atom) {
		Integer ret = this.map.get(atom);
		if (ret == null) {
			ret = -1;
		}
		return ret;
	}

	public Collection<Integer> getIndices() {
		return Collections.unmodifiableCollection(this.map.values());
	}

	public Integer getMaxIndex() {
		return maxIndex;
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
