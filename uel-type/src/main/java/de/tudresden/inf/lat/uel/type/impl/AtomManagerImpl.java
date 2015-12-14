package de.tudresden.inf.lat.uel.type.impl;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;

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
	public ConceptName createConceptName(String conceptName, boolean isVar) {
		Integer conceptNameId = conceptNames.addAndGetIndex(conceptName);
		Integer atomId = atoms.addAndGetIndex(new ConceptName(conceptNameId, isVar));
		return (ConceptName) atoms.get(atomId);
	}

	@Override
	public ExistentialRestriction createExistentialRestriction(String roleName, ConceptName child) {
		Integer roleId = this.roleNames.addAndGetIndex(roleName);
		Integer atomId = this.atoms.addAndGetIndex(new ExistentialRestriction(roleId, child));
		return (ExistentialRestriction) this.atoms.get(atomId);
	}

	@Override
	public ConceptName createUndefConceptName(ConceptName other, boolean isVar) {
		String newName = this.conceptNames.get(other.getConceptNameId()) + UNDEF_SUFFIX;
		return this.createConceptName(newName, isVar);
	}

	@Override
	public IndexedSet<Atom> getAtoms() {
		return this.atoms;
	}

	@Override
	public Atom getAtom(Integer atomId) {
		return atoms.get(atomId);
	}

	@Override
	public Integer getIndex(Atom atom) {
		return atoms.getIndex(atom);
	}

	@Override
	public ConceptName getConceptName(Integer atomId) {
		Atom atom = atoms.get(atomId);
		if ((atom == null) || !atom.isConceptName()) {
			throw new IllegalArgumentException("Argument is not a concept name identifier: '" + atomId + "'.");
		}
		return (ConceptName) atom;
	}

	@Override
	public ExistentialRestriction getExistentialRestriction(Integer atomId) {
		Atom atom = atoms.get(atomId);
		if ((atom == null) || !atom.isExistentialRestriction()) {
			throw new IllegalArgumentException(
					"Argument is not the identifier of an existential restriction: '" + atomId + "'.");
		}
		return (ExistentialRestriction) atom;
	}

	@Override
	public String printConceptName(Atom atom) {
		return this.conceptNames.get(((ConceptName) atom).getConceptNameId());
	}

	@Override
	public String printRoleName(Atom atom) {
		return this.roleNames.get(((ExistentialRestriction) atom).getRoleId());
	}

}
