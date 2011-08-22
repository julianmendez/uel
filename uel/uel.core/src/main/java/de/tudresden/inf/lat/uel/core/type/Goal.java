package de.tudresden.inf.lat.uel.core.type;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class implements a goal of unification, i.e., a set of equations between
 * concept terms with variables.
 * 
 * The goal is unique for the procedure, and should be accessible for most other
 * objects.
 * 
 * @author Barbara Morawska
 */
public class Goal {

	public static final String UNDEF_SUFFIX = "_UNDEF";
	public static final String VAR_PREFIX = "VAR";

	/**
	 * allatoms is a hash map implementing all flat atoms in the goal keys are
	 * names and values are flat atoms
	 */
	private Map<String, FAtom> allatoms = new HashMap<String, FAtom>();

	/**
	 * constants is a hash map implementing all constant concept names in the
	 * goal. keys are names and values are flat atoms
	 */
	private Map<String, FAtom> constants = new HashMap<String, FAtom>();

	/**
	 * eatoms is a hash map implementing all flat existential restrictions keys
	 * are names and values are flat atoms
	 */
	private Map<String, FAtom> eatoms = new HashMap<String, FAtom>();

	/**
	 * equations is a list containing all goal equations
	 * 
	 */
	private List<Equation> equations = new ArrayList<Equation>();

	private Equation mainEquation = null;

	private int nbrVar = 0;

	private Ontology ontology = null;

	/**
	 * variables is a hash map implementing all concept names which are treated
	 * as variables keys are names and values are flat atoms
	 */
	private Map<String, FAtom> variables = new HashMap<String, FAtom>();

	/**
	 * Constructs a new goal based on a specified ontology.
	 * 
	 * @param ont
	 *            an ontology
	 */
	public Goal(Ontology ont) {
		ontology = ont;
	}

	/**
	 * Method to add an equation e to the list of goal equations
	 * 
	 * @param e
	 *            equation
	 */
	public void addEquation(Equation e) {
		equations.add(e);
	}

