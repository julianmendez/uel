package de.tudresden.inf.lat.uel.asp.solver;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Disequation;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.Subsumption;
import de.tudresden.inf.lat.uel.type.impl.AbstractUnificationAlgorithm;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

/**
 * This class prepares the input for an ASP solver.
 * 
 * @author Stefan Borgwardt
 * 
 */
public class AspInput {

	private Goal goal;
	private AbstractUnificationAlgorithm parent;

	public AspInput(Goal goal, AbstractUnificationAlgorithm parent) {
		this.goal = goal;
		this.parent = parent;
	}

	public AtomManager getAtomManager() {
		return goal.getAtomManager();
	}

	public void appendProgram(OutputStreamBuilder output) {
		int i = 1;
		Set<Integer> remainingAtoms = new HashSet<Integer>(goal.getAtomManager().getExistentialRestrictions());
		Set<Integer> emptySet = new HashSet<Integer>();
		for (Definition d : goal.getDefinitions()) {
			encodeAxiom(output, d, i, "definition", "eq", remainingAtoms);
			i++;
		}
		for (Equation e : goal.getEquations()) {
			encodeAxiom(output, e, i, "equation", "eq", remainingAtoms);
			i++;
		}
		for (Subsumption s : goal.getSubsumptions()) {
			encodeSubsumption(output, s, i, remainingAtoms);
			i++;
		}
		for (Disequation e : goal.getDisequations()) {
			encodeAxiom(output, e, i, "disequation", "diseq", remainingAtoms);
			i++;
		}
		for (Dissubsumption s : goal.getDissubsumptions()) {
			encodeAxiom(output, s, i, "dissubsumption", "dissubs", remainingAtoms);
			i++;
		}

		output.append("% Additional atoms (blank existential restrictions)");
		output.append(System.lineSeparator());
		for (Integer remainingAtom : remainingAtoms) {
			output.append("atom(");
			encodeAtom(output, remainingAtom, emptySet);
			output.append(").");
			output.append(System.lineSeparator());
			output.append("atom(");
			encodeAtom(output, goal.getAtomManager().getChild(remainingAtom), emptySet);
			output.append(").");
			output.append(System.lineSeparator());
		}
		output.append(System.lineSeparator());

		output.append("% Types");
		output.append(System.lineSeparator());
		for (Integer type : goal.getTypes()) {
			output.append("type(");
			encodeAtom(output, type, emptySet);
			output.append(").");
			output.append(System.lineSeparator());
		}
		output.append(System.lineSeparator());

		output.append("% Domains");
		output.append(System.lineSeparator());
		for (Entry<Integer, Set<Integer>> e : goal.getDomains().entrySet()) {
			for (Integer type : e.getValue()) {
				output.append("domain(r");
				output.append(e.getKey());
				output.append(",");
				if (goal.getRoleGroupTypes().containsValue(type)) {
					output.append("rg(");
					encodeAtom(output, goal.getAtomManager().getAtom(getOriginalType(type)), emptySet);
					output.append(")");
				} else {
					encodeAtom(output, type, emptySet);
				}
				output.append(").");
				output.append(System.lineSeparator());
			}
		}
		output.append(System.lineSeparator());

		output.append("% Ranges");
		output.append(System.lineSeparator());
		for (Entry<Integer, Set<Integer>> e : goal.getRanges().entrySet()) {
			for (Integer type : e.getValue()) {
				output.append("range(r");
				output.append(e.getKey());
				output.append(",");
				encodeAtom(output, type, emptySet);
				output.append(").");
				output.append(System.lineSeparator());
			}
		}
		output.append(System.lineSeparator());

		output.append("% 'RoleGroup'");
		output.append(System.lineSeparator());
		output.append("rolegroup(r");
		output.append(goal.getAtomManager().getRoleId(goal.SNOMED_RoleGroup_URI()));
		output.append(").");
		output.append(System.lineSeparator());
		output.append(System.lineSeparator());

		output.append("% UNDEF names");
		output.append(System.lineSeparator());
		for (Integer undefId : goal.getAtomManager().getUndefNames()) {
			Integer origId = goal.getAtomManager().removeUndef(undefId);
			output.append("undef(");
			encodeAtom(output, origId, emptySet);
			output.append(",");
			encodeAtom(output, undefId, emptySet);
			output.append(").");
			output.append(System.lineSeparator());
		}
		output.append(System.lineSeparator());

		output.append("% Role number restrictions");
		output.append(System.lineSeparator());
		for (Entry<Integer, Integer> e : goal.getRoleNumberRestrictions().entrySet()) {
			output.append("number(r");
			output.append(e.getKey());
			output.append(",");
			output.append(e.getValue());
			output.append(").");
			output.append(System.lineSeparator());
		}
		output.append(System.lineSeparator());

		output.append("% Compatibility");
		output.append(System.lineSeparator());
		for (Integer var1 : goal.getAtomManager().getVariables()) {
			for (Integer var2 : goal.getAtomManager().getVariables()) {
				if (goal.areCompatible(var1, var2)) {
					// encoding.append("% " + parent.printAtom(var1) + " and " +
					// parent.printAtom(var2));
					// encoding.append(System.lineSeparator());
					output.append("compatible(var(x");
					output.append(var1);
					output.append("),var(x");
					output.append(var2);
					output.append(")).");
					output.append(System.lineSeparator());
				}
			}
		}
		output.append(System.lineSeparator());

		// output.append("% User variables");
		// output.append(System.lineSeparator());
		// for (Integer var : goal.getAtomManager().getUserVariables()) {
		// output.append("relevant(x");
		// output.append(var);
		// output.append(").");
		// output.append(System.lineSeparator());
		// }
		// output.append(System.lineSeparator());
	}

