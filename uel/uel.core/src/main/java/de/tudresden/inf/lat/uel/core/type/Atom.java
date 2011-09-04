package de.tudresden.inf.lat.uel.core.type;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class extends Atom. It implements a flat atom, hence an atom which is
 * either a concept name or an existential restriction with a concept name as an
 * argument.
 * 
 * Concept name can be a concept constant or a variable. While constructing flat
 * atoms from atoms, we introduce new variables and add new equations to the
 * goal.
 * 
 * @author Barbara Morawska
 */
public class Atom {

	private Atom child = null;
	private String id = null;
	private String name;
	private boolean existential;
	private Collection<Atom> setOfSubsumers = new ArrayList<Atom>();
	private boolean userVariable = false;
	private boolean variable = false;

	/**
	 * Constructor of flat atom (used in flattening Goal.addAndFlatten)
	 * 
	 * atom is possible non-flat atom c is a flat atom, which is an argument for
	 * a flat atom to be constructed.
	 * 
	 * 
	 * @param atom
	 * @param c
	 */
	public Atom(Atom c, Atom atom) {
		init(atom.getName(), atom.isExistential());
		child = c;
		updateId();
	}

	/*
	 * Atom has a name. If it is a concept name, this concept name is the
	 * <code>name</code>. If it is an existential restriction, then its role
	 * name is the <code>name</code>. In the case of existential restriction,
	 * the variable <code>root</code> is true and the hash map children
	 * implements conjunction of atoms that is an argument for the role name.
	 */

	/**
	 * Constructs a new atom.
	 * 
	 * @param str
	 *            name of the atom
	 * @param r
	 *            <code>true</code> if and only if the atom is an existential
	 *            restriction
	 */
	public Atom(String str, boolean r) {
		init(str, r);
		updateId();
	}

	/**
	 * Constructor of flat atom (used in ReaderAndParser to create a flat system
	 * variable).
	 * 
	 * name is <code>name</code> of an atom r is true if atom is an existential
	 * restriction v is true if atom is a variable arg is a flat atom, which is
	 * an argument for a role name in an existential restriction
	 * 
	 * @param str
	 * @param r
	 * @param v
	 * @param arg
	 */
	public Atom(String str, boolean r, boolean v, Atom arg) {
		init(str, r);
		variable = v;
		child = arg;
		updateId();
	}

	/**
	 * Adds a flat atom to a substitution set Used in Translator, to define
	 * substitution for variables.
	 * 
	 * @param atom
	 */
	public void addToSetOfSubsumers(Atom atom) {
		setOfSubsumers.add((Atom) atom);
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Atom) {
			Atom other = (Atom) o;

			ret = this.existential == other.existential
					&& this.name.equals(other.name)
					&& this.setOfSubsumers.equals(other.setOfSubsumers)
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
	public Atom getChild() {
		return child;
	}

	public String getId() {
		return this.id;
	}

	/**
	 * Returns name of the atom.
	 * 
	 * @return name of the atom
	 */
	public String getName() {
		return name;
	}

	public Collection<Atom> getSetOfSubsumers() {
		return this.setOfSubsumers;
	}

	@Override
	public int hashCode() {
		return this.setOfSubsumers.hashCode();
	}

	private void init(String n, boolean r) {
		name = n;
		existential = r;
	}

	/**
	 * Not used in UEL. Checks if this atom is a constant.
	 * 
	 * @return <code>true</code> if and only if this atoms is a constant
	 */
	public boolean isConstant() {
		return !(variable || this.isExistential());
	}

	/**
	 * Is true if the atom is an existential restriction.
	 * 
	 * @return <code>true</code> if and only if the atom is an existential
	 *         restriction
	 */
	public boolean isExistential() {
		return existential;
	}

	/**
	 * Checks if this flat atom is a system variable.
	 * 
	 * @return <code>true</code> if and only if this flat atom is a system
	 *         variable
	 */
	public boolean isUserVariable() {
		return userVariable;
	}

	/**
	 * Checks if a flat atom is a variable.
	 * 
	 * @return <code>true</code> if and only if a flat atom is a variable
	 */
	public boolean isVariable() {
		return variable;
	}

	/**
	 * Resets substitution set of a variable. This is necessary to be able to
	 * compute new substitution
	 * 
	 * Used in Translator
	 * 
	 */
	public void resetSetOfSubsumers() {
		setOfSubsumers = new ArrayList<Atom>();
	}

	/**
	 * Sets a flat atom to be a system variable. Used at Goal initialization.
	 * 
	 */
	public void setUserVariable(boolean value) {
		if (!isExistential()) {
			userVariable = value;
		} else {
			throw new IllegalStateException(
					"WARNING: cannot change existential atom into a system variable");
		}

	}

	/**
	 * If v is true, it defines this atom as a variable
	 * 
	 * @param v
	 */
	public void setVariable(boolean v) {
		variable = v;
	}

	@Override
	public String toString() {
		return getId();
	}

	private void updateId() {
		StringBuilder str = new StringBuilder(this.getName());
		if (child != null) {

			str = str.insert(0,
					(KRSSKeyword.open + KRSSKeyword.some + KRSSKeyword.space));

			str.append(KRSSKeyword.space);
			str.append(child.getId());
			str.append(KRSSKeyword.close);
		}
		this.id = str.toString();
	}

}
