/**
 * 
 */
package de.tudresden.inf.lat.uel.sat.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import de.tudresden.inf.lat.uel.sat.literals.Choice;
import de.tudresden.inf.lat.uel.sat.literals.ChoiceLiteral;
import de.tudresden.inf.lat.uel.sat.literals.Literal;
import de.tudresden.inf.lat.uel.sat.literals.OrderLiteral;
import de.tudresden.inf.lat.uel.sat.literals.SubsumptionLiteral;
import de.tudresden.inf.lat.uel.sat.literals.SubtypeLiteral;
import de.tudresden.inf.lat.uel.sat.literals.UnaryChoice;
import de.tudresden.inf.lat.uel.sat.type.SatInput;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Disequation;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.Subsumption;
import de.tudresden.inf.lat.uel.type.impl.AbstractUnificationAlgorithm;
import de.tudresden.inf.lat.uel.type.impl.IndexedSetImpl;

/**
 * @author Stefan Borgwardt
 *
 */
public abstract class AbstractSatUnificationAlgorithm extends AbstractUnificationAlgorithm {

	private static final String algorithmName = "SAT-based algorithm";
	private static final String keyAverageSize = "Average size of a clause";
	private static final String keyChoicePropositions = "Choice propositions";
	private static final String keyConfiguration = "Configuration";
	private static final String keyName = "Name";
	private static final String keyNumberOfClauses = "Number of clauses";
	private static final String keyNumberOfPropositions = "Number of propositions";
	private static final String keyOrderPropositions = "Order propositions";
	private static final String keySubsumptionPropositions = "Subsumption propositions";
	private static final String keySubtypePropositions = "Subtype propositions";
	private static final String keyTotalSize = "Total size of all clauses";
	private static final String notUsingMinimalAssignments = "all local assignments";
	private static final String usingMinimalAssignments = "only minimal assignments";

	/**
	 * An auxiliary variable to hold the SatInput under construction.
	 */
	protected SatInput input;

	/**
	 * An index of all literals used in the SAT encoding.
	 */
	protected final IndexedSet<Literal> literalManager = new IndexedSetImpl<Literal>();

	/**
	 * Indicates whether assignments should be minimized.
	 */
	protected final boolean onlyMinimalAssignments;

	/**
	 * Initialize a new SAT-based unification algorithm.
	 * 
	 * @param goal
	 *            the unification problem
	 * @param onlyMinimalAssignments
	 *            indicates whether assignments should be minimized
	 */
	public AbstractSatUnificationAlgorithm(Goal goal, boolean onlyMinimalAssignments) {
		super(goal);
		this.onlyMinimalAssignments = onlyMinimalAssignments;

		addInfo(keyName, algorithmName);
		if (onlyMinimalAssignments) {
			addInfo(keyConfiguration, usingMinimalAssignments);
		} else {
			addInfo(keyConfiguration, notUsingMinimalAssignments);
		}
	}

