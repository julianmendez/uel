package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Set;
import java.util.function.Function;

import de.tudresden.inf.lat.uel.core.processor.ShortFormProvider;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Definition;

/**
 * Class for rendering UEL objects in KRSS 2 syntax.
 * 
 * @author Stefan Borgwardt
 *
 */
public class KRSSRenderer extends StringRenderer {

	/**
	 * Construct a new KRSS 2 renderer.
	 * 
	 * @param atomManager
	 *            the atom manager
	 * @param provider
	 *            the short form provider
	 * @param background
	 *            (optional) a set of background definitions used for
	 *            abbreviating expressions
	 */
	protected KRSSRenderer(AtomManager atomManager, ShortFormProvider provider, Set<Definition> background) {
		super(atomManager, provider, background);
	}

	@Override
	protected <T, S> String translateExistentialRestriction(T role, S filler, Function<T, String> roleTranslator,
			Function<S, String> fillerTranslator) {
		sb.append(RendererKeywords.open);
		sb.append(RendererKeywords.some);
		sb.append(RendererKeywords.space);
		roleTranslator.apply(role);
		sb.append(RendererKeywords.space);
		fillerTranslator.apply(filler);
		sb.append(RendererKeywords.close);
		return "";
	}

	@Override
	protected String translateTop() {
		sb.append(RendererKeywords.krssTop);
		return "";
	}

	@Override
	protected <T> String translateTrueConjunction(Set<T> conjuncts, Function<T, String> conjunctTranslator) {
		sb.append(RendererKeywords.open);
		sb.append(RendererKeywords.and);
		sb.append(RendererKeywords.space);
		translateSet(conjuncts, conjunctTranslator, RendererKeywords.space);
		sb.append(RendererKeywords.close);
		return "";
	}

}
