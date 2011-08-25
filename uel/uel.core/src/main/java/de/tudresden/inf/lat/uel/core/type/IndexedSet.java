package de.tudresden.inf.lat.uel.core.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An object of this class is a set with indices, i.e. , each element in this
 * set has a unique non-negative number to identify it.
 * 
 * @author Julian Mendez
 */
public class IndexedSet<T> implements Set<T> {

	private List<T> list = new ArrayList<T>();
	private Map<T, Integer> map = new HashMap<T, Integer>();

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
		boolean ret = false;
		if (!this.map.containsKey(element)) {
			this.map.put(element, this.list.size());
			ret = this.list.add(element);
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
		return this.list.get(id);
	}

	private int getIndex(T atom) {
		Integer ret = this.map.get(atom);
		if (ret == null) {
			ret = -1;
		}
		return ret;
	}

	@Override
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return this.list.iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return this.list.size();
	}

	@Override
	public Object[] toArray() {
		return this.list.toArray();
	}

	@Override
	public <S> S[] toArray(S[] a) {
		if (a == null) {
			throw new NullPointerException();
		}

		return this.list.toArray(a);
	}

}
