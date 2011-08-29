package de.tudresden.inf.lat.uel.core.sat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import de.tudresden.inf.lat.uel.core.type.Atom;
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
 * This algorithm is explained below:
 * 
 * <div> Given a flat <i>EL</i>-unification problem &Gamma;, the set C(&Gamma;)
 * consists of the following clauses:
 * 
 * <ul>
 * <li>(1) Translation of the equations of &Gamma;. For every equation
 * A<sub>1</sub> &#8851; &hellip; &#8851; A<sub>m</sub> &equiv;<sup>?</sup>
 * B<sub>1</sub> &#8851; &hellip; &#8851; B<sub>n</sub> of &Gamma;, we create
 * the following Horn clauses, which express that any atom that occurs as a
 * top-level conjunct on one side of an equivalence must subsume a top-level
 * conjunct on the other side:
 * 
 * <ul>
 * <li>1. For every non-variable atom C &isin; {A<sub>1</sub>, &hellip; ,
 * A<sub>m</sub>}:<br />
 * [B<sub>1</sub> &#8930; C] &and; &hellip; &and; [B<sub>n</sub> &#8930; C]
 * &rarr;</li>
 * 
 * <li>2. For every non-variable atom C &isin; {B<sub>1</sub>, &hellip; ,
 * B<sub>n</sub>}:<br />
 * [A<sub>1</sub> &#8930; C] &and; &hellip; &and; [A<sub>m</sub> &#8930; C]
 * &rarr;</li>
 * 
 * <li>3. For every non-variable atom C of &Gamma; s.t. C &notin;
 * {A<sub>1</sub>, &hellip; A<sub>m</sub>, B<sub>1</sub>, &hellip;,
 * B<sub>n</sub>}:<br />
 * [A<sub>1</sub> &#8930; C] &and; &hellip; &and; [A<sub>m</sub> &#8930; C]
 * &rarr; [B<sub>j</sub> &#8930; C] for j = 1, &hellip;, n<br />
 * [B<sub>1</sub> &#8930; C] &and; &hellip; &and; [B<sub>n</sub> &#8930; C]
 * &rarr; [A<sub>i</sub> &#8930; C] for i = 1, &hellip;, m</li>
 * </ul>
 * </li>
 * 
 * <li>(2) Translation of the relevant properties of subsumption in <i>EL</i>.
 * 
 * <ul>
 * <li>1. For every pair of distinct concept constants A, B occurring in
 * &Gamma;, we say that A cannot be subsumed by B:<br />
 * &rarr; [A &#8930; B]</li>
 * 
 * <li>2. For every pair of distinct role names r, s and atoms
 * &exist;r<i>.</i>A, &exist;s<i>.</i>B of &Gamma;, we say that
 * &exist;r<i>.</i>A cannot be subsumed by &exist;s<i>.</i>B:<br />
 * &rarr; [&exist;r<i>.</i>A &#8930; &exist;s<i>.</i>B]</li>
 * 
 * <li>3. For every pair &exist;r<i>.</i>A, &exist;r<i>.</i>B of atoms of
 * &Gamma;, we say that &exist;r<i>.</i>A can only be subsumed by
 * &exist;r<i>.</i>B if A is already subsumed by B:<br />
 * [A &#8930; B] &rarr; [&exist;r<i>.</i>A &#8930; &exist;r<i>.</i>B]</li>
 * 
 * <li>4. For every concept constant A and every atom &exist;r<i>.</i>B of
 * &Gamma;, we say that A and &exist;r<i>.</i>B are not in a subsumption
 * relationship<br />
 * &rarr; [A &#8930; &exist;r<i>.</i>B] and &rarr; [&exist;r<i>.</i>B &#8930; A]
 * </li>
 * 
 * <li>5. Transitivity of subsumption is expressed using the non-Horn clauses:<br />
 * [C<sub>1</sub> &#8930; C<sub>3</sub>] &rarr; [C<sub>1</sub> &#8930;
 * C<sub>2</sub>] &or; [C<sub>2</sub> &#8930; C<sub>3</sub>] where
 * C<sub>1</sub>, C<sub>2</sub>, C<sub>3</sub> are atoms of &Gamma;.<br />
 * </li>
 * </ul>
 * Note that there are further properties that hold for subsumption in <i>EL</i>
 * (e.g., the fact that A &#8849; B implies &exist;r<i>.</i>A &#8849;
 * &exist;r<i>.</i>B), but that are not needed to ensure soundness of our
 * translation.</li>
 * 
 * <li>(3) Translation of the relevant properties of &gt;.
 * 
 * <ul>
 * <li>1. Transitivity and irreexivity of &gt; can be expressed using the Horn
 * clauses:<br />
 * [X &gt; X] &rarr; and [X &gt; Y] &and; [Y &gt; Z] &rarr; [X &gt; Z],<br />
 * where X, Y, Z are concept variables occurring in &Gamma;.</li>
 * 
 * <li>2. The connection between this order and the order &gt;<sub>&sigma;</sub>
 * is expressed using the non-Horn clauses:<br />
 * &rarr; [X &gt; Y] &or; [X &#8930; &exist;r<i>.</i>Y],<br />
 * where X, Y are concept variables occurring in &Gamma; and &exist;r<i>.</i>Y
 * is an atom of &Gamma;.</li>
 * </ul>
 * </li>
 * </ul>
 * </div>
 * 
 * @author Barbara Morawska
 * @author Julian Mendez
 */
public class Translator {

	private static final Logger logger = Logger.getLogger(Translator.class
			.getName());

	private Goal goal;

	private Integer identificator = 1;

	/**
	 * Identifiers are numbers, each number uniquely identifies a literal, i.e.
	 * a dissubsumption.
	 */
	private Map<Integer, Literal> identifiers = new HashMap<Integer, Literal>();

	private boolean invertLiteral = false;

	/**
	 * Literals are all dis-subsumptions between atoms in the goal the first
	 * hash map maps them to unique numbers.
	 */
	private Map<Literal, Integer> literals = new HashMap<Literal, Integer>();

	private Set<Integer> trueLiterals = new HashSet<Integer>();

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
		if (g == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		goal = g;
		setLiterals();
	}

	public Translator(Goal g, boolean inv) {
		if (g == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		goal = g;
		invertLiteral = inv;
		setLiterals();
	}

	/**
	 * This method encodes equations into propositional clauses in DIMACS CNF
	 * format, i.e. positive literal is represented by a positive number and a
	 * negative literal is represented by a corresponding negative number. Each
	 * clause is on one line. The end of a clause is marked by 0. Example of a
	 * clause in DIMACS format: 1 -3 0
	 */
	public SatInput computeSatInput() {
		SatInput ret = new SatInput();

		logger.finer("computing SAT input ...");

		logger.finer("running step 1 ...");
		ret.addAll(runStep1().getClauses());

		logger.finer("running step 2.1 ...");
		ret.addAll(runStep2_1().getClauses());

		logger.finer("running steps 2.2 and 2.3 ...");
		ret.addAll(runSteps2_2_N_2_3().getClauses());

		logger.finer("running step 2.4 ...");
		ret.addAll(runStep2_4().getClauses());

		logger.finer("running step 2.5 ...");
		ret.addAll(runStep2_5().getClauses());

		logger.finer("running step 3.1 reflexivity ...");
		ret.addAll(runStep3_1_r().getClauses());

		logger.finer("running step 3.1 transitivity ...");
		ret.addAll(runStep3_1_t().getClauses());

		logger.finer("running step 3.2 ...");
		ret.addAll(runStep3_2().getClauses());

		logger.finer("SAT input computed.");

		return ret;
	}

	private Set<Integer> createUpdate() {
		Set<Integer> ret = new HashSet<Integer>();
		Set<String> set = new HashSet<String>();
		set.addAll(goal.getConstants());
		set.addAll(goal.getEAtoms());
		for (String name : goal.getVariables()) {
			FAtom atom = goal.getAllAtoms().get(name);
			if (atom.isUserVariable()) {
				for (String otherName : set) {
					Literal literal = this.invertLiteral ? new SubsumptionLiteral(
							name, otherName) : new DissubsumptionLiteral(name,
							otherName);
					Integer literalId = literals.get(literal);
					ret.add(getLiteralValue(literalId) ? literalId : (-1)
							* literalId);
				}
			}
		}
		return ret;
	}

	/**
	 * Returns the literals.
	 * 
	 * @return the literals
	 */
	public Map<Literal, Integer> getLiterals() {
		return this.literals;
	}

	public boolean getLiteralValue(Integer literalId) {
		if (literalId == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		return this.trueLiterals.contains(literalId);
	}

	private int getMinusOrderLiteral(String left, String right) {
		return (-1) * getOrderLiteral(left, right);
	}

	private int getMinusSubOrDissubLiteral(String left, String right) {
		return (-1) * getSubOrDissubLiteral(left, right);
	}

	private int getOrderLiteral(String left, String right) {
		Literal literal = new OrderLiteral(left, right);
		return literals.get(literal);
	}

	private int getSubOrDissubLiteral(String left, String right) {
		Literal literal = invertLiteral ? new SubsumptionLiteral(left, right)
				: new DissubsumptionLiteral(left, right);
		int val = literals.get(literal);
		return invertLiteral ? (-1) * val : val;
	}

	public Set<Integer> getUpdate() {
		return Collections.unmodifiableSet(update);
	}

	private Set<Equation> getUpdatedUnifier() {
		Set<Equation> ret = new HashSet<Equation>();

		for (String variable : goal.getVariables()) {
			if (goal.getAllAtoms().get(variable).isUserVariable()) {

				Atom leftPart = goal.getAllAtoms().get(variable);
				Integer leftPartId = goal.getAtomManager().addAndGetIndex(
						leftPart);

				Set<Integer> rightPartIds = new HashSet<Integer>();
				Collection<FAtom> setOfSubsumers = goal.getAllAtoms()
						.get(variable).getSetOfSubsumers();
				for (FAtom subsumer : setOfSubsumers) {
					Atom newAtom = goal.getAllAtoms().get(subsumer.getId());
					rightPartIds.add(goal.getAtomManager().addAndGetIndex(
							newAtom));
				}

				ret.add(new Equation(leftPartId, rightPartIds, false));
			}
		}

		return ret;
	}

	/**
	 * Resets string update values for literals and S(X) for each X, before the
	 * next unifier is computed.
	 */
	public void reset() {

		update.clear();

		for (Integer key : identifiers.keySet()) {
			setLiteralValue(key, false);
		}

		for (String var : goal.getVariables()) {
			goal.getAllAtoms().get(var).resetSetOfSubsumers();
		}
	}

	/**
	 * Clauses created in Step 1
	 */
	private SatInput runStep1() {
		SatInput ret = new SatInput();

		Set<Equation> equations = new HashSet<Equation>();
		equations.addAll(goal.getEquations());
		equations.add(goal.getMainEquation());

		for (Equation e : equations) {
			ret.addAll(runStep1ForConstants(e).getClauses());
			ret.addAll(runStep1ForExistentialAtoms(e).getClauses());
		}

		return ret;
	}

	/**
	 * Step 1 for constants
	 * 
	 * @param e
	 *            equation
	 */
	private SatInput runStep1ForConstants(Equation e) {
		SatInput ret = new SatInput();

		String leftAtomName = goal.getAtomManager().get(e.getLeft()).getId();
		Set<String> rightAtomNames = new HashSet<String>();
		for (Integer atomId : e.getRight()) {
			rightAtomNames.add(goal.getAtomManager().get(atomId).getId());
		}

		for (String key1 : goal.getConstants()) {

			if (!leftAtomName.equals(key1) && !rightAtomNames.contains(key1)) {

				/*
				 * constant not in the equation
				 * 
				 * one side of an equation
				 */

				for (String key3 : rightAtomNames) {
					Set<Integer> clause = new HashSet<Integer>();

					String key2 = leftAtomName;
					clause.add(getMinusSubOrDissubLiteral(key2, key1));

					clause.add(getSubOrDissubLiteral(key3, key1));
					ret.add(clause);
				}

				/*
				 * another side of an equation
				 */

				String key2 = leftAtomName;
				Set<Integer> clause = new HashSet<Integer>();

				for (String key3 : rightAtomNames) {
					clause.add(getMinusSubOrDissubLiteral(key3, key1));
				}

				clause.add(getSubOrDissubLiteral(key2, key1));
				ret.add(clause);

			} else if (!leftAtomName.equals(key1)
					&& rightAtomNames.contains(key1)) {

				/*
				 * constant of the right but not on the left
				 */

				Set<Integer> clause = new HashSet<Integer>();

				String key2 = leftAtomName;
				clause.add(getMinusSubOrDissubLiteral(key2, key1));

				ret.add(clause);

			} else if (leftAtomName.equals(key1)
					&& !rightAtomNames.contains(key1)) {

				/*
				 * constant on the left but not on the right
				 */

				Set<Integer> clause = new HashSet<Integer>();

				for (String key3 : rightAtomNames) {
					clause.add(getMinusSubOrDissubLiteral(key3, key1));
				}
				ret.add(clause);
			}
		}

		return ret;
	}

	/**
	 * Step 1 for existential atoms
	 * 
	 * @param e
	 *            equation
	 */
	private SatInput runStep1ForExistentialAtoms(Equation e) {
		SatInput ret = new SatInput();

		String leftAtomName = goal.getAtomManager().get(e.getLeft()).getId();
		Set<String> rightAtomNames = new HashSet<String>();
		for (Integer atomId : e.getRight()) {
			rightAtomNames.add(goal.getAtomManager().get(atomId).getId());
		}

		for (String key1 : goal.getEAtoms()) {

			if (!leftAtomName.equals(key1) && !rightAtomNames.contains(key1)) {

				/*
				 * atom not in the equation
				 * 
				 * one side of equation
				 */

				for (String key3 : rightAtomNames) {
					Set<Integer> clause = new HashSet<Integer>();

					String key2 = leftAtomName;
					clause.add(getMinusSubOrDissubLiteral(key2, key1));

					clause.add(getSubOrDissubLiteral(key3, key1));
					ret.add(clause);
				}

				/*
				 * another side of the equation
				 */

				String key2 = leftAtomName;
				Set<Integer> clause = new HashSet<Integer>();

				for (String key3 : rightAtomNames) {
					clause.add(getMinusSubOrDissubLiteral(key3, key1));
				}
				clause.add(getSubOrDissubLiteral(key2, key1));
				ret.add(clause);

			} else if (!leftAtomName.equals(key1)
					&& rightAtomNames.contains(key1)) {

				/*
				 * existential atom on the right but not on the left side of an
				 * equation
				 */

				Set<Integer> clause = new HashSet<Integer>();

				String key2 = leftAtomName;
				clause.add(getMinusSubOrDissubLiteral(key2, key1));

				ret.add(clause);

				// end of outer if; key1 is not on the left, ask if it
				// is on the right
			} else if (leftAtomName.equals(key1)
					&& !rightAtomNames.contains(key1)) {

				/*
				 * existential atom on the left but not on the right side of
				 * equation
				 */

				Set<Integer> clause = new HashSet<Integer>();

				for (String key3 : rightAtomNames) {
					clause.add(getMinusSubOrDissubLiteral(key3, key1));
				}
				ret.add(clause);

			}
		}

		return ret;
	}

	/**
	 * Step 2.1
	 */
	private SatInput runStep2_1() {
		SatInput ret = new SatInput();

		for (String key1 : goal.getConstants()) {

			for (String key2 : goal.getConstants()) {

				if (!key2.equalsIgnoreCase(KRSSKeyword.top)
						&& (!key1.equals(key2))) {
					Set<Integer> clause = new HashSet<Integer>();

					clause.add(getSubOrDissubLiteral(key1, key2));

					ret.add(clause);
				}

			}

		}

		return ret;
	}

	/**
	 * Step 2.4
	 */
	private SatInput runStep2_4() {
		SatInput ret = new SatInput();

		for (String key1 : goal.getConstants()) {

			for (String key2 : goal.getEAtoms()) {
				{
					Set<Integer> clause = new HashSet<Integer>();

					clause.add(getSubOrDissubLiteral(key1, key2));

					ret.add(clause);
				}
				if (!key1.equalsIgnoreCase(KRSSKeyword.top)) {
					Set<Integer> clause = new HashSet<Integer>();

					clause.add(getSubOrDissubLiteral(key2, key1));

					ret.add(clause);

				}

			}

		}
		return ret;
	}

	/**
	 * Step 2.5
	 * 
	 * Transitivity of dis-subsumption
	 */
	private SatInput runStep2_5() {
		SatInput ret = new SatInput();

		for (String key1 : goal.getAllAtoms().keySet()) {

			for (String key2 : goal.getAllAtoms().keySet()) {

				if (!key1.equals(key2)) {
					for (String key3 : goal.getAllAtoms().keySet()) {

						if (!key1.equals(key3) && !key2.equals(key3)) {
							Set<Integer> clause = new HashSet<Integer>();

							clause.add(getSubOrDissubLiteral(key1, key2));
							clause.add(getSubOrDissubLiteral(key2, key3));
							clause.add(getMinusSubOrDissubLiteral(key1, key3));

							ret.add(clause);
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Step 3.1
	 * 
	 * Reflexivity for order literals
	 */
	private SatInput runStep3_1_r() {
		SatInput ret = new SatInput();

		for (String key1 : goal.getVariables()) {
			Set<Integer> clause = new HashSet<Integer>();

			clause.add(getMinusOrderLiteral(key1, key1));

			ret.add(clause);
		}
		return ret;
	}

	/**
	 * Step 3.1
	 * 
	 * Transitivity for order literals
	 */
	private SatInput runStep3_1_t() {
		SatInput ret = new SatInput();

		for (String key1 : goal.getVariables()) {

			for (String key2 : goal.getVariables()) {

				for (String key3 : goal.getVariables()) {

					if (!key1.equals(key2) && !key2.equals(key3)) {

						Set<Integer> clause = new HashSet<Integer>();

						clause.add(getMinusOrderLiteral(key1, key2));
						clause.add(getMinusOrderLiteral(key2, key3));
						clause.add(getOrderLiteral(key1, key3));

						ret.add(clause);
					}

				}

			}

		}
		return ret;
	}

	/**
	 * Step 3.2 Disjunction between order literals and dis-subsumption
	 */
	private SatInput runStep3_2() {
		SatInput ret = new SatInput();

		for (String key1 : goal.getEAtoms()) {

			FAtom eatom = goal.getAllAtoms().get(key1);
			FAtom child = eatom.getChild();

			if (child.isVariable()) {

				for (String key2 : goal.getVariables()) {

					Set<Integer> clause = new HashSet<Integer>();

					clause.add(getOrderLiteral(key2, child.getName()));

					clause.add(getSubOrDissubLiteral(key2, key1));

					ret.add(clause);
				}
			}
		}

		return ret;
	}

	/**
	 * Step 2.2 and Step 2.3
	 */
	private SatInput runSteps2_2_N_2_3() {
		SatInput ret = new SatInput();

		for (String key1 : goal.getEAtoms()) {

			for (String key2 : goal.getEAtoms()) {

				if (!key1.equals(key2)) {

					String role1 = goal.getAllAtoms().get(key1).getName();
					String role2 = goal.getAllAtoms().get(key2).getName();

					/*
					 * if roles are not equal, then Step 2.2
					 */

					if (!role1.equals(role2)) {
						Set<Integer> clause = new HashSet<Integer>();

						clause.add(getSubOrDissubLiteral(key1, key2));

						ret.add(clause);

						/*
						 * if the roles are equal, then clause in Step 2.3
						 */
					} else {

						FAtom child1 = goal.getAllAtoms().get(key1).getChild();
						FAtom child2 = goal.getAllAtoms().get(key2).getChild();

						String child1name = child1.getName();
						String child2name = child2.getName();

						if (!child1name.equals(child2name)) {
							Set<Integer> clause = new HashSet<Integer>();

							clause.add(getMinusSubOrDissubLiteral(child1name,
									child2name));
							clause.add(getSubOrDissubLiteral(key1, key2));

							ret.add(clause);
						}

					}

				}

			}

		}
		return ret;
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

				literals.put(literal, identificator);
				identifiers.put(identificator, literal);
				identificator++;
			}
		}

		/*
		 * 
		 * Literals for order on variables
		 */

		for (String key1 : goal.getVariables()) {

			first = key1;

			for (String key2 : goal.getVariables()) {

				second = key2;

				literal = new OrderLiteral(first, second);

				literals.put(literal, identificator);
				identifiers.put(identificator, literal);
				identificator++;

			}

		}
	}

	public void setLiteralValue(Integer literalId, boolean value) {
		if (literalId == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		if (value) {
			this.trueLiterals.add(literalId);
		} else if (this.trueLiterals.contains(literalId)) {
			this.trueLiterals.remove(literalId);
		}
	}

	private void setValuesForLiterals(Set<Integer> val) {
		if (val == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		for (Integer currentLiteral : val) {
			if (currentLiteral < 0) {
				Integer i = (-1) * currentLiteral;
				Literal literal = identifiers.get(i);
				if (literal.isDissubsumption() || literal.isSubsumption()) {
					setLiteralValue(i, false);
				}
			} else if (currentLiteral > 0) {
				setLiteralValue(currentLiteral, true);
			}
		}
	}

	/**
	 * Updates the translator with the SAT solver output, returning a new
	 * unifier.
	 * 
	 * @param val
	 *            SAT solver output
	 * @return a new unifier.
	 */
	public Set<Equation> toTBox(Set<Integer> val) {
		setValuesForLiterals(val);
		updateTBox();
		Set<Integer> newClause = createUpdate();
		Set<Integer> invertedClause = new TreeSet<Integer>();
		for (Integer e : newClause) {
			invertedClause.add((-1) * e);
		}
		update = invertedClause;
		Set<Equation> ret = getUpdatedUnifier();
		return Collections.unmodifiableSet(ret);
	}

	private void updateTBox() {
		/*
		 * Define S_X for each variable X
		 */

		for (Integer i : identifiers.keySet()) {

			String name1 = identifiers.get(i).getFirst();
			String name2 = identifiers.get(i).getSecond();

			if (identifiers.get(i).isDissubsumption()) {

				if (!getLiteralValue(i)) {

					if (goal.getVariables().contains(name1)) {
						if (goal.getConstants().contains(name2)) {

							goal.getAllAtoms()
									.get(name1)
									.addToSetOfSubsumers(
											goal.getAllAtoms().get(name2));

						} else if (goal.getEAtoms().contains(name2)) {

							goal.getAllAtoms()
									.get(name1)
									.addToSetOfSubsumers(
											goal.getAllAtoms().get(name2));
						}
					}
				}
			} else if (identifiers.get(i).isSubsumption()) {
				if (getLiteralValue(i)) {

					if (goal.getVariables().contains(name1)) {
						if (goal.getConstants().contains(name2)) {

							goal.getAllAtoms()
									.get(name1)
									.addToSetOfSubsumers(
											goal.getAllAtoms().get(name2));

						} else if (goal.getEAtoms().contains(name2)) {

							goal.getAllAtoms()
									.get(name1)
									.addToSetOfSubsumers(
											goal.getAllAtoms().get(name2));

						}
					}

				}
			}
		}
	}

}
