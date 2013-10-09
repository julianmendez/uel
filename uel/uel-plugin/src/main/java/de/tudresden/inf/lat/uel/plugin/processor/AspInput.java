package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

/**
 * This class prepares the input for an ASP solver.
 * 
 * @author Stefan Borgwardt
 * 
 */
public class AspInput {

	private Set<Equation> equations;
	private IndexedSet<Atom> atomManager;
	private Set<Integer> userVariables;
	private boolean changed;
	private String program;

	public AspInput(Set<Equation> equations, IndexedSet<Atom> atomManager,
			Set<Integer> userVariables) {
		this.equations = equations;
		this.atomManager = atomManager;
		this.userVariables = userVariables;
		this.changed = true;
	}

	public String getProgram() {
		if (changed) {
			updateProgram();
			changed = false;
		}
		return program;
	}

	public IndexedSet<Atom> getAtomManager() {
		return this.atomManager;
	}

	private void updateProgram() {
		StringBuilder encoding = new StringBuilder();
		int i = 1;
		for (Equation eq : equations) {
			encodeEquation(encoding, eq, i);
			i++;
		}
		for (Integer var : userVariables) {
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

	private void encodeAtom(StringBuilder encoding, Integer atomId, int side,
			int equationId) {
		encoding.append("hasatom(");
		encodeAtom(encoding, atomManager.get(atomId));
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
			encodeAtom(encoding, ex.getChild());
		} else {
			if (atom.isVariable()) {
				encoding.append("var(x");
			} else {
				encoding.append("cname(a");
			}
			encoding.append(atom.getConceptNameId());
		}
		encoding.append(")");
	}
}
