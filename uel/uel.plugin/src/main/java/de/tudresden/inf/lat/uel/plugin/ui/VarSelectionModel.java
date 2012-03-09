package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.tudresden.inf.lat.uel.plugin.processor.PluginGoal;
import de.tudresden.inf.lat.uel.plugin.type.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;

/**
 * An object of this class can manage which concept names are considered by the
 * user as variables and which ones as constants.
 * 
 * @author Julian Mendez
 */
class VarSelectionModel {

	private final Map<String, String> idLabelMap;
	private final PluginGoal pluginGoal;
	private Set<String> setOfOriginalVariables = new TreeSet<String>();

	public VarSelectionModel(Set<String> originalVariables,
			Map<String, String> labels, PluginGoal g) {
		if (originalVariables == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (labels == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (g == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.setOfOriginalVariables.addAll(originalVariables);
		this.idLabelMap = labels;
		this.pluginGoal = g;
	}

	public Set<String> getConstants() {
		Set<String> ret = new HashSet<String>();
		for (Integer atomId : getGoal().getConstants()) {
			ret.add(getGoal().getAtomManager().getConceptName(atomId));
		}
		return Collections.unmodifiableSet(ret);
	}

	public PluginGoal getGoal() {
		return this.pluginGoal;
	}

	public String getLabel(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String ret = this.idLabelMap.get(id);

		if (ret == null) {
			if (id.endsWith(AtomManager.UNDEF_SUFFIX)) {
				String origId = id.substring(0, id.length()
						- AtomManager.UNDEF_SUFFIX.length());
				ret = this.idLabelMap.get(origId);
				if (ret != null) {
					ret += AtomManager.UNDEF_SUFFIX;
				}
			}
		}

		if (ret == null) {
			int p = id.indexOf("#");
			if (p != -1) {
				ret = id.substring(p + 1);
			} else {
				ret = id;
			}
		}

		return ret;
	}

	public Set<String> getOriginalVariables() {
		return Collections.unmodifiableSet(this.setOfOriginalVariables);
	}

	public PluginGoal getPluginGoal() {
		return this.pluginGoal;
	}

	public Set<String> getVariables() {
		Set<String> ret = new HashSet<String>();
		for (Integer atomId : getGoal().getVariables()) {
			Atom atom = getGoal().getAtomManager().getAtoms().get(atomId);

			if (atom.isConceptName()) {
				ConceptName concept = (ConceptName) atom;
				if (getGoal().getUelInput().getUserVariables()
						.contains(concept.getConceptNameId())) {
					String name = getGoal().getAtomManager().getConceptName(
							atom.getConceptNameId());
					ret.add(name);
				}
			}
		}
		return Collections.unmodifiableSet(ret);
	}

	public void makeConstant(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Integer atomId = getGoal().getAtomManager().getConceptIndex(id);
		if (atomId == null) {
			throw new IllegalArgumentException("Unkown atom:'" + id + "'.");
		}
		this.pluginGoal.makeConstant(atomId);
	}

	public void makeVariable(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Integer atomId = getGoal().getAtomManager().getConceptIndex(id);
		if (atomId == null) {
			throw new IllegalArgumentException("Unkown atom :'" + id + "'.");
		}
		this.pluginGoal.makeVariable(atomId);
	}

}
