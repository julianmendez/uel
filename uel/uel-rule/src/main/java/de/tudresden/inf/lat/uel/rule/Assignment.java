package de.tudresden.inf.lat.uel.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.tudresden.inf.lat.uel.type.api.Atom;

/**
 * An assignment of sets of non-variable atoms to variables. Such an assignment
 * should always be acyclic.
 * 
 * @author Stefan Borgwardt
 */
class Assignment {

	private final Map<Integer, Set<Atom>> subs = new HashMap<Integer, Set<Atom>>();

	/**
	 * Create an empty assignment.
	 */
	Assignment() {
	}

	/**
	 * Create a copy of another assignment.
	 * 
	 * @param other
	 */
	Assignment(Assignment other) {
		addAll(other);
	}

	/**
	 * Add an atom to the assignment of a variable.
	 * 
	 * @param var
	 *            the index of the variable
	 * @param at
	 *            the new atom
	 * @return true iff the assignment was changed as a result of this operation
	 */
	boolean add(Integer var, Atom at) {
		if (at == null) {
			throw new IllegalArgumentException();
		}
		Set<Atom> flatAtoms = getOrInit(var);
		return flatAtoms.add(at);
	}

	/**
	 * Add a set of atoms to the assignment of a variable.
	 * 
	 * @param var
	 *            the index of the variable
	 * @param at
	 *            the new atoms
	 * @return true iff the assignment was changed as a result of this operation
	 */
	boolean addAll(Integer var, Set<Atom> at) {
		if (at == null)
			return false;
		Set<Atom> flatAtoms = getOrInit(var);
		return flatAtoms.addAll(at);
	}

	/**
	 * Add another variable assignment to this assignment.
	 * 
	 * @param other
	 *            the assignment to be merged into this one
	 * @return true iff the assignment was changed as a result of this operation
	 */
	boolean addAll(Assignment other) {
		if (other == null)
			return false;
		boolean ret = false;
		for (Entry<Integer, Set<Atom>> entry : other.subs.entrySet()) {
			if (addAll(entry.getKey(), entry.getValue()))
				ret = true;
		}
		return ret;
	}

	/**
	 * Remove a set of atoms from the assignment of a variable.
	 * 
	 * @param var
	 *            the index of the variable
	 * @param at
	 *            the atoms to be removed
	 * @return true iff the assignment was changed as a result of this operation
	 */
	boolean removeAll(Integer var, Set<Atom> at) {
		if (at == null)
			return false;
		if (subs.get(var) == null)
			return false;
		return subs.get(var).removeAll(at);
	}

	/**
	 * Subtract another variable assignment from this assignment.
	 * 
	 * @param other
	 *            the assignment to be removed from this one
	 * @return true iff the assignment was changed as a result of this operation
	 */
	boolean removeAll(Assignment other) {
		if (other == null)
			return false;
		boolean ret = false;
		for (Entry<Integer, Set<Atom>> entry : other.subs.entrySet()) {
			if (removeAll(entry.getKey(), entry.getValue()))
				ret = true;
		}
		return ret;
	}

	/**
	 * Retrieve the subsumers of a given variable according to this assignment.
	 * 
	 * @param var
	 *            the variable
	 * @return the set of assigned subsumers
	 */
	Set<Atom> getSubsumers(Integer var) {
		return getOrInit(var);
	}

	/**
	 * Retrieve the variable indices of the variables that are explicitly
	 * assigned some non-variable atoms by this assignment. This assignment
	 * might also be the empty set.
	 * 
	 * @return a set containing the indices of all variables involved in this
	 *         assignment
	 */
	Set<Integer> getKeys() {
		return subs.keySet();
	}

	private Set<Atom> getOrInit(Integer var) {
		Set<Atom> flatAtoms = subs.get(var);
		if (flatAtoms == null) {
			flatAtoms = new HashSet<Atom>();
			subs.put(var, flatAtoms);
		}
		return flatAtoms;
	}

	/**
	 * Check whether this assignment is empty.
	 * 
	 * @return true iff no variable is assigned any subsumer
	 */
	boolean isEmpty() {
		for (Set<Atom> subsumers : subs.values()) {
			if (!subsumers.isEmpty())
				return false;
		}
		return true;
	}

	/**
	 * Checks if there is a dependency of 'a' on 'b', i.e., whether 'b' is
	 * reachable from 'a' in the graph representation of the current assignment.
	 * It is important that the current assignment is acyclic; otherwise, this
	 * implementation might not terminate.
	 * 
	 * @param a
	 *            the start variable
	 * @param b
	 *            the goal variable
	 * @return true iff 'a' depends on 'b'
	 */
	boolean dependsOn(Integer a, Integer b) {
		for (Atom at : getSubsumers(a)) {
			if (!at.isGround()) {
				Integer nextVar = at.getConceptNameId();
				if (nextVar.equals(b)) {
					return true;
				}
				if (dependsOn(nextVar, b)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if a new assignment would make this assignment cyclic.
	 * 
	 * @param var
	 *            the variable index
	 * @param at
	 *            the new atom
	 * @return true iff the resulting assignment would be cyclic
	 */
	boolean makesCyclic(Integer var, Atom at) {
		if (at.isGround())
			return false;
		Integer conceptName = at.getConceptNameId();
		if (conceptName.equals(var))
			return true;
		return dependsOn(conceptName, var);
	}

	/**
	 * Checks if a new assignment would make this assignment cyclic.
	 * 
	 * @param var
	 *            the variable index
	 * @param newAtoms
	 *            the new atoms
	 * @return true iff the resulting assignment would be cyclic
	 */
	boolean makesCyclic(Integer var, Iterable<Atom> newAtoms) {
		for (Atom at : newAtoms) {
			if (makesCyclic(var, at)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the current assignment is cyclic.
	 * 
	 * In the event of a simultaneous application of several eager extension
	 * rules, the assignment might have become cyclic without any of the
	 * individual rule applications being at fault.
	 * 
	 * @return true iff this assignment induces a cyclic dependency relation
	 *         between variables
	 */
	// boolean isCyclic() {
	// Set<Integer> vars = getDomain();
	// while (!vars.isEmpty()) {
	// Deque<Integer> stack = new ArrayDeque<Integer>();
	// stack.push(vars.iterator().next());
	// while (!stack.isEmpty()) {
	// Integer var = stack.getFirst();
	// vars.remove(var);
	// boolean succ = false;
	// for (FlatAtom at : getSubsumers(var)) {
	// if (!at.isGround()) {
	// Integer nextVar = at.getConceptName();
	// if (stack.contains(nextVar)) {
	// return true;
	// }
	// if (vars.contains(nextVar)) {
	// stack.push(nextVar);
	// succ = true;
	// break;
	// }
	// }
	// }
	// if (!succ) {
	// stack.pop();
	// }
	// }
	// }
	// return false;
	// }

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (Entry<Integer, Set<Atom>> e : subs.entrySet()) {
			buf.append(e.getKey());
			buf.append("=");
			buf.append(e.getValue());
			buf.append(";");
		}
		buf.append("]");
		return buf.toString();
	}

}
