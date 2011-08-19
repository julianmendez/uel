package de.tudresden.inf.lat.uel.core.sat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import de.tudresden.inf.lat.uel.core.type.DissubsumptionLiteral;
import de.tudresden.inf.lat.uel.core.type.Equation;
import de.tudresden.inf.lat.uel.core.type.FAtom;
import de.tudresden.inf.lat.uel.core.type.Goal;
import de.tudresden.inf.lat.uel.core.type.KRSSKeyword;
import de.tudresden.inf.lat.uel.core.type.Literal;
import de.tudresden.inf.lat.uel.core.type.OrderLiteral;
import de.tudresden.inf.lat.uel.core.type.SubsumptionLiteral;

/**
 * This class performs reduction of goal equations to propositional clauses. The
 * reduction is explained in F. Baader, B. Morawska,
 * "SAT Encoding of Unification in EL", LPAR 2010.
 * 
 * It has also the methods to translate an output of a sat solver to a unifier.
 * 
 * @author Barbara Morawska
 */
public class Translator {

	public static final String NOT_UNIFIABLE = "NOT UNIFIABLE / NO MORE UNIFIERS";

	private Goal goal;
	private Integer identificator = 1;

	/**
	 * Identifiers are numbers, each number uniquely identifies a literal, i.e.
	 * a dissubsumption.
	 */
	private HashMap<Integer, Literal> identifiers = new HashMap<Integer, Literal>();

	private boolean invertLiteral = false;

	/**
	 * Literals are all dis-subsumptions between atoms in the goal the first
	 * hash map maps them to unique numbers.
	 */
	private HashMap<String, Integer> literals = new HashMap<String, Integer>();

	/**
	 * Update is a set of numbers or numbers encoding the negation of the
	 * computed unifier. This is needed for computation of the
	 */
	private Set<Integer> update = new HashSet<Integer>();

	/**
	 * Constructs a new translator.
	 * 
	 * @param g
	 *            goal
	 */
	public Translator(Goal g) {
		goal = g;
		setLiterals();
	}

	public Translator(Goal g, boolean inv) {
		goal = g;
		invertLiteral = inv;
		setLiterals();
	}

	/**
	 * Returns the literals.
	 * 
	 * @return the literals
	 */
	public Map<String, Integer> getLiterals() {
		return this.literals;
	}

	private int getMinusLiteralId(Literal literal) {
		int val = literals.get(literal.toString());
		return invertLiteral ? val : (-1) * val;
	}

	private int getMinusOrderLiteral(String left, String right) {
		return (-1) * getOrderLiteral(left, right);
	}

	private int getMinusSubOrDissubLiteral(String left, String right) {
		return (-1) * getSubOrDissubLiteral(left, right);
	}

	private int getOrderLiteral(String left, String right) {
		Literal literal = new OrderLiteral(left, right);
		return literals.get(literal.toString());
	}

