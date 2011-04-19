package de.tudresden.inf.lat.uel.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import de.tudresden.inf.lat.uel.ontmanager.Ontology;
import de.tudresden.inf.lat.uel.parser.ReaderAndParser;

/**
 * 
 * This static class implements a goal of unification, i.e., a set of equations
 * between concept terms with variables.
 * 
 * It is static, because the goal is unique for the procedure, and should be
 * accessible for most other objects.
 * 
 * @author Barbara Morawska
 * 
 */

public class Goal {

	private static File tbox;

	/**
	 * constants is a hash map implementing all constant concept names in the
	 * goal. keys are names and values are flat atoms
	 */
	public static HashMap<String, FAtom> constants = new HashMap<String, FAtom>();
	/**
	 * variables is a hash map implementing all concept names which are treated
	 * as variables keys are names and values are flat atoms
	 */
	public static HashMap<String, FAtom> variables = new HashMap<String, FAtom>();
	/**
	 * eatoms is a hash map implementing all flat existential restrictions keys
	 * are names and values are flat atoms
	 */
	public static HashMap<String, FAtom> eatoms = new HashMap<String, FAtom>();
	/**
	 * allatoms is a hash map implementing all flat atoms in the goal keys are
	 * names and values are flat atoms
	 */
	public static HashMap<String, FAtom> allatoms = new HashMap<String, FAtom>();

	private static int NbrVar = 0;
	/**
	 * equations is a list containing all goal equations
	 * 
	 */
	public static ArrayList<Equation> equations = new ArrayList<Equation>();

	private Goal() {
	};

	/**
	 * This method initialize goal.
	 * 
	 * <filename> is the name of the input file (the file containing unification
	 * problem). The method calls ReaderAndParser to parse and flatten goal
	 * equations.
	 * 
	 * Then if variable Unifier.text is true, all equations are written to a
	 * file <filename>.TBox
	 * 
	 * Then all variables, constants and existential atoms are identified.
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public static void initialize(String filename) throws Exception {

		File goal = new File(filename);

		ReaderAndParser.readFromFile(goal);

		if (Unifier.getTest()) {

			tbox = new File(filename + ".TBox");

			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(tbox)));

			for (Equation eq : equations) {

				eq.printFDefinition(writer);

			}
			writer.close();

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
	 * The method used in the flattening method <addFlatten> to add a relevant
	 * definition from the ontology to the goal.
	 * 
	 * <concept> is a concept name that may be defined in the ontology.
	 * 
	 * @param concept
	 * @throws Exception
	 */
	public static void importDefinition(Atom concept) throws Exception {

		Equation eq = Ontology.getDefinition(concept.toString());
		Goal.addFlatten(eq);

	}

	/**
	 * This method is used by ReaderAndParser.readFromFile and importDefinition
	 * to flatten an equation and to add it to the list of goal equations
	 * 
	 * @param e
	 *            (equation that need to be flattened)
	 * @throws Exception
	 */
	public static void addFlatten(Equation e) throws Exception {

		FAtom a;
		Atom b = null;
		Equation newequation = new Equation();

		if (e.left.size() != 1) {

			throw new RuntimeException(" Wrong format of this definition ");

		}

		for (String key : e.left.keySet()) {
			b = e.left.get(key);
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

			a = new FAtom(b, null);

			a.setVar(true);

			variables.put(a.toString(), a);
			allatoms.put(a.toString(), a);

			newequation.left.put(a.toString(), a);

			/*
			 * FLATTENING
			 */

			for (String key : e.right.keySet()) {

				a = new FAtom(e.right.get(key));

				if (allatoms.containsKey(a.toString())) {

					newequation.right.put(a.toString(), allatoms.get(a
							.toString()));

				} else {

					newequation.right.put(a.toString(), a);

				}

			}

			Goal.addEquation(newequation);

		}
	}

	/**
	 * Method to get the list of goal equations
	 * 
	 * @return
	 */
	public ArrayList<Equation> getEquations() {

		return equations;
	}

	/**
	 * Method to add an equation e to the list of goal equations
	 * 
	 * @param e
	 */
	public static void addEquation(Equation e) {

		equations.add(e);

	}

	/**
	 * This method is not used by UEL. It is here for testing purposes.
	 * Prints all atoms of the goal.
	 * 
	 */
	public static void printAllAtoms() {

		System.out.print("From goal all atoms (" + allatoms.size() + "):");

		for (String key : allatoms.keySet()) {

			System.out.print(allatoms.get(key));
			System.out.print(" | ");

		}

		System.out.println("");

	}

	/**
	 * This method is not used by UEL. It is here for testing purposes.
	 * Prints all constants of the goal.
	 * 
	 */
	public static void printConstants() {

		System.out
				.println("From goal all constants(" + constants.size() + "):");

		for (String key : constants.keySet()) {

			System.out.print(constants.get(key));
			System.out.print(" | ");

		}

		System.out.println("");

	}

	/**
	 * This method is not used by UEL. It is here for testing purposes.
	 * Prints all variables of the goal.
	 * 
	 */
	public static void printVariables() {

		System.out.println("From goal all variables: (" + variables.size()
				+ "):");

		for (String key : variables.keySet()) {

			System.out.print(variables.get(key));
			System.out.print(" | ");

		}

		System.out.println("");
	}

	/**
	 * This method is not used by UEL. It is here for testing purposes.
	 * Prints all existential atoms of the goal.
	 * 
	 */
	public static void printEatoms() {

		System.out.println("From goal all existential restrictions ("
				+ eatoms.size() + "):");

		for (String key : eatoms.keySet()) {

			System.out.print(eatoms.get(key));
			System.out.print(" | ");

		}

		System.out.println("");
	}

	/**
	 * This method is not used by UEL. It is here for testing purposes.
	 * Prints all equations of the goal.
	 * 
	 */
	public static void printGoal() throws Exception {

		int i = 0;

		for (Equation e : equations) {

			System.out.println("Goal equation nr." + i + ":");
			e.printEquation();

			i++;
		}

	}

	/**
	 * This method is used by a constructor of a flat atom <FAtom(Atom)> from
	 * atom. This requires to introduce a new variable. New variables are
	 * identified by unique numbers. The next unique number is stored in
	 * <nbrVar>.
	 * 
	 * @param nbrVar
	 */
	public static void setNbrVar(int nbrVar) {
		NbrVar = nbrVar;
	}

	/**
	 * This method is used by a constructor of a flat atom <FAtom(Atom)> from
	 * atom. This requires to introduce a new variable. New variables are
	 * identified by unique numbers. The next unique number is stored in
	 * <nbrVar>.
	 * 
	 * @return
	 */
	public static int getNbrVar() {
		return NbrVar;
	}

	/**
	 * This method is not used by UEL. It is here for testing purposes.
	 * Prints all equations of the goal to a Print Writer out.
	 * 
	 * @param out
	 */
	public static void printDefinitions(PrintWriter out) {

		for (Equation eq : equations) {

			eq.printFDefinition(out);
			out.println();

		}

	}

}
