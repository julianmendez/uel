package de.tudresden.inf.lat.uel.core.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
public class FAtom extends Atom {

	public static final String VAR_PREFIX = "VAR";

	private FAtom child = null;
	private String id = null;
	private Collection<FAtom> setOfSubsumers = new ArrayList<FAtom>();
	private boolean userVariable = false;
	private boolean var = false;

	/**
	 * Constructor of flat atom which takes a non-flat atom, flattens it:
	 * introduces new variable, adds an equation to the goal, checks for
	 * additional definitions in the ontology, if ontology is loaded, and
	 * flattens them and adds to the goal.
	 * 
	 * Flattening is recursive.
	 * 
	 * @param atom
	 */
	public FAtom(Atom atom, Goal goal) {
		super(atom.getName(), atom.isRoot());

		if (!atom.isRoot()) {
			child = null;
			updateId();

			goal.importAnyDefinition(atom);

		} else if (!atom.isFlat()) {

			FAtom b = null;
			String newvar = VAR_PREFIX + goal.getNbrVar();

			b = new FAtom(newvar, false, true, null);

			goal.setNbrVar(goal.getNbrVar() + 1);

			child = b;
			updateId();

			Map<String, Atom> bMap = new HashMap<String, Atom>();
			bMap.put(b.toString(), b);
			goal.addFlatten(new Equation(bMap, atom.getChildren(), false));

		} else {

			for (String key : atom.getChildren().keySet()) {

				if (goal.getAllAtoms().containsKey(key)) {
					child = goal.getAllAtoms().get(key);
				} else {
					child = new FAtom(atom.getChildren().get(key), goal);
				}
				updateId();
			}
		}

		if (!goal.getAllAtoms().containsKey(this.toString())) {

			goal.getAllAtoms().put(this.toString(), this);

		}
	}

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
	public FAtom(FAtom c, Atom atom) {
		super(atom.getName(), atom.isRoot());
		child = c;
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
	 * @param name
	 * @param r
	 * @param v
	 * @param arg
	 */
	public FAtom(String name, boolean r, boolean v, FAtom arg) {
		super(name, r);
		var = v;
		child = arg;
		updateId();
	}

	/**
	 * Adds a flat atom to a substitution set Used in Translator, to define
	 * substitution for variables.
	 * 
	 * @param atom
	 */
	public void addToSetOfSubsumers(FAtom atom) {
		setOfSubsumers.add((FAtom) atom);
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof FAtom) {
			FAtom other = (FAtom) o;
			ret = super.equals(other)
					&& this.userVariable == other.userVariable
					&& this.var == other.var
					&& this.setOfSubsumers.equals(other.setOfSubsumers);
			ret = ret
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
	public FAtom getChild() {
		return child;
	}

	public String getId() {
		return this.id;
	}

	public Collection<FAtom> getSetOfSubsumers() {
		return this.setOfSubsumers;
	}

	@Override
	public int hashCode() {
		return this.setOfSubsumers.hashCode();
	}

	/**
	 * Not used in UEL. Checks if this atom is a constant.
	 * 
	 * @return <code>true</code> if and only if this atoms is a constant
	 */
	public boolean isConstant() {
		return !(var || this.isRoot());
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
		return var;
	}

	/**
	 * Resets substitution set of a variable. This is necessary to be able to
	 * compute new substitution
	 * 
	 * Used in Translator
	 * 
	 */
	public void resetSetOfSubsumers() {
		setOfSubsumers = new ArrayList<FAtom>();
	}

	/**
	 * Sets a flat atom to be a system variable. Used at Goal initialization.
	 * 
	 */
	public void setUserVariable(boolean value) {
		if (!isRoot()) {
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
		var = v;
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
			str.append(child.toString());
			str.append(KRSSKeyword.close);
		}
		this.id = str.toString();
	}

}