	/**
	 * This method encodes equations into propositional clauses in DIMACS CNF
	 * format, i.e. positive literal is represented by a positive number and a
	 * negative literal is represented by a corresponding negative number. Each
	 * clause is on one line. The end of a clause is marked by 0. Example of a
	 * clause in DIMACS format: 1 -3 0
	 */
	public SatInput getSatInput() {

		SatInput ret = new SatInput();
		Set<Integer> clause = new HashSet<Integer>();

		/*
		 * 
		 * Clauses created in Step 1
		 */
		Set<Equation> equations = new HashSet<Equation>();
		equations.addAll(goal.getEquations());
		equations.add(goal.getMainEquation());

		for (Equation e : equations) {

			/*
			 * Step 1 for constants
			 */

			for (String key1 : goal.getConstants().keySet()) {

				if (!e.getLeft().containsKey(key1)
						&& !e.getRight().containsKey(key1)) {

					/*
					 * constant not in the equation
					 * 
					 * 
					 * 
					 * one side of an equation
					 */

					for (String key3 : e.getRight().keySet()) {
						for (String key2 : e.getLeft().keySet()) {
							clause.add(getMinusSubOrDissubLiteral(key2, key1));
						}

						clause.add(getSubOrDissubLiteral(key3, key1));
						ret.add(clause);
						clause.clear();

					}

					/*
					 * another side of an equation
					 */

					for (String key2 : e.getLeft().keySet()) {
						for (String key3 : e.getRight().keySet()) {
							clause.add(getMinusSubOrDissubLiteral(key3, key1));
						}

						clause.add(getSubOrDissubLiteral(key2, key1));
						ret.add(clause);
						clause.clear();

					}

				} else if (!e.getLeft().containsKey(key1)
						&& e.getRight().containsKey(key1)) {

					/*
					 * constant of the right but not on the left
					 */

					for (String key2 : e.getLeft().keySet()) {
						clause.add(getMinusSubOrDissubLiteral(key2, key1));
					}
					ret.add(clause);
					clause.clear();

				} else if (e.getLeft().containsKey(key1)
						&& !e.getRight().containsKey(key1)) {

					/*
					 * constant on the left but not on the right
					 */

					for (String key3 : e.getRight().keySet()) {
						clause.add(getMinusSubOrDissubLiteral(key3, key1));
					}
					ret.add(clause);
					clause.clear();

				}
			}

			/*
			 * Step 1 for existential atoms
			 */

			for (String key1 : goal.getEAtoms().keySet()) {

				if (!e.getLeft().containsKey(key1)
						&& !e.getRight().containsKey(key1)) {

					/*
					 * atom not in the equation
					 * 
					 * one side of equation
					 */

					for (String key3 : e.getRight().keySet()) {
						for (String key2 : e.getLeft().keySet()) {
							clause.add(getMinusSubOrDissubLiteral(key2, key1));
						}
						clause.add(getSubOrDissubLiteral(key3, key1));
						ret.add(clause);
						clause.clear();

					}

					/*
					 * another side of the equation
					 */

					for (String key2 : e.getLeft().keySet()) {
						for (String key3 : e.getRight().keySet()) {
							clause.add(getMinusSubOrDissubLiteral(key3, key1));
						}
						clause.add(getSubOrDissubLiteral(key2, key1));
						ret.add(clause);
						clause.clear();

					}

				} else if (!e.getLeft().containsKey(key1)
						&& e.getRight().containsKey(key1)) {

					/*
					 * 
					 * existential atom on the right but not on the left side of
					 * an equation
					 */

					for (String key2 : e.getLeft().keySet()) {
						clause.add(getMinusSubOrDissubLiteral(key2, key1));

					}
					ret.add(clause);
					clause.clear();

					// end of outer if; key1 is not on the left, ask if it
					// is on the right
				} else if (e.getLeft().containsKey(key1)
						&& !e.getRight().containsKey(key1)) {

					/*
					 * 
					 * existential atom on the left but not on the right side of
					 * equation
					 */

					for (String key3 : e.getRight().keySet()) {
						clause.add(getMinusSubOrDissubLiteral(key3, key1));
					}
					ret.add(clause);
					clause.clear();

				}
			}

		}
		/*
		 * 
		 * Clauses created in Step 2.
		 * 
		 * 
		 * Step 2.1
		 */

		for (String key1 : goal.getConstants().keySet()) {

			for (String key2 : goal.getConstants().keySet()) {

				if (!key2.equalsIgnoreCase(KRSSKeyword.top)
						&& (!key1.equals(key2))) {
					clause.add(getSubOrDissubLiteral(key1, key2));

					ret.add(clause);
					clause.clear();

				}

			}

		}

		/*
		 * 
		 * Step 2.2 and Step 2.3
		 */

		for (String key1 : goal.getEAtoms().keySet()) {

			for (String key2 : goal.getEAtoms().keySet()) {

				if (!key1.equals(key2)) {

					String role1 = goal.getEAtoms().get(key1).getName();
					String role2 = goal.getEAtoms().get(key2).getName();

					/*
					 * if roles are not equal, then Step 2.2
					 */

					if (!role1.equals(role2)) {
						clause.add(getSubOrDissubLiteral(key1, key2));

						ret.add(clause);
						clause.clear();

						/*
						 * if the roles are equal, then clause in Step 2.3
						 */
					} else {

						FAtom child1 = goal.getEAtoms().get(key1).getChild();
						FAtom child2 = goal.getEAtoms().get(key2).getChild();

						String child1name = child1.getName();
						String child2name = child2.getName();

						if (!child1name.equals(child2name)) {
							clause.add(getMinusSubOrDissubLiteral(child1name,
									child2name));
							clause.add(getSubOrDissubLiteral(key1, key2));

							ret.add(clause);
							clause.clear();

						}

					}

				}

			}

		}

		/*
		 * 
		 * Step 2.4
		 */

		for (String key1 : goal.getConstants().keySet()) {

			for (String key2 : goal.getEAtoms().keySet()) {
				clause.add(getSubOrDissubLiteral(key1, key2));

				ret.add(clause);
				clause.clear();

				if (!key1.equalsIgnoreCase(KRSSKeyword.top)) {
					clause.add(getSubOrDissubLiteral(key2, key1));

					ret.add(clause);
					clause.clear();

				}

			}

		}

		/*
		 * Step 3.1
		 * 
		 * Reflexivity for order literals
		 */
		for (String key1 : goal.getVariables().keySet()) {
			clause.add(getMinusOrderLiteral(key1, key1));

			ret.add(clause);
			clause.clear();

		}// end of clauses in step 2.5

		/*
		 * 
		 * Step 3.1
		 * 
		 * Transitivity for order literals
		 */

		for (String key1 : goal.getVariables().keySet()) {

			for (String key2 : goal.getVariables().keySet()) {

				for (String key3 : goal.getVariables().keySet()) {

					if (!key1.equals(key2) && !key2.equals(key3)) {

						clause.add(getMinusOrderLiteral(key1, key2));
						clause.add(getMinusOrderLiteral(key2, key3));
						clause.add(getOrderLiteral(key1, key3));

						ret.add(clause);
						clause.clear();

					}

				}

			}

		}

		/*
		 * 
		 * Step 2.5
		 * 
		 * Transitivity of dis-subsumption
		 */

		for (String key1 : goal.getAllAtoms().keySet()) {

			for (String key2 : goal.getAllAtoms().keySet()) {

				for (String key3 : goal.getAllAtoms().keySet()) {

					if (!key1.equals(key2) && !key1.equals(key3)
							&& !key2.equals(key3)) {
						clause.add(getSubOrDissubLiteral(key1, key2));
						clause.add(getSubOrDissubLiteral(key2, key3));
						clause.add(getMinusSubOrDissubLiteral(key1, key3));

						ret.add(clause);
						clause.clear();

					}

				}
			}
		}

		/*
		 * 
		 * Step 3.2 Disjunction between order literals and dis-subsumption
		 */

		for (String key1 : goal.getEAtoms().keySet()) {

			FAtom eatom = goal.getEAtoms().get(key1);
			FAtom child = eatom.getChild();

			if (child.isVar()) {

				for (String key2 : goal.getVariables().keySet()) {

					clause.add(getOrderLiteral(key2, child.getName()));

					clause.add(getSubOrDissubLiteral(key2, key1));

					ret.add(clause);
					clause.clear();

				}

			}

		}

		return ret;

	}

