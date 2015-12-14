package de.tudresden.inf.lat.uel.asp.solver;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.SmallEquation;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

/**
 * This class prepares the input for an ASP solver.
 * 
 * @author Stefan Borgwardt
 * 
 */
public class AspInput {

	private UelInput uelInput;
	private boolean changed;
	private String program;

	public AspInput(UelInput uelInput) {
		this.uelInput = uelInput;
		this.changed = true;
	}

	public String getProgram() {
		if (changed) {
			updateProgram();
			changed = false;
		}
		return program;
	}

	public IndexedSet<Atom> getAtoms() {
		return uelInput.getAtoms();
	}

	private void updateProgram() {
		StringBuilder encoding = new StringBuilder();
		int i = 1;
		for (Equation eq : uelInput.getEquations()) {
			encodeEquation(encoding, eq, i);
			i++;
		}
		i = 1;
		for (SmallEquation eq : uelInput.getGoalDisequations()) {
			encodeDisequation(encoding, eq, i);
			i++;
		}
		for (Integer var : uelInput.getUserVariables()) {
			encoding.append("relevant(x");
			encoding.append(var);
			encoding.append(").\n");
		}
		program = encoding.toString();
	}

	private void encodeEquation(StringBuilder encoding, Equation eq, int index) {
		encoding.append("%equation ");
		encoding.append(index);
		encoding.append("\n");
		// lhs
		encodeAtom(encoding, eq.getLeft(), 0, index);
		// rhs
		for (Integer at : eq.getRight()) {
			encodeAtom(encoding, at, 1, index);
		}
		encoding.append("\n");
	}

	private void encodeDisequation(StringBuilder encoding, SmallEquation eq,
			int index) {
		encoding.append("%disequation ");
		encoding.append(index);
		encoding.append("\n");
		encoding.append("diseq(");
		encodeAtom(encoding, uelInput.getAtoms().get(eq.getLeft()));
		encoding.append(", ");
		encodeAtom(encoding, uelInput.getAtoms().get(eq.getRight()));
		encoding.append(").\n\n");
	}

	private void encodeAtom(StringBuilder encoding, Integer atomId, int side,
			int equationId) {
		encoding.append("hasatom(");
		encodeAtom(encoding, uelInput.getAtoms().get(atomId));
		encoding.append(", ");
		encoding.append(side);
		encoding.append(", ");
		encoding.append(equationId);
		encoding.append(").\n");
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
			encoding.append(uelInput.getAtoms().getIndex(atom));
		}
		encoding.append(")");
	}
}
