package de.tudresden.inf.lat.uel.core.type;

import java.util.Map;
import java.util.logging.Logger;

/**
 * This class implement an object atom. Atom is a concept term, which is not a
 * conjunction. Hence atom can be a concept name or an existential restriction.
 * 
 * @author Barbara Morawska
 */
public class Atom {

	private static final Logger logger = Logger.getLogger(Atom.class.getName());

	/*
	 * Atom has a name. If it is a concept name, this concept name is the
	 * <code>name</code>. If it is an existential restriction, then its role
	 * name is the <code>name</code>. In the case of existential restriction,
	 * the variable <code>root</code> is true and the hash map children
	 * implements conjunction of atoms that is an argument for the role name.
	 */

	private Map<String, Atom> children;
	private String id = null;
	private final String name;
	private final boolean root;

	/**
	 * Constructs a new atom.
	 * 
	 * @param n
	 *            name of the atom
	 * @param r
	 *            <code>true</code> if and only if the atom is an existential
	 *            restriction
	 */
	public Atom(String n, boolean r) {
		name = n;
		root = r;
		updateId();
	}

	/**
	 * Constructs a new atom.
	 * 
	 * @param n
	 *            the name of the atom
	 * @param r
	 *            <code>true</code> if and only if the atom is an existential
	 *            restriction
	 * @param argchild
	 *            conjunction of atoms, which is an argument for the role name
	 *            when r is true
	 */
	public Atom(String n, boolean r, Map<String, Atom> argchild) {
		name = n;
		children = argchild;
		root = r;
		updateId();
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Atom) {
			Atom other = (Atom) o;
			ret = this.name.equals(other.name) && this.root == other.root;
			ret = ret
					&& ((this.children == null && other.children == null) || (this.children != null && this.children
							.equals(other.children)));
		}
		return ret;
	}

	/**
	 * Returns the map representing a conjunction of atoms, which is an argument
	 * for a role name in an existential atom.
	 * 
	 * @return the hash map representing a conjunction of atoms
	 */
	public Map<String, Atom> getChildren() {
		Map<String, Atom> ret = null;
		if (root) {
			ret = children;
		} else {
			logger.warning("WARNING: Cannot return children of a variable or constant");
		}
		return ret;
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

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	/**
	 * Checks if the atom is flat. Used by the constructor FAtom(Atom).
	 * 
	 * @return <code>true</code> if and only if the atom is flat
	 */
	public boolean isFlat() {
		boolean test = true;
		if (isRoot()) {
			if (children.size() == 1) {
				for (String key : children.keySet()) {
					if (children.get(key).isRoot()) {
						test = false;
					}
				}
			} else {
				test = false;
			}
		}
		return test;
	}

	/**
	 * Is true if the atom is an existential restriction.
	 * 
	 * @return <code>true</code> if and only if the atom is an existential
	 *         restriction
	 */
	public boolean isRoot() {
		return root;
	}

	@Override
	public String toString() {
		return getId();
	}

	private void updateId() {
		StringBuilder str = new StringBuilder(name);
		if (children != null) {
			str.append("[");
			for (String atom : children.keySet()) {
				str.append(children.get(atom).toString());
				str.append("  ");
			}
			str.append("]");
		}
		id = str.toString();
	}

}
