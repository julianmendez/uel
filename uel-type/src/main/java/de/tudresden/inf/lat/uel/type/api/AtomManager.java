package de.tudresden.inf.lat.uel.type.api;

import java.util.Set;

import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

/**
 * An atom manager manages identifiers for atoms (concept names and existential
 * restrictions) and maintains indices of variables, constants, and more.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public interface AtomManager {

	/**
	 * A string used to distinguish 'undef' concept names from regular ones.
	 */
	String UNDEF_SUFFIX = "_UNDEF";

	/**
	 * A string used to distinguish 'rolegroup' type identifiers.
	 */
	String ROLEGROUP_SUFFIX = "_ROLEGROUP";

	/**
	 * Create a new concept name with the given string representation and add it
	 * to the index. If the concept name already exists, this method simply
	 * returns the existing identifier.
	 * 
	 * @param conceptName
	 *            the string representation of the concept name (OWLClass)
	 * @return the integer id of the concept name
	 */
	Integer createConceptName(String conceptName);

	/**
	 * Create a new existential restriction from the given role name and child
	 * atom.
	 * 
	 * @param roleName
	 *            the string representation of the role name (OWLObjectProperty)
	 * @param child
	 *            the integer id of the filler
	 * @return the integer id of the existential restriction
	 */
	Integer createExistentialRestriction(String roleName, Integer child);

	/**
	 * Create a new 'undef' concept name from an existing concept name id.
	 * 
	 * @param originId
	 *            the original id
	 * @return the 'undef' id
	 */
	Integer createUndefConceptName(Integer originId);

	/**
	 * Create a new 'role group' type from an existing type id. 'Role group' ids
	 * are not added to the sets of constants or variables.
	 * 
	 * @param originid
	 *            the original id
	 * @return the 'role group' id
	 */
	Integer createRoleGroupConceptName(Integer originid);

	/**
	 * Retrieve a certain atom.
	 * 
	 * @param atomId
	 *            the atom id
	 * @return the Atom object designated by the id
	 */
	Atom getAtom(Integer atomId);

	/**
	 * Retrieve the filler of an existential restriction.
	 * 
	 * @param atomId
	 *            the id of the existential restriction
	 * @return the filler id
	 */
	Integer getChild(Integer atomId);

	/**
	 * Retrieve a certain concept name.
	 * 
	 * @param atomId
	 *            the concept name id
	 * @return the ConceptName object designated by the id
	 */
	ConceptName getConceptName(Integer atomId);

	/**
	 * Obtain the set of all constants.
	 * 
	 * @return the concept name ids
	 */
	Set<Integer> getConstants();

	/**
	 * Obtain the set of all definition variables.
	 * 
	 * @return the concept name ids
	 */
	Set<Integer> getDefinitionVariables();

	/**
	 * Retrieve a certain existential restriction.
	 * 
	 * @param atomId
	 *            the atom id
	 * @return the ExistentialRestriction object designated by the id
	 */
	ExistentialRestriction getExistentialRestriction(Integer atomId);

	/**
	 * Obtain the set of all existential restrictions.
	 * 
	 * @return the atom ids
	 */
	Set<Integer> getExistentialRestrictions();

	/**
	 * Obtain the set of all flattening variables.
	 * 
	 * @return the concept name ids
	 */
	Set<Integer> getFlatteningVariables();

	/**
	 * Retrieve the id of a certain atom.
	 * 
	 * @param atom
	 *            the Atom object
	 * @return the associated id
	 */
	Integer getIndex(Atom atom);

	/**
	 * Obtain the set of all user variables.
	 * 
	 * @return the concept name ids
	 */
	Set<Integer> getUserVariables();

	/**
	 * Obtain the set of all (definition, flattening, and user) variables.
	 * 
	 * @return the concept name ids
	 */
	Set<Integer> getVariables();

	/**
	 * Mark a concept name as a constant.
	 * 
	 * @param atomId
	 *            the concept name id
	 */
	void makeConstant(Integer atomId);

	/**
	 * Mark a concept name as a definition variable.
	 * 
	 * @param atomId
	 *            the concept name id
	 */
	void makeDefinitionVariable(Integer atomId);

	/**
	 * Mark a concept name as a flattening variable.
	 * 
	 * @param atomId
	 *            the concept name id
	 */
	void makeFlatteningVariable(Integer atomId);

	/**
	 * Mark a concept name as a user variable.
	 * 
	 * @param atomId
	 *            the concept name id
	 */
	void makeUserVariable(Integer atomId);

	/**
	 * Retrieve the string representation of a concept name.
	 * 
	 * @param atomId
	 *            the concept name id
	 * @return the string representation of the id
	 */
	String printConceptName(Integer atomId);

	/**
	 * Retrieve the string representation of a role name used in a certain
	 * existential restriction.
	 * 
	 * @param atomId
	 *            the atom id
	 * @return the string representation of the role name used in the atom
	 */
	String printRoleName(Integer atomId);

	/**
	 * Obtain the set of all role ids (different from atom ids).
	 * 
	 * @return all integer ids of roles
	 */
	Set<Integer> getRoleIds();

	/**
	 * Retrieve the string reprentation of a role id.
	 * 
	 * @param roleId
	 *            the role id
	 * @return the string representation of the id
	 */
	String getRoleName(Integer roleId);

	/**
	 * Retrieve the role id of a certain role name.
	 * 
	 * @param roleName
	 *            the string representation of the role name
	 * @return the associated role id
	 */
	Integer getRoleId(String roleName);

	/**
	 * Return the number of atoms used.
	 * 
	 * @return the number of atoms
	 */
	int size();
}
