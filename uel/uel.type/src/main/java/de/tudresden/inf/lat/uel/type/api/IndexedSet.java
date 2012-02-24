package de.tudresden.inf.lat.uel.type.api;

import java.util.Collection;
import java.util.Set;

/**
 * 
 * @author Julian Mendez
 *
 * @param <T>
 */
public interface IndexedSet<T> extends Set<T> {

	public boolean add(T element, Integer index);

	public int addAndGetIndex(T element);

	public T get(int id);

	public int getIndex(T atom);

	public Collection<Integer> getIndices();

	public Integer getMaxIndex();

}
