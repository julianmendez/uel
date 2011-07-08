package de.tudresden.inf.lat.uel.main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import de.tudresden.inf.lat.uel.ontmanager.Ontology;
import de.tudresden.inf.lat.uel.parser.ReaderAndParser;

/**
 * 
 * This class implements a goal of unification, i.e., a set of equations between
 * concept terms with variables.
 * 
 * The goal is unique for the procedure, and should be accessible for most other
 * objects.
 * 
 * @author Barbara Morawska
 * 
 */

public class Goal {

	/**
	 * allatoms is a hash map implementing all flat atoms in the goal keys are
	 * names and values are flat atoms
	 */
	private HashMap<String, FAtom> allatoms = new HashMap<String, FAtom>();

	/**
	 * constants is a hash map implementing all constant concept names in the
	 * goal. keys are names and values are flat atoms
	 */
	private HashMap<String, FAtom> constants = new HashMap<String, FAtom>();

	/**
	 * eatoms is a hash map implementing all flat existential restrictions keys
	 * are names and values are flat atoms
	 */
	private HashMap<String, FAtom> eatoms = new HashMap<String, FAtom>();

	/**
	 * equations is a list containing all goal equations
	 * 
	 */
	private ArrayList<Equation> equations = new ArrayList<Equation>();

	private int nbrVar = 0;

	private Ontology ontology;

	private ReaderAndParser readerAndParser = new ReaderAndParser();

	/**
	 * variables is a hash map implementing all concept names which are treated
	 * as variables keys are names and values are flat atoms
	 */
	private HashMap<String, FAtom> variables = new HashMap<String, FAtom>();

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
	 * This method is used by ReaderAndParser.read and importDefinition to
	 * flatten an equation and to add it to the list of goal equations
	 * 
	 * @param e
	 *            equation that needs to be flattened
	 */
	public void addFlatten(Equation e) {

		FAtom a;
		Atom b = null;
		Equation newequation = new Equation();

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

			a = new FAtom(null, b);

			a.setVar(true);

			variables.put(a.toString(), a);
			allatoms.put(a.toString(), a);

			newequation.getLeft().put(a.toString(), a);

			/*
			 * FLATTENING
			 */

			for (String key : e.getRight().keySet()) {

				a = new FAtom(e.getRight().get(key), this);

				if (allatoms.containsKey(a.toString())) {

					newequation.getRight().put(a.toString(),
							allatoms.get(a.toString()));

				} else {

					newequation.getRight().put(a.toString(), a);

				}

			}

			addEquation(newequation);

		}
	}

	public HashMap<String, FAtom> getAllAtoms() {
		return allatoms;
	}

	public HashMap<String, FAtom> getConstants() {
		return constants;
	}

	public HashMap<String, FAtom> getEAtoms() {
		return eatoms;
	}

	/**
	 * Method to get the list of goal equations
	 * 
	 * @return the list of goal equations
	 */
	public ArrayList<Equation> getEquations() {

		return equations;
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

	public HashMap<String, FAtom> getVariables() {
		return variables;
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
		if (ontology.getDefinitions().containsKey(concept.toString())
				&& !variables.containsKey(concept.toString())) {

			Equation eq = ontology.getDefinition(concept.toString());
			addFlatten(eq);

		}

	}

	public void initialize(Reader input) throws IOException {
		initialize(input, false, null);
	}

	private void initialize(Reader input, boolean test, Writer output)
			throws IOException {

		readerAndParser.read(input, this);

		if (test) {

			PrintWriter writer = new PrintWriter(new BufferedWriter(output));

			for (Equation eq : equations) {

				eq.printFDefinition(writer);

			}
			writer.flush();

		}

		for (String key : variables.keySet()) {
			variables.get(key).setVar(true);
			variables.get(key).SysVar();
		}

		for (String key : allatoms.keySet()) {

			FAtom a = allatoms.get(key);

			if (!variables.containsKey(key) && a.getName().contains("VAR")) {

				a.setVar(true);
				variables.put(key, a);
			} else if (!variables.containsKey(key) && !a.isRoot()) {

				constants.put(key, a);

			} else if (a.isRoot()) {

				eatoms.put(key, a);
			}

		}
	}

	/**
	 * This method initializes the goal using a reader containing unification
	 * problem. The method calls ReaderAndParser to parse and flatten goal
	 * equations.
	 * 
	 * Then if variable Unifier.text is true, all equations are written to a
	 * writer.
	 * 
	 * Then all variables, constants and existential atoms are identified.
	 * 
	 * @param input
	 *            input
	 * @param output
	 *            output
	 * @throws IOException
	 */
	public void initializeWithTest(Reader input, Writer output)
			throws IOException {
		initialize(input, true, output);
	}

	/**
	 * This method is not used by UEL. It is here for testing purposes. Prints
	 * all atoms of the goal.
	 * 
	 */
	public void printAllAtoms() {

		System.out.print("From goal all atoms (" + allatoms.size() + "):");

		for (String key : allatoms.keySet()) {

			System.out.print(allatoms.get(key));
			System.out.print(" | ");

		}

		System.out.println("");

	}

	/**
	 * This method is not used by UEL. It is here for testing purposes. Prints
	 * all constants of the goal.
	 * 
	 */
	public void printConstants() {

		System.out
				.println("From goal all constants(" + constants.size() + "):");

		for (String key : constants.keySet()) {

			System.out.print(constants.get(key));
			System.out.print(" | ");

		}

		System.out.println("");

	}

	/**
	 * This method is not used by UEL. It is here for testing purposes. Prints
	 * all equations of the goal to a Print Writer out.
	 * 
	 * @param out
	 */
	public void printDefinitions(PrintWriter out) {

		for (Equation eq : equations) {

			eq.printFDefinition(out);
			out.println();

		}

	}

	/**
	 * This method is not used by UEL. It is here for testing purposes. Prints
	 * all existential atoms of the goal.
	 * 
	 */
	public void printEatoms() {

		System.out.println("From goal all existential restrictions ("
				+ eatoms.size() + "):");

		for (String key : eatoms.keySet()) {

			System.out.print(eatoms.get(key));
			System.out.print(" | ");

		}

		System.out.println("");
	}

	/**
	 * This method is not used by UEL. It is here for testing purposes. Prints
	 * all equations of the goal.
	 * 
	 */
	public void printGoal() {

		int i = 0;

		for (Equation e : equations) {

			System.out.println("Goal equation nr." + i + ":");
			e.printEquation();

			i++;
		}

	}

	/**
	 * This method is not used by UEL. It is here for testing purposes. Prints
	 * all variables of the goal.
	 * 
	 */
	public void printVariables() {

		System.out.println("From goal all variables: (" + variables.size()
				+ "):");

		for (String key : variables.keySet()) {

			System.out.print(variables.get(key));
			System.out.print(" | ");

		}

		System.out.println("");
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

}
