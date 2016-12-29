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
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.Subsumption;

/**
 * This is a class representing the set of goal subsumptions of a unification
 * problem.
 * 
 * @author Stefan Borgwardt
 */
class NormalizedGoal implements Set<FlatSubsumption> {

	private static void convert(Definition d, AtomManager atomManager, Set<FlatSubsumption> flatSubsumptions) {
		Atom definiendum = atomManager.getAtom(d.getDefiniendum());
		List<Atom> right = toAtoms(d.getRight(), atomManager);
		for (Atom rightAtom : right) {
			flatSubsumptions.add(new FlatSubsumption(definiendum, rightAtom));
		}
		flatSubsumptions.add(new FlatSubsumption(right, definiendum));
	}

	private static void convert(Equation e, AtomManager atomManager, Set<FlatSubsumption> flatSubsumptions) {
		List<Atom> left = toAtoms(e.getLeft(), atomManager);
		List<Atom> right = toAtoms(e.getRight(), atomManager);
		for (Atom rightAtom : right) {
			flatSubsumptions.add(new FlatSubsumption(left, rightAtom));
		}
		for (Atom leftAtom : left) {
			flatSubsumptions.add(new FlatSubsumption(right, leftAtom));
		}
	}

	private static void convert(Subsumption s, AtomManager atomManager, Set<FlatSubsumption> flatSubsumptions) {
		List<Atom> left = toAtoms(s.getLeft(), atomManager);
		List<Atom> right = toAtoms(s.getRight(), atomManager);
		for (Atom rightAtom : right) {
			flatSubsumptions.add(new FlatSubsumption(left, rightAtom));
		}
	}

	private static Set<FlatSubsumption> convertInput(Goal input) {
		Set<FlatSubsumption> flatSubsumptions = new HashSet<>();
		for (Definition d : input.getDefinitions()) {
			convert(d, input.getAtomManager(), flatSubsumptions);
		}
		for (Subsumption s : input.getSubsumptions()) {
			convert(s, input.getAtomManager(), flatSubsumptions);
		}
		for (Equation e : input.getEquations()) {
			convert(e, input.getAtomManager(), flatSubsumptions);
		}
		// TODO dissubsumptions and disequations are not supported yet
		return flatSubsumptions;
	}

	private static List<Atom> toAtoms(Set<Integer> atomIds, AtomManager atomManager) {
		List<Atom> atoms = new ArrayList<>();
		for (Integer atomId : atomIds) {
			atoms.add(atomManager.getAtom(atomId));
		}
		return atoms;
	}

	private Set<FlatSubsumption> goal;
	private int maxSize;
	private Map<Atom, Set<FlatSubsumption>> variableBodyIndex;
	private Map<Atom, Set<FlatSubsumption>> variableHeadIndex;

	/**
	 * Construct a new goal from a set of equations given by a UelInput object.
	 * 
	 * @param input
	 *            the input object
	 */
	NormalizedGoal(Goal input) {
		goal = convertInput(input);
		maxSize = goal.size();
		variableBodyIndex = new HashMap<>();
		variableHeadIndex = new HashMap<>();
		for (FlatSubsumption sub : goal) {
			addToIndex(sub);
		}
	}

	@Override
	public boolean add(FlatSubsumption sub) {
		if (!goal.add(sub)) {
			return false;
		}
		if (goal.size() > maxSize)
			maxSize = goal.size();
		addToIndex(sub);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends FlatSubsumption> c) {
		if (!goal.addAll(c)) {
			return false;
		}
		if (goal.size() > maxSize)
			maxSize = goal.size();
		for (FlatSubsumption sub : c) {
			addToIndex(sub);
		}
		return true;
	}

	private void addToIndex(FlatSubsumption sub) {
		for (Atom at : sub.getBody()) {
			if (at.isVariable()) {
				getOrInitBodyIndex(at).add(sub);
			}
		}
		if (sub.getHead().isVariable()) {
			getOrInitHeadIndex(sub.getHead()).add(sub);
		}
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

	/**
	 * Expand all goal subsumptions with a certain variable on the right-hand
	 * side using a set of new subsumers.
	 * 
	 * @param assign
	 *            an assignment specifying the new subsumers
	 * @return a set containing the subsumptions added as a result of this
	 *         operation
	 */
	Set<FlatSubsumption> expand(Assignment assign) {
		Set<FlatSubsumption> newSubs = new HashSet<>();
		for (Atom var : assign.getKeys()) {
			for (FlatSubsumption sub : getOrInitHeadIndex(var)) {
				expand(sub, assign.getSubsumers(var), newSubs);
			}
		}
		return newSubs;
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
	Set<FlatSubsumption> expand(FlatSubsumption sub, Set<Atom> subsumers) {
		Set<FlatSubsumption> newSubs = new HashSet<>();
		expand(sub, subsumers, newSubs);
		return newSubs;
	}

	private void expand(FlatSubsumption sub, Set<Atom> subsumers, Set<FlatSubsumption> collection) {
		for (Atom at : subsumers) {
			FlatSubsumption newSub = new FlatSubsumption(sub.getBody(), at);
			if (add(newSub)) {
				// only add the subsumption if it is new
				collection.add(newSub);
			}
		}
	}

	/**
	 * Retrieve the maximal number of subsumptions observed so far.
	 * 
	 * @return the maximal number of subsumptions in the history of this goal
	 */
	public int getMaxSize() {
		return maxSize;
	}

	private Set<FlatSubsumption> getOrInitBodyIndex(Atom var) {
		if (!variableBodyIndex.containsKey(var)) {
			variableBodyIndex.put(var, new HashSet<>());
		}
		return variableBodyIndex.get(var);
	}

	private Set<FlatSubsumption> getOrInitHeadIndex(Atom var) {
		if (!variableHeadIndex.containsKey(var)) {
			variableHeadIndex.put(var, new HashSet<>());
		}
		return variableHeadIndex.get(var);
	}

	/**
	 * Return all stored subsumptions that have the specified variable on the
	 * top-level of their body.
	 * 
	 * @param var
	 *            the variable index
	 * @return the set of all subsumptions satisfying the condition
	 */
	protected Set<FlatSubsumption> getSubsumptionsByBodyVariable(Atom var) {
		return getOrInitBodyIndex(var);
	}

	/**
	 * Return all stored subsumptions that have the specified variable as their
	 * head.
	 * 
	 * @param var
	 *            the variable index
	 * @return the set of all subsumptions satisfying the condition
	 */
	protected Set<FlatSubsumption> getSubsumptionsByHeadVariable(Atom var) {
		return getOrInitHeadIndex(var);
	}

	@Override
	public boolean isEmpty() {
		return goal.isEmpty();
	}

	@Override
	public Iterator<FlatSubsumption> iterator() {
		return goal.iterator();
	}

	@Override
	public boolean remove(Object o) {
		if (!goal.remove(o)) {
			return false;
		}
		removeFromIndex((FlatSubsumption) o);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (!goal.removeAll(c)) {
			return false;
		}
		for (Object o : c) {
			if (o instanceof FlatSubsumption) {
				removeFromIndex((FlatSubsumption) o);
			}
		}
		return true;
	}

	private void removeFromIndex(FlatSubsumption sub) {
		for (Atom at : sub.getBody()) {
			if (at.isVariable()) {
				variableBodyIndex.get(at).remove(sub);
			}
		}
		if (sub.getHead().isVariable()) {
			variableHeadIndex.get(sub.getHead()).remove(sub);
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return goal.size();
	}

	@Override
	public Object[] toArray() {
		return goal.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return goal.toArray(a);
	}

}