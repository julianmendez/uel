package de.tudresden.inf.lat.uel.type.impl;

import de.tudresden.inf.lat.uel.type.api.Atom;

/**
 * Represents a flat EL-atom consisting of a role name and a concept name.
 * 
 * @author Stefan Borgwardt
 */
public class ExistentialRestriction implements Atom {

	private final ConceptName conceptName;
	private final Integer role;

	/**
	 * Construct a new flat existential restriction.
	 * 
	 * @param role
	 *            the role name
	 * @param child
	 *            the concept name
	 */
	public ExistentialRestriction(Integer role, ConceptName child) {
		this.role = role;
		this.conceptName = child;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ExistentialRestriction)) {
			return false;
		}

		ExistentialRestriction other = (ExistentialRestriction) obj;
		if (!other.role.equals(role)) {
			return false;
		}
		if (!other.conceptName.equals(conceptName)) {
			return false;
		}
		return true;
	}

	@Override
	public ConceptName getConceptName() {
		return conceptName;
	}

	/**
	 * Retrieve the role name of this flat atom.
	 * 
	 * @return the role name
	 */
	public Integer getRoleId() {
		return role;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + conceptName.hashCode();
		result = prime * result + role;
		return result;
	}

	/**
	 * Check whether this flat atom is a concept name.
	 * 
	 * @return true iff this atom has no associated role name
	 */
	@Override
	public boolean isConceptName() {
		return false;
	}

	/**
	 * Check whether this flat atom is a constant.
	 * 
	 * @return true iff this atom is not an existential restriction adn is
	 *         ground
	 */
	@Override
	public boolean isConstant() {
		return false;
	}

	/**
	 * Check whether this flat atom is an existential restriction.
	 * 
	 * @return true iff this atom has an associated role name
	 */
	@Override
	public boolean isExistentialRestriction() {
		return true;
	}

	/**
	 * Check whether this flat atom is ground.
	 * 
	 * @return true iff the concept name is not a variable
	 */
	@Override
	public boolean isGround() {
		return getConceptName().isGround();
	}

	@Override
	public boolean isVariable() {
		return false;
	}

	@Override
	public boolean isTop() {
		return false;
	}

	@Override
	public String toString() {
		return "some " + role + " " + conceptName;
	}

}
