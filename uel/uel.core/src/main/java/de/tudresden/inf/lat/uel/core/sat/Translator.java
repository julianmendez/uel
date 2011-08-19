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

	private static final Integer minus = -1;
	public static final String NOT_UNIFIABLE = "NOT UNIFIABLE / NO MORE UNIFIERS";

	private Goal goal;
	private Integer identificator = 1;

	/**
	 * Identifiers are numbers, each number uniquely identifies a literal, i.e.
	 * a dissubsumption.
	 */
	private HashMap<Integer, Literal> identifiers = new HashMap<Integer, Literal>();

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

	/**
	 * Returns the literals.
	 * 
	 * @return the literals
	 */
	public Map<String, Integer> getLiterals() {
		return this.literals;
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

							Literal lit2 = new DissubsumptionLiteral(key2, key1);

							clause.add(minus * literals.get(lit2.toString()));

						}

						Literal lit3 = new DissubsumptionLiteral(key3, key1);

						clause.add(literals.get(lit3.toString()));
						ret.add(clause);
						clause.clear();

					}

					/*
					 * another side of an equation
					 */

					for (String key2 : e.getLeft().keySet()) {
						for (String key3 : e.getRight().keySet()) {

							Literal lit2 = new DissubsumptionLiteral(key3, key1);

							clause.add(minus * literals.get(lit2.toString()));

						}

						Literal lit3 = new DissubsumptionLiteral(key2, key1);

						clause.add(literals.get(lit3.toString()));
						ret.add(clause);
						clause.clear();

					}

				} else if (!e.getLeft().containsKey(key1)
						&& e.getRight().containsKey(key1)) {

					/*
					 * constant of the right but not on the left
					 */

					for (String key2 : e.getLeft().keySet()) {
						Literal lit4 = new DissubsumptionLiteral(key2, key1);

						clause.add(minus * literals.get(lit4.toString()));

					}
					ret.add(clause);
					clause.clear();

				} else if (e.getLeft().containsKey(key1)
						&& !e.getRight().containsKey(key1)) {

					/*
					 * constant on the left but not on the right
					 */

					for (String key3 : e.getRight().keySet()) {
						Literal lit5 = new DissubsumptionLiteral(key3, key1);

						clause.add(minus * literals.get(lit5.toString()));

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

							Literal lit2 = new DissubsumptionLiteral(key2, key1);

							clause.add(minus * literals.get(lit2.toString()));

						}

						Literal lit3 = new DissubsumptionLiteral(key3, key1);

						clause.add(literals.get(lit3.toString()));
						ret.add(clause);
						clause.clear();

					}

					/*
					 * another side of the equation
					 */

					for (String key2 : e.getLeft().keySet()) {
						for (String key3 : e.getRight().keySet()) {

							Literal lit2 = new DissubsumptionLiteral(key3, key1);

							clause.add(minus * literals.get(lit2.toString()));
						}

						Literal lit3 = new DissubsumptionLiteral(key2, key1);

						clause.add(literals.get(lit3.toString()));
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
						Literal lit4 = new DissubsumptionLiteral(key2, key1);

						clause.add(minus * literals.get(lit4.toString()));

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
						Literal lit5 = new DissubsumptionLiteral(key3, key1);

						clause.add(minus * literals.get(lit5.toString()));

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

					Literal lit = new DissubsumptionLiteral(key1, key2);

					clause.add(literals.get(lit.toString()));

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

						Literal lit = new DissubsumptionLiteral(key1, key2);

						clause.add(literals.get(lit.toString()));

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

							Literal lit1 = new DissubsumptionLiteral(
									child1name, child2name);

							Literal lit2 = new DissubsumptionLiteral(key1, key2);

							clause.add(minus * literals.get(lit1.toString()));

							clause.add(literals.get(lit2.toString()));

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

				Literal lit = new DissubsumptionLiteral(key1, key2);

				clause.add(literals.get(lit.toString()));

				ret.add(clause);
				clause.clear();

				if (!key1.equalsIgnoreCase(KRSSKeyword.top)) {

					Literal lit1 = new DissubsumptionLiteral(key2, key1);

					clause.add(literals.get(lit1.toString()));

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

			Literal lit = new OrderLiteral(key1, key1);

			clause.add(minus * literals.get(lit.toString()));

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

						Literal lit1 = new OrderLiteral(key1, key2);

						Literal lit2 = new OrderLiteral(key2, key3);

						Literal lit3 = new OrderLiteral(key1, key3);

						clause.add(minus * literals.get(lit1.toString()));

						clause.add(minus * literals.get(lit2.toString()));

						clause.add(literals.get(lit3.toString()));

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

						Literal lit1 = new DissubsumptionLiteral(key1, key2);

						Literal lit2 = new DissubsumptionLiteral(key2, key3);

						Literal lit3 = new DissubsumptionLiteral(key1, key3);

						clause.add(minus * literals.get(lit3.toString()));

						clause.add(literals.get(lit1.toString()));

						clause.add(literals.get(lit2.toString()));

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

					Literal lit1 = new OrderLiteral(key2, child.getName());

					Literal lit2 = new DissubsumptionLiteral(key2, key1);

					clause.add(literals.get(lit1.toString()));

					clause.add(literals.get(lit2.toString()));

					ret.add(clause);
					clause.clear();

				}

			}

		}

		return ret;

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
				literal = new DissubsumptionLiteral(first, second);

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
						literal.setValue(true);
					}
				} else if (currentLiteral > 0) {
					Literal literal = identifiers.get(currentLiteral);
					literal.setValue(false);
				}
			}

			/*
			 * Define S_X for each variable X
			 */

			for (Integer i : identifiers.keySet()) {

				String name1 = identifiers.get(i).getFirst();
				String name2 = identifiers.get(i).getSecond();

				if (identifiers.get(i).getValue()
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

				} else if (identifiers.get(i).isDissubsumption()) {

					if (goal.getVariables().containsKey(name1)
							&& goal.getVariables().get(name1).isUserVariable()) {
						if (goal.getConstants().containsKey(name2)) {

							update.add(minus * i);

						} else if (goal.getEAtoms().containsKey(name2)) {

							update.add(minus * i);
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
						Literal lit = new DissubsumptionLiteral(var.toString(),
								atom.toString());
						int i = literals.get(lit.toString());
						update.add(minus * i);
					}
				}
			}
		}
	}

}
