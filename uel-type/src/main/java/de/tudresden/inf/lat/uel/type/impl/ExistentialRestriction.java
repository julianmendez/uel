package de.tudresden.inf.lat.uel.type.impl;

import de.tudresden.inf.lat.uel.type.api.Atom;

/**
 * Represents a flat EL-atom consisting of a role name and a concept name.
 * 
 * @author Stefan Borgwardt
 */
public class ExistentialRestriction implements Atom {

	private final ConceptName child;
	private final Integer role;

	/**
	 * Construct a new flat existential restriction.
	 * 
	 * @param role
	 *            the role name identifier
	 * @param child
	 *            the concept name
	 */
	public ExistentialRestriction(Integer role, ConceptName child) {
		this.role = role;
		this.child = child;
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
		if (!other.child.equals(child)) {
			return false;
		}
		return true;
	}

	@Override
	public ConceptName getConceptName() {
		return child;
	}

	/**
	 * Retrieve the role name of this existential restriction.
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
		result = prime * result + child.hashCode();
		result = prime * result + role;
		return result;
	}

	@Override
	public boolean isConceptName() {
		return false;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isExistentialRestriction() {
		return true;
	}

	@Override
	public boolean isGround() {
		return child.isGround();
	}

	@Override
	public boolean isVariable() {
		return false;
	}

	@Override
	public String toString() {
		return "some " + role + " " + child;
	}

}