	private Integer getOriginalType(Integer roleGroupType) {
		for (Integer type : goal.getRoleGroupTypes().keySet()) {
			if (goal.getRoleGroupTypes().get(type).equals(roleGroupType)) {
				return type;
			}
		}
		return null;
	}

	private void encodeAxiom(OutputStreamBuilder output, Axiom d, int index, String comment, String predicate,
			Set<Integer> remainingAtoms) {
		output.append("%");
		output.append(comment);
		output.append(" ");
		output.append(index);
		output.append(System.lineSeparator());

		output.append(predicate);
		output.append("(");
		output.append(index);
		output.append(").");
		output.append(System.lineSeparator());

		encodeAtoms(output, d, index, remainingAtoms);
		output.append(System.lineSeparator());
	}

	private void encodeSubsumption(OutputStreamBuilder output, Subsumption s, int index, Set<Integer> remainingAtoms) {
		// TODO more direct ASP encoding for subsumptions
		output.append("%subsumption ");
		output.append(index);
		output.append(System.lineSeparator());

		output.append("eq(");
		output.append(index);
		output.append(").");
		output.append(System.lineSeparator());

		encodeAtoms(output, s, index, remainingAtoms);
		encodeAtoms(output, s.getLeft(), 1, index, remainingAtoms);
		output.append(System.lineSeparator());
	}

	private void encodeAtoms(OutputStreamBuilder output, Axiom axiom, int index, Set<Integer> remainingAtoms) {
		encodeAtoms(output, axiom.getLeft(), 0, index, remainingAtoms);
		encodeAtoms(output, axiom.getRight(), 1, index, remainingAtoms);
	}

	private void encodeAtoms(OutputStreamBuilder output, Set<Integer> atomIds, int side, int index,
			Set<Integer> remainingAtoms) {
		for (Integer atomId : atomIds) {
			encodeAtom(output, atomId, side, index, remainingAtoms);
		}
	}

	private void encodeAtom(OutputStreamBuilder output, Integer atomId, int side, int equationId,
			Set<Integer> remainingAtoms) {
		output.append("hasatom(");
		encodeAtom(output, atomId, remainingAtoms);
		output.append(", ");
		output.append(side);
		output.append(", ");
		output.append(equationId);
		output.append(").");
		output.append(System.lineSeparator());
	}

	private void encodeAtom(OutputStreamBuilder output, Atom atom, Set<Integer> remainingAtoms) {
		remainingAtoms.remove(goal.getAtomManager().getIndex(atom));
		if (atom.isExistentialRestriction()) {
			ExistentialRestriction ex = (ExistentialRestriction) atom;
			output.append("exists(r");
			output.append(ex.getRoleId());
			output.append(", ");
			encodeAtom(output, ex.getConceptName(), remainingAtoms);
		} else {
			if (atom.isVariable()) {
				output.append("var(x");
			} else {
				output.append("cname(a");
			}
			output.append(goal.getAtomManager().getIndex(atom));
		}
		output.append(")");
	}

	private void encodeAtom(OutputStreamBuilder output, Integer origId, Set<Integer> remainingAtoms) {
		encodeAtom(output, goal.getAtomManager().getAtom(origId), remainingAtoms);
	}
}