	private void checkInterrupted() throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
	}

	private Choice choice(SatInput input, Set<Integer> previousChoiceLiterals, IndexedSet<Literal> literalManager,
			int numberOfChoices) {
		return new UnaryChoice(input, previousChoiceLiterals, literalManager, numberOfChoices);
	}

	private Set<Integer> chooseSubsumption(Set<Integer> leftIds, Integer rightId) {
		return leftIds.stream().map(leftId -> subsumption(leftId, rightId)).collect(Collectors.toSet());
	}

	/**
	 * This method encodes equations into propositional clauses in DIMACS CNF
	 * format, i.e. positive literal is represented by a positive number and a
	 * negative literal is represented by a corresponding negative number. Each
	 * clause is on one line. The end of a clause is marked by 0. Example of a
	 * clause in DIMACS format: 1 -3 0
	 *
	 * @return an object representing the DIMACS CNF encoding of the unification
	 *         problem
	 * 
	 * @throws InterruptedException
	 *             if the process is interrupted
	 */
	protected SatInput computeSatInput() throws InterruptedException {
		input = new SatInput();

		encodeGoal();
		encodeSubsumptionBetweenConstants();
		encodeSubsumptionBetweenExistentialRestrictions();
		encodeSubsumptionBetweenConstantsAndExistentialRestrictions();
		encodeTransitivityOfSubsumption();
		encodeReflexivityOfOrder();
		encodeTransitivityOfOrder();
		encodeConnectionBetweenOrderAndSubsumption();

		if (goal.hasNegativePart()) {
			// add clauses with auxiliary variables needed for soundness of
			// disunification
			encodeConditionsForDissubsumptions();
		}

		if (!goal.getTypes().isEmpty()) {
			encodeDomainAndRangeRestrictions();
			encodeRoleGroupRestrictions();
			encodeCompatibilityRestrictions();
		}

		if (!goal.getRoleNumberRestrictions().isEmpty()) {
			encodeRoleNumberRestrictions();
		}

		if (goal.restrictUndefContext()) {
			encodeUndefContextRestriction();
		}

		if (onlyMinimalAssignments) {
			encodeMinimalAssignments();
		}

		updateInfo();
		return input;
	}

	private void computeSubsets(Collection<List<Integer>> subsets, Vector<Integer> currentStack, List<Integer> list,
			int left, int remainingCardinality) {
		if (remainingCardinality == 0) {
			subsets.add(new ArrayList<Integer>(currentStack));
			return;
		}

		for (int i = left; i < list.size(); i++) {
			currentStack.addElement(list.get(i));
			computeSubsets(subsets, currentStack, list, i + 1, remainingCardinality - 1);
			currentStack.removeElementAt(currentStack.size() - 1);
		}
	}

	private Collection<List<Integer>> computeSubsets(Set<Integer> set, int cardinality) {
		if (cardinality > set.size()) {
			return Collections.emptySet();
		}

		List<Integer> list = new ArrayList<Integer>(set);

		Collection<List<Integer>> subsets = new ArrayList<List<Integer>>();
		computeSubsets(subsets, new Vector<Integer>(list.size()), list, 0, cardinality);
		return subsets;
	}

	private void encodeCompatibilityRestrictions() throws InterruptedException {
		// no substitution set can contain incompatible variables
		for (Integer atomId1 : getVariables()) {
			for (Integer atomId2 : getVariables()) {
				if (!goal.areCompatible(atomId1, atomId2)) {
					checkInterrupted();

					for (Integer varId : getVariables()) {
						input.addNegativeClause(subsumption(varId, atomId1), subsumption(varId, atomId2));
					}
				}
			}
		}

		// no variable can have incompatible role group types
		for (List<Integer> typePair : computeSubsets(goal.getRoleGroupTypes().keySet(), 2)) {
			Integer type1 = typePair.get(0);
			Integer type2 = typePair.get(1);
			if (!goal.areCompatible(type1, type2)) {
				checkInterrupted();

				Integer roleGroupType1 = goal.getRoleGroupTypes().get(type1);
				Integer roleGroupType2 = goal.getRoleGroupTypes().get(type2);
				for (Integer varId : getVariables()) {
					input.addNegativeClause(subtype(varId, roleGroupType1), subtype(varId, roleGroupType2));
				}
			}
		}

		// no variable can have a role group type and a normal type
		for (Integer type : goal.getTypes()) {
			for (Integer roleGroupType : goal.getRoleGroupTypes().values()) {
				checkInterrupted();

				for (Integer varId : getVariables()) {
					input.addNegativeClause(subsumption(varId, type), subtype(varId, roleGroupType));
				}
			}
		}
	}

	private void encodeConditionsForDissubsumptions() throws InterruptedException {
		for (Integer atomId : getUsedAtomIds()) {
			for (Integer varId : getVariables()) {
				checkInterrupted();

				// TODO negate choice literals?
				encodeDissubsumptionVariable(Collections.singleton(subsumption(atomId, varId)),
						Collections.singleton(atomId), varId);
			}
		}
	}

	private void encodeConnectionBetweenOrderAndSubsumption() {
		for (Integer atomId1 : getExistentialRestrictions()) {
			Integer childId = goal.getAtomManager().getChild(atomId1);
			if (getVariables().contains(childId)) {
				for (Integer atomId2 : getVariables()) {
					input.addImplication(order(atomId2, childId), subsumption(atomId2, atomId1));
				}
			}
		}
	}

	private void encodeDefinition(Definition d) {
		encodeSubsumption(new Subsumption(d.getLeft(), d.getRight()));
		if (!d.isPrimitive()) {
			encodeSubsumption(new Subsumption(d.getRight(), d.getLeft()));
		}
	}

	private void encodeDisequation(Disequation e) {
		// choose which direction of the equation does not hold
		Choice c = choice(input, Collections.emptySet(), literalManager, 2);
		encodeDissubsumption(c.getChoiceLiterals(0), new Dissubsumption(e.getLeft(), e.getRight()));
		encodeDissubsumption(c.getChoiceLiterals(1), new Dissubsumption(e.getRight(), e.getLeft()));
	}

	private void encodeDissubsumption(Dissubsumption e) {
		encodeDissubsumption(Collections.<Integer> emptySet(), e);
	}

	private void encodeDissubsumption(Set<Integer> choiceLiterals, Dissubsumption e) {
		if (e.getRight().size() == 0) {
			input.add(choiceLiterals);
		} else if (e.getRight().size() == 1) {
			encodeDissubsumption(choiceLiterals, e.getLeft(), e.getRight().iterator().next());
		} else {
			// choose which of the right-hand side atoms does not subsume the
			// left-hand side
			Choice c = choice(input, choiceLiterals, literalManager, e.getRight().size());
			int j = 0;
			for (Integer rightId : e.getRight()) {
				encodeDissubsumption(c.addChoiceLiterals(choiceLiterals, j), e.getLeft(), rightId);
				j++;
			}
		}
	}

	private void encodeDissubsumption(Set<Integer> choiceLiterals, Set<Integer> leftIds, Integer rightId) {
		if (leftIds.size() == 1) {
			// assert single dissubsumption, the rest will be handled by
			// 'addClausesForDisunification'
			Set<Integer> clause = new HashSet<Integer>(choiceLiterals);
			clause.add(-subsumption(leftIds.iterator().next(), rightId));
			input.add(clause);
		} else if (getVariables().contains(rightId)) {
			encodeDissubsumptionVariable(choiceLiterals, leftIds, rightId);
		} else {
			// 'rightId' is a non-variable atom --> it should not subsume any of
			// the leftIds
			encodeDissubsumptionNonVariableAtom(choiceLiterals, leftIds, rightId);
		}
	}

	private void encodeDissubsumptionNonVariableAtom(Set<Integer> choiceLiterals, Set<Integer> leftIds,
			Integer rightId) {
		Set<Integer> clause;
		for (Integer leftId : leftIds) {
			// TODO negate choice literals?
			clause = new HashSet<Integer>(choiceLiterals);
			clause.add(-subsumption(leftId, rightId));
			input.add(clause);
		}
	}

	private void encodeDissubsumptionVariable(Set<Integer> choiceLiterals, Set<Integer> leftIds, Integer rightId) {
		// choose which non-variable atom solves the dissubsumption
		Choice c = choice(input, choiceLiterals, literalManager, getNonVariableAtoms().size());
		int j = 0;
		for (Integer atomId : getNonVariableAtoms()) {
			Set<Integer> currentChoiceLiterals = c.addChoiceLiterals(choiceLiterals, j);

			// Under the current choice, 'rightId' is subsumed by 'atomId' ...
			// TODO negate choice literals?
			Set<Integer> clause = new HashSet<Integer>(currentChoiceLiterals);
			clause.add(subsumption(rightId, atomId));
			input.add(clause);

			// ... and 'atomId' does not subsume any of the 'leftIds'.
			encodeDissubsumptionNonVariableAtom(currentChoiceLiterals, leftIds, atomId);

			// next choice
			j++;
		}
	}

	private void encodeDomainAndRangeRestrictions() throws InterruptedException {
		// domain restrictions
		for (Integer varId : getVariables()) {
			for (Integer eatomId : getExistentialRestrictions()) {
				Integer roleId = goal.getAtomManager().getRoleId(eatomId);
				Set<Integer> domain = goal.getDomains().get(roleId);
				if (domain != null) {
					checkInterrupted();

					Set<Integer> head = domain.stream().map(type -> goal.getRoleGroupTypes().values().contains(type)
							? subtype(varId, type) : subsumption(varId, type)).collect(Collectors.toSet());
					input.addImplication(head, subsumption(varId, eatomId));
				}
			}
		}

		// range restrictions
		for (Integer eatomId : getExistentialRestrictions()) {
			Integer roleId = goal.getAtomManager().getRoleId(eatomId);
			Integer childId = goal.getAtomManager().getChild(eatomId);
			Set<Integer> range = goal.getRanges().get(roleId);
			if (range != null) {
				checkInterrupted();

				input.add(range.stream().map(type -> subsumption(childId, type)).collect(Collectors.toSet()));
			}
		}
	}

	private void encodeEquation(Equation e) {
		encodeSubsumption(new Subsumption(e.getLeft(), e.getRight()));
		encodeSubsumption(new Subsumption(e.getRight(), e.getLeft()));
	}

	private void encodeGoal() {
		for (Definition d : goal.getDefinitions()) {
			encodeDefinition(d);
		}
		for (Equation e : goal.getEquations()) {
			encodeEquation(e);
		}
		for (Subsumption s : goal.getSubsumptions()) {
			encodeSubsumption(s);
		}
		for (Disequation e : goal.getDisequations()) {
			encodeDisequation(e);
		}
		for (Dissubsumption s : goal.getDissubsumptions()) {
			encodeDissubsumption(s);
		}
	}

	private void encodeMinimalAssignments() throws InterruptedException {
		// minimize substitution sets
		for (Integer varId : getUserVariables()) {
			checkInterrupted();

			for (Integer atomId : getNonVariableAtoms()) {
				input.addMinimizeLiteral(subsumption(varId, atomId));
			}
		}
		// minimize subtype literals
		for (Integer type : goal.getTypes()) {
			checkInterrupted();

			for (Integer conceptNameId : getConceptNames()) {
				input.addMinimizeLiteral(subtype(conceptNameId, type));
			}
		}
	}

	private void encodeReflexivityOfOrder() {
		for (Integer atomId1 : getVariables()) {
			input.add(-order(atomId1, atomId1));
		}
	}

	private void encodeRoleGroupRestrictions() {
		Integer roleGroupId = goal.getAtomManager().getRoleId(goal.SNOMED_RoleGroup_URI());
		for (Integer eatomId : goal.getAtomManager().getExistentialRestrictions(roleGroupId)) {
			Integer childId = goal.getAtomManager().getChild(eatomId);

			// 'RoleGroup' translates between 'normal types' and 'role group
			// types'
			for (Integer varId : getVariables()) {
				Integer subsumptionLiteral = subsumption(varId, eatomId);
				for (Integer type : goal.getRoleGroupTypes().keySet()) {
					Integer roleGroupType = goal.getRoleGroupTypes().get(type);
					Integer varTypeLiteral = subsumption(varId, type);
					Integer childRoleGroupTypeLiteral = subtype(childId, roleGroupType);
					input.addImplication(varTypeLiteral, childRoleGroupTypeLiteral, subsumptionLiteral);
				}
			}

			// variables occurring inside RoleGroup must have a role group
			// type
			if (getVariables().contains(childId)) {
				input.add(goal.getRoleGroupTypes().values().stream()
						.map(roleGroupType -> subtype(childId, roleGroupType)).collect(Collectors.toSet()));
			}
		}
	}

	private void encodeRoleNumberRestrictions() throws InterruptedException {
		for (Integer roleId : goal.getAtomManager().getRoleIds()) {
			int number = goal.getRoleNumberRestrictions().get(roleId);
			if (number > 0) {
				// 'roleId' is only allowed to have 'number' existential
				// restrictions in a conjunction
				Set<Integer> ex = goal.getAtomManager().getExistentialRestrictions(roleId);
				for (List<Integer> subset : computeSubsets(ex, number + 1)) {
					checkInterrupted();

					// for each collection of 'number'+1 such restrictions, ...
					Set<Integer> options = new HashSet<Integer>();
					Set<Integer> eatomOptions = new HashSet<Integer>();
					Set<Integer> otherEAtoms = new HashSet<Integer>(ex);
					otherEAtoms.removeAll(subset);
					Set<Integer> variableChildren = subset.stream()
							.map(atomId -> goal.getAtomManager().getChild(atomId)).filter(getVariables()::contains)
							.collect(Collectors.toSet());
					for (List<Integer> twoVariableChildren : computeSubsets(variableChildren, 2)) {
						// ... there must be two of them whose children are
						// compatible ...
						Integer varChild1 = twoVariableChildren.get(0);
						Integer varChild2 = twoVariableChildren.get(1);
						if (goal.areCompatible(varChild1, varChild2)) {
							// ... and either one subsumes the other ...
							options.add(subsumption(varChild1, varChild2));
							options.add(subsumption(varChild2, varChild1));

							// ... or both subsume a third one that is also
							// present in the conjunction.
							for (Integer eatomId : otherEAtoms) {
								Integer otherChild = goal.getAtomManager().getChild(eatomId);
								if (getVariables().contains(otherChild)
										&& goal.isCommonSubsumee(otherChild, varChild1, varChild2)) {
									eatomOptions.add(eatomId);
								}
							}
						}
					}
					for (Integer varId : getVariables()) {
						Set<Integer> clause = new HashSet<Integer>(options);
						for (Integer eatomId : eatomOptions) {
							clause.add(subsumption(varId, eatomId));
						}
						for (Integer eatomId : subset) {
							clause.add(-subsumption(varId, eatomId));
						}
						input.add(clause);
					}
				}
			}
		}
	}

	private void encodeSubsumption(Subsumption s) {
		// if top is on the right-hand side, do nothing
		for (Integer rightId : s.getRight()) {
			if (getVariables().contains(rightId)) {
				encodeSubsumptionVariable(s.getLeft(), rightId);
			} else {
				encodeSubsumptionNonVariableAtom(s.getLeft(), rightId);
			}
		}
	}

	private void encodeSubsumptionBetweenConstants() {
		for (Integer atomId1 : getConstants()) {
			for (Integer atomId2 : getConstants()) {
				if (!atomId1.equals(atomId2)) {
					input.add(-subsumption(atomId1, atomId2));
				}
			}

			if (goal.hasNegativePart()) {
				// positive clause needed for soundness of disunification
				input.add(subsumption(atomId1, atomId1));
			}
		}
	}

	private void encodeSubsumptionBetweenConstantsAndExistentialRestrictions() {
		for (Integer atomId1 : getConstants()) {
			for (Integer atomId2 : getExistentialRestrictions()) {
				input.add(-subsumption(atomId1, atomId2));
				input.add(-subsumption(atomId2, atomId1));
			}
		}
	}

	private void encodeSubsumptionBetweenExistentialRestrictions() {
		for (Integer atomId1 : getExistentialRestrictions()) {
			for (Integer atomId2 : getExistentialRestrictions()) {
				if (!atomId1.equals(atomId2)) {

					Integer atomSubsumption = subsumption(atomId1, atomId2);
					Integer role1 = goal.getAtomManager().getRoleId(atomId1);
					Integer role2 = goal.getAtomManager().getRoleId(atomId2);

					if (!role1.equals(role2)) {
						// if roles are not equal, then Step 2.2
						input.add(-atomSubsumption);
					} else {
						// if the roles are equal, then clause in Step 2.3
						Integer child1 = goal.getAtomManager().getChild(atomId1);
						Integer child2 = goal.getAtomManager().getChild(atomId2);
						Integer childSubsumption = subsumption(child1, child2);

						if (!child1.equals(child2)) {
							input.addImplication(childSubsumption, atomSubsumption);
						}

						if (goal.hasNegativePart()) {
							// converse clause needed for soundness of
							// disunification
							input.addImplication(atomSubsumption, childSubsumption);
						}
					}
				}
			}

			if (goal.hasNegativePart()) {
				// converse clause (as above) for trival subsumption
				// between an existential restriction and itself
				input.add(subsumption(atomId1, atomId1));
			}
		}
	}

	private void encodeSubsumptionNonVariableAtom(Set<Integer> leftIds, Integer rightId) {
		input.add(chooseSubsumption(leftIds, rightId));
	}

	private void encodeSubsumptionVariable(Set<Integer> leftIds, Integer rightId) {
		for (Integer atomId : getNonVariableAtoms()) {
			if (!leftIds.contains(atomId)) {
				input.addImplication(chooseSubsumption(leftIds, atomId), subsumption(rightId, atomId));
			}
		}
	}

	private void encodeTransitivityOfOrder() throws InterruptedException {
		for (Integer atomId1 : getVariables()) {
			for (Integer atomId2 : getVariables()) {
				checkInterrupted();

				for (Integer atomId3 : getVariables()) {
					if (!atomId1.equals(atomId2) && !atomId2.equals(atomId3)) {
						input.addImplication(order(atomId1, atomId3), order(atomId1, atomId2), order(atomId2, atomId3));
					}
				}
			}
		}
	}

	private void encodeTransitivityOfSubsumption() throws InterruptedException {
		// TODO check: for soundness of disunification it is enough that atomId1
		// or atomId2 is a variable!?
		for (Integer atomId1 : getUsedAtomIds()) {
			boolean var1 = getVariables().contains(atomId1);
			for (Integer atomId2 : getUsedAtomIds()) {
				if (var1 || getVariables().contains(atomId2)) {
					if (!atomId1.equals(atomId2)) {
						checkInterrupted();

						for (Integer atomId3 : getUsedAtomIds()) {
							if (!atomId1.equals(atomId3) && !atomId2.equals(atomId3)) {
								input.addImplication(subsumption(atomId1, atomId3), subsumption(atomId1, atomId2),
										subsumption(atomId2, atomId3));
							}
						}
					}
				}
			}
		}
	}

	private void encodeUndefContextRestriction() throws InterruptedException {
		// UNDEF names can only occur in the context of their associated
		// definition
		for (Integer undefId : goal.getAtomManager().getUndefNames()) {
			Integer origId = goal.getAtomManager().removeUndef(undefId);
			checkInterrupted();

			for (Integer varId : getVariables()) {
				input.addImplication(subsumption(varId, origId), subsumption(varId, undefId));
			}
		}
	}

	/**
	 * Construct a new OrderLiteral.
	 * 
	 * @param varId1
	 *            a variable id
	 * @param varId2
	 *            a variable id
	 * @return the id of a literal specifying that 'varId1' is strictly smaller
	 *         than 'varId2'
	 */
	protected Integer order(Integer varId1, Integer varId2) {
		Literal literal = new OrderLiteral(varId1, varId2);
		return literalManager.addAndGetIndex(literal);
	}

	/**
	 * Construct a new SubsumptionLiteral.
	 * 
	 * @param atomId1
	 *            an atom id
	 * @param atomId2
	 *            an atom id
	 * @return the id of a literal specifying a subsumption between 'atomId1'
	 *         and 'atomId2'
	 */
	protected Integer subsumption(Integer atomId1, Integer atomId2) {
		Literal literal = new SubsumptionLiteral(atomId1, atomId2);
		return literalManager.addAndGetIndex(literal);
	}

	/**
	 * Construct a new SuptypeLiteral.
	 * 
	 * @param atomId
	 *            an atom id
	 * @param type
	 *            a type
	 * @return the id of a literal specifying that 'atomId' has 'type
	 */
	protected Integer subtype(Integer atomId, Integer type) {
		Literal literal = new SubtypeLiteral(atomId, type);
		return literalManager.addAndGetIndex(literal);
	}

	@Override
	protected void updateInfo() {
		if (literalManager != null) {
			addInfo(keyNumberOfPropositions, literalManager.size());
			addInfo(keyChoicePropositions, literalManager.stream().filter(l -> l instanceof ChoiceLiteral).count());
			addInfo(keySubsumptionPropositions,
					literalManager.stream().filter(l -> l instanceof SubsumptionLiteral).count());
			addInfo(keySubtypePropositions, literalManager.stream().filter(l -> l instanceof SubtypeLiteral).count());
			addInfo(keyOrderPropositions, literalManager.stream().filter(l -> l instanceof OrderLiteral).count());
		}
		if (input != null) {
			long numberOfClauses = input.getClauses().size();
			long totalSize = input.getClauses().stream().mapToInt(c -> c.size()).sum();
			addInfo(keyNumberOfClauses, numberOfClauses);
			addInfo(keyTotalSize, totalSize);
			addInfo(keyAverageSize, ((float) totalSize) / ((float) numberOfClauses));
		}
	}

}
