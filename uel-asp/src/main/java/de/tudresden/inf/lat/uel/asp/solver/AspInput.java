package de.tudresden.inf.lat.uel.asp.solver;

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

	public AspInput(Goal goal) {
		this.goal = goal;
		updateProgram();
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
		for (Definition d : goal.getDefinitions()) {
			encodeAxiom(encoding, d, i, "definition", "eq");
			i++;
		}
		for (Equation e : goal.getEquations()) {
			encodeAxiom(encoding, e, i, "equation", "eq");
			i++;
		}
		for (Subsumption s : goal.getSubsumptions()) {
			encodeSubsumption(encoding, s, i);
			i++;
		}
		for (Disequation e : goal.getDisequations()) {
			encodeAxiom(encoding, e, i, "disequation", "diseq");
			i++;
		}
		for (Dissubsumption s : goal.getDissubsumptions()) {
			encodeAxiom(encoding, s, i, "dissubsumption", "dissubs");
			i++;
		}

		encoding.append("% Types");
		encoding.append(System.lineSeparator());
		for (Integer type : goal.getTypes()) {
			encoding.append("type(");
			encodeAtom(encoding, type);
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
					encodeAtom(encoding, goal.getAtomManager().getAtom(getOriginalType(type)));
					encoding.append(")");
				} else {
					encodeAtom(encoding, type);
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
				encodeAtom(encoding, type);
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
			encodeAtom(encoding, origId);
			encoding.append(",");
			encodeAtom(encoding, undefId);
			encoding.append(").");
			encoding.append(System.lineSeparator());
		}
		encoding.append(System.lineSeparator());
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

	private void encodeAxiom(StringBuilder encoding, Axiom d, int index, String comment, String predicate) {
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

		encodeAtoms(encoding, d, index);
		encoding.append(System.lineSeparator());
	}

	private void encodeSubsumption(StringBuilder encoding, Subsumption s, int index) {
		// TODO more direct ASP encoding for subsumptions
		encoding.append("%subsumption ");
		encoding.append(index);
		encoding.append(System.lineSeparator());

		encoding.append("eq(");
		encoding.append(index);
		encoding.append(").");
		encoding.append(System.lineSeparator());

		encodeAtoms(encoding, s, index);
		encodeAtoms(encoding, s.getLeft(), 1, index);
		encoding.append(System.lineSeparator());
	}

	private void encodeAtoms(StringBuilder encoding, Axiom axiom, int index) {
		encodeAtoms(encoding, axiom.getLeft(), 0, index);
		encodeAtoms(encoding, axiom.getRight(), 1, index);
	}

	private void encodeAtoms(StringBuilder encoding, Set<Integer> atomIds, int side, int index) {
		for (Integer atomId : atomIds) {
			encodeAtom(encoding, atomId, side, index);
		}
	}

	private void encodeAtom(StringBuilder encoding, Integer atomId, int side, int equationId) {
		encoding.append("hasatom(");
		encodeAtom(encoding, atomId);
		encoding.append(", ");
		encoding.append(side);
		encoding.append(", ");
		encoding.append(equationId);
		encoding.append(").");
		encoding.append(System.lineSeparator());
	}

	private void encodeAtom(StringBuilder encoding, Atom atom) {
		if (atom.isExistentialRestriction()) {
			ExistentialRestriction ex = (ExistentialRestriction) atom;
			encoding.append("exists(r");
			encoding.append(ex.getRoleId());
			encoding.append(", ");
			encodeAtom(encoding, ex.getConceptName());
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

	private void encodeAtom(StringBuilder encoding, Integer origId) {
		encodeAtom(encoding, goal.getAtomManager().getAtom(origId));
	}
}
