package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Map;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Definition;

public abstract class StringRenderer extends Renderer<String, String> {

	public static StringRenderer createInstance(AtomManager atomManager, Map<String, String> shortFormMap,
			Set<Definition> background) {
		return new ManchesterRenderer(atomManager, shortFormMap, background);
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
		// TODO remove '<' and '>' around result?
		return sb.toString();
	}

	@Override
	protected void initialize() {
		sb = new StringBuilder();
	}

	@Override
	protected String translateAxiom(Axiom axiom) {
		translateConjunction(axiom.getLeft());
		sb.append(" ");
		sb.append(axiom.getConnective());
		sb.append(" ");
		translateConjunction(axiom.getRight());
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		return "";
	}

	@Override
	protected String translateName(Integer atomId) {
		sb.append(renderName(atomId));
		return "";
	}
}