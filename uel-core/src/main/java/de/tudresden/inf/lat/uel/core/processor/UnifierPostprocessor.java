/**
 * 
 */
package de.tudresden.inf.lat.uel.core.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.tudresden.inf.lat.uel.core.renderer.StringRenderer;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.impl.DefinitionSet;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

/**
 * This class provides a method for minimizing unifiers after they have been
 * computed.
 * 
 * @author Stefan Borgwardt
 *
 */
public class UnifierPostprocessor {

	private AtomManager atomManager;
	private Goal goal;
	private StringRenderer renderer;

	/**
	 * Initialize the post-processor.
	 * 
	 * @param atomManager
	 *            the atom manager
	 * @param goal
	 *            the unification problem
	 * @param renderer
	 *            the string renderer for debug output
	 */
	public UnifierPostprocessor(AtomManager atomManager, Goal goal, StringRenderer renderer) {
		this.atomManager = atomManager;
		this.goal = goal;
		this.renderer = renderer;
	}

	private boolean areEquivalent(Integer leftId, DefinitionSet leftDefs, Integer rightId, DefinitionSet rightDefs) {
		return isSubsumed(leftId, leftDefs, rightId, rightDefs) && isSubsumed(rightId, rightDefs, leftId, leftDefs);
	}

	/**
	 * Compares two unifiers for equivalence (w.r.t. their definitions and the
	 * background definitions).
	 * 
	 * @param unifier1
	 *            the first unifier
	 * @param unifier2
	 *            the second unifier
	 * @return true iff the unifiers are equivalent
	 */
	public boolean areEquivalent(Unifier unifier1, Unifier unifier2) {
		for (Integer varId : atomManager.getUserVariables()) {
			// compare only variables without VAR suffix
			if (!atomManager.printConceptName(varId).endsWith(AtomManager.VAR_SUFFIX)) {
				if (!areEquivalent(varId, unifier1.getDefinitions(), varId, unifier2.getDefinitions())) {
					return false;
				}
			}
		}
		return true;
	}

	private int compare(Integer atomId1, Integer atomId2, DefinitionSet defs) {
		boolean sub1 = isSubsumed(atomId1, defs, atomId2, defs);
		boolean sub2 = isSubsumed(atomId2, defs, atomId1, defs);
		if (!sub1 && !sub2) {
			// the atoms are incomparable
			return 0;
		}
		if (sub1 && !sub2) {
			// atomId1 is strictly smaller equal to atomId2
			return -1;
		}
		if (sub2 && !sub1) {
			return 1;
		}
		// otherwise, atomId1 and atomId2 are equivalent and the order remains
		// to be determined

		int size1 = getStructuralSize(atomId1, defs);
		int size2 = getStructuralSize(atomId2, defs);
		if (size1 < size2) {
			// the structure of atomId1 is smaller than that of atomId2
			return -1;
		}
		if (size2 < size1) {
			// atomId2 is smaller
			return 1;
		}

		// structural size is the same; finally compare actual size
		size1 = getFullSize(atomId1, defs);
		size2 = getFullSize(atomId2, defs);
		return Integer.compare(size1, size2);
	}

	private Set<Integer> expand(Set<Integer> atomIds, DefinitionSet defs) {
		Set<Integer> toVisit = new HashSet<Integer>();
		toVisit.addAll(atomIds);
		Map<Integer, Set<Integer>> expansions = new HashMap<Integer, Set<Integer>>();

		// expand definitions and substitutions "forward"
		while (!toVisit.isEmpty()) {
			Integer atomId = toVisit.iterator().next();
			Set<Integer> exp = expandOneStep(atomId, defs);
			expansions.put(atomId, exp);

			toVisit.addAll(exp);
			toVisit.removeAll(expansions.keySet());
		}

		// return only those atoms that are not replaced by other atoms (i.e.,
		// non-variable atoms or variables without definitions)
		return expansions.entrySet().stream().filter(e -> e.getValue().contains(e.getKey()))
				.map(Entry<Integer, Set<Integer>>::getKey).collect(Collectors.toSet());
	}

	private Set<Integer> expandOneStep(Integer atomId, DefinitionSet defs) {
		if (atomManager.getConstants().contains(atomId) || atomManager.getExistentialRestrictions().contains(atomId)
				|| atomManager.getUndefNames().contains(atomId)) {
			// non-variable atoms and UNDEF names are not expanded
			return Collections.singleton(atomId);
		}

		// now 'atomId' must be a (non-UNDEF) variable
		Set<Integer> definiens = goal.getDefiniens(atomId);
		if (definiens != null) {
			// if there exists a background definition, use that one
			return definiens;
		}

		definiens = defs.getDefiniens(atomId);
		if (definiens != null) {
			// try to expand the variable using its substitution
			return definiens;
		}

		// there was no defintion or substitution (e.g. if the unifier is
		// incomplete, or we have not yet started the unfication algorithm) ->
		// return the variable itself
		return Collections.singleton(atomId);
	}

	private int getFullSize(Integer atomId, DefinitionSet defs) {
		if (atomManager.getFlatteningVariables().contains(atomId)) {
			Definition def = goal.getDefinition(atomId);
			Set<Integer> expanded;
			if (def != null) {
				expanded = def.getRight();
			} else {
				expanded = defs.getDefiniens(atomId);
			}
			return sum(expanded, id -> getFullSize(id, defs));
		} else if (atomManager.getExistentialRestrictions().contains(atomId)) {
			return atomManager.getRoleId(atomId) + 100 * getFullSize(atomManager.getChild(atomId), defs);
		} else {
			return atomId;
		}
	}

