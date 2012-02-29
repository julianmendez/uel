package de.tudresden.inf.lat.uel.sat.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.tudresden.inf.lat.uel.type.api.AtomChangeListener;
import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;

/**
 * An object of this class is an existential restriction.
 * 
 * @author Julian Mendez
 */
public class ExistentialRestriction implements SatAtom {

	private List<AtomChangeListener> changeListener = new ArrayList<AtomChangeListener>();
	private final ConceptName child;
	private final String id;
	private final String name;

	/**
	 * Constructs an existential restriction.
	 * 
	 * @param str
	 *            name
	 * @param ch
	 *            child
	 */
	public ExistentialRestriction(String str, ConceptName ch) {
		this.name = str;
		this.child = ch;
		this.id = updateId();
	}

	@Override
	public boolean addAtomChangeListener(AtomChangeListener o) {
		return this.changeListener.add(o);
	}

	@Override
	public ConceptName asConceptName() {
		throw new ClassCastException();
	}

	@Override
	public ExistentialRestriction asExistentialRestriction() {
		return this;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof ExistentialRestriction) {
			ExistentialRestriction other = (ExistentialRestriction) o;
			ret = this.name.equals(other.name)
					&& ((this.child == null && other.child == null) || (this.child != null && this.child
							.equals(other.child)));
		}
		return ret;
	}

	@Override
	public Collection<AtomChangeListener> getAtomChangeListeners() {
		return Collections.unmodifiableCollection(this.changeListener);
	}

	/**
	 * Returns an argument in the flat atom, which is an existential
	 * restriction. Otherwise it returns null.
	 * 
	 * Used in defining clauses in Translator
	 * 
	 * @return an argument in the flat atom, which is an existential
	 *         restriction; otherwise it returns null
	 */
	public ConceptName getChild() {
		return this.child;
	}

	@Override
	public Integer getConceptNameId() {
		// FIXME
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public String getRoleName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean isConceptName() {
		return false;
	}

	@Override
	public boolean isConstant() {
		// FIXME
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isExistentialRestriction() {
		return true;
	}

	@Override
	public boolean isGround() {
		// FIXME
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVariable() {
		// FIXME
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAtomChangeListener(AtomChangeListener o) {
		return this.changeListener.remove(o);
	}

	@Override
	public String toString() {
		return getId();
	}

	private String updateId() {
		StringBuilder str = new StringBuilder(this.getName());
		if (this.child != null) {

			str = str.insert(0,
					(KRSSKeyword.open + KRSSKeyword.some + KRSSKeyword.space));

			str.append(KRSSKeyword.space);
			str.append(this.child.getId());
			str.append(KRSSKeyword.close);
		}
		return str.toString();
	}

}
