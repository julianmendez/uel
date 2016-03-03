package de.tudresden.inf.lat.uel.type.api;

import java.util.Set;

import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

/**
 * An atom manager manages identifiers for atoms (concept names and existential
 * restrictions) and also identifiers and names for concepts and roles.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public interface AtomManager {

	String UNDEF_SUFFIX = "_UNDEF";

	Integer createConceptName(String conceptName);

	Integer createExistentialRestriction(String roleName, Integer child);

	Integer createUndefConceptName(Integer originId);

	Atom getAtom(Integer atomId);

	Integer getChild(Integer atomId);

	ConceptName getConceptName(Integer atomId);

	Set<Integer> getConstants();

	Set<Integer> getDefinitionVariables();

	ExistentialRestriction getExistentialRestriction(Integer atomId);

	Set<Integer> getExistentialRestrictions();

	Set<Integer> getFlatteningVariables();

	Integer getIndex(Atom atom);

	Set<Integer> getUserVariables();

	Set<Integer> getVariables();

	void makeConstant(Integer atomId);

	void makeDefinitionVariable(Integer atomId);

	void makeFlatteningVariable(Integer atomId);

	void makeUserVariable(Integer atomId);

	String printConceptName(Integer atomId);

	String printRoleName(Integer atomId);

	Set<Integer> getRoleIds();

	String getRoleName(Integer roleId);

	int size();
}
