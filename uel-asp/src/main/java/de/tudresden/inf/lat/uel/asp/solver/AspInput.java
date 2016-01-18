package de.tudresden.inf.lat.uel.asp.solver;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Disequation;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.api.Subsumption;
import de.tudresden.inf.lat.uel.type.cons.RendererKeywords;
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
			encodeDefinition(encoding, d, i);
			i++;
		}
		for (Equation e : goal.getEquations()) {
			encodeEquation(encoding, e, i);
			i++;
		}
		for (Subsumption s : goal.getSubsumptions()) {
			encodeSubsumption(encoding, s, i);
		}
		i = 1;
		for (Disequation e : goal.getDisequations()) {
			encodeDisequation(encoding, e, i);
			i++;
		}
		for (Dissubsumption s : goal.getDissubsumptions()) {
			encodeDissubsumption(encoding, s, i);
			i++;
		}
		for (Integer var : goal.getAtomManager().getUserVariables()) {
			encoding.append("relevant(x");
			encoding.append(var);
			encoding.append(").");
			encoding.append(RendererKeywords.newLine);
		}
		program = encoding.toString();
	}

	private void encodeDefinition(StringBuilder encoding, Definition d, int index) {
		encoding.append("%definition ");
		encoding.append(index);
		encoding.append(RendererKeywords.newLine);
		// lhs
		encodePositiveAtom(encoding, d.getDefiniendum(), 0, index);
		// rhs
		for (Integer rightId : d.getRight()) {
			encodePositiveAtom(encoding, rightId, 1, index);
		}
		encoding.append(RendererKeywords.newLine);
	}

	private void encodeEquation(StringBuilder encoding, Equation e, int index) {
		encoding.append("%equation ");
		encoding.append(index);
		encoding.append(RendererKeywords.newLine);
		// lhs
		for (Integer leftId : e.getLeft()) {
			encodePositiveAtom(encoding, leftId, 0, index);
		}
		// rhs
		for (Integer rightId : e.getRight()) {
			encodePositiveAtom(encoding, rightId, 1, index);
		}
		encoding.append(RendererKeywords.newLine);
	}

	private void encodeSubsumption(StringBuilder encoding, Subsumption s, int index) {
		encoding.append("%subsumption ");
		encoding.append(index);
		encoding.append(RendererKeywords.newLine);
		// lhs
		for (Integer leftId : s.getLeft()) {
			encodePositiveAtom(encoding, leftId, 0, index);
			encodePositiveAtom(encoding, leftId, 1, index);
		}
		// rhs
		for (Integer rightId : s.getRight()) {
			encodePositiveAtom(encoding, rightId, 1, index);
		}
		encoding.append(RendererKeywords.newLine);
	}

	private void encodeDisequation(StringBuilder encoding, Disequation e, int index) {
		encoding.append("%disequation ");
		encoding.append(index);
		encoding.append(RendererKeywords.newLine);
		// lhs
		for (Integer leftId : e.getLeft()) {
			encodeNegativeAtom(encoding, leftId, 0, index);
		}
		// rhs
		for (Integer rightId : e.getRight()) {
			encodeNegativeAtom(encoding, rightId, 1, index);
		}
		encoding.append(RendererKeywords.newLine);
	}

	private void encodeDissubsumption(StringBuilder encoding, Dissubsumption s, int index) {
		encoding.append("%disequation ");
		encoding.append(index);
		encoding.append(RendererKeywords.newLine);
		// lhs
		for (Integer leftId : s.getLeft()) {
			encodeNegativeAtom(encoding, leftId, 0, index);
		}
		// rhs
		for (Integer rightId : s.getRight()) {
			encodeNegativeAtom(encoding, rightId, 1, index);
		}
		encoding.append(RendererKeywords.newLine);
		encoding.append("lefttoright(");
		encoding.append(index);
		encoding.append(").");
		encoding.append(RendererKeywords.newLine);
	}

	private void encodePositiveAtom(StringBuilder encoding, Integer atomId, int side, int equationId) {
		encoding.append("eqhasatom(");
		encodeAtom(encoding, goal.getAtomManager().getAtom(atomId));
		encoding.append(", ");
		encoding.append(side);
		encoding.append(", ");
		encoding.append(equationId);
		encoding.append(").");
		encoding.append(RendererKeywords.newLine);
	}

	private void encodeNegativeAtom(StringBuilder encoding, Integer atomId, int side, int disequationId) {
		encoding.append("diseqhasatom(");
		encodeAtom(encoding, goal.getAtomManager().getAtom(atomId));
		encoding.append(", ");
		encoding.append(side);
		encoding.append(", ");
		encoding.append(disequationId);
		encoding.append(").");
		encoding.append(RendererKeywords.newLine);
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
