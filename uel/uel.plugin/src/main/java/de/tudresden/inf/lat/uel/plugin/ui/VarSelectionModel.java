package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.lat.uel.core.type.Equation;
import de.tudresden.inf.lat.uel.core.type.Ontology;

/**
 * 
 * @author Julian Mendez
 */
class VarSelectionModel {

	private Map<String, String> idLabelMap = new HashMap<String, String>();
	private Ontology ontology = null;
	private Set<String> setOfConstants = new TreeSet<String>();
	private Set<String> setOfOriginalVariables = new TreeSet<String>();
	private Set<String> setOfVariables = new TreeSet<String>();

	public VarSelectionModel(Set<String> originalVariables,
			Map<String, String> labels, Ontology ontology) {
		if (originalVariables == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (labels == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (ontology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.setOfOriginalVariables.addAll(originalVariables);
		this.setOfVariables.addAll(originalVariables);
		this.idLabelMap = labels;
		this.ontology = ontology;
		recomputeVariablesAndConstants();
	}

	private Set<String> computeConstants(Set<String> variables) {
		Set<String> ret = new TreeSet<String>();
		for (String name : variables) {
			ret.addAll(getRecursiveDefinitionSymbols(name));
			ret.addAll(getRecursivePrimitiveDefinitionSymbols(name));
		}
		ret.removeAll(variables);
		return ret;
	}

	private Set<String> computeVariables(Set<String> originalVariables,
			Set<String> currentVariables) {
		Set<String> ret = new TreeSet<String>();
		Set<String> toVisit = new HashSet<String>();
		Set<String> visited = new HashSet<String>();
		toVisit.addAll(originalVariables);
		while (!toVisit.isEmpty()) {
			String var = toVisit.iterator().next();
			visited.add(var);
			if (currentVariables.contains(var)) {
				toVisit.addAll(getRecursiveDefinitionSymbols(var));
				toVisit.addAll(getRecursivePrimitiveDefinitionSymbols(var));
				ret.add(var);
			}
			toVisit.removeAll(visited);
		}
		return ret;
	}

	public Set<String> getConstants() {
		return Collections.unmodifiableSet(this.setOfConstants);
	}

	public String getLabel(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String ret = this.idLabelMap.get(id);
		if (ret == null) {
			ret = id;
		}
		return ret;
	}

	public Ontology getOntology() {
		return this.ontology;
	}

	public Set<String> getOriginalVariables() {
		return Collections.unmodifiableSet(this.setOfOriginalVariables);
	}

	private Set<String> getRecursiveDefinitionSymbols(String name) {
		Set<String> currentSet = getOntology().getDefinitionSymbols(name);
		if (currentSet.isEmpty() && this.setOfOriginalVariables.contains(name)) {
			currentSet = getOntology().getPrimitiveDefinitionSymbols(name);
		}

		Set<String> visited = new HashSet<String>();
		Set<String> toVisit = new HashSet<String>();
		toVisit.addAll(currentSet);
		Set<String> ret = new HashSet<String>();
		while (!toVisit.isEmpty()) {
			String elem = toVisit.iterator().next();
			visited.add(elem);
			toVisit.removeAll(visited);
			Equation def = getOntology().getDefinition(elem);
			if (def == null) {
				ret.add(elem);
			} else {
				toVisit.addAll(getOntology().getDefinitionSymbols(elem));
			}
		}
		return ret;
	}

	private Set<String> getRecursivePrimitiveDefinitionSymbols(String name) {
		Set<String> defSymbols = getRecursiveDefinitionSymbols(name);
		defSymbols.retainAll(this.setOfVariables);
		Set<String> ret = new HashSet<String>();
		for (String symbol : defSymbols) {
			ret.addAll(getOntology().getPrimitiveDefinitionSymbols(symbol));
		}
		return ret;
	}

	public Set<String> getVariables() {
		return Collections.unmodifiableSet(this.setOfVariables);
	}

	public boolean makeConstant(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		boolean ret = false;
		if (this.setOfVariables.contains(id)
				&& !this.setOfOriginalVariables.contains(id)) {
			ret = this.setOfVariables.remove(id);
			recomputeVariablesAndConstants();
		}
		return ret;
	}

	public boolean makeVariable(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		boolean ret = false;
		if (this.setOfConstants.contains(id)) {
			ret = this.setOfVariables.add(id);
			recomputeVariablesAndConstants();
		}
		return ret;
	}

	private void recomputeVariablesAndConstants() {
		this.setOfVariables = computeVariables(this.setOfOriginalVariables,
				this.setOfVariables);
		this.setOfConstants = computeConstants(this.setOfVariables);
	}

}
