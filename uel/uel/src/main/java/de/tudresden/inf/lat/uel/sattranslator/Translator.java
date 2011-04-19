package de.tudresden.inf.lat.uel.sattranslator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File; //import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter; //import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudresden.inf.lat.uel.main.Equation;
import de.tudresden.inf.lat.uel.main.FAtom;
import de.tudresden.inf.lat.uel.main.Goal;
import de.tudresden.inf.lat.uel.main.Literal;



/**
 * 
 * This static class performes reduction of goal equations to propositional
 * clauses. The reduction is explained in F. Baader, B. Morawska,
 * "SAT Encoding of Unification in EL", LPAR 2010.
 * 
 * 
 * It has also the methods to translate an output of a sat solver to a unifier.
 * 
 * 
 * @author Barbara Morawska
 * 
 */

public class Translator {

	static private Integer identificator = 1;

	/*
	 * Literals are all dis-subsumptions between atoms in the goal the first
	 * hash map maps them to unique numbers.
	 */

	static private HashMap<String, Integer> literals = new HashMap<String, Integer>();

	/*
	 * Identifiers are numbers, each number uniquely identifies a literal, i.e.
	 * a subsumption.
	 */
	static private HashMap<Integer, Literal> identifiers = new HashMap<Integer, Literal>();

	/**
	 * 
	 * Update is a string of numbers or numbers preceeded with "-" encoding the
	 * negation of the computed unifier. This is needed for computation of the
	 * next unifier.
	 * 
	 * 
	 */

	public static StringBuilder Update = new StringBuilder("");

	private Translator() {
	};

	/**
	 * This method encodes equations into propositional clauses in DIMACS
	 * format, i.e. positive literal is represented by a positive number and a
	 * negative literal is represented by a corresponding negative number. Each
	 * clause is on one line. The end of a clause is marked by 0. Example of a
	 * clause in DIMACS format: 1 -3 0
	 * 
	 * Parameter infile is a file previously created for writing propositional
	 * clauses.
	 * 
	 * It should be previously created, because the name of the same file is
	 * then sent to a sat solver by Unifier.
	 * 
	 * @param infile
	 */
	public static void toDIMACS(File infile) {

		setLiterals();

		try {

			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(infile)));

			/*
			 * 
			 * Clauses created in Step 1
			 */

