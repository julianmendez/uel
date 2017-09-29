package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Collection;
import java.util.function.Function;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.processor.ShortFormProvider;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Disequation;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.Subsumption;
import de.tudresden.inf.lat.uel.type.impl.DefinitionSet;

/**
 * Base class for all String renderers for UEL objects. Subclasses implement
 * different syntax formats.
 * 
 * @author Stefan Borgwardt
 */
public abstract class StringRenderer extends Renderer<String, String, String> {

	/**
	 * Factory method for creating the default string renderer.
	 * 
	 * @param atomManager
	 *            the atom manager
	 * @param provider
	 *            the short form provider
	 * @param background
	 *            (optional) a set of background definitions used for
	 *            abbreviating expressions
	 * @return the new string renderer
	 */
	public static StringRenderer createInstance(AtomManager atomManager, ShortFormProvider provider,
			DefinitionSet background) {
		return new ManchesterRenderer(atomManager, provider, background);
	}

	/**
	 * Internal variable used for constructing the output strings.
	 */
	protected StringBuilder sb;

	/**
	 * Construct a new string renderer.
	 * 
	 * @param atomManager
	 *            the atom manager
	 * @param provider
	 *            the short form provider
	 * @param background
	 *            (optional) a set of background definitions used for
	 *            abbreviating expressions
	 */
	protected StringRenderer(AtomManager atomManager, ShortFormProvider provider, DefinitionSet background) {
		super(atomManager, provider, background);
	}

	@Override
	protected String finalizeAxioms() {
		return sb.toString();
	}

	@Override
	protected String finalizeExpression() {
		if ((sb.charAt(0) == '<') && (sb.charAt(sb.length() - 1) == '>')) {
			if (sb.indexOf(">") == sb.length() - 1) {
				// if there are no more occurrences of these characters, remove
				// them
				sb.deleteCharAt(sb.length() - 1);
				sb.deleteCharAt(0);
			}
		}
		return sb.toString();
	}

	@Override
	protected void initialize() {
		sb = new StringBuilder();
	}

	@Override
	protected void newLine() {
		sb.append(System.lineSeparator());
	}

	@Override
	protected String translateAtomList(String description, Collection<Integer> atomIds) {
		sb.append(description);
		sb.append(RendererKeywords.colon);
		translateCollection(atomIds, id -> translateAtom(id, false), RendererKeywords.comma);
		newLine();
		newLine();
		return "";
	}

	@Override
	protected String translateAxiom(Axiom axiom) {
		translateConjunction(axiom.getLeft());
		sb.append(RendererKeywords.space);
		sb.append(axiom.getConnective());
		sb.append(RendererKeywords.space);
		translateConjunction(axiom.getRight());
		newLine();
		newLine();
		return "";
	}

	@Override
	protected String translateAxiom(OWLAxiom axiom, boolean positive) {
		if (axiom instanceof OWLSubClassOfAxiom) {

			translateClassExpression(((OWLSubClassOfAxiom) axiom).getSubClass());
			sb.append(RendererKeywords.space);
			sb.append(positive ? Subsumption.CONNECTIVE : Dissubsumption.CONNECTIVE);
			sb.append(RendererKeywords.space);
			translateClassExpression(((OWLSubClassOfAxiom) axiom).getSuperClass());

		} else if (axiom instanceof OWLEquivalentClassesAxiom) {

			String connective = positive ? Equation.CONNECTIVE : Disequation.CONNECTIVE;

			for (OWLClassExpression expr : ((OWLEquivalentClassesAxiom) axiom).getClassExpressions()) {
				translateClassExpression(expr);
				sb.append(RendererKeywords.space);
				sb.append(connective);
				sb.append(RendererKeywords.space);
			}

			sb.setLength(sb.length() - 2 * RendererKeywords.space.length() - connective.length());

		} else {
			throw new RuntimeException("Unsupported axiom type:" + axiom);
		}

		newLine();
		newLine();
		return "";
	}

	@Override
	protected String translateClass(OWLClass cls) {
		sb.append(renderEntity(cls));
		return "";
	}

	@Override
	protected String translateName(Integer atomId) {
		sb.append(renderName(atomId));
		return "";
	}

	@Override
	protected String translateObjectProperty(OWLObjectProperty prop) {
		sb.append(renderEntity(prop));
		return "";
	}

	@Override
	protected String translateRole(Integer roleId) {
		sb.append(renderRole(roleId));
		return "";
	}

	@Override
	protected String translateRoleList(String description, Collection<Integer> roleIds) {
		sb.append(description);
		sb.append(RendererKeywords.colon);
		translateCollection(roleIds, this::translateRole, RendererKeywords.comma);
		newLine();
		newLine();
		return "";
	}

	/**
	 * Add a set to the rendering under construction.
	 * 
	 * @param <T>
	 *            the element type
	 * @param set
	 *            the set of elements to be rendered
	 * @param elementTranslator
	 *            a translation function for individual elements
	 * @param separator
	 *            the string for separating consecutive elements
	 */
	protected <T> void translateCollection(Collection<T> set, Function<T, String> elementTranslator, String separator) {
		for (T element : set) {
			elementTranslator.apply(element);
			sb.append(separator);
		}
		if (!set.isEmpty()) {
			sb.setLength(sb.length() - separator.length());
		}
	}
}