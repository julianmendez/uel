package de.tudresden.inf.lat.uel.sat.solver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.tudresden.inf.lat.uel.sat.type.SatOutput;
import de.tudresden.inf.lat.uel.sat.type.SatSolver;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.impl.DefinitionSet;
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
public class SatUnificationAlgorithm extends AbstractSatUnificationAlgorithm {

	private boolean firstTime = true;
	private Unifier result;
	private SatSolver solver;
	private Set<Integer> valuation;

	/**
	 * Initialize an instance of the SAT-based unification algorithm.
	 * 
	 * @param goal
	 *            the unification problem
	 * @param onlyMinimalAssignments
	 *            indicates whether only unifiers based on minimal assignments
	 *            should be returned
	 */
	public SatUnificationAlgorithm(Goal goal, boolean onlyMinimalAssignments) {
		super(goal, onlyMinimalAssignments);
	}

	@Override
	public void cleanup() {
		if (solver != null) {
			solver.cleanup();
		}
	}

	private DefinitionSet computeDefinitions() {
		DefinitionSet definitions = new DefinitionSet(getVariables().size());
		getVariables().stream().map(varId -> new Definition(varId, computeSubsumers(varId), false))
				.forEach(definitions::add);
		return definitions;
	}

	@Override
	public boolean computeNextUnifier() throws InterruptedException {
		SatOutput satoutput = null;
		boolean unifiable = false;
		try {
			if (firstTime) {
				// System.out.println("Initializing SAT problem ...");
				if (onlyMinimalAssignments) {
					solver = new Sat4jMaxSatSolver();
				} else {
					solver = new Sat4jSolver();
				}
				computeSatInput();

				//// DEBUG
				// StringBuffer sbuf = new StringBuffer();
				// for (Set<Integer> clause : input.getClauses()) {
				// for (Integer literalId : clause) {
				// if (literalId < 0) {
				// sbuf.append("-");
				// }
				// Literal literal = literalManager.get(Math.abs(literalId));
				// sbuf.append("[");
				// if (literal == null) {
				// sbuf.append("NULL");
				// } else if (literal instanceof OrderLiteral) {
				// appendAtom(sbuf, literal.getFirst());
				// sbuf.append(" > ");
				// appendAtom(sbuf, literal.getSecond());
				// } else if (literal instanceof SubsumptionLiteral) {
				// appendAtom(sbuf, literal.getFirst());
				// sbuf.append(" ⊑ ");
				// appendAtom(sbuf, literal.getSecond());
				// } else if (literal instanceof ChoiceLiteral) {
				// sbuf.append("c");
				// sbuf.append(((ChoiceLiteral) literal).hashCode());
				// } else if (literal instanceof SubtypeLiteral) {
				// sbuf.append("type(");
				// appendAtom(sbuf, literal.getFirst());
				// sbuf.append(", ");
				// appendAtom(sbuf, literal.getSecond());
				// sbuf.append(")");
				// }
				// sbuf.append("] ");
				// }
				// sbuf.append("\n");
				// }
				// System.out.println(sbuf);

				callbackPreprocessing();
				satoutput = solver.solve(input);
				unifiable = satoutput.isSatisfiable();

				// the SatInput object is not needed anymore
				// input = null;
			} else {
				Set<Integer> update = computeUpdate();
				if (update.isEmpty()) {
					unifiable = false;
				} else {
					// this.numberOfClauses++;
					satoutput = solver.update(update);
					unifiable = satoutput.isSatisfiable();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (unifiable) {
			valuation = satoutput.getOutput();
			// outputUnsatisfiedSoftClauses();
			result = computeUnifier();
		} else {
			// release resources used by the solver after all unifiers have been
			// computed
			solver.cleanup();
		}

		firstTime = false;
		return unifiable;
	}

	// private void appendAtom(StringBuffer sbuf, Integer atomId) {
	// if (goal.getAtomManager().getExistentialRestrictions().contains(atomId))
	// {
	// sbuf.append("∃" + goal.getAtomManager().printRoleName(atomId) + "."
	// +
	// goal.getAtomManager().printConceptName(goal.getAtomManager().getChild(atomId)));
	// } else {
	// sbuf.append(goal.getAtomManager().printConceptName(atomId));
	// }
	// }

	private Set<Integer> computeSubsumers(Integer varId) {
		return getNonVariableAtoms().stream().filter(atomId -> valuation.contains(subsumption(varId, atomId)))
				.collect(Collectors.toSet());
	}

	private Map<Integer, Set<Integer>> computeTypeAssignment() {
		if (goal.getTypes().isEmpty()) {
			return null;
		}
		return getConceptNames().stream()
				.collect(Collectors.toMap(Function.identity(), atomId -> computeTypes(atomId)));
	}

	private Set<Integer> computeTypes(Integer atomId) {
		return goal.getTypes().stream().filter(
				type -> valuation.contains(subsumption(atomId, type)) || valuation.contains(subtype(atomId, type)))
				.collect(Collectors.toSet());
	}

	private Unifier computeUnifier() {
		return new Unifier(computeDefinitions(), computeTypeAssignment());
	}

	private Set<Integer> computeUpdate() {
		Set<Integer> update = new HashSet<Integer>();
		// for (Integer firstAtomId : getUserVariables()) {
		for (Integer varId : getVariables()) {
			for (Integer atomId : getNonVariableAtoms()) {
				Integer literalId = subsumption(varId, atomId);
				boolean literalValue = valuation.contains(literalId);
				if (!onlyMinimalAssignments || literalValue) {
					update.add(literalValue ? -literalId : literalId);
				}
			}
		}
		return update;
	}

	@Override
	public Unifier getUnifier() {
		return result;
	}

}