			for (Equation e : Goal.equations) {

				/*
				 * Step 1 for constants
				 */

				for (String key1 : Goal.constants.keySet()) {

					if (!e.left.containsKey(key1) && !e.right.containsKey(key1)) {

						/*
						 * constant not in the equation
						 * 
						 * 
						 * 
						 * one side of an equation
						 */

						for (String key3 : e.right.keySet()) {
							for (String key2 : e.left.keySet()) {

								Literal lit2 = new Literal(key2, key1, 's');

								out.print(" -");
								out.print(literals.get(lit2.toString()));
								out.print(" ");

							}

							Literal lit3 = new Literal(key3, key1, 's');

							out.print(" " + literals.get(lit3.toString()));
							out.print(" 0 \n");

						}

						/*
						 * another side of an equation
						 */

						for (String key2 : e.left.keySet()) {
							for (String key3 : e.right.keySet()) {

								Literal lit2 = new Literal(key3, key1, 's');

								out.print(" -");
								out.print(literals.get(lit2.toString()));
								out.print(" ");

							}

							Literal lit3 = new Literal(key2, key1, 's');

							out.print(literals.get(lit3.toString()));
							out.print(" 0 \n");

						}

					} else if (!e.left.containsKey(key1)
							&& e.right.containsKey(key1)) {

						/*
						 * constant of the right but not on the left
						 */

						for (String key2 : e.left.keySet()) {
							Literal lit4 = new Literal(key2, key1, 's');

							out.print(" -");
							out.print(literals.get(lit4.toString()));
							out.print(" ");

						}
						out.print(" 0 \n");

					} else if (e.left.containsKey(key1)
							&& !e.right.containsKey(key1)) {

						/*
						 * constant on the left but not on the right
						 */

						for (String key3 : e.right.keySet()) {
							Literal lit5 = new Literal(key3, key1, 's');

							out.print(" -");
							out.print(literals.get(lit5.toString()));
							out.print(" ");

						}
						out.print(" 0 \n");

					}
				}

				/*
				 * Step 1 for existential atoms
				 */

				for (String key1 : Goal.eatoms.keySet()) {

					if (!e.left.containsKey(key1) && !e.right.containsKey(key1)) {

						/*
						 * atom not in the equation
						 * 
						 * one side of equation
						 */

						for (String key3 : e.right.keySet()) {
							for (String key2 : e.left.keySet()) {

								Literal lit2 = new Literal(key2, key1, 's');

								out.print(" -");
								out.print(literals.get(lit2.toString()));
								out.print(" ");

							}

							Literal lit3 = new Literal(key3, key1, 's');

							out.print(literals.get(lit3.toString()));
							out.print(" 0 \n");

						}

						/*
						 * another side of the equation
						 */

						for (String key2 : e.left.keySet()) {
							for (String key3 : e.right.keySet()) {

								Literal lit2 = new Literal(key3, key1, 's');

								out.print(" -");
								out.print(literals.get(lit2.toString()));
								out.print(" ");
							}

							Literal lit3 = new Literal(key2, key1, 's');

							out.print(literals.get(lit3.toString()));
							out.print(" 0 \n");

						}

					} else if (!e.left.containsKey(key1)
							&& e.right.containsKey(key1)) {

						/*
						 * 
						 * existential atom on the right but not on the left
						 * side of an equation
						 */

						for (String key2 : e.left.keySet()) {
							Literal lit4 = new Literal(key2, key1, 's');

							out.print(" -");
							out.print(literals.get(lit4.toString()));
							out.print(" ");

						}
						out.print(" 0 \n");

						// end of outer if; key1 is not on the left, ask if it
						// is on the right
					} else if (e.left.containsKey(key1)
							&& !e.right.containsKey(key1)) {

						/*
						 * 
						 * existential atom on the left but not on the right
						 * side of equation
						 */

						for (String key3 : e.right.keySet()) {
							Literal lit5 = new Literal(key3, key1, 's');

							out.print(" -");
							out.print(literals.get(lit5.toString()));
							out.print(" ");

						}
						out.print(" 0 \n");

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

			for (String key1 : Goal.constants.keySet()) {

				for (String key2 : Goal.constants.keySet()) {

					if (!key2.equals("TOP") && (!key1.equals(key2))) {

						Literal lit = new Literal(key1, key2, 's');

						out.print(literals.get(lit.toString()));

						out.print(" 0 \n");

					}

				}

			}

			/*
			 * 
			 * Step 2.2 and Step 2.3
			 */

			for (String key1 : Goal.eatoms.keySet()) {

				for (String key2 : Goal.eatoms.keySet()) {

					if (!key1.equals(key2)) {

						String role1 = Goal.eatoms.get(key1).getName();
						String role2 = Goal.eatoms.get(key2).getName();

						/*
						 * if roles are not equal, then Step 2.2
						 */

						if (!role1.equals(role2)) {

							Literal lit = new Literal(key1, key2, 's');

							out.print(literals.get(lit.toString()));

							out.print(" 0 \n");

							/*
							 * if the roles are equal, then clause in Step 2.3
							 */
						} else {

							FAtom child1 = (FAtom) Goal.eatoms.get(key1)
									.getChild();
							FAtom child2 = (FAtom) Goal.eatoms.get(key2)
									.getChild();

							String child1name = child1.getName();
							String child2name = child2.getName();

							if (!child1name.equals(child2name)) {

								Literal lit1 = new Literal(child1name,
										child2name, 's');

								Literal lit2 = new Literal(key1, key2, 's');

								out.print(" -");
								out.print(literals.get(lit1.toString()));
								out.print(" ");

								out.print(literals.get(lit2.toString()));
								out.print(" ");

								out.print(" 0 \n");

							}

						}

					}

				}

			}

			/*
			 * 
			 * Step 2.4
			 */

			for (String key1 : Goal.constants.keySet()) {

				for (String key2 : Goal.eatoms.keySet()) {

					Literal lit = new Literal(key1, key2, 's');

					out.print(literals.get(lit.toString()));

					out.print(" 0 \n");

					if (!key1.equals("TOP")) {

						Literal lit1 = new Literal(key2, key1, 's');

						out.print(literals.get(lit1.toString()));

						out.print(" 0 \n");

					}

				}

			}

			/*
			 * Step 3.1
			 * 
			 * Reflexivity for order literals
			 */
			for (String key1 : Goal.variables.keySet()) {

				Literal lit = new Literal(key1, key1, 'o');

				out.print(" -");
				out.print(literals.get(lit.toString()));
				out.print(" ");

				out.print(" 0 \n");

			}// end of clauses in step 2.5

			/*
			 * 
			 * Step 3.1
			 * 
			 * Transitivity for order literals
			 */

			for (String key1 : Goal.variables.keySet()) {

				for (String key2 : Goal.variables.keySet()) {

					for (String key3 : Goal.variables.keySet()) {

						if (!key1.equals(key2) && !key2.equals(key3)) {

							Literal lit1 = new Literal(key1, key2, 'o');

							Literal lit2 = new Literal(key2, key3, 'o');

							Literal lit3 = new Literal(key1, key3, 'o');

							out.print(" -");
							out.print(literals.get(lit1.toString()));
							out.print(" ");

							out.print(" -");
							out.print(literals.get(lit2.toString()));
							out.print(" ");

							out.print(literals.get(lit3.toString()));
							out.print(" ");

							out.print(" 0 \n");

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

			for (String key1 : Goal.allatoms.keySet()) {

				for (String key2 : Goal.allatoms.keySet()) {

					for (String key3 : Goal.allatoms.keySet()) {

						if (!key1.equals(key2) && !key1.equals(key3)
								&& !key2.equals(key3)) {

							Literal lit1 = new Literal(key1, key2, 's');

							Literal lit2 = new Literal(key2, key3, 's');

							Literal lit3 = new Literal(key1, key3, 's');

							out.print(" -");
							out.print(literals.get(lit3.toString()));
							out.print(" ");

							out.print(literals.get(lit1.toString()));
							out.print(" ");

							out.print(literals.get(lit2.toString()));
							out.print(" ");

							out.print(" 0 \n");

						}

					}
				}
			}

			/*
			 * 
			 * Step 3.2 Disjunction between order literals and dis-subsumption
			 */

			for (String key1 : Goal.eatoms.keySet()) {

				FAtom eatom = (FAtom) Goal.eatoms.get(key1);
				FAtom child = (FAtom) eatom.getChild();

				if (child.isVar()) {

					for (String key2 : Goal.variables.keySet()) {

						Literal lit1 = new Literal(key2, child.getName(), 'o');

						Literal lit2 = new Literal(key2, key1, 's');

						// out.print(" -");
						out.print(literals.get(lit1.toString()));
						out.print(" ");

						out.print(literals.get(lit2.toString()));
						out.print(" ");

						out.print(" 0 \n");

					}

				}

			}

			out.flush();

			out.close();

		} catch (Exception ex) {
			ex.printStackTrace();

		}

	}

	/*
	 * 
	 * Method to create dis-subsumptions and order literals from all pairs of
	 * atoms of the goal
	 */
	private static void setLiterals() {

		String first;
		String second;
		Literal literal;

		/*
		 * Literals for dis-subsumptions
		 */

		for (String key1 : Goal.allatoms.keySet()) {

			first = key1;

			for (String key2 : Goal.allatoms.keySet()) {

				second = key2;
				literal = new Literal(first, second, 's');

				literals.put(literal.toString(), identificator);
				identifiers.put(identificator, literal);
				identificator++;
			}
		}

		/*
		 * 
		 * Literals for order on variables
		 */

		for (String key1 : Goal.variables.keySet()) {

			first = key1;

			for (String key2 : Goal.variables.keySet()) {

				second = key2;

				literal = new Literal(first, second, 'o');

				literals.put(literal.toString(), identificator);
				identifiers.put(identificator, literal);
				identificator++;

			}

		}

	}

	/**
	 * This method reads a file <outfile> which contains an output from SAT
	 * solver i.e. UNSAT or SAT with the list of true literals. If the file
	 * contains SAT and the list of true literals, the method translates it to a
	 * TBox, which is written to the file <result> and returns "true".
	 * Otherwise, it writed "NOT UNIFIABLE" to file <result> and returns
	 * "false".
	 * 
	 * @param outfile
	 * @param result
	 * @return
	 */
	public static boolean toTBox(File outfile, File result) {

		Pattern answer = Pattern.compile("^SAT");
		Matcher manswer;
		String line;
		boolean response = false;

		try {
			FileReader fileReader = new FileReader(outfile);

			BufferedReader reader = new BufferedReader(fileReader);

			line = reader.readLine();

			manswer = answer.matcher(line);

			/*
			 * if SAT is in the beginning of the file
			 */
			if (manswer.find()) {

				response = true;

				Pattern sign = Pattern.compile("^-");
				Matcher msign;

				line = reader.readLine();
				StringTokenizer st = new StringTokenizer(line);

				StringBuilder token;

				while (st.hasMoreTokens()) {

					token = new StringBuilder(st.nextToken());
					msign = sign.matcher(token);

					if (msign.find()) {

						token = token.delete(0, msign.end());

						Integer i = Integer.parseInt(token.toString());

						Literal literal = (Literal) identifiers.get(i);

						literal.setValue(true);

					}

				}

				/*
				 * Define S_X for each variable X
				 */

				for (Integer i : identifiers.keySet()) {

					String name1 = identifiers.get(i).getFirst();
					String name2 = identifiers.get(i).getSecond();

					if (identifiers.get(i).getValue()
							&& identifiers.get(i).getKind() == 's') {

						if (Goal.variables.containsKey(name1)) {
							if (Goal.constants.containsKey(name2)) {

								Goal.variables.get(name1).addToS(
										(FAtom) Goal.constants.get(name2));

								if (!Goal.variables.get(name1).isSys()) {
									Update.append(i + " ");
								}

							} else if (Goal.eatoms.containsKey(name2)) {

								Goal.variables.get(name1).addToS(
										(FAtom) Goal.eatoms.get(name2));

								if (!Goal.variables.get(name1).isSys()) {
									Update.append(i + " ");
								}
							}
						}

					} else if (identifiers.get(i).getKind() == 's') {

						if (Goal.variables.containsKey(name1)
								&& !Goal.variables.get(name1).isSys()) {
							if (Goal.constants.containsKey(name2)) {

								// goal.variables.get(name1).addToS((Atom)
								// goal.constants.get(name2));

								Update.append("-" + i + " ");

							} else if (Goal.eatoms.containsKey(name2)) {

								// goal.variables.get(name1).addToS((Atom)
								// goal.eatoms.get(name2));

								Update.append("-" + i + " ");
							}
						}

					}

				}

				PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter(result)));

				out.println("UNIFIABLE:");
				out.println("Unifier: ");

				for (String variable : Goal.variables.keySet()) {

					if (!Goal.variables.get(variable).isSys()) {
						out.print("(define-concept ");
						out.print(variable + " ");

						Goal.variables.get(variable).printS(out);
						out.println(" ) ");
					}
				}

				out.println("-------------------------------------");
				out.close();

			} else {

				PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter(result)));

				out.println("NOT UNIFIABLE");

				out.close();

			}

		} catch (Exception ex) {

			ex.printStackTrace();

		}

		return response;

	}

	/*
	 * This method is the same as toTBox but it does not overwrite result.
	 * Instead it appends a unifier to the existing file <result>
	 */

	public static boolean toTBoxB(File outfile, File result,
			int numberofsolutions) {

		Pattern answer = Pattern.compile("^SAT");
		Matcher manswer;
		String line;
		boolean response = false;

		try {
			FileReader fileReader = new FileReader(outfile);

			BufferedReader reader = new BufferedReader(fileReader);

			line = reader.readLine();

			manswer = answer.matcher(line);

			if (manswer.find()) {

				response = true;

				Pattern sign = Pattern.compile("^-");
				Matcher msign;

				line = reader.readLine();
				StringTokenizer st = new StringTokenizer(line);

				StringBuilder token;

				while (st.hasMoreTokens()) {

					token = new StringBuilder(st.nextToken());
					msign = sign.matcher(token);

					if (msign.find()) {

						token = token.delete(0, msign.end());

						Integer i = Integer.parseInt(token.toString());

						Literal literal = (Literal) identifiers.get(i);

						literal.setValue(true);

					}

				}

				for (Integer i : identifiers.keySet()) {

					String name1 = identifiers.get(i).getFirst();
					String name2 = identifiers.get(i).getSecond();

					if (identifiers.get(i).getValue()
							&& identifiers.get(i).getKind() == 's') {

						if (Goal.variables.containsKey(name1)) {
							if (Goal.constants.containsKey(name2)) {

								Goal.variables.get(name1).addToS(
										(FAtom) Goal.constants.get(name2));

								if (!Goal.variables.get(name1).isSys()) {
									Update.append(i + " ");
								}

							} else if (Goal.eatoms.containsKey(name2)) {

								Goal.variables.get(name1).addToS(
										(FAtom) Goal.eatoms.get(name2));

								if (!Goal.variables.get(name1).isSys()) {
									Update.append(i + " ");
								}
							}
						}

					} else if (identifiers.get(i).getKind() == 's') {

						if (Goal.variables.containsKey(name1)
								&& !Goal.variables.get(name1).isSys()) {
							if (Goal.constants.containsKey(name2)) {

								Update.append("-" + i + " ");

							} else if (Goal.eatoms.containsKey(name2)) {

								Update.append("-" + i + " ");
							}
						}

					}

				}

				PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter(result, true)));

				out.println(numberofsolutions + " UNIFIER: ");

				for (String variable : Goal.variables.keySet()) {

					if (!Goal.variables.get(variable).isSys()) {
						out.print("(define-concept ");
						out.print(variable + " ");

						Goal.variables.get(variable).printS(out);
						out.println(" ) ");
					}
				}

				out.println("---------------------------------------");
				out.close();

			} else {

				PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter(result, true)));