	private int getSubOrDissubLiteral(String left, String right) {
		Literal literal = invertLiteral ? new SubsumptionLiteral(left, right)
				: new DissubsumptionLiteral(left, right);
		int val = literals.get(literal.toString());
		return invertLiteral ? (-1) * val : val;
	}

	public Set<Integer> getUpdate() {
		return Collections.unmodifiableSet(update);
	}

	/**
	 * Resets string update values for literals and S(X) for each X, before the
	 * next unifier is computed.
	 */
	public void reset() {

		update.clear();

		for (Integer key : identifiers.keySet()) {

			identifiers.get(key).setValue(false);

		}

		for (String var : goal.getVariables().keySet()) {

			goal.getVariables().get(var).resetS();

		}

	}

	/**
	 * Creates dis-subsumptions and order literals from all pairs of atoms of
	 * the goal
	 */
	private void setLiterals() {

		String first;
		String second;
		Literal literal;

		/*
		 * Literals for dis-subsumptions
		 */

		for (String key1 : goal.getAllAtoms().keySet()) {

			first = key1;

			for (String key2 : goal.getAllAtoms().keySet()) {

				second = key2;
				literal = invertLiteral ? new SubsumptionLiteral(first, second)
						: new DissubsumptionLiteral(first, second);

				literals.put(literal.toString(), identificator);
				identifiers.put(identificator, literal);
				identificator++;
			}
		}

		/*
		 * 
		 * Literals for order on variables
		 */

		for (String key1 : goal.getVariables().keySet()) {

			first = key1;

			for (String key2 : goal.getVariables().keySet()) {

				second = key2;

				literal = new OrderLiteral(first, second);

				literals.put(literal.toString(), identificator);
				identifiers.put(identificator, literal);
				identificator++;

			}

		}

	}