	/**
	 * This method is to flatten an equation and to add it to the list of goal
	 * equations
	 * 
	 * @param e
	 *            equation that needs to be flattened
	 */
	public void addFlatten(Equation e) {

		FAtom a;
		Atom b = null;

		if (e.getLeft().size() != 1) {

			throw new RuntimeException(" Wrong format of this definition ");

		}

		for (String key : e.getLeft().keySet()) {
			b = e.getLeft().get(key);
		}

		if (b.isRoot()) {

			throw new RuntimeException(
					" Definition should not have an existential restriction on its left side ");

		}

		if (variables.containsKey(b.getName())) {

			System.out
					.println("Warning: This definition was already added to the goal"
							+ b.getName());

		} else {

			Map<String, Atom> leftPart = new HashMap<String, Atom>();
			Map<String, Atom> rightPart = new HashMap<String, Atom>();

			a = new FAtom(null, b);

			a.setVar(true);

			variables.put(a.toString(), a);
			allatoms.put(a.toString(), a);

			leftPart.put(a.toString(), a);

			/*
			 * FLATTENING
			 */

			for (String key : e.getRight().keySet()) {

				a = new FAtom(e.getRight().get(key), this);

				if (allatoms.containsKey(a.toString())) {

					rightPart.put(a.toString(), allatoms.get(a.toString()));

				} else {

					rightPart.put(a.toString(), a);

				}

			}

			if (e.isPrimitive()) {
				/*
				 * Adding new variable to the right side
				 */

				FAtom var = new FAtom(b.getName() + UNDEF_SUFFIX, false, true,
						null);
				var.setUserVariable(true);
				this.allatoms.put(var.getName(), var);
				this.variables.put(var.getName(), var);
				rightPart.put(var.getName(), var);
			}

			addEquation(new Equation(leftPart, rightPart, false));

		}
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Goal) {
			Goal other = (Goal) o;
			ret = this.allatoms.equals(other.allatoms)
					&& this.constants.equals(other.constants)
					&& this.eatoms.equals(other.eatoms)
					&& this.equations.equals(other.equations)
					&& this.mainEquation.equals(other.mainEquation)
					&& this.variables.equals(other.variables)
					&& this.ontology.equals(other.ontology)
					&& this.nbrVar == other.nbrVar;
		}
		return ret;
	}

	public Map<String, FAtom> getAllAtoms() {
		return allatoms;
	}

	public Map<String, FAtom> getConstants() {
		return constants;
	}

	public Map<String, FAtom> getEAtoms() {
		return eatoms;
	}

	/**
	 * Method to get the list of goal equations
	 * 
	 * @return the list of goal equations
	 */
	public List<Equation> getEquations() {
		return equations;
	}

	/**
	 * Returns a string representation of the equations, excluding the main
	 * equation.
	 * 
	 * @return a string representation of the equations, excluding the main
	 *         equation
	 */
	public String getGoalEquations() {
		StringBuffer sbuf = new StringBuffer();
		for (Equation eq : getEquations()) {
			sbuf.append(eq.toString());
			sbuf.append("\n");
		}
		return sbuf.toString();
	}

	public Equation getMainEquation() {
		return mainEquation;
	}

	/**
	 * This method is used by a constructor of a flat atom
	 * <code>FAtom(Atom)</code> from atom. This requires to introduce a new
	 * variable. New variables are identified by unique numbers. The next unique
	 * number is stored in <code>nbrVar</code>.
	 * 
	 * @return nbrVar
	 */
	public int getNbrVar() {
		return nbrVar;
	}

	public Map<String, FAtom> getVariables() {
		return variables;
	}

	@Override
	public int hashCode() {
		return this.ontology.hashCode() + 31 * this.mainEquation.hashCode();
	}

	/**
	 * The method used in the flattening method <code>addFlatten</code> to add a
	 * relevant definition from the ontology to the goal.
	 * 
	 * <code>concept</code> is a concept name that may be defined in the
	 * ontology.
	 * 
	 * @param concept
	 *            concept
	 */
	public void importDefinition(Atom concept) {
		if (ontology.containsDefinition(concept.toString())
				&& !variables.containsKey(concept.toString())) {

			Equation eq = ontology.getDefinition(concept.toString());
			addFlatten(eq);
		}
	}

	public void importPrimitiveDefinition(Atom concept) {
		if (ontology.containsPrimitiveDefinition(concept.toString())
				&& !variables.containsKey(concept.toString())) {

			Equation eq = ontology.getPrimitiveDefinition(concept.toString());
			addFlatten(eq);
		}
	}

	public void initialize(List<Equation> equationList, FAtom left,
			FAtom right, Set<String> vars) throws IOException {
		initialize(equationList, left, right, null, vars);
	}

	private void initialize(List<Equation> list, FAtom left, FAtom right,
			Writer output, Set<String> vars) throws IOException {

		setMainEquation(new Equation(left, right, false));
		for (Equation eq : list) {
			addFlatten(eq);
		}

		for (String key : variables.keySet()) {
			variables.get(key).setVar(true);
		}

		for (String var : vars) {
			Atom a = new Atom(var, false);
			importPrimitiveDefinition(a);
		}

		for (String key : allatoms.keySet()) {

			FAtom a = allatoms.get(key);

			if (vars.contains(a.getName())) {

				a.setVar(true);
				a.setUserVariable(!a.equals(left) && !a.equals(right));
				variables.put(key, a);
			} else if (!variables.containsKey(key) && !a.isRoot()) {

				constants.put(key, a);

			} else if (a.isRoot()) {

				eatoms.put(key, a);
			}
		}
	}

	/**
	 * This method is not used by UEL. It is here for testing purposes. Prints
	 * all atoms of the goal.
	 * 
	 */
	public String printAllAtoms() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("From goal all atoms (" + allatoms.size());
		sbuf.append("):\n");
		for (String key : allatoms.keySet()) {
			sbuf.append(allatoms.get(key));
			sbuf.append(" | ");
		}
		sbuf.append("\n");
		return sbuf.toString();
	}

	/**
	 * This method is not used by UEL. It is here for testing purposes. Prints
	 * all constants of the goal.
	 * 
	 */
	public String printConstants() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("From goal all constants(" + constants.size());
		sbuf.append("):\n");
		for (String key : constants.keySet()) {
			sbuf.append(constants.get(key));
			sbuf.append(" | ");
		}
		sbuf.append("\n");
		return sbuf.toString();
	}

	/**
	 * This method is not used by UEL. It is here for testing purposes. Prints
	 * all existential atoms of the goal.
	 * 
	 */
	public String printEatoms() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("From goal all existential restrictions (" + eatoms.size());
		sbuf.append("):\n");
		for (String key : eatoms.keySet()) {
			sbuf.append(eatoms.get(key));
			sbuf.append(" | ");
		}
		sbuf.append("\n");
		return sbuf.toString();
	}

	/**
	 * This method is not used by UEL. It is here for testing purposes. Prints
	 * all equations of the goal.
	 * 
	 */
	public String printGoal() {
		StringBuffer sbuf = new StringBuffer();
		int i = 0;
		for (Equation e : equations) {
			sbuf.append("Goal equation nr." + i);
			sbuf.append(":");
			sbuf.append(e.printEquation());
			sbuf.append("\n");
			i++;
		}
		return sbuf.toString();
	}

	public String printSubsumers() {
		StringBuffer sbuf = new StringBuffer();
		for (FAtom var : getVariables().values()) {
			sbuf.append(var);
			sbuf.append(":");
			sbuf.append(var.getS());
			sbuf.append("\n");
		}
		return sbuf.toString();
	}

	/**
	 * This method is not used by UEL. It is here for testing purposes. Prints
	 * all variables of the goal.
	 * 
	 */
	public String printVariables() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("From goal all variables: (" + variables.size());
		sbuf.append("):\n");
		for (String key : variables.keySet()) {
			sbuf.append(variables.get(key));
			sbuf.append(" | ");
		}
		sbuf.append("\n");
		return sbuf.toString();
	}

	public void setMainEquation(Equation equation) {
		this.mainEquation = equation;
	}

	/**
	 * This method is used by a constructor of a flat atom
	 * <code>FAtom(Atom)</code> from atom. This requires to introduce a new
	 * variable. New variables are identified by unique numbers. The next unique
	 * number is stored in <code>nbrVar</code>.
	 * 
	 * @param nbrV
	 */
	public void setNbrVar(int nbrV) {
		nbrVar = nbrV;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(this.mainEquation);
		sbuf.append("\n");
		sbuf.append(getGoalEquations());
		return sbuf.toString();
	}

}
