package de.tudresden.inf.lat.uel.sat.type;

import de.tudresden.inf.lat.uel.type.cons.KRSSKeyword;

/**
 * An object of this class is an existential restriction.
 * 
 * @author Julian Mendez
 */
public class ExistentialRestriction implements SatAtom {

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
	public String getId() {
		return this.id;
	}

	@Override
	public String getName() {
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
	public boolean isExistentialRestriction() {
		return true;
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

	@Override
	public Integer getConceptName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getRole() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConstant() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGround() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVariable() {
		// TODO Auto-generated method stub
		return false;
	}

}