	/**
	 * This method is the same as <code>toTBox(Reader, Writer)</code>, but does
	 * not write a unifier.
	 */
	public boolean toTBox(Reader outfile) throws IOException {

		boolean response = false;
		BufferedReader reader = new BufferedReader(outfile);
		String line = reader.readLine();

		/*
		 * if SAT is in the beginning of the file
		 */
		if (line.startsWith(Solver.msgSat)) {
			response = true;
			line = reader.readLine();
			StringTokenizer st = new StringTokenizer(line);

			while (st.hasMoreTokens()) {
				Integer currentLiteral = Integer.parseInt(st.nextToken());
				if (currentLiteral < 0) {
					Integer i = (-1) * currentLiteral;
					Literal literal = identifiers.get(i);
					if (literal.isDissubsumption()) {
						literal.setValue(false);
					}
				} else if (currentLiteral > 0) {
					Literal literal = identifiers.get(currentLiteral);
					literal.setValue(true);
				}
			}

			/*
			 * Define S_X for each variable X
			 */

			for (Integer i : identifiers.keySet()) {

				String name1 = identifiers.get(i).getFirst();
				String name2 = identifiers.get(i).getSecond();

				if (!identifiers.get(i).getValue()
						&& identifiers.get(i).isDissubsumption()) {

					if (goal.getVariables().containsKey(name1)) {
						if (goal.getConstants().containsKey(name2)) {

							goal.getVariables().get(name1)
									.addToS(goal.getConstants().get(name2));

							if (goal.getVariables().get(name1).isUserVariable()) {
								update.add(i);
							}

						} else if (goal.getEAtoms().containsKey(name2)) {

							goal.getVariables().get(name1)
									.addToS(goal.getEAtoms().get(name2));

							if (goal.getVariables().get(name1).isUserVariable()) {
								update.add(i);
							}
						}
					}

				} else if (identifiers.get(i).isDissubsumption()
						|| identifiers.get(i).isSubsumption()) {

					if (goal.getVariables().containsKey(name1)
							&& goal.getVariables().get(name1).isUserVariable()) {
						if (goal.getConstants().containsKey(name2)) {

							update.add(getMinusLiteralId(identifiers.get(i)));

						} else if (goal.getEAtoms().containsKey(name2)) {

							update.add(getMinusLiteralId(identifiers.get(i)));
						}
					}

				}

			}
			if (update.size() == 0) {
				updateWithNegations();
			}
		}

		return response;

	}

	/**
	 * Reads a reader <code>outfile</code> which contains an output from SAT
	 * solver i.e. UNSAT or SAT with the list of true literals. If the file
	 * contains SAT and the list of true literals, the method translates it to a
	 * TBox, which is written to the writer <code>result</code> and returns
	 * "true". Otherwise, it writes "NOT UNIFIABLE" and returns "false".
	 * 
	 * @param outfile
	 *            reader containing the SAT solver output
	 * @param result
	 *            the result
	 * @throws IOException
	 * @return <code>true</code> if and only if the output of the SAT solver
	 *         contains SAT.
	 */
	public boolean toTBox(Reader outfile, Writer result) throws IOException {
		boolean ret = toTBox(outfile);

		if (ret) {

			PrintWriter out = new PrintWriter(new BufferedWriter(result));

			for (String variable : goal.getVariables().keySet()) {
				if (goal.getVariables().get(variable).isUserVariable()) {
					out.print(KRSSKeyword.open + KRSSKeyword.define_concept
							+ KRSSKeyword.space);
					out.print(variable + KRSSKeyword.space);

					out.print(goal.getVariables().get(variable).printS());
					out.println(KRSSKeyword.space + KRSSKeyword.close
							+ KRSSKeyword.space);
				}
			}

			out.flush();

		} else {

			PrintWriter out = new PrintWriter(new BufferedWriter(result));

			out.println(NOT_UNIFIABLE);

			out.flush();

		}

		return ret;
	}

	private void updateWithNegations() {
		for (FAtom var : goal.getVariables().values()) {
			if (var.isUserVariable()) {
				for (FAtom atom : goal.getAllAtoms().values()) {
					if (atom.isCons() || atom.isRoot()) {
						update.add(getMinusSubOrDissubLiteral(var.toString(),
								atom.toString()));
					}
				}
			}
		}
	}

}
