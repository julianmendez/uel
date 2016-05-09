package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Set;
import java.util.function.Function;

import de.tudresden.inf.lat.uel.core.processor.ShortFormProvider;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;

/**
 * Class for rendering UEL objects in Manchester syntax.
 * 
 * @author Stefan Borgwardt
 *
 */
public class ManchesterRenderer extends StringRenderer {

	/**
	 * Construct a new Manchester renderer.
	 * 
	 * @param atomManager
	 *            the atom manager
	 * @param provider
	 *            the short form provider
	 * @param background
	 *            (optional) a set of background definitions used for
	 *            abbreviating expressions
	 */
	protected ManchesterRenderer(AtomManager atomManager, ShortFormProvider provider, Set<Definition> background) {
		super(atomManager, provider, background);
	}

	@Override
	protected Renderer<String, String, String> clone() {
		return new ManchesterRenderer(atomManager, provider, background);
	}

	@Override
	protected <T, S> String translateExistentialRestriction(T role, S filler, Function<T, String> roleTranslator,
			Function<S, String> fillerTranslator) {
		sb.append(RendererKeywords.open);
		roleTranslator.apply(role);
		sb.append(RendererKeywords.space);
		sb.append(RendererKeywords.some);
		sb.append(RendererKeywords.space);
		fillerTranslator.apply(filler);
		sb.append(RendererKeywords.close);
		return "";
	}

	@Override
	protected String translateTop() {
		sb.append(RendererKeywords.owlThing);
		return "";
	}

	@Override
	protected String translateTrueConjunction(Set<String> conjuncts) {
		sb.append(RendererKeywords.open);
		appendSet(conjuncts, RendererKeywords.space + RendererKeywords.and + RendererKeywords.space);
		sb.append(RendererKeywords.close);
		return "";
	}

}
