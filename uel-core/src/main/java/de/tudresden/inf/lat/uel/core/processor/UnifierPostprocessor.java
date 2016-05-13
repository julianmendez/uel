/**
 * 
 */
package de.tudresden.inf.lat.uel.core.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

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
		Set<Integer> expanded = new HashSet<Integer>();
		Set<Integer> visited = new HashSet<Integer>();
		Set<Integer> toVisit = new HashSet<Integer>();
		expanded.addAll(atomIds);
		toVisit.addAll(atomIds);

		while (!toVisit.isEmpty()) {
			Integer atomId = toVisit.iterator().next();
			expanded.remove(atomId);
			toVisit.remove(atomId);
			visited.add(atomId);

			// System.out.print("Expanding " + renderer.renderAtom(atomId,
			// false) + " ... ");
			Set<Integer> exp = expandOneStep(atomId, defs);
			// System.out.println(renderer.renderAtomList("to", exp));
			expanded.addAll(exp);
			toVisit.addAll(exp);
			toVisit.removeAll(visited);
		}

		return expanded;
	}

	private Set<Integer> expandOneStep(Integer atomId, DefinitionSet defs) {
		if (atomManager.getConstants().contains(atomId) || atomManager.getExistentialRestrictions().contains(atomId)) {
			// non-variable atoms cannot be expanded
			return Collections.singleton(atomId);
		} else if (atomManager.getUserVariables().contains(atomId)) {
			if (atomManager.getUndefNames().contains(atomId)) {
				// UNDEF variables are not expanded
				return Collections.singleton(atomId);
			} else {
				// all other user variables are expanded using their original
				// definitions
				return defs.getDefiniens(atomId);
			}
		} else {
			// 'atomId' must be a definition or flattening variable
			Definition def = goal.getDefinition(atomId);
			if (def != null) {
				return def.getRight();
			} else {
				return defs.getDefiniens(atomId);
			}
		}
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
			return atomManager.getExistentialRestriction(atomId).getRoleId()
					+ 100 * getFullSize(atomManager.getChild(atomId), defs);
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

	private boolean isSubsumed(Set<Integer> leftIds, DefinitionSet leftDefs, Integer rightId, DefinitionSet rightDefs) {
		if (!atomManager.getExistentialRestrictions().contains(rightId)) {
			// every constant must occur also on the left-hand side
			return leftIds.contains(rightId);
		}

		Integer roleId = atomManager.getExistentialRestriction(rightId).getRoleId();
		Integer childId = atomManager.getChild(rightId);
		for (Integer leftId : leftIds) {
			if (atomManager.getExistentialRestrictions().contains(leftId)) {
				if (atomManager.getExistentialRestriction(leftId).getRoleId().equals(roleId)) {
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
	 * Minimize a given unifier, resulting in a new unifier.
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

		replaceUndefNames(defs);

		for (Integer varId : atomManager.getVariables()) {
			if (atomManager.getDefinitionVariables().contains(varId)) {
				continue;
			}
			// System.out.println("*** Minimizing substitution set of " +
			// renderer.renderAtom(varId, false));
			// System.out.println(renderer.renderAtomList("Original substitution
			// set", defs.getDefiniens(varId)));

			// keep only minimal atoms (w.r.t. subsumption and size)
			Set<Integer> minimalAtoms = new HashSet<Integer>();
			for (Integer atomId : defs.getDefiniens(varId)) {
				// if there is not yet a minimal atom smaller than 'atomId' ...
				List<Integer> replaceAtoms = new ArrayList<Integer>();
				boolean notMinimal = false;
				for (Integer minimalId : minimalAtoms) {
					int c = compare(minimalId, atomId, defs);
					if (c < 0) {
						// System.out.println(renderer.renderAtom(minimalId,
						// true) + " is smaller than "
						// + renderer.renderAtom(atomId, true));
						notMinimal = true;
						break;
					}
					if (c > 0) {
						// System.out.println(renderer.renderAtom(atomId, true)
						// + " is smaller than "
						// + renderer.renderAtom(minimalId, true));
						replaceAtoms.add(minimalId);
					}
					// c == 0 means that the atoms are incomparable
				}
				if (notMinimal) {
					// System.out.println(renderer.renderAtom(atomId, true) + "
					// is not minimal.");
				} else {
					minimalAtoms.removeAll(replaceAtoms);
					minimalAtoms.add(atomId);
					// System.out.println("New minimal atom: " +
					// renderer.renderAtom(atomId, true));
				}
			}
			defs.getDefiniens(varId).retainAll(minimalAtoms);

			// System.out.println(renderer.renderAtomList("Final substitution
			// set", defs.getDefiniens(varId)));
			// System.out.println();
			// System.out.println();
		}

		return new Unifier(defs, unifier.getTypeAssignment());

	}

	private void replaceUndefNames(DefinitionSet defs) {
		// replace UNDEF names by originals
		for (Integer varId : atomManager.getUserVariables()) {
			Set<Integer> definiens = defs.getDefiniens(varId);
			for (Integer undefId : atomManager.getUndefNames()) {
				if (definiens.contains(undefId)) {
					definiens.remove(undefId);
					definiens.add(goal.getAtomManager().removeUndef(undefId));
				}
			}
		}
	}

	private <T> int sum(Set<T> set, Function<T, Integer> map) {
		return set.stream().map(map).reduce(0, Integer::sum);
	}

}
