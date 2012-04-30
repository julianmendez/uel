package de.tudresden.inf.lat.uel.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.UelInput;

/**
 * This is a class representing the set of goal subsumptions of a unification
 * problem.
 * 
 * @author Stefan Borgwardt
 */
class Goal implements Set<Subsumption> {
	private Map<Integer, Set<Subsumption>> variableBodyIndex;
	private Map<Integer, Set<Subsumption>> variableHeadIndex;
	private Set<Subsumption> goal;
	private int maxSize;

	/**
	 * Construct a new goal from a set of equations given by a UelInput object.
	 * 
	 * @param input
	 *            the input object
	 */
	Goal(UelInput input) {
		goal = convertInput(input);
		maxSize = goal.size();
		variableBodyIndex = new HashMap<Integer, Set<Subsumption>>();
		variableHeadIndex = new HashMap<Integer, Set<Subsumption>>();
		for (Subsumption sub : goal) {
			addToIndex(sub);
		}
	}

	private static Set<Subsumption> convertInput(UelInput input) {
		Set<Equation> equations = input.getEquations();
		IndexedSet<Atom> atomManager = input.getAtomManager();

		Set<Subsumption> subsumptions = new HashSet<Subsumption>();
		for (Equation eq : equations) {
			// look up atom IDs in the atom manager
			Atom head = atomManager.get(eq.getLeft());
			List<Atom> body = new ArrayList<Atom>();
			for (Integer id : eq.getRight()) {
				body.add(atomManager.get(id));
			}

			// create subsumptions representing the equation
			subsumptions.add(new Subsumption(body, head));
			for (Atom at : body) {
				subsumptions.add(new Subsumption(head, at));
			}
		}
		return subsumptions;
	}

	@Override
	public Iterator<Subsumption> iterator() {
		return goal.iterator();
	}

	/**
	 * Return all stored subsumptions that have the specified variable on the
	 * top-level of their body.
	 * 
	 * @param var
	 *            the variable index
	 * @return the set of all subsumptions satisfying the condition
	 */
	Set<Subsumption> getSubsumptionsByBodyVariable(Integer var) {
		return getOrInitLHSIndex(var);
	}

	/**
	 * Return all stored subsumptions that have the specified variable as their
	 * head.
	 * 
	 * @param var
	 *            the variable index
	 * @return the set of all subsumptions satisfying the condition
	 */
	Set<Subsumption> getSubsumptionsByHeadVariable(Integer var) {
		return getOrInitRHSIndex(var);
	}

	private Set<Subsumption> getOrInitLHSIndex(Integer var) {
		if (!variableBodyIndex.containsKey(var)) {
			variableBodyIndex.put(var, new HashSet<Subsumption>());
		}
		return variableBodyIndex.get(var);
	}

	private Set<Subsumption> getOrInitRHSIndex(Integer var) {
		if (!variableHeadIndex.containsKey(var)) {
			variableHeadIndex.put(var, new HashSet<Subsumption>());
		}
		return variableHeadIndex.get(var);
	}

	@Override
	public boolean add(Subsumption sub) {
		if (!goal.add(sub)) {
			return false;
		}
		if (goal.size() > maxSize)
			maxSize = goal.size();
		addToIndex(sub);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Subsumption> c) {
		if (!goal.addAll(c)) {
			return false;
		}
		if (goal.size() > maxSize)
			maxSize = goal.size();
		for (Subsumption sub : c) {
			addToIndex(sub);
		}
		return true;
	}

	@Override
	public void clear() {
		goal.clear();
		variableBodyIndex.clear();
	}

	@Override
	public boolean contains(Object o) {
		return goal.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return goal.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return goal.isEmpty();
	}

	@Override
	public boolean remove(Object o) {
		if (!goal.remove(o)) {
			return false;
		}
		removeFromIndex((Subsumption) o);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (!goal.removeAll(c)) {
			return false;
		}
		for (Object o : c) {
			if (o instanceof Subsumption) {
				removeFromIndex((Subsumption) o);
			}
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return goal.size();
	}

	/**
	 * Retrieve the maximal number of subsumptions observed so far.
	 * 
	 * @return the maximal number of subsumptions in the history of this goal
	 */
	public int getMaxSize() {
		return maxSize;
	}

	@Override
	public Object[] toArray() {
		return goal.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return goal.toArray(a);
	}

	private void addToIndex(Subsumption sub) {
		for (Atom at : sub.getBody()) {
			if (at.isVariable()) {
				getOrInitLHSIndex(at.getConceptNameId()).add(sub);
			}
		}
		if (sub.getHead().isVariable()) {
			getOrInitRHSIndex(sub.getHead().getConceptNameId()).add(sub);
		}
	}

	private void removeFromIndex(Subsumption sub) {
		for (Atom at : sub.getBody()) {
			if (at.isVariable()) {
				variableBodyIndex.get(at.getConceptNameId()).remove(sub);
			}
		}
		if (sub.getHead().isVariable()) {
			variableHeadIndex.get(sub.getHead().getConceptNameId()).remove(sub);
		}
	}

	private void expand(Subsumption sub, Set<Atom> subsumers,
			Set<Subsumption> collection) {
		for (Atom at : subsumers) {
			Subsumption newSub = new Subsumption(sub.getBody(), at);
			if (add(newSub)) {
				// only add the subsumption if it is new
				collection.add(newSub);
			}
		}
	}

	/**
	 * Expand a goal subsumption using a set of subsumers.
	 * 
	 * @param sub
	 *            a goal subsumption with a variable on the right-hand side
	 * @param subsumers
	 *            a set of subsumers of the variable
	 * @return a set containing the subsumptions added as a result of this
	 *         operation
	 */
	Set<Subsumption> expand(Subsumption sub, Set<Atom> subsumers) {
		Set<Subsumption> newSubs = new HashSet<Subsumption>();
		expand(sub, subsumers, newSubs);
		return newSubs;
	}

	/**
	 * Expand all goal subsumptions with a certain variable on the right-hand
	 * side using a set of new subsumers.
	 * 
	 * @param assign
	 *            an assignment specifying the new subsumers
	 * @return a set containing the subsumptions added as a result of this
	 *         operation
	 */
	Set<Subsumption> expand(Assignment assign) {
		Set<Subsumption> newSubs = new HashSet<Subsumption>();
		for (Integer var : assign.getKeys()) {
			for (Subsumption sub : getOrInitRHSIndex(var)) {
				expand(sub, assign.getSubsumers(var), newSubs);
			}
		}
		return newSubs;
	}

}