	private int getStructuralSize(Integer atomId, DefinitionSet defs) {
		if (atomManager.getFlatteningVariables().contains(atomId)) {
			Definition def = goal.getDefinition(atomId);
			Set<Integer> expanded;
			if (def != null) {
				expanded = def.getRight();
			} else {
				expanded = defs.getDefiniens(atomId);
			}
			return sum(expanded, id -> getStructuralSize(id, defs));
		} else if (atomManager.getExistentialRestrictions().contains(atomId)) {
			return 2 + getStructuralSize(atomManager.getChild(atomId), defs);
		} else {
			return 1;
		}
	}

	private boolean isSubsumed(Integer leftId, DefinitionSet leftDefs, Integer rightId, DefinitionSet rightDefs) {
		return isSubsumed(Collections.singleton(leftId), leftDefs, Collections.singleton(rightId), rightDefs);
	}

	public boolean isSubsumed(Integer leftId, Integer rightId) {
		return isSubsumed(leftId, DefinitionSet.EMPTY_SET, rightId, DefinitionSet.EMPTY_SET);
	}

	private boolean isSubsumed(Set<Integer> leftIds, DefinitionSet leftDefs, Integer rightId, DefinitionSet rightDefs) {
		if (!atomManager.getExistentialRestrictions().contains(rightId)) {
			// every constant must occur also on the left-hand side
			return leftIds.contains(rightId);
		}

		Integer roleId = atomManager.getRoleId(rightId);
		Integer childId = atomManager.getChild(rightId);
		for (Integer leftId : leftIds) {
			if (atomManager.getExistentialRestrictions().contains(leftId)) {
				if (atomManager.getRoleId(leftId).equals(roleId)) {
					if (isSubsumed(atomManager.getChild(leftId), leftDefs, childId, rightDefs)) {
						// a matching existential restriction was found on the
						// left-hand side
						return true;
					}
				}
			}
		}

		// the existential restriction is not matched on the left-hand side
		return false;
	}

	private boolean isSubsumed(Set<Integer> leftIds, DefinitionSet leftDefs, Set<Integer> rightIds,
			DefinitionSet rightDefs) {
		Set<Integer> leftExpIds = expand(leftIds, leftDefs);
		Set<Integer> rightExpIds = expand(rightIds, rightDefs);

		for (Integer rightId : rightExpIds) {
			if (!isSubsumed(leftExpIds, leftDefs, rightId, rightDefs)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Minimize a given unifier, resulting in an equivalent unifier.
	 * 
	 * @param unifier
	 *            the original unifier
	 * @return the new unifier
	 */
	public Unifier minimizeUnifier(Unifier unifier) {
		// copy the unifier
		DefinitionSet defs = new DefinitionSet();
		for (Definition def : unifier.getDefinitions()) {
			defs.add(new Definition(def));
		}

		// saturate all substitutions of user variables by exhaustively applying
		// all background definitions "backward"
		saturateWithDefinitions(defs);
		// System.out.println(renderer.renderUnifier(new Unifier(defs), false,
		// false, true));

		for (Integer varId : atomManager.getVariables()) {
			// if (atomManager.getDefinitionVariables().contains(varId)) {
			// continue;
			// }
			// System.out.println("*** Minimizing substitution set of " +
			// renderer.renderAtom(varId, false));
			// System.out.println(renderer.renderAtomList("Original substitution
			// set", defs.getDefiniens(varId)));

			// keep only minimal atoms (w.r.t. subsumption and size)
			List<Integer> minimalAtoms = minimalElements((a, b) -> compare(a, b, defs), defs.getDefiniens(varId));
			defs.getDefiniens(varId).retainAll(minimalAtoms);

			// System.out.println(renderer.renderAtomList("Final substitution
			// set", defs.getDefiniens(varId)));
			// System.out.println();
			// System.out.println();
		}

		return new Unifier(defs, unifier.getTypeAssignment());
	}

	public static <T> List<T> minimalElements(Comparator<T> comparator, Set<T> set) {
		List<T> minimalElements = new ArrayList<T>();
		for (T element : set) {
			// check if there is already a minimal element smaller than
			// 'element' ...
			List<T> replaceElements = new ArrayList<T>();
			boolean minimal = true;
			for (T minimalElement : minimalElements) {
				int c = comparator.compare(minimalElement, element);
				if (c < 0) {
					minimal = false;
					break;
				}
				if (c > 0) {
					replaceElements.add(minimalElement);
				}
				// c == 0 means that the elements are incomparable
			}
			if (minimal) {
				minimalElements.removeAll(replaceElements);
				minimalElements.add(element);
			}
		}
		return minimalElements;
	}

	private void saturateWithDefinitions(DefinitionSet defs) {
		for (Integer varId : atomManager.getVariables()) {
			saturateWithDefinitions(defs.getDefiniens(varId));
		}
	}

	private void saturateWithDefinitions(Set<Integer> atomIds) {
		boolean changed = true;

		while (changed) {
			changed = false;

			for (Integer varId : atomManager.getDefinitionVariables()) {
				if (!atomIds.contains(varId)) {
					if (atomIds.containsAll(goal.getDefiniens(varId))) {
						atomIds.add(varId);
						changed = true;
					}
				}
			}
		}
	}

	private <T> int sum(Set<T> set, Function<T, Integer> map) {
		return set.stream().map(map).reduce(0, Integer::sum);
	}

}
