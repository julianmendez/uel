package de.tudresden.inf.lat.uel.type.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;

public class AtomManagerImpl implements AtomManager {

	private final IndexedSet<Atom> atoms = new IndexedSetImpl<>();
	private final Map<Integer, Integer> childMap = new HashMap<>();
	private final IndexedSet<String> conceptNames = new IndexedSetImpl<>();
	private final Set<Integer> constants = new HashSet<>();
	private final Set<Integer> definitionVariables = new HashSet<>();
	private final Set<Integer> flatteningVariables = new HashSet<>();
	private final IndexedSet<String> roleNames = new IndexedSetImpl<>();
	private final Set<Integer> userVariables = new HashSet<>();
	private final Set<Integer> variables = new HashSet<>();

	public AtomManagerImpl() {
	}

	@Override
	public Integer createConceptName(String conceptName) {
		Integer conceptNameId = conceptNames.addAndGetIndex(conceptName);
		Integer atomId = atoms.addAndGetIndex(new ConceptName(conceptNameId));
		if (!variables.contains(atomId)) {
			// if the concept name had already been created earlier and marked
			// as a variable, then do not mark it as a constant
			constants.add(atomId);
		}
		return atomId;
	}

	@Override
	public Integer createExistentialRestriction(String roleName, Integer childId) {
		Integer roleId = roleNames.addAndGetIndex(roleName);
		Integer atomId = atoms.addAndGetIndex(new ExistentialRestriction(roleId, getConceptName(childId)));
		childMap.put(atomId, childId);
		return atomId;
	}

	@Override
	public Integer createUndefConceptName(Integer originId) {
		String newName = conceptNames.get(getConceptName(originId).getConceptNameId()) + UNDEF_SUFFIX;
		return createConceptName(newName);
	}

	@Override
	public Atom getAtom(Integer atomId) {
		return atoms.get(atomId);
	}

	@Override
	public Integer getChild(Integer atomId) {
		return childMap.get(atomId);
	}

	@Override
	public ConceptName getConceptName(Integer atomId) {
		Atom atom = atoms.get(atomId);
		if ((atom == null) || !atom.isConceptName()) {
			throw new IllegalArgumentException("Argument does not represent a concept name.");
		}
		return (ConceptName) atom;
	}

	@Override
	public Set<Integer> getConstants() {
		return Collections.unmodifiableSet(constants);
	}

	@Override
	public Set<Integer> getDefinitionVariables() {
		return Collections.unmodifiableSet(definitionVariables);
	}

	@Override
	public ExistentialRestriction getExistentialRestriction(Integer atomId) {
		Atom atom = atoms.get(atomId);
		if ((atom == null) || !atom.isExistentialRestriction()) {
			throw new IllegalArgumentException("Argument does not represent an existential restriction.");
		}
		return (ExistentialRestriction) atom;
	}

	@Override
	public Set<Integer> getExistentialRestrictions() {
		return Collections.unmodifiableSet(childMap.keySet());
	}

	@Override
	public Set<Integer> getFlatteningVariables() {
		return Collections.unmodifiableSet(flatteningVariables);
	}

	@Override
	public Integer getIndex(Atom atom) {
		return atoms.getIndex(atom);
	}

	@Override
	public Set<Integer> getRoleIds() {
		return Collections.unmodifiableSet(roleNames.getIndices());
	}

	@Override
	public String getRoleName(Integer roleId) {
		return roleNames.get(roleId);
	}

	@Override
	public Set<Integer> getUserVariables() {
		return Collections.unmodifiableSet(userVariables);
	}

	@Override
	public Set<Integer> getVariables() {
		return Collections.unmodifiableSet(variables);
	}

	@Override
	public void makeConstant(Integer atomId) {
		getConceptName(atomId).makeConstant();
		constants.add(atomId);
		variables.remove(atomId);
		userVariables.remove(atomId);
		definitionVariables.remove(atomId);
		flatteningVariables.remove(atomId);
	}

	@Override
	public void makeDefinitionVariable(Integer atomId) {
		getConceptName(atomId).makeVariable();
		constants.remove(atomId);
		variables.add(atomId);
		userVariables.remove(atomId);
		definitionVariables.add(atomId);
		flatteningVariables.remove(atomId);
	}

	@Override
	public void makeFlatteningVariable(Integer atomId) {
		getConceptName(atomId).makeVariable();
		constants.remove(atomId);
		variables.add(atomId);
		userVariables.remove(atomId);
		definitionVariables.remove(atomId);
		flatteningVariables.add(atomId);
	}

	@Override
	public void makeUserVariable(Integer atomId) {
		getConceptName(atomId).makeVariable();
		constants.remove(atomId);
		variables.add(atomId);
		userVariables.add(atomId);
		definitionVariables.remove(atomId);
		flatteningVariables.remove(atomId);
	}

	@Override
	public String printConceptName(Integer atomId) {
		return conceptNames.get(getConceptName(atomId).getConceptNameId());
	}

	@Override
	public String printRoleName(Integer atomId) {
		return roleNames.get(getExistentialRestriction(atomId).getRoleId());
	}

	@Override
	public int size() {
		return atoms.size();
	}
}
