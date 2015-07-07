package de.tudresden.inf.lat.uel.type.impl;

import de.tudresden.inf.lat.uel.type.api.Atom;

/**
 * Represents a flat EL-atom consisting of a concept name.
 * 
 * @author Stefan Borgwardt
 */
public class ConceptName implements Atom {

	/**
	 * Create a concept name representing the top concept.
	 * 
	 * @param id
	 *            the identifier of the top concept
	 * @return a new instance of top
	 */
	public static ConceptName createTop(Integer id) {
		ConceptName ret = new ConceptName(id, false);
		return ret;
	}

	private final Integer conceptNameId;
	private boolean isVariable;
	private boolean isAuxiliaryVariable = false;
	private boolean isTop = false;

	/**
	 * Construct a new concept name.
	 * @param conceptName
	 *            the concept name
	 * @param isVar
	 *            a flag indicating whether the concept name is a variable
	 */
	public ConceptName(Integer conceptName, boolean isVar) {
		this.conceptNameId = conceptName;
		this.isVariable = isVar;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ConceptName)) {
			return false;
		}

		ConceptName other = (ConceptName) obj;
		if (!other.conceptNameId.equals(conceptNameId) || other.isTop != isTop) {
			return false;
		}
		return true;
	}

	@Override
	public Integer getConceptNameId() {
		return conceptNameId;
	}

	@Override
	public int hashCode() {
		return conceptNameId;
	}

	/**
	 * Check whether this flat atom is a concept name.
	 * 
	 * @return true iff this atom has no associated role name
	 */
	@Override
	public boolean isConceptName() {
		return true;
	}

	/**
	 * Check whether this flat atom is a constant.
	 * 
	 * @return true iff this atom is not an existential restriction adn is
	 *         ground
	 */
	@Override
	public boolean isConstant() {
		return !isVariable;
	}

	/**
	 * Check whether this flat atom is an existential restriction.
	 * 
	 * @return true iff this atom has an associated role name
	 */
	@Override
	public boolean isExistentialRestriction() {
		return false;
	}

	/**
	 * Check whether this flat atom is ground.
	 * 
	 * @return true iff the concept name is not a variable
	 */
	@Override
	public boolean isGround() {
		return isConstant();
	}

	/**
	 * Check whether this flat atom is top.
	 * 
	 * @return true iff this atom is top
	 */
	public boolean isTop() {
		return this.isTop;
	}

	/**
	 * Check whether this flat atom is a variable.
	 * 
	 * @return true iff this atom is not an existential restriction and is not
	 *         ground
	 */
	@Override
	public boolean isVariable() {
		return isVariable;
	}

	/**
	 * Set this flat atom as a variable.
	 * 
	 * @param isVar
	 *            true iff this flat atom is variable
	 */
	public void setVariable(boolean isVar) {
		this.isVariable = isVar;
	}
	
	public void setAuxiliaryVariable(boolean isAuxiliaryVar) {
		this.isAuxiliaryVariable = isAuxiliaryVar;
	}
	
	public boolean isAuxiliaryVariable() {
		return this.isAuxiliaryVariable;
	}

	@Override
	public String toString() {
		return conceptNameId.toString() + " {" + (isVariable() ? "v" : "c")
				+ "}";
	}

}