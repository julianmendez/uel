package de.tudresden.inf.lat.uel.rule;

import de.tudresden.inf.lat.uel.type.api.Atom;

/**
 * Represents a flat EL-atom consisting of a role name (optional) and a concept name.
 * 
 * @author Stefan Borgwardt
 */
public final class FlatAtom implements Atom {

	private static final Integer EMPTY_ROLE = -1;
	
	private final Integer role;
	private final Integer conceptName;
	private final boolean ground;
	
	/**
	 * Construct a new flat existential restriction.
	 * 
	 * @param role the role name
	 * @param conceptName the concept name
	 * @param ground a flag indicating whether the concept name is not a variable
	 */
	public FlatAtom(Integer role, Integer conceptName, boolean ground) {
		this.role = role;
		this.conceptName = conceptName;
		this.ground = ground;
	}
	
	/**
	 * Construct a new flat atom without existential restriction, i.e. a concept name.
	 * 
	 * @param conceptName the concept name
	 * @param ground a flag indicating whether the concept name is not a variable
	 */
	public FlatAtom(Integer conceptName, boolean ground) {
		this(EMPTY_ROLE, conceptName, ground);
	}
	
	/**
	 * Retrieve the role name of this flat atom.
	 * @return the role name
	 */
	@Override
	public Integer getRole() {
		return role;
	}
	
	/**
	 * Retrieve the concept name of this flat atom.
	 * @return the concept name
	 */
	@Override
	public Integer getConceptName() {
		return conceptName;
	}
	
	/**
	 * Check whether this flat atom is ground.
	 * @return true iff the concept name is not a variable
	 */
	@Override
	public boolean isGround() {
		return ground;
	}

	/**
	 * Check whether this flat atom is an existential restriction.
	 * @return true iff this atom has an associated role name
	 */
	@Override
	public boolean isExistentialRestriction() {
		return role != EMPTY_ROLE;
	}
	
	/**
	 * Check whether this flat atom is a concept name.
	 * @return true iff this atom has no associated role name
	 */
	@Override
	public boolean isConceptName() {
		return role == EMPTY_ROLE;
	}
	
	/**
	 * Check whether this flat atom is a variable.
	 * @return true iff this atom is not an existential restriction and is not ground
	 */
	@Override
	public boolean isVariable() {
		return !isExistentialRestriction() && !isGround();
	}
	
	/**
	 * Check whether this flat atom is a constant.
	 * @return true iff this atom is not an existential restriction adn is ground
	 */
	@Override
	public boolean isConstant() {
		return !isExistentialRestriction() && isGround();
	}
	
	/**
	 * Return the 'FlatAtom' object that represents the concept name of this atom.
	 * @return the concept name of this atom encapsulated in an object of type 'FlatAtom'
	 */
	public FlatAtom getChild() {
		return new FlatAtom(conceptName, ground);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + conceptName.hashCode();
		result = prime * result + role.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof FlatAtom)) return false;
		
		FlatAtom other = (FlatAtom) obj;
		if (!other.role.equals(role)) return false;
		if (!other.conceptName.equals(conceptName)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		if (isExistentialRestriction()) {
			return "some " + role + " " + conceptName;
		} else {
			return conceptName.toString() + " {" + (isVariable()?"v":"c") + "}";
		}
	}
	
}
