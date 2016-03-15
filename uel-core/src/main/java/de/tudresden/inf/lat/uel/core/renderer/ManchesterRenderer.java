package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Set;

import de.tudresden.inf.lat.uel.core.processor.ShortFormProvider;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.cons.RendererKeywords;

public class ManchesterRenderer extends StringRenderer {

	protected ManchesterRenderer(AtomManager atomManager, ShortFormProvider provider, Set<Definition> background) {
		super(atomManager, provider, background);
	}

	@Override
	protected String translateExistentialRestriction(String roleName, Integer childId) {
		sb.append(RendererKeywords.open);
		sb.append(roleName);
		sb.append(RendererKeywords.space);
		sb.append(RendererKeywords.some);
		sb.append(RendererKeywords.space);
		translateChild(childId);
		sb.append(RendererKeywords.close);
		return "";
	}

	@Override
	protected String translateTop() {
		sb.append(RendererKeywords.owlThing);
		return "";
	}

	@Override
	protected String translateTrueConjunction(Set<Integer> atomIds) {
		sb.append(RendererKeywords.open);

		for (Integer atomId : atomIds) {
			translateAtom(atomId);
			sb.append(RendererKeywords.space);
			sb.append(RendererKeywords.and);
			sb.append(RendererKeywords.space);
		}

		sb.setLength(sb.length() - 2 * RendererKeywords.space.length() - RendererKeywords.and.length());
		sb.append(RendererKeywords.close);
		return "";
	}

}
