package de.tudresden.inf.lat.uel.type.impl;

import de.tudresden.inf.lat.uel.type.api.Atom;

/**
 * Represents a flat EL-atom consisting of a concept name.
 * 
 * @author Stefan Borgwardt
 */
public class ConceptName implements Atom {

	private final Integer conceptNameId;
	private boolean isVariable = false;

	/**
	 * Construct a new concept name.
	 * 
	 * @param conceptNameId
	 *            the concept name identifier
	 */
	protected ConceptName(Integer conceptNameId) {
		this.conceptNameId = conceptNameId;
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
		if (!other.conceptNameId.equals(conceptNameId)) {
			return false;
		}
		return true;
	}

	@Override
	public ConceptName getConceptName() {
		return this;
	}

	Integer getConceptNameId() {
		return conceptNameId;
	}

	@Override
	public int hashCode() {
		return conceptNameId;
	}

	@Override
	public boolean isConceptName() {
		return true;
	}

	@Override
	public boolean isConstant() {
		return !isVariable();
	}

	@Override
	public boolean isExistentialRestriction() {
		return false;
	}

	@Override
	public boolean isGround() {
		return isConstant();
	}

	@Override
	public boolean isVariable() {
		return isVariable;
	}

	void makeConstant() {
		isVariable = false;
	}

	void makeVariable() {
		isVariable = true;
	}

	@Override
	public String toString() {
		return conceptNameId.toString() + " {" + (isVariable() ? "v" : "c") + "}";
	}

}
