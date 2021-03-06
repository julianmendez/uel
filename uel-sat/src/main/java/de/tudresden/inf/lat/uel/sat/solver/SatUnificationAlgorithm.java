package de.tudresden.inf.lat.uel.sat.solver;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import de.tudresden.inf.lat.uel.sat.literals.Choice;
import de.tudresden.inf.lat.uel.sat.literals.Literal;
import de.tudresden.inf.lat.uel.sat.literals.OrderLiteral;
import de.tudresden.inf.lat.uel.sat.literals.SubsumptionLiteral;
import de.tudresden.inf.lat.uel.sat.type.SatInput;
import de.tudresden.inf.lat.uel.sat.type.SatOutput;
import de.tudresden.inf.lat.uel.sat.type.Solver;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Disequation;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.Subsumption;
import de.tudresden.inf.lat.uel.type.api.UnificationAlgorithm;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;
import de.tudresden.inf.lat.uel.type.impl.IndexedSetImpl;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

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
 * <li>(1) Translation of the equations of &Gamma;. For every equation A
 * <sub>1</sub> \u2293 &hellip; \u2293 A<sub>m</sub> &equiv;<sup>?</sup> B
 * <sub>1</sub> \u2293 &hellip; \u2293 B<sub>n</sub> of &Gamma;, we create the
 * following Horn clauses, which express that any atom that occurs as a
 * top-level conjunct on one side of an equivalence must subsume a top-level
 * conjunct on the other side:
 *
 * <ul>
 * <li>1. For every non-variable atom C &isin; {A<sub>1</sub>, &hellip; , A
 * <sub>m</sub>}:<br>
 * [B<sub>1</sub> \u22E2 C] &and; &hellip; &and; [B<sub>n</sub> \u22E2 C] &rarr;
 * </li>
 *
 * <li>2. For every non-variable atom C &isin; {B<sub>1</sub>, &hellip; , B
 * <sub>n</sub>}:<br>
 * [A<sub>1</sub> \u22E2 C] &and; &hellip; &and; [A<sub>m</sub> \u22E2 C] &rarr;
 * </li>
 *
 * <li>3. For every non-variable atom C of &Gamma; s.t. C &notin; {A<sub>1</sub>
 * , &hellip; A<sub>m</sub>, B<sub>1</sub>, &hellip;, B<sub>n</sub>}:<br>
 * [A<sub>1</sub> \u22E2 C] &and; &hellip; &and; [A<sub>m</sub> \u22E2 C] &rarr;
 * [B<sub>j</sub> \u22E2 C] for j = 1, &hellip;, n<br>
 * [B<sub>1</sub> \u22E2 C] &and; &hellip; &and; [B<sub>n</sub> \u22E2 C] &rarr;
 * [A<sub>i</sub> \u22E2 C] for i = 1, &hellip;, m</li>
 * </ul>
 * </li>
 *
 * <li>(2) Translation of the relevant properties of subsumption in <i>EL</i>.
 *
 * <ul>
 * <li>1. For every pair of distinct concept constants A, B occurring in
 * &Gamma;, we say that A cannot be subsumed by B:<br>
 * &rarr; [A \u22E2 B]</li>
 *
 * <li>2. For every pair of distinct role names r, s and atoms &exist;r<i>.</i>
 * A, &exist;s<i>.</i>B of &Gamma;, we say that &exist;r<i>.</i>A cannot be
 * subsumed by &exist;s<i>.</i>B:<br>
 * &rarr; [&exist;r<i>.</i>A \u22E2 &exist;s<i>.</i>B]</li>
 *
 * <li>3. For every pair &exist;r<i>.</i>A, &exist;r<i>.</i>B of atoms of
 * &Gamma;, we say that &exist;r<i>.</i>A can only be subsumed by &exist;r
 * <i>.</i>B if A is already subsumed by B:<br>
 * [A \u22E2 B] &rarr; [&exist;r<i>.</i>A \u22E2 &exist;r<i>.</i>B]</li>
 *
 * <li>4. For every concept constant A and every atom &exist;r<i>.</i>B of
 * &Gamma;, we say that A and &exist;r<i>.</i>B are not in a subsumption
 * relationship<br>
 * &rarr; [A \u22E2 &exist;r<i>.</i>B] and &rarr; [&exist;r<i>.</i>B \u22E2 A]
 * </li>
 *
 * <li>5. Transitivity of subsumption is expressed using the non-Horn clauses:
 * <br>
 * [C<sub>1</sub> \u22E2 C<sub>3</sub>] &rarr; [C<sub>1</sub> \u22E2 C
 * <sub>2</sub>] &or; [C<sub>2</sub> \u22E2 C<sub>3</sub>] where C<sub>1</sub>,
 * C<sub>2</sub>, C<sub>3</sub> are atoms of &Gamma;.<br>
 * </li>
 * </ul>
 * Note that there are further properties that hold for subsumption in <i>EL</i>
 * (e.g., the fact that A \u2291 B implies &exist;r<i>.</i>A \u2291 &exist;r
 * <i>.</i>B), but that are not needed to ensure soundness of our translation.
 * </li>
 *
 * <li>(3) Translation of the relevant properties of &gt;.
 *
 * <ul>
 * <li>1. Transitivity and irreexivity of &gt; can be expressed using the Horn
 * clauses:<br>
 * [X &gt; X] &rarr; and [X &gt; Y] &and; [Y &gt; Z] &rarr; [X &gt; Z],<br>
 * where X, Y, Z are concept variables occurring in &Gamma;.</li>
 *
 * <li>2. The connection between this order and the order &gt;<sub>&sigma;</sub>
 * is expressed using the non-Horn clauses:<br>
 * &rarr; [X &gt; Y] &or; [X \u22E2 &exist;r<i>.</i>Y],<br>
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
public class SatUnificationAlgorithm implements UnificationAlgorithm {