				out.println("NO MORE UNIFIERS");

				out.close();

			}

		} catch (Exception ex) {

			ex.printStackTrace();

		}

		return response;

	}

	/*
	 * This method is the same as toTBox, but does not write a unifier to file
	 * <result>
	 */
	public static boolean toTBox(File outfile) {

		Pattern answer = Pattern.compile("^SAT");
		Matcher manswer;
		String line;
		boolean response = false;

		try {
			FileReader fileReader = new FileReader(outfile);

			BufferedReader reader = new BufferedReader(fileReader);

			line = reader.readLine();

			manswer = answer.matcher(line);

			if (manswer.find()) {

				response = true;

				Pattern sign = Pattern.compile("^-");
				Matcher msign;

				line = reader.readLine();

				StringTokenizer st = new StringTokenizer(line);

				StringBuilder token;

				while (st.hasMoreTokens()) {

					token = new StringBuilder(st.nextToken());
					msign = sign.matcher(token);

					if (msign.find()) {

						token = token.delete(0, msign.end());

						Integer i = Integer.parseInt(token.toString());

						Literal literal = (Literal) identifiers.get(i);

						literal.setValue(true);

					}

				}

				for (Integer i : identifiers.keySet()) {

					String name1 = identifiers.get(i).getFirst();
					String name2 = identifiers.get(i).getSecond();

					if (identifiers.get(i).getValue()
							&& identifiers.get(i).getKind() == 's') {

						if (Goal.variables.containsKey(name1)) {
							if (Goal.constants.containsKey(name2)) {

								Goal.variables.get(name1).addToS(
										(FAtom) Goal.constants.get(name2));

								if (!Goal.variables.get(name1).isSys()) {
									Update.append(i + " ");
								}

							} else if (Goal.eatoms.containsKey(name2)) {

								Goal.variables.get(name1).addToS(
										(FAtom) Goal.eatoms.get(name2));

								if (!Goal.variables.get(name1).isSys()) {
									Update.append(i + " ");
								}
							}
						}

					} else if (identifiers.get(i).getKind() == 's') {

						if (Goal.variables.containsKey(name1)
								&& !Goal.variables.get(name1).isSys()) {
							if (Goal.constants.containsKey(name2)) {

								Update.append("-" + i + " ");

							} else if (Goal.eatoms.containsKey(name2)) {

								Update.append("-" + i + " ");
							}
						}

					}

				}

			}

		} catch (Exception ex) {

			ex.printStackTrace();
		}

		return response;

	}

	/*
	 * Method used by UEL to reset string Update values for literals and
	 * S(X) for each X, before the next unifier is computed.
	 */
	public static void reset() {

		Update = new StringBuilder("");

		for (Integer key : identifiers.keySet()) {

			identifiers.get(key).setValue(false);

		}

		for (String var : Goal.variables.keySet()) {

			Goal.variables.get(var).resetS();

		}

	}

}
