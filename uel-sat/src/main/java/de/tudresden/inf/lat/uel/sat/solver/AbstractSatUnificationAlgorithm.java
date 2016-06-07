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
import java.util.function.Function;
import java.util.stream.Collectors;

import de.tudresden.inf.lat.uel.sat.literals.Choice;
import de.tudresden.inf.lat.uel.sat.literals.ChoiceLiteral;
import de.tudresden.inf.lat.uel.sat.literals.Literal;
import de.tudresden.inf.lat.uel.sat.literals.OrderLiteral;
import de.tudresden.inf.lat.uel.sat.literals.SubsumptionLiteral;
import de.tudresden.inf.lat.uel.sat.literals.SubtypeLiteral;
import de.tudresden.inf.lat.uel.sat.literals.UnaryChoice;
import de.tudresden.inf.lat.uel.sat.type.SatInput;
import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Disequation;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.Subsumption;
import de.tudresden.inf.lat.uel.type.impl.AbstractUnificationAlgorithm;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;
import de.tudresden.inf.lat.uel.type.impl.IndexedSetImpl;

/**
 * @author Stefan Borgwardt
 *
 */
public abstract class AbstractSatUnificationAlgorithm extends AbstractUnificationAlgorithm {

	private static final String algorithmName = "SAT-based algorithm";
	private static final String keyAverageSize = "Average size of a clause";
	private static final String keyConfiguration = "Configuration";
	private static final String keyName = "Name";
	private static final String keyNumberOfClauses = "Number of clauses";
	private static final String keyNumberOfPropositions = "Number of propositions";
	private static final String keyTotalSize = "Total size of all clauses";
	private static final String notUsingMinimalAssignments = "all local assignments";
	private static final String usingMinimalAssignments = "only minimal assignments";

	private final IndexedSet<Literal> literalManager = new IndexedSetImpl<Literal>();
	protected final boolean onlyMinimalAssignments;
	private Function<String, String> shortFormMap = Function.identity();

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

	private void addClausesForDisunification(SatInput input) throws InterruptedException {
		for (Integer atomId : getUsedAtomIds()) {
			for (Integer varId : getVariables()) {
				// TODO negate choice literals?
				runStep1DissubsumptionVariable(Collections.singleton(subsumption(atomId, varId)),
						Collections.singleton(atomId), varId, input);
			}
		}
	}

