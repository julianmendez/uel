package de.tudresden.inf.lat.uel.type.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;

/**
 * The canonical implementation of an atom manager for UEL.
 * 
 * @author Stefan Borgwardt
 *
 */
public class AtomManagerImpl implements AtomManager {

	private final IndexedSet<Atom> atoms = new IndexedSetImpl<Atom>();
	private final Map<Integer, Integer> childMap = new HashMap<Integer, Integer>();
	private final IndexedSet<String> conceptNames = new IndexedSetImpl<String>();
	private final Set<Integer> constants = new HashSet<Integer>();
	private final Set<Integer> definitionVariables = new HashSet<Integer>();
	private final Map<Integer, Set<Integer>> existentialRestrictions = new HashMap<Integer, Set<Integer>>();
	private final Set<Integer> flatteningVariables = new HashSet<Integer>();
	private final Map<Integer, Integer> roleIdMap = new HashMap<Integer, Integer>();
	private final IndexedSet<String> roleNames = new IndexedSetImpl<String>();
	private final Set<Integer> undefs = new HashSet<Integer>();
	private final Set<Integer> userVariables = new HashSet<Integer>();
	private final Set<Integer> variables = new HashSet<Integer>();

	/**
	 * Initialize a new atom manager with empty indices.
	 */
	public AtomManagerImpl() {
	}

	private Integer createAppendedName(Integer originId, String suffix, boolean onlyTypes) {
		String newName = conceptNames.get(getConceptName(originId).getConceptNameId()) + suffix;
		return createConceptName(newName, onlyTypes);
	}

	@Override
	public Integer createBlankExistentialRestriction(Integer roleId) {
		String roleName = getRoleName(roleId);
		Integer fillerId = createConceptName(roleName + VAR_SUFFIX, false);
		makeUserVariable(fillerId);
		return createExistentialRestriction(roleName, fillerId);
	}

	@Override
	public Integer createConceptName(String conceptName, boolean onlyTypes) {
		Integer conceptNameId = conceptNames.addAndGetIndex(conceptName);
		Integer atomId = atoms.addAndGetIndex(new ConceptName(conceptNameId));
		if (!variables.contains(atomId) && !onlyTypes) {
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
		roleIdMap.put(atomId, roleId);

		Set<Integer> set = existentialRestrictions.get(roleId);
		if (set == null) {
			set = new HashSet<Integer>();
			existentialRestrictions.put(roleId, set);
		}
		set.add(atomId);

		return atomId;
	}

	@Override
	public Integer createRoleGroupConceptName(Integer originId) {
		Integer newId = createAppendedName(originId, ROLEGROUP_SUFFIX, true);
		return newId;
	}

	@Override
	public Integer createUndefConceptName(Integer originId) {
		Integer undefId = createAppendedName(originId, UNDEF_SUFFIX, false);
		undefs.add(undefId);
		return undefId;
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
	public Set<Integer> getExistentialRestrictions(Integer roleId) {
		Set<Integer> ret = existentialRestrictions.get(roleId);
		if (ret == null) {
			// in case there are no role group atoms
			ret = Collections.emptySet();
		} else {
			ret = Collections.unmodifiableSet(ret);
		}
		return ret;
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
	public Integer getRoleId(Integer atomId) {
		return roleIdMap.get(atomId);
	}

	@Override
	public Integer getRoleId(String roleName) {
		return roleNames.getIndex(roleName);
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
	public Set<Integer> getUndefNames() {
		return Collections.unmodifiableSet(undefs);
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
		return roleNames.get(roleIdMap.get(atomId));
	}

	@Override
	public Integer removeUndef(Integer undefId) {
		String undefName = printConceptName(undefId);
		if (!undefName.endsWith(UNDEF_SUFFIX)) {
			throw new IllegalArgumentException("Argument does not represent an UNDEF concept name.");
		}
		String origName = undefName.substring(0, undefName.length() - UNDEF_SUFFIX.length());
		return createConceptName(origName, false);
	}

	@Override
	public int size() {
		return atoms.size();
	}
}
