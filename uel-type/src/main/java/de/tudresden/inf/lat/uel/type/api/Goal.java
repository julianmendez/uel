package de.tudresden.inf.lat.uel.type.api;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.impl.DefinitionSet;

/**
 * An object implementing this interface is an input for the UEL system.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public interface Goal {

	/**
	 * Returns the atom manager.
	 * 
	 * @return the atom manager
	 */
	AtomManager getAtomManager();

	/**
	 * Returns the set of flattened definitions.
	 * 
	 * @return the set of definitions
	 */
	default DefinitionSet getDefinitions() {
		return new DefinitionSet();
	}

	/**
	 * Returns a specific definition.
	 * 
	 * @param varId
	 *            The id of the variable that is defined.
	 * @return the definition of 'varId', if it exists, and 'null' otherwise
	 */
	default Definition getDefinition(Integer varId) {
		return null;
	}

	default Set<Integer> getDefiniens(Integer varId) {
		return null;
	}

	/**
	 * Returns the set of flattened goal equations.
	 * 
	 * @return the set of goal equations
	 */
	default Set<Equation> getEquations() {
		return Collections.emptySet();
	}

	/**
	 * Returns the set of flattened and small goal disequations.
	 * 
	 * @return the set of goal disequations
	 */
	default Set<Disequation> getDisequations() {
		return Collections.emptySet();
	}

	/**
	 * Returns the set of all flattened equations (definitions and goal).
	 * 
	 * @return the set of equations
	 */
	default Set<Subsumption> getSubsumptions() {
		return Collections.emptySet();
	}

	/**
	 * Returns the set of user variables.
	 * 
	 * @return the set of user variables
	 */
	default Set<Dissubsumption> getDissubsumptions() {
		return Collections.emptySet();
	}

	default Set<Integer> getTypes() {
		return Collections.emptySet();
	}

	default Integer getDirectSupertype(Integer type) {
		return null;
	}

	default boolean subtypeOrEquals(Integer type1, Integer type2) {
		while (type1 != null) {
			if (type1.equals(type2)) {
				return true;
			}
			type1 = getDirectSupertype(type1);
		}
		return false;
	}

	default boolean areDisjoint(Integer type1, Integer type2) {
		return !subtypeOrEquals(type1, type2) && !subtypeOrEquals(type2, type1);
	}

	default Map<Integer, Set<Integer>> getDomains() {
		return Collections.emptyMap();
	}

	default Map<Integer, Set<Integer>> getRanges() {
		return Collections.emptyMap();
	}

	default Map<Integer, Integer> getRoleGroupTypes() {
		return Collections.emptyMap();
	}

	default boolean hasNegativePart() {
		return false;
	}

	default Map<Integer, Integer> getTypeAssignment() {
		return Collections.emptyMap();
	}

	default Map<Integer, Integer> getRoleNumberRestrictions() {
		return Collections.emptyMap();
	}

	default boolean restrictUndefContext() {
		return false;
	}

	default boolean areCompatible(Integer atomId1, Integer atomId2) {
		return true;
	}
	
	default boolean isCommonSubsumee(Integer subsumee, Integer subsumer1, Integer subsumer2) {
		return false;
	}

	default boolean isTop(Integer id) {
		return false;
	}
	
	/**
	 * Retrieves the URI of the 'RoleGroup' in SNOMED CT.
	 * @return RoleGroup uri
	 */
	default String SNOMED_RoleGroup_URI() {
		return null;
	}
	
	/**
	 * Retrieves the URI of 'SNOMED CT Concept' in SNOMED CT.
	 * @return SNOMED CT Concept uri
	 */
	default String SNOMED_CT_Concept_URI() {
		return null;
	}

}