	private void addTypeRestrictions(SatInput input) {
		// // experimental - minimize subtype literals
		for (Integer conceptNameId : getConceptNames()) {
			for (Integer type : goal.getRoleGroupTypes().values()) {
				input.addMinimizeLiteral(subtype(conceptNameId, type));
			}
		}

		// // a - all types have themselves as types
		// for (Integer type : goal.getRoleGroupTypes().keySet()) {
		// input.add(subtype(type, type));
		// }
		//
		// // b - every other concept name must also have a type
		// for (Integer conceptNameId : getConceptNames()) {
		// if (!goal.getTypes().contains(conceptNameId)) {
		// if (goal.getTypeAssignment().containsKey(conceptNameId)) {
		// // if there is a direct type hint in the goal, use it ...
		// input.add(subtype(conceptNameId,
		// goal.getTypeAssignment().get(conceptNameId)));
		// } else {
		// // otherwise only assert that there must exist a type
		// input.add(goal.getTypes().stream().map(type -> subtype(conceptNameId,
		// type))
		// .collect(Collectors.toSet()));
		// }
		// }
		// }
		//
		// // c - types are inherited by subconcepts
		// for (Integer conceptNameId1 : getConceptNames()) {
		// for (Integer conceptNameId2 : getConceptNames()) {
		// for (Integer type : goal.getTypes()) {
		// input.add(implication(subtype(conceptNameId1, type),
		// subsumption(conceptNameId1, conceptNameId2),
		// subtype(conceptNameId2, type)));
		// }
		// }
		// }
		//
		// // c' - types are inherited by/from UNDEF concept names
		// for (Integer undefId : goal.getAtomManager().getUndefNames()) {
		// Integer origId = goal.getAtomManager().removeUndef(undefId);
		// for (Integer type : goal.getTypes()) {
		// Integer origTypeLiteral = subtype(origId, type);
		// Integer undefTypeLiteral = subtype(undefId, type);
		// input.add(implication(undefTypeLiteral, origTypeLiteral));
		// input.add(implication(origTypeLiteral, undefTypeLiteral));
		// }
		// }
		//
		// // d - no concept name can have disjoint types
		// List<Integer> types = new ArrayList<Integer>(goal.getTypes());
		// for (int i = 0; i < types.size(); i++) {
		// for (int j = i + 1; j < types.size(); j++) {
		// Integer type1 = types.get(i);
		// Integer type2 = types.get(j);
		// if (goal.areDisjoint(type1, type2)) {
		// for (Integer conceptNameId : getConceptNames()) {
		// input.add(negativeClause(subtype(conceptNameId, type1),
		// subtype(conceptNameId, type2)));
		// }
		// }
		// }
		// }

		// d' - no variable can have incompatible role group types
		for (List<Integer> typePair : computeSubsets(goal.getRoleGroupTypes().keySet(), 2)) {
			Integer type1 = typePair.get(0);
			Integer type2 = typePair.get(1);
			if (!goal.areCompatible(type1, type2)) {
				Integer roleGroupType1 = goal.getRoleGroupTypes().get(type1);
				Integer roleGroupType2 = goal.getRoleGroupTypes().get(type2);
				for (Integer varId : getVariables()) {
					input.addNegativeClause(subtype(varId, roleGroupType1), subtype(varId, roleGroupType2));
				}
			}
		}

		// d'' - no variable can have a role group type and a normal type
		for (Integer type : goal.getTypes()) {
			// ignore the top concept
			if (goal.getDirectSupertype(type) != null) {
				for (Integer roleGroupType : goal.getRoleGroupTypes().values()) {
					for (Integer varId : getVariables()) {
						input.addNegativeClause(subsumption(varId, type), subtype(varId, roleGroupType));
					}
				}
			}
		}

		// domain restrictions
		for (Integer varId : getVariables()) {
			for (Integer eatomId : getExistentialRestrictions()) {
				Integer roleId = goal.getAtomManager().getRoleId(eatomId);
				Set<Integer> domain = goal.getDomains().get(roleId);
				if (domain != null) {
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
				input.add(range.stream().map(type -> subsumption(childId, type)).collect(Collectors.toSet()));
			}
		}

		// 'RoleGroup' translates between 'normal types' and 'role group types'
		// Integer roleGroupId =
		// goal.getAtomManager().getRoleId("http://www.ihtsdo.org/RoleGroup");
		// for (Integer eatomId : getExistentialRestrictions()) {
		// if (goal.getAtomManager().getRoleId(eatomId).equals(roleGroupId)) {
		// Integer childId = goal.getAtomManager().getChild(eatomId);
		// for (Integer varId : getVariables()) {
		// Integer subsumptionLiteral = subsumption(varId, eatomId);
		// for (Integer type : goal.getRoleGroupTypes().keySet()) {
		// Integer roleGroupType = goal.getRoleGroupTypes().get(type);
		// Integer varTypeLiteral = subsumption(varId, type);
		// Integer childRoleGroupTypeLiteral = subtype(childId, roleGroupType);
		// // System.out.println("If subsumption(" +
		// // printAtom(varId) + "," +
		// // printAtom(eatomId)
		// // + "), then subsumption(" + printAtom(varId) + "," +
		// // printAtom(type) +
		// // ") <-> subtype("
		// // + printAtom(childId) + "," + printAtom(roleGroupType)
		// // + ")");
		// input.addImplication(childRoleGroupTypeLiteral, varTypeLiteral,
		// subsumptionLiteral);
		// input.addImplication(varTypeLiteral, childRoleGroupTypeLiteral,
		// subsumptionLiteral);
		// }
		// }
		// }
		// }
		
		// OLD: transparent roles
		// for (Integer eatomId : getExistentialRestrictions()) {
		// Integer roleId =
		// goal.getAtomManager().getExistentialRestriction(eatomId).getRoleId();
		// if (goal.getTransparentRoles().contains(roleId)) {
		// Integer childId = goal.getAtomManager().getChild(eatomId);
		// for (Integer varId : getVariables()) {
		// Integer subsumptionLiteral = subsumption(varId, eatomId);
		// for (Integer type : goal.getTypes()) {
		// Integer varTypeLiteral = subtype(varId, type);
		// Integer childTypeLiteral = subtype(childId, type);
		// input.add(implication(childTypeLiteral, varTypeLiteral,
		// subsumptionLiteral));
		// input.add(implication(varTypeLiteral, childTypeLiteral,
		// subsumptionLiteral));
		// }
		// }
		// }
		// }
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
	 * @return an object representing the DIMACS CNF encoding of the input
	 *         subsumptions
	 * 
	 * @throws InterruptedException
	 *             if the process is interrupted
	 */
	protected SatInput computeSatInput() throws InterruptedException {
		SatInput ret = new SatInput();

		runStep1(ret);

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		runStep2_1(ret);

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		runSteps2_2_N_2_3(ret);

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		runStep2_4(ret);

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		runStep2_5(ret);

		runStep3_1_r(ret);

		runStep3_1_t(ret);

		runStep3_2(ret);

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		if (goal.hasNegativePart()) {
			// add clauses with auxiliary variables needed for soundness of
			// disunification
			addClausesForDisunification(ret);
		}

		if (!goal.getTypes().isEmpty()) {
			// encode type restrictions
			addTypeRestrictions(ret);
		}

		if (!goal.getRoleNumberRestrictions().isEmpty()) {
			enforceRoleNumberRestrictions(ret);
		}

		if (goal.restrictUndefContext()) {
			enforceUndefContext(ret);
		}

		if (onlyMinimalAssignments) {
			for (Integer varId : getUserVariables()) {
				for (Integer atomId : getNonVariableAtoms()) {
					ret.addMinimizeLiteral(subsumption(varId, atomId));
				}
			}
		}

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		updateInfo(ret);
		return ret;
	}

	private void computeSubsets(Collection<List<Integer>> subsets, Vector<Integer> currentStack, List<Integer> list,
			int left, int remainingCardinality) {
		// System.out.println("Current stack: " + currentStack);
		if (remainingCardinality == 0) {
			subsets.add(new ArrayList<Integer>(currentStack));
			return;
		}

		for (int i = left; i < list.size(); i++) {
			currentStack.addElement(list.get(i));
			// System.out.println("Pushed " + list.get(i) + " onto the current
			// stack.");
			computeSubsets(subsets, currentStack, list, i + 1, remainingCardinality - 1);
			currentStack.removeElementAt(currentStack.size() - 1);
			// System.out.println("Popped " + k + " from the stack. Current
			// stack: " + currentStack);
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

	private void enforceRoleNumberRestrictions(SatInput input) {

		// System.out.println("enforcing number restrictions");
		for (Integer roleId : goal.getAtomManager().getRoleIds()) {
			int number = goal.getRoleNumberRestrictions().get(roleId);
			// System.out.println(goal.getAtomManager().getRoleName(roleId));
			// System.out.println(number);
			if (number > 0) {
				Set<Integer> ex = goal.getAtomManager().getExistentialRestrictions(roleId);
				// System.out.println("restrictions: " + ex);
				for (List<Integer> subset : computeSubsets(ex, number + 1)) {
					// System.out.println("subset " + subset);
					Set<Integer> options = new HashSet<Integer>();
					Set<Integer> children = subset.stream().map(atomId -> goal.getAtomManager().getChild(atomId))
							.collect(Collectors.toSet());
					for (List<Integer> twoChildren : computeSubsets(children, 2)) {
						Integer child1 = twoChildren.get(0);
						Integer child2 = twoChildren.get(1);
						if (goal.areCompatible(child1, child2)) {
							if (getVariables().contains(child1) || getVariables().contains(child2)) {
								options.add(subsumption(child1, child2));
								options.add(subsumption(child2, child1));
							}
						}
					}
					for (Integer varId : getVariables()) {
						Set<Integer> clause = new HashSet<Integer>(options);
						for (Integer eatomId : subset) {
							clause.add(-subsumption(varId, eatomId));
						}
						input.add(clause);
					}
				}
			}
		}

		// OLD version with only 2 existential restrictions
		// for (Integer eatomId1 : getExistentialRestrictions()) {
		// for (Integer eatomId2 : getExistentialRestrictions()) {
		// if (!eatomId1.equals(eatomId2)) {
		// if
		// (goal.getAtomManager().getRoleId(eatomId1).equals(goal.getAtomManager().getRoleId(eatomId2)))
		// {
		// Integer child1 = goal.getAtomManager().getChild(eatomId1);
		// Integer child2 = goal.getAtomManager().getChild(eatomId2);
		// if (!goal.areCompatible(child1, child2)) {
		// // System.out
		// // .println("Not compatible: " + printAtom(eatomId1)
		// // + " and " + printAtom(eatomId2));
		// for (Integer varId : getVariables()) {
		// input.add(negativeClause(subsumption(varId, eatomId1),
		// subsumption(varId, eatomId2)));
		// }
		// } else {
		// // even if compatible, they need to be related via
		// // subsumption if possible
		// if (getVariables().contains(child1) ||
		// getVariables().contains(child2)) {
		// // System.out.println(
		// // "Possibly compatible: " + printAtom(eatomId1)
		// // + " and " + printAtom(eatomId2));
		// // List<Entry<String, String>> cases =
		// // Arrays.asList(
		// // new SimpleEntry<String,
		// // String>("http://www.ihtsdo.org/SCT_363714003_VAR",
		// // "http://www.ihtsdo.org/SCT_307124006"),
		// // new SimpleEntry<String,
		// // String>("http://www.ihtsdo.org/RoleGroup_VAR",
		// // "var0"),
		// // new SimpleEntry<String, String>("var1",
		// // "var0"),
		// // new SimpleEntry<String,
		// // String>("http://www.ihtsdo.org/SCT_260686004_VAR",
		// // "http://www.ihtsdo.org/SCT_129265001"),
		// // new SimpleEntry<String, String>("var4",
		// // "var3"));
		// // if (cases.stream().anyMatch(
		// // e ->
		// // child1.equals(goal.getAtomManager().createConceptName(e.getKey(),
		// // true))
		// // && child2.equals(
		// // goal.getAtomManager().createConceptName(e.getValue(),
		// // true)))) {
		// // System.out.println("!");
		// for (Integer varId : getVariables()) {
		// Set<Integer> head = new HashSet<Integer>(
		// Arrays.asList(subsumption(child1, child2), subsumption(child2,
		// child1)));
		// input.add(implication(head, subsumption(varId, eatomId1),
		// subsumption(varId, eatomId2)));
		// }
		// // }
		// } else {
		// // System.out
		// // .println("Compatible: " + printAtom(eatomId1)
		// // + " and " + printAtom(eatomId2));
		// }
		// }
		// }
		// }
		// }
		// }

		for (Integer atomId1 : getConceptNames()) {
			for (Integer atomId2 : getConceptNames()) {
				if (!goal.areCompatible(atomId1, atomId2)) {
					// System.out.println("Not compatible: " +
					// printAtom(atomId1) + " and " + printAtom(atomId2));
					for (Integer varId : getVariables()) {
						input.addNegativeClause(subsumption(varId, atomId1), subsumption(varId, atomId2));
					}
				}
			}
		}
	}

	private void enforceUndefContext(SatInput input) {
		// UNDEF names can only occur in the context of their associated
		// definition
		for (Integer undefId : goal.getAtomManager().getUndefNames()) {
			Integer origId = goal.getAtomManager().removeUndef(undefId);
			for (Integer varId : getVariables()) {
				input.addImplication(subsumption(varId, origId), subsumption(varId, undefId));
			}
		}
	}

	protected Integer order(Integer atomId1, Integer atomId2) {
		Literal literal = new OrderLiteral(atomId1, atomId2);
		return literalManager.addAndGetIndex(literal);
	}

	protected String printAtom(Integer atomId) {
		Atom a = goal.getAtomManager().getAtom(atomId);
		if (a instanceof ExistentialRestriction) {
			String roleName = shortFormMap.apply(goal.getAtomManager().printRoleName(atomId));
			String child = shortFormMap
					.apply(goal.getAtomManager().printConceptName(goal.getAtomManager().getChild(atomId)));
			return "(" + roleName + " some " + child + ")";
		} else {
			return shortFormMap.apply(goal.getAtomManager().printConceptName(atomId));
		}
	}

	protected String printAtoms(Set<Integer> atomIds) {
		StringBuilder sb = new StringBuilder();
		for (Integer atomId : atomIds) {
			sb.append(printAtom(atomId));
			sb.append(", ");
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 2);
		}
		return sb.toString();
	}

	private void runStep1(Definition d, SatInput input) {
		runStep1(new Subsumption(d.getLeft(), d.getRight()), input);
		if (!d.isPrimitive()) {
			runStep1(new Subsumption(d.getRight(), d.getLeft()), input);
		}
	}

	private void runStep1(Disequation e, SatInput input) {
		// choose which direction of the equation does not hold
		Choice c = choice(input, Collections.emptySet(), literalManager, 2);
		runStep1(c.getChoiceLiterals(0), new Dissubsumption(e.getLeft(), e.getRight()), input);
		runStep1(c.getChoiceLiterals(1), new Dissubsumption(e.getRight(), e.getLeft()), input);
	}

	private void runStep1(Dissubsumption e, SatInput input) {
		runStep1(Collections.<Integer> emptySet(), e, input);
	}

	private void runStep1(Equation e, SatInput input) {
		runStep1(new Subsumption(e.getLeft(), e.getRight()), input);
		runStep1(new Subsumption(e.getRight(), e.getLeft()), input);
	}

	/**
	 * Clauses created in Step 1
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

	private void runStep1(Set<Integer> choiceLiterals, Dissubsumption e, SatInput input) {
		if (e.getRight().size() == 0) {
			input.add(choiceLiterals);
		} else if (e.getRight().size() == 1) {
			runStep1Dissubsumption(choiceLiterals, e.getLeft(), e.getRight().iterator().next(), input);
		} else {
			// choose which of the right-hand side atoms does not subsume the
			// left-hand side
			Choice c = choice(input, choiceLiterals, literalManager, e.getRight().size());
			int j = 0;
			for (Integer rightId : e.getRight()) {
				runStep1Dissubsumption(c.addChoiceLiterals(choiceLiterals, j), e.getLeft(), rightId, input);
				j++;
			}
		}
	}

	private void runStep1(Subsumption s, SatInput input) {
		// if top is on the right-hand side, do nothing
		for (Integer rightId : s.getRight()) {
			if (getVariables().contains(rightId)) {
				runStep1SubsumptionVariable(s.getLeft(), rightId, input);
			} else {
				runStep1SubsumptionNonVariableAtom(s.getLeft(), rightId, input);
			}
		}
	}

	private void runStep1Dissubsumption(Set<Integer> choiceLiterals, Set<Integer> leftIds, Integer rightId,
			SatInput input) {
		if (leftIds.size() == 1) {
			// assert single dissubsumption, the rest will be handled by
			// 'addClausesForDisunification'
			Set<Integer> clause = new HashSet<Integer>(choiceLiterals);
			clause.add(-subsumption(leftIds.iterator().next(), rightId));
			input.add(clause);
		} else if (getVariables().contains(rightId)) {
			runStep1DissubsumptionVariable(choiceLiterals, leftIds, rightId, input);
		} else {
			// 'rightId' is a non-variable atom --> it should not subsume any of
			// the leftIds
			runStep1DissubsumptionNonVariableAtom(choiceLiterals, leftIds, rightId, input);
		}
	}

	private void runStep1DissubsumptionNonVariableAtom(Set<Integer> choiceLiterals, Set<Integer> leftIds,
			Integer rightId, SatInput input) {
		Set<Integer> clause;
		for (Integer leftId : leftIds) {
			// TODO negate choice literals?
			clause = new HashSet<Integer>(choiceLiterals);
			clause.add(-subsumption(leftId, rightId));
			input.add(clause);
		}
	}

	private void runStep1DissubsumptionVariable(Set<Integer> choiceLiterals, Set<Integer> leftIds, Integer rightId,
			SatInput input) {
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
			runStep1DissubsumptionNonVariableAtom(currentChoiceLiterals, leftIds, atomId, input);

			// next choice
			j++;
		}
	}

	private void runStep1SubsumptionNonVariableAtom(Set<Integer> leftIds, Integer rightId, SatInput input) {
		input.add(chooseSubsumption(leftIds, rightId));
	}

	private void runStep1SubsumptionVariable(Set<Integer> leftIds, Integer rightId, SatInput input) {
		for (Integer atomId : getNonVariableAtoms()) {
			if (!leftIds.contains(atomId)) {
				input.addImplication(chooseSubsumption(leftIds, atomId), subsumption(rightId, atomId));
			}
		}
	}

	/**
	 * Step 2.1
	 */
	private void runStep2_1(SatInput input) {
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

	/**
	 * Step 2.4
	 */
	private void runStep2_4(SatInput input) {
		for (Integer atomId1 : getConstants()) {
			for (Integer atomId2 : getExistentialRestrictions()) {
				input.add(-subsumption(atomId1, atomId2));
				input.add(-subsumption(atomId2, atomId1));
			}
		}
	}

	/**
	 * Step 2.5
	 *
	 * Transitivity of dis-subsumption
	 */
	private void runStep2_5(SatInput input) throws InterruptedException {
		Collection<Integer> atomIds = getUsedAtomIds();

		for (Integer atomId1 : atomIds) {

			for (Integer atomId2 : atomIds) {

				if (!atomId1.equals(atomId2)) {
					for (Integer atomId3 : atomIds) {

						if (!atomId1.equals(atomId3) && !atomId2.equals(atomId3)) {
							input.addImplication(subsumption(atomId1, atomId3), subsumption(atomId1, atomId2),
									subsumption(atomId2, atomId3));
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
	 */
	private void runStep3_1_r(SatInput input) {
		for (Integer atomId1 : getVariables()) {
			input.add(-order(atomId1, atomId1));
		}
	}

	/**
	 * Step 3.1
	 *
	 * Transitivity for order literals
	 */
	private void runStep3_1_t(SatInput input) throws InterruptedException {
		for (Integer atomId1 : getVariables()) {

			for (Integer atomId2 : getVariables()) {

				for (Integer atomId3 : getVariables()) {

					if (!atomId1.equals(atomId2) && !atomId2.equals(atomId3)) {
						input.addImplication(order(atomId1, atomId3), order(atomId1, atomId2), order(atomId2, atomId3));
					}

				}

			}

			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

		}
	}

	/**
	 * Step 3.2 Connection between order literals and subsumption
	 */
	private void runStep3_2(SatInput input) {
		for (Integer atomId1 : getExistentialRestrictions()) {
			Integer childId = goal.getAtomManager().getChild(atomId1);
			if (getVariables().contains(childId)) {
				for (Integer atomId2 : getVariables()) {
					input.addImplication(order(atomId2, childId), subsumption(atomId2, atomId1));
				}
			}
		}
	}

	/**
	 * Step 2.2 and Step 2.3
	 */
	private void runSteps2_2_N_2_3(SatInput input) {
		for (Integer atomId1 : getExistentialRestrictions()) {

			for (Integer atomId2 : getExistentialRestrictions()) {

				if (!atomId1.equals(atomId2)) {

					Integer atomSubsumption = subsumption(atomId1, atomId2);

					/*
					 * if roles are not equal, then Step 2.2
					 */

					Integer role1 = goal.getAtomManager().getRoleId(atomId1);
					Integer role2 = goal.getAtomManager().getRoleId(atomId2);
					if (!role1.equals(role2)) {
						input.add(-atomSubsumption);

						/*
						 * if the roles are equal, then clause in Step 2.3
						 */
					} else {

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

	public void setShortFormMap(Function<String, String> map) {
		shortFormMap = map;
	}

	protected Integer subsumption(Integer atomId1, Integer atomId2) {
		Literal literal = new SubsumptionLiteral(atomId1, atomId2);
		return literalManager.addAndGetIndex(literal);
	}

	protected Integer subtype(Integer atomId, Integer type) {
		Literal literal = new SubtypeLiteral(atomId, type);
		return literalManager.addAndGetIndex(literal);
	}

	@Override
	protected void updateInfo() {
	}

	private void updateInfo(SatInput satInput) {
		if (this.literalManager != null) {
			addInfo(keyNumberOfPropositions, literalManager.size());
			addInfo("Choice propositions", literalManager.stream().filter(l -> l instanceof ChoiceLiteral).count());
			addInfo("Subsumption propositions",
					literalManager.stream().filter(l -> l instanceof SubsumptionLiteral).count());
			addInfo("Subtype propositions", literalManager.stream().filter(l -> l instanceof SubtypeLiteral).count());
			addInfo("Order propositions", literalManager.stream().filter(l -> l instanceof OrderLiteral).count());
		}
		long numberOfClauses = satInput.getClauses().size();
		long totalSize = satInput.getClauses().stream().mapToInt(c -> c.size()).sum();
		addInfo(keyNumberOfClauses, numberOfClauses);
		addInfo(keyTotalSize, totalSize);
		addInfo(keyAverageSize, ((float) totalSize) / ((float) numberOfClauses));
	}

}
