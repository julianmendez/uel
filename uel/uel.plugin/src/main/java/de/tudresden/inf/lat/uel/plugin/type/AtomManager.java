package de.tudresden.inf.lat.uel.plugin.type;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
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

	/**
	 * Suffix used for concept names created automatically by primitive
	 * definitions.
	 */
	public static final String UNDEF_SUFFIX = "_UNDEF";

	/**
	 * Adds a new concept to the manager and returns the identifier of the given
	 * concept name.
	 * 
	 * @param name
	 *            concept name
	 * @return the identifier of the given concept name
	 */
	public Integer addConcept(String name);

	/**
	 * Adds a new role to the manager and returns the identifier of the given
	 * role name.
	 * 
	 * @param name
	 *            role name
	 * @return the identifier of the given role name
	 */
	public Integer addRole(String name);

	/**
	 * Creates a new concept name atom with the given name.
	 * 
	 * @param conceptName
	 *            name of the concept name
	 * @param isVar
	 *            <code>true</code> if and only if the new concept name atom is
	 *            a variable
	 * @return a new concept name atom registered in this atom manager
	 */
	public ConceptName createConceptName(String conceptName, boolean isVar);

	/**
	 * Creates a new existential restriction atom with the given role name and
	 * child.
	 * 
	 * @param roleName
	 *            name of the role
	 * @param child
	 *            the concept name in the existential restriction
	 * @return a new existential restriction atom registered in this atom
	 *         manager
	 */
	public ExistentialRestriction createExistentialRestriction(String roleName,
			ConceptName child);

	/**
	 * Creates a new concept name atom taking another concept name atom as
	 * reference. This kind of concept name atom is created when transforming a
	 * primitive definition into a tru definition.
	 * 
	 * @param concept
	 *            reference concept
	 * @param isVar
	 *            <code>true</code>if and only if the new concept name atom is a
	 *            variable
	 * @return a new concept name atom taking another concept name atom as
	 *         reference
	 */
	public ConceptName createUndefConceptName(ConceptName concept, boolean isVar);

	/**
	 * Returns the stored atoms and their indices.
	 * 
	 * @return the stored atoms and their indices
	 */
	public IndexedSet<Atom> getAtoms();

	/**
	 * Returns the index of the given concept name.
	 * 
	 * @param conceptName
	 *            concept name
	 * @return the index of the given concept name.
	 */
	public Integer getConceptIndex(String conceptName);

	/**
	 * Returns the concept name of the given index.
	 * 
	 * @param id
	 *            index
	 * @return the concept name of the given index
	 */
	public String getConceptName(Integer id);

	/**
	 * Returns the index of the given role name.
	 * 
	 * @param roleName
	 *            role name
	 * @return the index of the given role name
	 */
	public Integer getRoleIndex(String roleName);

	/**
	 * Returns the role name of the given index.
	 * 
	 * @param id
	 *            index
	 * @return the role name of the given index
	 */
	public String getRoleName(Integer id);

}
