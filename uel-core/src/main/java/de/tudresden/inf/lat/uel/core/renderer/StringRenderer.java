package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Set;

import de.tudresden.inf.lat.uel.core.processor.ShortFormProvider;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Definition;

public abstract class StringRenderer extends Renderer<String, String> {

	public static StringRenderer createInstance(AtomManager atomManager, ShortFormProvider provider,
			Set<Definition> background) {
		return new ManchesterRenderer(atomManager, provider, background);
	}

	protected StringBuilder sb;

	protected StringRenderer(AtomManager atomManager, ShortFormProvider provider, Set<Definition> background) {
		super(atomManager, provider, background);
	}

	@Override
	protected String translateAtomList(String description, Set<Integer> atomIds) {
		sb.append(description);
		sb.append(": ");

		for (Integer atomId : atomIds) {
			translateAtom(atomId);
			sb.append(", ");
		}

		sb.setLength(sb.length() - 2);
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		return "";
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
	protected String translateRoleList(String description, Set<Integer> roleIds) {
		sb.append(description);
		sb.append(": ");

		for (Integer roleId : roleIds) {
			sb.append(renderRole(roleId));
			sb.append(", ");
		}

		sb.setLength(sb.length() - 2);
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