package de.tudresden.inf.lat.uel.type.api;

import java.util.Set;

/**
 * 
 * @author Julian Mendez
 */
public interface AtomManager extends IndexedSet<Atom>, AtomChangeListener {

	public Set<Integer> getConstants();

	public Set<Integer> getEAtoms();

	public Set<Integer> getVariables();

}
