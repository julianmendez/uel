package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.api.UelOutput;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

public class AspProcessor implements UelProcessor {

	private Set<Equation> equations;
	private IndexedSet<Atom> atomManager;
	private Set<Integer> userVariables;

	public AspProcessor(UelInput input) {
		equations = input.getEquations();
		atomManager = input.getAtomManager();
		userVariables = input.getUserVariables();
	}

	@Override
	public boolean computeNextUnifier() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Entry<String, String>> getInfo() {
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

		Entry<String, String> e = new AbstractMap.SimpleEntry<String, String>(
				"ASP encoding", encoding.toString());
		List<Entry<String, String>> res = new ArrayList<Entry<String, String>>();
		res.add(e);
		return res;
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

	@Override
	public UelInput getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UelOutput getUnifier() {
		// TODO Auto-generated method stub
		return null;
	}

}
