package de.tudresden.inf.lat.uel.asp.solver;

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
		for (Integer var : goal.getAtomManager().getUserVariables()) {
			encoding.append("relevant(x");
			encoding.append(var);
			encoding.append(").");
			encoding.append(System.lineSeparator());
		}
		program = encoding.toString();
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
		encodeAtom(encoding, goal.getAtomManager().getAtom(atomId));
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
}
