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
 * @author Stefan Borgwardt
 *
 */
public class UnifierPostprocessor {

	AtomManager atomManager;
	Goal goal;
	StringRenderer renderer;

	public UnifierPostprocessor(AtomManager atomManager, Goal goal, StringRenderer renderer) {
		this.atomManager = atomManager;
		this.goal = goal;
		this.renderer = renderer;
	}

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
			System.out.println("*** Minimizing substitution set of " + renderer.renderAtom(varId, false));
			System.out.println(renderer.renderAtomList("Original substitution set", defs.getDefiniens(varId)));

			// keep only minimal atoms (w.r.t. subsumption and size)
			Set<Integer> minimalAtoms = new HashSet<Integer>();
			for (Integer atomId : defs.getDefiniens(varId)) {
				// if there is not yet a minimal atom smaller than 'atomId' ...
				List<Integer> replaceAtoms = new ArrayList<Integer>();
				boolean notMinimal = false;
				for (Integer minimalId : minimalAtoms) {
					int c = compare(minimalId, atomId, defs);
					if (c < 0) {
						System.out.println(renderer.renderAtom(minimalId, true) + " is smaller than "
								+ renderer.renderAtom(atomId, true));
						notMinimal = true;
						break;
					}
					if (c > 0) {
						System.out.println(renderer.renderAtom(atomId, true) + " is smaller than "
								+ renderer.renderAtom(minimalId, true));
						replaceAtoms.add(minimalId);
					}
					// c == 0 means that the atoms are incomparable
				}
				if (notMinimal) {
					System.out.println(renderer.renderAtom(atomId, true) + " is not minimal.");
				} else {
					minimalAtoms.removeAll(replaceAtoms);
					minimalAtoms.add(atomId);
					System.out.println("New minimal atom: " + renderer.renderAtom(atomId, true));
				}
			}
			defs.getDefiniens(varId).retainAll(minimalAtoms);

			System.out.println(renderer.renderAtomList("Final substitution set", defs.getDefiniens(varId)));
			System.out.println();
			System.out.println();
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

	private int compare(Integer atomId1, Integer atomId2, DefinitionSet defs) {
		boolean sub1 = isSubsumed(atomId1, atomId2, defs);
		boolean sub2 = isSubsumed(atomId2, atomId1, defs);
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

	private <T> int sum(Set<T> set, Function<T, Integer> map) {
		return set.stream().map(map).reduce(0, Integer::sum);
	}

	private Set<Integer> expand(Integer atomId, DefinitionSet defs) {
		Set<Integer> expanded = new HashSet<Integer>();
		Set<Integer> visited = new HashSet<Integer>();
		Set<Integer> toVisit = new HashSet<Integer>();
		expanded.add(atomId);
		toVisit.add(atomId);

		while (!toVisit.isEmpty()) {
			atomId = toVisit.iterator().next();
			expanded.remove(atomId);
			toVisit.remove(atomId);
			visited.add(atomId);

			System.out.print("Expanding " + renderer.renderAtom(atomId, false) + " ... ");
			Set<Integer> exp = expandOneStep(atomId, defs);
			System.out.println(renderer.renderAtomList("to", exp));
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

	private boolean isSubsumed(Integer leftId, Integer rightId, DefinitionSet defs) {
		System.out.println("Checking subsumption between " + renderer.renderAtom(leftId, false) + " and "
				+ renderer.renderAtom(rightId, false));
		Set<Integer> leftAtoms = expand(leftId, defs);
		Set<Integer> rightAtoms = expand(rightId, defs);
		System.out.println(renderer.renderAtomList("Left expansion", leftAtoms));
		System.out.println(renderer.renderAtomList("Right expansion", rightAtoms));

		for (Integer atomId : rightAtoms) {
			if (!isSubsumed(leftAtoms, atomId, defs)) {
				System.out.println(renderer.renderAtom(leftId, false) + " is not subsumed by "
						+ renderer.renderAtom(rightId, false));
				return false;
			}
		}

		System.out
				.println(renderer.renderAtom(leftId, false) + " is subsumed by " + renderer.renderAtom(rightId, false));
		return true;
	}

	private boolean isSubsumed(Set<Integer> leftIds, Integer rightId, DefinitionSet defs) {
		if (leftIds.contains(rightId)) {
			return true;
		}
		if (!atomManager.getExistentialRestrictions().contains(rightId)) {
			// every constant must occur also on the left-hand side
			return false;
		}

		Integer roleId = atomManager.getExistentialRestriction(rightId).getRoleId();
		Integer childId = atomManager.getChild(rightId);
		for (Integer leftId : leftIds) {
			if (atomManager.getExistentialRestrictions().contains(leftId)) {
				if (atomManager.getExistentialRestriction(leftId).getRoleId().equals(roleId)) {
					if (isSubsumed(atomManager.getChild(leftId), childId, defs)) {
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

}
