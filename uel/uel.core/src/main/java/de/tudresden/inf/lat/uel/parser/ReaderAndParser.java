package de.tudresden.inf.lat.uel.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.HashMap;

import de.tudresden.inf.lat.uel.main.Atom;
import de.tudresden.inf.lat.uel.main.Equation;
import de.tudresden.inf.lat.uel.main.FAtom;
import de.tudresden.inf.lat.uel.main.Goal;

/**
 * This is a class, which is a parser of the file with the goal of unification.
 * 
 * @author Barbara Morawska
 * 
 */

public class ReaderAndParser {

	private StreamTokenizer str;

	/*
	 * leftvar is the first defined concept in the goal file
	 */
	private String leftvar;

	/*
	 * rightvar is the second defined concept in the goal file
	 */
	private String rightvar;

	public ReaderAndParser() {
	}

	/**
	 * This method reads from a Reader. It creates goal equations. Two equations
	 * from two definitions, and one equations between the concepts defined in
	 * the input.
	 * 
	 * @throws IOException
	 */

	public void read(Reader input, Goal goal) throws IOException {

		Equation equation;

		/*
		 * sides of a goal equation
		 */
		HashMap<String, Atom> left;
		HashMap<String, Atom> right;

		/*
		 * two defined concepts from the input
		 */
		HashMap<String, Atom> mainleft = new HashMap<String, Atom>();
		HashMap<String, Atom> mainright = new HashMap<String, Atom>();

		/*
		 * Opens input file
		 */

		Reader r = new BufferedReader(input);

		initTokenizer(r);

		int token = str.nextToken();

		/*
		 * Parse first goal equation
		 */
		if (token != StreamTokenizer.TT_EOF) {

			/*
			 * Definition always starts with (
			 */

			if (token != '(') {

				throw new RuntimeException("Expecting '(' but found " + str);
			}

			token = str.nextToken();
			String namestr = str.sval;

			/*
			 * looks for the first concept defined in the input
			 */

			if (namestr.equalsIgnoreCase("DEFINE-CONCEPT")
					|| namestr.equalsIgnoreCase("define-primitive-concept")
					|| namestr.equalsIgnoreCase("DEFCONCEPT")) {

				/*
				 * 
				 * the name of the defined concept is a word or a number and a
				 * system variable
				 */
				token = str.nextToken();

				if (token == StreamTokenizer.TT_WORD) {

					leftvar = str.sval;
				} else if (token == StreamTokenizer.TT_NUMBER) {

					leftvar = new Integer((int) str.nval).toString();
				} else {

					throw new RuntimeException("Expecting concept but found "
							+ str);

				}

				/*
				 * The first concept is initialized as a flat atom,
				 */

				FAtom var = new FAtom(leftvar, false, true, null);

				left = new HashMap<String, Atom>();
				left.put(var.toString(), var);
				mainleft.put(var.toString(), var);

				/*
				 * Parse left side of the goal equation
				 */
				right = parse();

				/*
				 * Add equation to the goal while flattening it
				 */

				equation = new Equation(left, right);

				goal.addFlatten(equation);

				/*
				 * close bracket for this definition
				 */
				token = str.nextToken();

				if (token != ')')
					throw new RuntimeException("Expected ) but found " + str);

			} else {

				throw new RuntimeException("Empty goal equation " + str);

			}

			token = str.nextToken();

			/*
			 * Parse second goal equation
			 */

			if (token != StreamTokenizer.TT_EOF) {

				/*
				 * Definition always starts with (
				 */

				if (token != '(') {

					throw new RuntimeException("Expecting '(' but found " + str);
				}

				token = str.nextToken();
				namestr = str.sval;

				/*
				 * looks for the second concept defined in the input
				 */

				if (namestr.equalsIgnoreCase("DEFINE-CONCEPT")
						|| namestr.equalsIgnoreCase("define-primitive-concept")
						|| namestr.equalsIgnoreCase("DEFCONCEPT")) {

					/*
					 * 
					 * the name of the defined concept is a word or a number and
					 * a system variable
					 */
					token = str.nextToken();

					if (token == StreamTokenizer.TT_WORD) {
						rightvar = str.sval;
					} else if (token == StreamTokenizer.TT_NUMBER) {

						rightvar = new Integer((int) str.nval).toString();
					}

					/*
					 * The second concept is initialized as a flat atom,
					 */

					FAtom var = new FAtom(rightvar, false, true, null);

					left = new HashMap<String, Atom>();
					left.put(var.toString(), var);
					mainright.put(var.toString(), var);

					/*
					 * Parse left side of the second goal equation
					 */

					right = parse();

					/*
					 * Add equation to the goal while flattening it
					 */

					equation = new Equation(left, right);

					goal.addFlatten(equation);

					/*
					 * close bracket for this definition
					 */

					token = str.nextToken();
					if (token != ')')
						throw new RuntimeException("Expected ) but found "
								+ str);

				}

				/*
				 * Add main equation
				 */

				equation = new Equation(mainleft, mainright);

				goal.addEquation(equation);

			} else {

				throw new RuntimeException(
						"Empty right side of a goal equation " + str);

			}
		}

	}

