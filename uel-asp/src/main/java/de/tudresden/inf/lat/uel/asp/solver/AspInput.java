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
	private String program;
	private AbstractUnificationAlgorithm parent;

	public AspInput(Goal goal, AbstractUnificationAlgorithm parent) {
		this.goal = goal;
		this.parent = parent;
		updateProgram();
//		System.out.println(program);
	}

	public String getProgram() {
		return program;
	}

	public AtomManager getAtomManager() {
		return goal.getAtomManager();
	}

	private void updateProgram() {
		StringBuilder encoding = new StringBuilder();
		int i = 1;
		Set<Integer> remainingAtoms = new HashSet<Integer>(goal.getAtomManager().getExistentialRestrictions());
		Set<Integer> emptySet = new HashSet<Integer>();
		for (Definition d : goal.getDefinitions()) {
			encodeAxiom(encoding, d, i, "definition", "eq", remainingAtoms);
			i++;
		}
		for (Equation e : goal.getEquations()) {
			encodeAxiom(encoding, e, i, "equation", "eq", remainingAtoms);
			i++;
		}
		for (Subsumption s : goal.getSubsumptions()) {
			encodeSubsumption(encoding, s, i, remainingAtoms);
			i++;
		}
		for (Disequation e : goal.getDisequations()) {
			encodeAxiom(encoding, e, i, "disequation", "diseq", remainingAtoms);
			i++;
		}
		for (Dissubsumption s : goal.getDissubsumptions()) {
			encodeAxiom(encoding, s, i, "dissubsumption", "dissubs", remainingAtoms);
			i++;
		}

		encoding.append("% Additional atoms (blank existential restrictions)");
		encoding.append(System.lineSeparator());
		for (Integer remainingAtom : remainingAtoms) {
			encoding.append("atom(");
			encodeAtom(encoding, remainingAtom, emptySet);
			encoding.append(").");
			encoding.append(System.lineSeparator());
			encoding.append("atom(");
			encodeAtom(encoding, goal.getAtomManager().getChild(remainingAtom), emptySet);
			encoding.append(").");
			encoding.append(System.lineSeparator());
		}
		encoding.append(System.lineSeparator());

		encoding.append("% Types");
		encoding.append(System.lineSeparator());
		for (Integer type : goal.getTypes()) {
			encoding.append("type(");
			encodeAtom(encoding, type, emptySet);
			encoding.append(").");
			encoding.append(System.lineSeparator());
		}
		encoding.append(System.lineSeparator());

		encoding.append("% Domains");
		encoding.append(System.lineSeparator());
		for (Entry<Integer, Set<Integer>> e : goal.getDomains().entrySet()) {
			for (Integer type : e.getValue()) {
				encoding.append("domain(r");
				encoding.append(e.getKey());
				encoding.append(",");
				if (goal.getRoleGroupTypes().containsValue(type)) {
					encoding.append("rg(");
					encodeAtom(encoding, goal.getAtomManager().getAtom(getOriginalType(type)), emptySet);
					encoding.append(")");
				} else {
					encodeAtom(encoding, type, emptySet);
				}
				encoding.append(").");
				encoding.append(System.lineSeparator());
			}
		}
		encoding.append(System.lineSeparator());

		encoding.append("% Ranges");
		encoding.append(System.lineSeparator());
		for (Entry<Integer, Set<Integer>> e : goal.getRanges().entrySet()) {
			for (Integer type : e.getValue()) {
				encoding.append("range(r");
				encoding.append(e.getKey());
				encoding.append(",");
				encodeAtom(encoding, type, emptySet);
				encoding.append(").");
				encoding.append(System.lineSeparator());
			}
		}
		encoding.append(System.lineSeparator());

		encoding.append("% 'RoleGroup'");
		encoding.append(System.lineSeparator());
		encoding.append("rolegroup(r");
		encoding.append(goal.getAtomManager().getRoleId("http://www.ihtsdo.org/RoleGroup"));
		encoding.append(").");
		encoding.append(System.lineSeparator());
		encoding.append(System.lineSeparator());

		encoding.append("% UNDEF names");
		encoding.append(System.lineSeparator());
		for (Integer undefId : goal.getAtomManager().getUndefNames()) {
			Integer origId = goal.getAtomManager().removeUndef(undefId);
			encoding.append("undef(");
			encodeAtom(encoding, origId, emptySet);
			encoding.append(",");
			encodeAtom(encoding, undefId, emptySet);
			encoding.append(").");
			encoding.append(System.lineSeparator());
		}
		encoding.append(System.lineSeparator());

		encoding.append("% role number restrictions");
		encoding.append(System.lineSeparator());
		for (Entry<Integer, Integer> e : goal.getRoleNumberRestrictions().entrySet()) {
			encoding.append("number(r");
			encoding.append(e.getKey());
			encoding.append(",");
			encoding.append(e.getValue());
			encoding.append(").");
			encoding.append(System.lineSeparator());
		}
		encoding.append(System.lineSeparator());

		encoding.append("% Compatibility");
		encoding.append(System.lineSeparator());
		for (Integer var1 : goal.getAtomManager().getVariables()) {
			for (Integer var2 : goal.getAtomManager().getVariables()) {
				if (goal.areCompatible(var1, var2)) {
					// encoding.append("% " + parent.printAtom(var1) + " and " +
					// parent.printAtom(var2));
					// encoding.append(System.lineSeparator());
					encoding.append("compatible(var(x");
					encoding.append(var1);
					encoding.append("),var(x");
					encoding.append(var2);
					encoding.append(")).");
					encoding.append(System.lineSeparator());
				}
			}
		}
		encoding.append(System.lineSeparator());

		encoding.append("% User variables");
		encoding.append(System.lineSeparator());
		for (Integer var : goal.getAtomManager().getUserVariables()) {
			encoding.append("relevant(x");
			encoding.append(var);
			encoding.append(").");
			encoding.append(System.lineSeparator());
		}
		encoding.append(System.lineSeparator());

		program = encoding.toString();
	}

	private Integer getOriginalType(Integer roleGroupType) {
		for (Integer type : goal.getRoleGroupTypes().keySet()) {
			if (goal.getRoleGroupTypes().get(type).equals(roleGroupType)) {
				return type;
			}
		}
		return null;
	}

	private void encodeAxiom(StringBuilder encoding, Axiom d, int index, String comment, String predicate,
			Set<Integer> remainingAtoms) {
		encoding.append("%");
		encoding.append(comment);
		encoding.append(" ");
		encoding.append(index);
		encoding.append(System.lineSeparator());

		encoding.append(predicate);
		encoding.append("(");
		encoding.append(index);
		encoding.append(").");
		encoding.append(System.lineSeparator());

		encodeAtoms(encoding, d, index, remainingAtoms);
		encoding.append(System.lineSeparator());
	}

	private void encodeSubsumption(StringBuilder encoding, Subsumption s, int index, Set<Integer> remainingAtoms) {
		// TODO more direct ASP encoding for subsumptions
		encoding.append("%subsumption ");
		encoding.append(index);
		encoding.append(System.lineSeparator());

		encoding.append("eq(");
		encoding.append(index);
		encoding.append(").");
		encoding.append(System.lineSeparator());

		encodeAtoms(encoding, s, index, remainingAtoms);
		encodeAtoms(encoding, s.getLeft(), 1, index, remainingAtoms);
		encoding.append(System.lineSeparator());
	}

	private void encodeAtoms(StringBuilder encoding, Axiom axiom, int index, Set<Integer> remainingAtoms) {
		encodeAtoms(encoding, axiom.getLeft(), 0, index, remainingAtoms);
		encodeAtoms(encoding, axiom.getRight(), 1, index, remainingAtoms);
	}

	private void encodeAtoms(StringBuilder encoding, Set<Integer> atomIds, int side, int index,
			Set<Integer> remainingAtoms) {
		for (Integer atomId : atomIds) {
			encodeAtom(encoding, atomId, side, index, remainingAtoms);
		}
	}

	private void encodeAtom(StringBuilder encoding, Integer atomId, int side, int equationId,
			Set<Integer> remainingAtoms) {
		encoding.append("hasatom(");
		encodeAtom(encoding, atomId, remainingAtoms);
		encoding.append(", ");
		encoding.append(side);
		encoding.append(", ");
		encoding.append(equationId);
		encoding.append(").");
		encoding.append(System.lineSeparator());
	}

	private void encodeAtom(StringBuilder encoding, Atom atom, Set<Integer> remainingAtoms) {
		remainingAtoms.remove(goal.getAtomManager().getIndex(atom));
		if (atom.isExistentialRestriction()) {
			ExistentialRestriction ex = (ExistentialRestriction) atom;
			encoding.append("exists(r");
			encoding.append(ex.getRoleId());
			encoding.append(", ");
			encodeAtom(encoding, ex.getConceptName(), remainingAtoms);
		} else {
			if (atom.isVariable()) {
				encoding.append("var(x");
			} else {
				encoding.append("cname(a");
			}
			encoding.append(goal.getAtomManager().getIndex(atom));
		}
		encoding.append(")");
	}

	private void encodeAtom(StringBuilder encoding, Integer origId, Set<Integer> remainingAtoms) {
		encodeAtom(encoding, goal.getAtomManager().getAtom(origId), remainingAtoms);
	}
}
