package de.tudresden.inf.lat.uel.type.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomChangeEvent;
import de.tudresden.inf.lat.uel.type.api.AtomChangeListener;
import de.tudresden.inf.lat.uel.type.api.AtomChangeType;

/**
 * Represents a flat EL-atom consisting of a concept name.
 * 
 * @author Stefan Borgwardt
 */
public class ConceptName implements Atom {

	public static ConceptName createTop(Integer id) {
		ConceptName ret = new ConceptName(id, true);
		return ret;
	}

	private List<AtomChangeListener> changeListener = new ArrayList<AtomChangeListener>();
	private final Integer conceptNameId;
	private boolean isGround;
	private boolean isTop = false;

	/**
	 * Construct a new concept name.
	 * 
	 * @param isVar
	 *            a flag indicating whether the concept name is a variable
	 * @param conceptName
	 *            the concept name
	 */
	public ConceptName(boolean isVar, Integer conceptName) {
		this(conceptName, !isVar);
	}

	/**
	 * Construct a new concept name.
	 * 
	 * @param conceptName
	 *            the concept name
	 * @param ground
	 *            a flag indicating whether the concept name is not a variable
	 */
	public ConceptName(Integer conceptName, boolean ground) {
		this.conceptNameId = conceptName;
		this.isGround = ground;
	}

	@Override
	public boolean addAtomChangeListener(AtomChangeListener o) {
		return this.changeListener.add(o);
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
	public Collection<AtomChangeListener> getAtomChangeListeners() {
		return Collections.unmodifiableCollection(this.changeListener);
	}

	@Override
	public Integer getConceptNameId() {
		return conceptNameId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + conceptNameId;
		return result;
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
		return isGround();
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
		return isGround;
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
		return !isGround();
	}

	private void notify(AtomChangeEvent event) {
		for (AtomChangeListener listener : this.changeListener) {
			listener.atomTypeChanged(event);
		}
	}

	@Override
	public boolean removeAtomChangeListener(AtomChangeListener o) {
		return this.changeListener.remove(o);
	}

	/**
	 * Set this flat atom as a variable.
	 * 
	 * @param isVariable
	 *            true iff this flat atom is variable
	 */
	public void setVariable(boolean isVariable) {
		boolean wasVariable = isVariable();
		this.isGround = !isVariable;
		if (!wasVariable && isVariable) {
			notify(new AtomChangeEvent(this,
					AtomChangeType.FROM_CONSTANT_TO_VARIABLE));
		}
		if (wasVariable && !isVariable) {
			notify(new AtomChangeEvent(this,
					AtomChangeType.FROM_VARIABLE_TO_CONSTANT));
		}
	}

	@Override
	public String toString() {
		return conceptNameId.toString() + " {" + (isVariable() ? "v" : "c")
				+ "}";
	}

}
