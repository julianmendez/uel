package de.tudresden.inf.lat.uel.type.api;

import java.util.Set;

/**
 * An object of this class is a set where each element has an index. This index
 * is used as identifier. It is an integer and is unique in the set. These
 * identifiers need not be consecutive.
 * 
 * @author Julian Mendez
 * 
 * @param <T>
 *            type of the object stored in this indexed set
 */
public interface IndexedSet<T> extends Set<T> {

	/**
	 * Adds an element to this indexed set with a given index. If the given
	 * element has been used with a different index or the index with a
	 * different element, an exception is thrown.
	 * 
	 * @param element
	 *            element
	 * @param index
	 *            index
	 * @return <code>true</code> if and only if this indexed set has been
	 *         modified.
	 * @throws IllegalArgumentException
	 *             when trying to add an existing element with a different index
	 *             or a new element with a used index
	 */
	boolean add(T element, Integer index);

	/**
	 * Adds an element to this indexed set and returns its index. If the element
	 * is already in the set, this method just retrieves its index.
	 * 
	 * @param element
	 *            element
	 * @return the index of the given element
	 * @see #getIndex
	 */
	int addAndGetIndex(T element);

	/**
	 * Returns the element for a given index.
	 * 
	 * @param id
	 *            index
	 * @return the element for a given index
	 */
	T get(int id);

	/**
	 * Returns the index of the given element.
	 * 
	 * @param element
	 *            element
	 * @return the index of the given element
	 */
	int getIndex(T element);

	/**
	 * Returns a collection containing the indices.
	 * 
	 * @return a collection containing the indices
	 */
	Set<Integer> getIndices();

	/**
	 * Returns the maximum index in this indexed set.
	 * 
	 * @return the maximum index in this indexed set
	 */
	Integer getMaxIndex();

	/**
	 * Returns the next unused index. This number is strictly greater than the
	 * maximum index.
	 * 
	 * @return the next unused index
	 * @see #getMaxIndex()
	 */
	Integer getNextIndex();

}