	private static final String keyConfiguration = "Configuration";
	private static final String keyName = "Name";
	private static final String keyNumberOfClauses = "Number of clauses";
	private static final String keyNumberOfPropositions = "Number of propositions";
	private static final String keyNumberOfVariables = "Number of variables";
	private static final Logger logger = Logger.getLogger(SatUnificationAlgorithm.class.getName());
	private static final String notUsingMinimalAssignments = "all local assignments";
	private static final String algorithmName = "SAT-based algorithm";
	private static final String usingMinimalAssignments = "only minimal assignments";

	private boolean firstTime = true;
	private final IndexedSet<Literal> literalManager = new IndexedSetImpl<>();
	private long numberOfClauses = 0;
	private final boolean onlyMinimalAssignments;
	private Unifier result;
	private Solver solver;
	private final Map<Integer, Set<Integer>> subsumers = new HashMap<>();
	private final Set<Integer> trueLiterals = new HashSet<>();
	private final Set<Integer> nonVariableAtoms = new HashSet<>();
	private final Set<Integer> usedAtomIds = new HashSet<>();
	private final Goal goal;
	private Set<Integer> update = new HashSet<>();

	public SatUnificationAlgorithm(Goal goal, boolean useMinimalAssignments) {
		if (goal == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.goal = goal;
		this.nonVariableAtoms.addAll(goal.getAtomManager().getConstants());
		this.nonVariableAtoms.addAll(goal.getAtomManager().getExistentialRestrictions());
		this.usedAtomIds.addAll(nonVariableAtoms);
		this.usedAtomIds.addAll(goal.getAtomManager().getVariables());
		this.onlyMinimalAssignments = useMinimalAssignments;
		setLiterals();
	}

	private void addClausesForDisunification(SatInput input) throws InterruptedException {
		for (Integer atomId : getUsedAtomIds()) {
			for (Integer varId : getVariables()) {
				runStep1DissubsumptionVariable(Collections.singleton(getSubsumptionLiteral(atomId, varId)),
						Collections.singleton(atomId), varId, input);
			}
		}
	}

	private boolean addEntry(List<Entry<String, String>> list, String key, String value) {
		return list.add(new SimpleEntry<String, String>(key, value));
	}

	private boolean addToSetOfSubsumers(Integer atomId1, Integer atomId2) {
		Set<Integer> ret = subsumers.get(atomId1);
		if (ret == null) {
			ret = new HashSet<>();
			subsumers.put(atomId1, ret);
		}
		return ret.add(atomId2);
	}

	@Override
	public void cleanup() {
		if (solver != null) {
			solver.cleanup();
		}
	}

	@Override
	public boolean computeNextUnifier() throws InterruptedException {
		SatOutput satoutput = null;
		boolean unifiable = false;
		try {
			if (this.firstTime) {
				if (this.onlyMinimalAssignments) {
					this.solver = new Sat4jMaxSatSolver();
				} else {
					this.solver = new Sat4jSolver();
				}
				SatInput satInput = computeSatInput();
				//// DEBUG
				// StringBuffer sbuf = new StringBuffer();
				// for (Set<Integer> clause : satInput.getClauses()) {
				// for (Integer literalId : clause) {
				// if (literalId < 0) {
				// sbuf.append("-");
				// }
				// Literal literal = literalManager.get(Math.abs(literalId));
				// sbuf.append("[");
				// if (literal == null) {
				// sbuf.append("NULL");
				// } else if (literal instanceof OrderLiteral) {
				// OrderLiteral ol = (OrderLiteral) literal;
				// Integer f = ol.getFirst();
				// Integer s = ol.getSecond();
				// appendAtom(sbuf, f);
				// sbuf.append(" > ");
				// appendAtom(sbuf, s);
				// } else if (literal instanceof SubsumptionLiteral) {
				// SubsumptionLiteral sl = (SubsumptionLiteral) literal;
				// Integer f = sl.getFirst();
				// Integer s = sl.getSecond();
				// appendAtom(sbuf, f);
				// sbuf.append(" ⊑ ");
				// appendAtom(sbuf, s);
				// } else if (literal instanceof ChoiceLiteral) {
				// sbuf.append("c");
				// sbuf.append(((ChoiceLiteral) literal).hashCode());
				// }
				// sbuf.append("] ");
				// }
				// sbuf.append(Solver.NEWLINE);
				// }
				this.numberOfClauses = satInput.getClauses().size();
				satoutput = this.solver.solve(satInput);
				unifiable = satoutput.isSatisfiable();
			} else {
				Set<Integer> update = getUpdate();
				if (update.isEmpty()) {
					unifiable = false;
				} else {
					this.numberOfClauses++;
					satoutput = this.solver.update(update);
					unifiable = satoutput.isSatisfiable();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		reset();
		if (unifiable) {
			this.result = new Unifier(toDefinitions(satoutput.getOutput()));
		} else {
			// release resources used by the solver after all unifiers have been
			// computed
			solver.cleanup();
		}

		this.firstTime = false;
		return unifiable;
	}

	//// DEBUG
	// private void appendAtom(StringBuffer sbuf, Integer atomId) {
	// if (goal.getAtomManager().getExistentialRestrictions().contains(atomId))
	// {
	// sbuf.append("∃");
	// sbuf.append(shortForm(goal.getAtomManager().printRoleName(atomId)));
	// sbuf.append(".");
	// appendAtom(sbuf, goal.getAtomManager().getChild(atomId));
	// } else {
	// sbuf.append(shortForm(goal.getAtomManager().printConceptName(atomId)));
	// }
	// }
	//
	// private String shortForm(String iri) {
	// String[] parts = iri.split("#");
	// return parts[parts.length - 1];
	// }

	/**
	 * This method encodes equations into propositional clauses in DIMACS CNF
	 * format, i.e. positive literal is represented by a positive number and a
	 * negative literal is represented by a corresponding negative number. Each
	 * clause is on one line. The end of a clause is marked by 0. Example of a
	 * clause in DIMACS format: 1 -3 0
	 *
	 * @return an object representing the DIMACS CNF encoding of the input
	 *         subsumptions
	 * 
	 * @throws InterruptedException
	 *             if the process is interrupted
	 */
	private SatInput computeSatInput() throws InterruptedException {
		SatInput ret = new SatInput();

		logger.finer("computing SAT input ...");

		logger.finer("running step 1 ...");
		runStep1(ret);

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		logger.finer("running step 2.1 ...");
		runStep2_1(ret);

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		logger.finer("running steps 2.2 and 2.3 ...");
		runSteps2_2_N_2_3(ret);

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		logger.finer("running step 2.4 ...");
		runStep2_4(ret);

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		logger.finer("running step 2.5 ...");
		runStep2_5(ret);

		logger.finer("running step 3.1 reflexivity ...");
		runStep3_1_r(ret);

		logger.finer("running step 3.1 transitivity ...");
		runStep3_1_t(ret);

		logger.finer("running step 3.2 ...");
		runStep3_2(ret);

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		if (goal.hasNegativePart()) {
			// add clauses with auxiliary variables needed for soundness of
			// disunification
			logger.finer("adding clauses for dissubsumptions ...");
			addClausesForDisunification(ret);
		}

		if (this.onlyMinimalAssignments) {
			logger.finer("adding literals to be minimized ...");
			for (Integer varId : getUserVariables()) {
				for (Integer atomId : getNonVariableAtoms()) {
					ret.addMinimizeLiteral(getSubsumptionLiteral(varId, atomId));
				}
			}
		}

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		logger.finer("SAT input computed.");

		return ret;
	}

	private void createUpdate() {
		for (Integer firstAtomId : getUserVariables()) {
			for (Integer secondAtomId : getNonVariableAtoms()) {
				Integer literalId = getSubsumptionLiteral(firstAtomId, secondAtomId);
				if (!this.onlyMinimalAssignments || getLiteralValue(literalId)) {
					update.add(getLiteralValue(literalId) ? (-1) * literalId : literalId);
				}
			}
		}
	}

	private Set<Integer> getConstants() {
		return goal.getAtomManager().getConstants();
	}

	private Set<Integer> getExistentialRestrictions() {
		return goal.getAtomManager().getExistentialRestrictions();
	}

	private Set<Integer> getNonVariableAtoms() {
		return nonVariableAtoms;
	}

	@Override
	public List<Entry<String, String>> getInfo() {
		List<Entry<String, String>> ret = new ArrayList<>();
		addEntry(ret, keyName, algorithmName);

		if (this.onlyMinimalAssignments) {
			addEntry(ret, keyConfiguration, usingMinimalAssignments);
		} else {
			addEntry(ret, keyConfiguration, notUsingMinimalAssignments);
		}

		if (this.literalManager != null) {
			addEntry(ret, keyNumberOfPropositions, "" + this.literalManager.size());
		}
		addEntry(ret, keyNumberOfClauses, "" + this.numberOfClauses);
		addEntry(ret, keyNumberOfVariables, "" + getVariables().size());
		return Collections.unmodifiableList(ret);
	}

	@Override
	public Goal getGoal() {
		return goal;
	}

	private boolean getLiteralValue(Integer literalId) {
		if (literalId == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		return this.trueLiterals.contains(literalId);
	}

	private Integer getMinusOrderLiteral(Integer atomId1, Integer atomId2) {
		return (-1) * getOrderLiteral(atomId1, atomId2);
	}

	private Integer getSubsumptionLiteral(Integer atomId1, Integer atomId2) {
		Literal literal = new SubsumptionLiteral(atomId1, atomId2);
		return literalManager.addAndGetIndex(literal);
	}

	private Integer getOrderLiteral(Integer atomId1, Integer atomId2) {
		Literal literal = new OrderLiteral(atomId1, atomId2);
		return this.literalManager.addAndGetIndex(literal);
	}

	private Set<Integer> getSetOfSubsumers(Integer atomId) {
		Set<Integer> list = subsumers.get(atomId);
		if (list == null) {
			list = new HashSet<>();
			subsumers.put(atomId, list);
		}
		return list;
	}

	private Integer getMinusSubsumptionLiteral(Integer atomId1, Integer atomId2) {
		return (-1) * getSubsumptionLiteral(atomId1, atomId2);
	}

	@Override
	public Unifier getUnifier() {
		return result;
	}

	private Set<Integer> getUpdate() {
		return Collections.unmodifiableSet(this.update);
	}

	private Set<Definition> getUpdatedDefinitions() {
		Set<Definition> definitions = new HashSet<>();
		for (Integer leftPartId : getVariables()) {
			definitions.add(new Definition(leftPartId, new HashSet<>(getSetOfSubsumers(leftPartId)), false));
		}
		return definitions;
	}

	private Set<Integer> getUsedAtomIds() {
		return usedAtomIds;
	}

	private Set<Integer> getVariables() {
		return goal.getAtomManager().getVariables();
	}

	private Set<Integer> getUserVariables() {
		return goal.getAtomManager().getUserVariables();
	}

	/**
	 * Resets string update values for literals and S(X) for each X, before the
	 * next unifier is computed.
	 */
	public void reset() {

		update = new HashSet<>();

		for (Integer key : literalManager.getIndices()) {
			setLiteralValue(key, false);
		}

		for (Integer atomId : getVariables()) {
			resetSetOfSubsumers(atomId);
		}
	}

	private void resetSetOfSubsumers(Integer atomId) {
		Set<Integer> list = subsumers.get(atomId);
		if (list == null) {
			list = new HashSet<>();
			subsumers.put(atomId, list);
		}
		list.clear();
	}

	/**
	 * Clauses created in Step 1
	 * 
	 * @param input
	 *            input
	 */
	private void runStep1(SatInput input) {
		// encode positive part of the goal
		for (Definition d : goal.getDefinitions()) {
			runStep1(d, input);
		}
		for (Equation e : goal.getEquations()) {
			runStep1(e, input);
		}
		for (Subsumption s : goal.getSubsumptions()) {
			runStep1(s, input);
		}
		// negative part
		for (Disequation e : goal.getDisequations()) {
			runStep1(e, input);
		}
		for (Dissubsumption s : goal.getDissubsumptions()) {
			runStep1(s, input);
		}
	}

	private void runStep1(Definition d, SatInput input) {
		runStep1(new Subsumption(d.getLeft(), d.getRight()), input);
		if (!d.isPrimitive()) {
			runStep1(new Subsumption(d.getRight(), d.getLeft()), input);
		}
	}

	private void runStep1(Equation e, SatInput input) {
		runStep1(new Subsumption(e.getLeft(), e.getRight()), input);
		runStep1(new Subsumption(e.getRight(), e.getLeft()), input);
	}

	private void runStep1(Subsumption s, SatInput input) {
		for (Integer rightId : s.getRight()) {
			if (getVariables().contains(rightId)) {
				runStep1SubsumptionVariable(s.getLeft(), rightId, input);
			} else if (getNonVariableAtoms().contains(rightId)) {
				runStep1SubsumptionNonVariableAtom(s.getLeft(), rightId, input);
			}
			// if top is on the right-hand side, do nothing
		}
	}

	private void runStep1SubsumptionVariable(Set<Integer> leftIds, Integer rightId, SatInput input) {
		for (Integer atomId : getNonVariableAtoms()) {
			if (!leftIds.contains(atomId)) {
				Set<Integer> clause = new HashSet<>();
				clause.add(getMinusSubsumptionLiteral(rightId, atomId));
				for (Integer leftId : leftIds) {
					clause.add(getSubsumptionLiteral(leftId, atomId));
				}
				input.add(clause);
			}
		}
	}

	private void runStep1SubsumptionNonVariableAtom(Set<Integer> leftIds, Integer rightId, SatInput input) {
		Set<Integer> clause = new HashSet<>();
		for (Integer leftId : leftIds) {
			clause.add(getSubsumptionLiteral(leftId, rightId));
		}
		input.add(clause);
	}

	private void runStep1(Disequation e, SatInput input) {
		// choose which direction of the equation does not hold
		Choice c = new Choice(literalManager, 2);
		runStep1(c.getChoiceLiterals(0), new Dissubsumption(e.getLeft(), e.getRight()), input);
		runStep1(c.getChoiceLiterals(1), new Dissubsumption(e.getRight(), e.getLeft()), input);
	}

	private void runStep1(Dissubsumption e, SatInput input) {
		runStep1(Collections.<Integer> emptySet(), e, input);
	}

	private void runStep1(Set<Integer> choiceLiterals, Dissubsumption e, SatInput input) {
		if (e.getRight().size() == 0) {
			input.add(choiceLiterals);
		} else if (e.getRight().size() == 1) {
			runStep1Dissubsumption(choiceLiterals, e.getLeft(), e.getRight().iterator().next(), input);
		} else {
			// choose which of the right-hand side atoms does not subsume the
			// left-hand side
			Choice c = new Choice(literalManager, e.getRight().size());
			int j = 0;
			for (Integer rightId : e.getRight()) {
				runStep1Dissubsumption(c.addChoiceLiterals(choiceLiterals, j), e.getLeft(), rightId, input);
				j++;
			}
			c.ruleOutOtherChoices(input);
		}
	}

	private void runStep1Dissubsumption(Set<Integer> choiceLiterals, Set<Integer> leftIds, Integer rightId,
			SatInput input) {
		if (getVariables().contains(rightId))
			runStep1DissubsumptionVariable(choiceLiterals, leftIds, rightId, input);
		else {
			// 'rightId' is a non-variable atom --> it should not subsume any of
			// the leftIds
			runStep1DissubsumptionNonVariableAtom(choiceLiterals, leftIds, rightId, input);
		}
	}

	private void runStep1DissubsumptionVariable(Set<Integer> choiceLiterals, Set<Integer> leftIds, Integer rightId,
			SatInput input) {
		// choose which non-variable atom solves the dissubsumption
		Choice c = new Choice(literalManager, getNonVariableAtoms().size());
		int j = 0;
		for (Integer atomId : getNonVariableAtoms()) {
			Set<Integer> currentChoiceLiterals = c.addChoiceLiterals(choiceLiterals, j);

			// Under the current choice, 'rightId' is subsumed by 'atomId'
			// ...
			Set<Integer> clause = new HashSet<>(currentChoiceLiterals);
			clause.add(getSubsumptionLiteral(rightId, atomId));
			input.add(clause);

			// ... and 'atomId' does not subsume any of the 'leftIds'.
			runStep1DissubsumptionNonVariableAtom(currentChoiceLiterals, leftIds, atomId, input);

			// next choice
			j++;
		}
		c.ruleOutOtherChoices(input);
	}

	private void runStep1DissubsumptionNonVariableAtom(Set<Integer> choiceLiterals, Set<Integer> leftIds,
			Integer rightId, SatInput input) {
		Set<Integer> clause;
		for (Integer leftId : leftIds) {
			clause = new HashSet<>(choiceLiterals);
			clause.add(getMinusSubsumptionLiteral(leftId, rightId));
			input.add(clause);
		}
	}

	/**
	 * Step 2.1
	 * 
	 * @param input
	 *            input
	 */
	private void runStep2_1(SatInput input) {
		for (Integer atomId1 : getConstants()) {

			for (Integer atomId2 : getConstants()) {
				if (!atomId1.equals(atomId2)) {
					Set<Integer> clause = new HashSet<>();
					clause.add(getMinusSubsumptionLiteral(atomId1, atomId2));
					input.add(clause);
				}
			}

			if (goal.hasNegativePart()) {
				// positive clause needed for soundness of disunification
				Set<Integer> clause = new HashSet<>();
				clause.add(getSubsumptionLiteral(atomId1, atomId1));
				input.add(clause);
			}

		}
	}

	/**
	 * Step 2.4
	 * 
	 * @param input
	 *            input
	 */
	private void runStep2_4(SatInput input) {
		for (Integer atomId1 : getConstants()) {

			for (Integer atomId2 : getExistentialRestrictions()) {
				{
					Set<Integer> clause = new HashSet<>();
					clause.add(getMinusSubsumptionLiteral(atomId1, atomId2));
					input.add(clause);
				}

				Set<Integer> clause = new HashSet<>();
				clause.add(getMinusSubsumptionLiteral(atomId2, atomId1));
				input.add(clause);

			}

		}
	}

	/**
	 * Step 2.5
	 *
	 * Transitivity of dis-subsumption
	 * 
	 * @param input
	 *            input
	 * @throws InterruptedException
	 *             if the thread was interrupted
	 */
	private void runStep2_5(SatInput input) throws InterruptedException {
		Collection<Integer> atomIds = getUsedAtomIds();

		for (Integer atomId1 : atomIds) {

			for (Integer atomId2 : atomIds) {

				if (!atomId1.equals(atomId2)) {
					for (Integer atomId3 : atomIds) {

						if (!atomId1.equals(atomId3) && !atomId2.equals(atomId3)) {
							Set<Integer> clause = new HashSet<>();
							clause.add(getMinusSubsumptionLiteral(atomId1, atomId2));
							clause.add(getMinusSubsumptionLiteral(atomId2, atomId3));
							clause.add(getSubsumptionLiteral(atomId1, atomId3));
							input.add(clause);
						}
					}
				}
			}

			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
		}
	}

	/**
	 * Step 3.1
	 *
	 * Reflexivity for order literals
	 * 
	 * @param input
	 *            input
	 * 
	 */
	private void runStep3_1_r(SatInput input) {
		for (Integer atomId1 : getVariables()) {
			Set<Integer> clause = new HashSet<>();
			clause.add(getMinusOrderLiteral(atomId1, atomId1));
			input.add(clause);
		}
	}

	/**
	 * Step 3.1
	 *
	 * Transitivity for order literals
	 * 
	 * @param input
	 *            input
	 * @throws InterruptedException
	 *             if the thread was interrupted
	 * 
	 */
	private void runStep3_1_t(SatInput input) throws InterruptedException {
		for (Integer atomId1 : getVariables()) {

			for (Integer atomId2 : getVariables()) {

				for (Integer atomId3 : getVariables()) {

					if (!atomId1.equals(atomId2) && !atomId2.equals(atomId3)) {

						Set<Integer> clause = new HashSet<>();
						clause.add(getMinusOrderLiteral(atomId1, atomId2));
						clause.add(getMinusOrderLiteral(atomId2, atomId3));
						clause.add(getOrderLiteral(atomId1, atomId3));
						input.add(clause);
					}

				}

			}

			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

		}
	}

	/**
	 * Step 3.2 Disjunction between order literals and dis-subsumption
	 * 
	 * @param input
	 *            input
	 */
	private void runStep3_2(SatInput input) {
		for (Integer atomId1 : getExistentialRestrictions()) {

			Integer childId = goal.getAtomManager().getChild(atomId1);

			if (getVariables().contains(childId)) {
				for (Integer atomId2 : getVariables()) {
					Set<Integer> clause = new HashSet<>();
					clause.add(getOrderLiteral(atomId2, childId));
					clause.add(getMinusSubsumptionLiteral(atomId2, atomId1));
					input.add(clause);
				}
			}
		}
	}

	/**
	 * Step 2.2 and Step 2.3
	 * 
	 * @param input
	 *            input
	 */
	private void runSteps2_2_N_2_3(SatInput input) {
		for (Integer atomId1 : getExistentialRestrictions()) {

			for (Integer atomId2 : getExistentialRestrictions()) {

				if (!atomId1.equals(atomId2)) {

					ExistentialRestriction ex1 = goal.getAtomManager().getExistentialRestriction(atomId1);
					ExistentialRestriction ex2 = goal.getAtomManager().getExistentialRestriction(atomId2);
					Integer role1 = ex1.getRoleId();
					Integer role2 = ex2.getRoleId();

					/*
					 * if roles are not equal, then Step 2.2
					 */

					if (!role1.equals(role2)) {
						Set<Integer> clause = new HashSet<>();
						clause.add(getMinusSubsumptionLiteral(atomId1, atomId2));
						input.add(clause);

						/*
						 * if the roles are equal, then clause in Step 2.3
						 */
					} else {

						Integer child1 = goal.getAtomManager().getChild(atomId1);
						Integer child2 = goal.getAtomManager().getChild(atomId2);

						if (!child1.equals(child2)) {
							Set<Integer> clause = new HashSet<>();
							clause.add(getSubsumptionLiteral(child1, child2));
							clause.add(getMinusSubsumptionLiteral(atomId1, atomId2));
							input.add(clause);
						}

						if (goal.hasNegativePart()) {
							// converse clause needed for soundness of
							// disunification
							Set<Integer> clause = new HashSet<>();
							clause.add(getSubsumptionLiteral(atomId1, atomId2));
							clause.add(getMinusSubsumptionLiteral(child1, child2));
							input.add(clause);
						}

					}

				}

			}

			if (goal.hasNegativePart()) {
				// converse clause (as above) for trival subsumption
				// between an existential restriction and itself
				Set<Integer> clause = new HashSet<>();
				clause.add(getSubsumptionLiteral(atomId1, atomId1));
				input.add(clause);
			}

		}
	}

	/**
	 * Creates dis-subsumptions and order literals from all pairs of atoms of
	 * the goal
	 */
	private void setLiterals() {
		// TODO is this necessary?

		/*
		 * Literals for dis-subsumptions
		 */

		for (Integer atomId1 : getUsedAtomIds()) {
			for (Integer atomId2 : getUsedAtomIds()) {
				getSubsumptionLiteral(atomId1, atomId2);
			}
		}

		/*
		 * 
		 * Literals for order on variables
		 */

		for (Integer atomId1 : getVariables()) {
			for (Integer atomId2 : getVariables()) {
				getOrderLiteral(atomId1, atomId2);
			}
		}
	}

	private void setLiteralValue(Integer literalId, boolean value) {
		if (literalId == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		if (value) {
			this.trueLiterals.add(literalId);
		} else {
			this.trueLiterals.remove(literalId);
		}
	}

	private void setValuesForLiterals(Set<Integer> val) {
		if (val == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		for (Integer literalId : val) {
			boolean value = true;
			if (literalId < 0) {
				literalId = (-1) * literalId;
				value = false;
			}
			Literal literal = literalManager.get(literalId);
			if (literal.isSubsumption()) {
				setLiteralValue(literalId, value);
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
	public Set<Definition> toDefinitions(Set<Integer> val) {
		setValuesForLiterals(val);
		updateTBox();
		createUpdate();
		Set<Definition> ret = getUpdatedDefinitions();
		return ret;
	}

	private void updateTBox() {
		/*
		 * Define S_X for each variable X
		 */

		for (Integer i : this.literalManager.getIndices()) {

			if (this.literalManager.get(i).isSubsumption()) {
				if (getLiteralValue(i)) {

					Integer atomId1 = this.literalManager.get(i).getFirst();
					Integer atomId2 = this.literalManager.get(i).getSecond();
					if (getVariables().contains(atomId1)) {
						if (getNonVariableAtoms().contains(atomId2)) {
							addToSetOfSubsumers(atomId1, atomId2);
						}
					}

				}
			}
		}
	}

}
