package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Collection;
import java.util.function.Function;

import de.tudresden.inf.lat.uel.core.processor.ShortFormProvider;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.impl.DefinitionSet;

/**
 * Class for rendering UEL objects in Functional syntax.
 * 
 * @author Stefan Borgwardt
 *
 */
public class FunctionalRenderer extends StringRenderer {

	/**
	 * Construct a new Functional renderer.
	 * 
	 * @param atomManager
	 *            the atom manager
	 * @param provider
	 *            the short form provider
	 * @param background
	 *            (optional) a set of background definitions used for
	 *            abbreviating expressions
	 */
	protected FunctionalRenderer(AtomManager atomManager, ShortFormProvider provider, DefinitionSet background) {
		super(atomManager, provider, background);
	}

	@Override
	protected <T, S> String translateExistentialRestriction(T role, S filler, Function<T, String> roleTranslator,
			Function<S, String> fillerTranslator) {
		sb.append(RendererKeywords.objectSomeValuesFrom);
		sb.append(RendererKeywords.open);
		roleTranslator.apply(role);
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
	protected <T> String translateTrueConjunction(Collection<T> conjuncts, Function<T, String> conjunctTranslator) {
		sb.append(RendererKeywords.objectIntersectionOf);
		sb.append(RendererKeywords.open);
		translateCollection(conjuncts, conjunctTranslator, RendererKeywords.space);
		sb.append(RendererKeywords.close);
		return "";
	}

}
