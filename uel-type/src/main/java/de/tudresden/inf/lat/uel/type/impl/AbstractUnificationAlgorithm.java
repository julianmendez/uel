/**
 * 
 */
package de.tudresden.inf.lat.uel.type.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.UnificationAlgorithm;

/**
 * @author Stefan Borgwardt
 *
 */
public abstract class AbstractUnificationAlgorithm implements UnificationAlgorithm {

	protected final Set<Integer> conceptNames = new HashSet<Integer>();
	protected final Goal goal;
	private List<Entry<String, String>> info = new ArrayList<Entry<String, String>>();
	protected final Set<Integer> nonVariableAtoms = new HashSet<Integer>();
	private final Set<Integer> usedAtomIds = new HashSet<Integer>();

	protected AbstractUnificationAlgorithm(Goal goal) {
		this.goal = goal;
		this.nonVariableAtoms.addAll(getConstants());
		this.nonVariableAtoms.addAll(getExistentialRestrictions());
		this.usedAtomIds.addAll(nonVariableAtoms);
		this.usedAtomIds.addAll(getVariables());
		this.conceptNames.addAll(getConstants());
		this.conceptNames.addAll(getVariables());
	}

	protected void addInfo(String key, Object value) {
		info.removeAll(info.stream().filter(e -> e.getKey().equals(key)).collect(Collectors.toList()));
		info.add(new SimpleEntry<String, String>(key, value.toString()));
	}

	protected Set<Integer> getConceptNames() {
		return conceptNames;
	}

	protected Set<Integer> getConstants() {
		return goal.getAtomManager().getConstants();
	}

	protected Set<Integer> getExistentialRestrictions() {
		return goal.getAtomManager().getExistentialRestrictions();
	}

	@Override
	public List<Entry<String, String>> getInfo() {
		updateInfo();
		return Collections.unmodifiableList(info);
	}

	protected Set<Integer> getNonVariableAtoms() {
		return nonVariableAtoms;
	}

	protected Set<Integer> getUsedAtomIds() {
		return usedAtomIds;
	}

	protected Set<Integer> getUserVariables() {
		return goal.getAtomManager().getUserVariables();
	}

	protected Set<Integer> getVariables() {
		return goal.getAtomManager().getVariables();
	}

	protected abstract void updateInfo();

}
