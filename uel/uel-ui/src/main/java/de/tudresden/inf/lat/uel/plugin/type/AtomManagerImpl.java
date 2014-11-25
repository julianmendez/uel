package de.tudresden.inf.lat.uel.plugin.type;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;
import de.tudresden.inf.lat.uel.type.impl.IndexedSetImpl;

/**
 * This is the default implementation of an atom manager.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 * @see AtomManager
 */
public class AtomManagerImpl implements AtomManager {

	private IndexedSet<Atom> atoms = new IndexedSetImpl<Atom>();
	private IndexedSet<String> conceptNames = new IndexedSetImpl<String>();
	private IndexedSet<String> roleNames = new IndexedSetImpl<String>();

	/**
	 * Constructs a new atom manager.
	 */
	public AtomManagerImpl() {
	}

	@Override
	public Integer addConcept(String str) {
		return this.conceptNames.addAndGetIndex(str);
	}

	@Override
	public Integer addRole(String str) {
		return this.roleNames.addAndGetIndex(str);
	}

	@Override
	public ConceptName createConceptName(String str, boolean isVar) {
		ConceptName ret;
		if (this.conceptNames.contains(str)) {
			Integer conceptId = this.conceptNames.getIndex(str);
			Integer index = this.atoms.getIndex(new ConceptName(conceptId,
					isVar));
			ret = (ConceptName) this.atoms.get(index);
		} else {
			Integer conceptId = Math.max(this.atoms.getNextIndex(),
					this.conceptNames.getNextIndex());
			this.conceptNames.add(str, conceptId);
			ret = new ConceptName(conceptId, isVar);
			this.atoms.add(ret);
		}
		return ret;
	}

	@Override
	public ExistentialRestriction createExistentialRestriction(String roleName,
			ConceptName child) {
		Integer roleId = this.roleNames.addAndGetIndex(roleName);
		Integer index = this.atoms.addAndGetIndex(new ExistentialRestriction(
				roleId, child));
		ExistentialRestriction ret = (ExistentialRestriction) this.atoms
				.get(index);
		return ret;
	}

	@Override
	public ConceptName createUndefConceptName(ConceptName other, boolean isVar) {
		String newName = this.conceptNames.get(other.getConceptNameId())
				+ UNDEF_SUFFIX;
		return this.createConceptName(newName, isVar);
	}

	@Override
	public IndexedSet<Atom> getAtoms() {
		return this.atoms;
	}

	@Override
	public Integer getConceptIndex(String conceptName) {
		return this.conceptNames.getIndex(conceptName);
	}

	@Override
	public String getConceptName(Integer id) {
		return this.conceptNames.get(id);
	}

	@Override
	public Integer getRoleIndex(String roleName) {
		return this.roleNames.getIndex(roleName);
	}

	@Override
	public String getRoleName(Integer id) {
		return this.roleNames.get(id);
	}

}