	/*
	 * Parse method Parsing right side of a definition
	 */

	private HashMap<String, Atom> parse() throws IOException {

		HashMap<String, Atom> result = new HashMap<String, Atom>();
		Atom a;
		String s = "";
		int token = str.nextToken();

		/*
		 * if ( then expect AND or SOMEclose bracket afterwards
		 */

		if (token == '(') {

			token = str.nextToken();
			if (token != StreamTokenizer.TT_WORD)
				throw new RuntimeException("Expecting constructor but found "
						+ str);

			s = str.sval;

			if (s.equalsIgnoreCase("AND")) {
				token = str.nextToken();

				/*
				 * parsing inside (AND
				 */

				while (token != StreamTokenizer.TT_EOF) {

					/*
					 * after ( expected SOME
					 */

					if (token == '(') {
						token = str.nextToken();

						if (token != StreamTokenizer.TT_WORD
								&& token != StreamTokenizer.TT_NUMBER)
							throw new RuntimeException(
									"Expecting SOME but found " + str);

						String ss = "";
						if (token == StreamTokenizer.TT_WORD) {

							ss = str.sval;
						} else if (token == StreamTokenizer.TT_NUMBER) {

							ss = new Integer((int) str.nval).toString();
						}

						s = "(".concat(ss);

						/*
						 * And cannot appear in AND
						 */
						if (s.equalsIgnoreCase("(AND"))
							throw new RuntimeException(
									"AND cannot occur inside (AND ...) " + str);

						/*
						 * SOME was found inside AND
						 */
						if (s.equalsIgnoreCase("(SOME")) {

							token = str.nextToken();
							if (token != StreamTokenizer.TT_WORD
									&& token != StreamTokenizer.TT_NUMBER)
								throw new RuntimeException(
										"Expecting role name but found " + str);

							String rolename = "";

							if (token == StreamTokenizer.TT_WORD) {

								rolename = str.sval;

							} else if (token == StreamTokenizer.TT_NUMBER) {

								rolename = new Integer((int) str.nval)
										.toString();

							}

							HashMap<String, Atom> arglist = new HashMap<String, Atom>(
									parse());

							a = new Atom(rolename, true, arglist);

							result.put(a.toString(), a);

							token = str.nextToken();
							if (token != ')')
								throw new RuntimeException("Expected ) found"
										+ str);

						}

					} else if (token == StreamTokenizer.TT_WORD
							|| token == StreamTokenizer.TT_NUMBER) {

						/*
						 * Constant or variable found as an argument for AND
						 */

						String newname = "";

						if (token == StreamTokenizer.TT_WORD) {
							newname = str.sval;
						} else if (token == StreamTokenizer.TT_NUMBER) {

							newname = new Integer((int) str.nval).toString();

						}

						Atom c = new Atom(newname, false, null);
						result.put(newname, c);

					} else if (token == ')') {

						return result;

					}

					token = str.nextToken();

				}

				/*
				 * Parsing SOME at top level
				 */

			} else if (s.equalsIgnoreCase("SOME")) {

				token = str.nextToken();

				if (token != StreamTokenizer.TT_WORD
						&& token != StreamTokenizer.TT_NUMBER)
					throw new RuntimeException("Expecting role name but found "
							+ str);

				String rolename = "";

				if (token == StreamTokenizer.TT_WORD) {

					rolename = str.sval;

				} else if (token == StreamTokenizer.TT_NUMBER) {

					rolename = new Integer((int) str.nval).toString();

				}

				HashMap<String, Atom> arglist = new HashMap<String, Atom>(
						parse());

				a = new Atom(rolename, true, arglist);

				result.put(a.toString(), a);

				/*
				 * close bracket after (SOME...
				 */

				token = str.nextToken();
				if (token != ')')
					throw new RuntimeException("Expected ) found" + str);

				return result;

			}

			/*
			 * PARSING CONSTANT OR VARIABLE AT TOP LEVEL
			 */

		} else if (token == StreamTokenizer.TT_WORD
				|| token == StreamTokenizer.TT_NUMBER) {
			/*
			 * Found a constant or variable at the top level
			 */

			if (token == StreamTokenizer.TT_WORD) {

				s = str.sval;
			} else if (token == StreamTokenizer.TT_NUMBER) {

				s = new Integer((int) str.nval).toString();
			}

			if (!s.equalsIgnoreCase("TOP")) {

				a = new Atom(s, false, null);
				result.put(a.toString(), a);
				return result;

			} else {

				throw new RuntimeException(
						"Something wrong: Top should not be here  " + str);
			}

		}

		return result;

	}

	private void initTokenizer(Reader reader) {

		str = new StreamTokenizer(reader);
		str.ordinaryChar('(');
		str.ordinaryChar(')');
		str.wordChars('_', '_');

	}

}
