package de.tudresden.inf.lat.uel.plugin.type;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

/**
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public interface AtomManager {

	public static final String UNDEF_SUFFIX = "_UNDEF";

	public Integer addConcept(String str);

	public Integer addRole(String str);

	public ConceptName createConceptName(String conceptName, boolean isVar);

	public ExistentialRestriction createExistentialRestriction(String roleName,
			ConceptName child);

	public ConceptName createUndefConceptName(ConceptName concept, boolean isVar);

	public IndexedSet<Atom> getAtoms();

	public Integer getConceptIndex(String conceptName);

	public String getConceptName(Integer id);

	public Integer getRoleIndex(String roleName);

	public String getRoleName(Integer id);

}
