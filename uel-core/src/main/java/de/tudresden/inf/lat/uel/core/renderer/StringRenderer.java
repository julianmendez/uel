package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Goal;

public abstract class StringRenderer extends Renderer<String, String> {

	public static StringRenderer createInstance(AtomManager atomManager, Map<String, String> shortFormMap,
			Set<Definition> background) {
		return new KRSSRenderer(atomManager, shortFormMap, background);
	}

	public static String removeQuotes(String str) {
		String ret = str;
		if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
			ret = str.substring(1, str.length() - 1);
		}
		return ret;
	}

	protected StringBuilder sb;

	protected StringRenderer(AtomManager atomManager, Map<String, String> shortFormMap, Set<Definition> background) {
		super(atomManager, shortFormMap, background);
	}

	@Override
	protected String finalizeAxioms() {
		return sb.toString();
	}

	@Override
	protected String finalizeExpression() {
		return sb.toString();
	}

	@Override
	protected void initialize() {
		sb = new StringBuilder();
	}

	public String renderAtomWithoutQuotes(Integer atomId) {
		return removeQuotes(renderAtom(atomId));
	}

	public String renderGoal(Goal input) {
		sb = new StringBuilder();
		translateAxioms(input.getDefinitions());
		translateAxioms(input.getEquations());
		translateAxioms(input.getSubsumptions());
		translateAxioms(input.getDisequations());
		translateAxioms(input.getDissubsumptions());
		return sb.toString();
	}

	public String renderNameWithoutQuotes(Integer atomId) {
		return removeQuotes(renderName(atomId));
	}

	private void translateAxioms(Set<? extends Axiom> axioms) {
		for (Axiom axiom : axioms) {
			translateConjunction(axiom.getLeft());
			sb.append(" ");
			sb.append(axiom.getConnective());
			sb.append(" ");
			translateConjunction(axiom.getRight());
			sb.append(System.lineSeparator());
			sb.append(System.lineSeparator());
		}
	}

	@Override
	protected String translateName(Integer atomId) {
		sb.append(renderName(atomId));
		return "";
	}
}