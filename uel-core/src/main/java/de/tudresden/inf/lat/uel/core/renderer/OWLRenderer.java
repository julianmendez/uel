package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;
import de.tudresden.inf.lat.uel.type.api.Subsumption;
import de.tudresden.inf.lat.uel.type.impl.DefinitionSet;

/**
 * This class can render UEL objects as OWL objects. The output cannot
 * distinguish between positive axioms (subsumptions, equations) and negative
 * axioms (dissubsumptions, disequations). This distinction has to be made
 * separately, e.g., by saving the output in two different files.
 * 
 * @author Stefan Borgwardt
 */
public class OWLRenderer extends Renderer<OWLClassExpression, OWLObjectProperty, Set<OWLAxiom>> {

	private Set<OWLAxiom> axioms;
	private final OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
	private OWLClassExpression expr;

	/**
	 * Construct a new OWL renderer.
	 * 
	 * @param atomManager
	 *            the atom manager
	 * @param background
	 *            (optional) a set of background definitions
	 */
	public OWLRenderer(AtomManager atomManager, DefinitionSet background) {
		super(atomManager, null, background);
	}

	@Override
	protected Set<OWLAxiom> finalizeAxioms() {
		return axioms;
	}

	@Override
	protected OWLClassExpression finalizeExpression() {
		return expr;
	}

	@Override
	protected void initialize() {
		axioms = new HashSet<>();
		expr = null;
	}

	@Override
	protected void newLine() {
	}

	@Override
	protected OWLClassExpression translateAtomList(String description, Collection<Integer> atomIds) {
		return translateConjunction(atomIds);
	}

	@Override
	protected Set<OWLAxiom> translateAxiom(Axiom axiom) {
		OWLClassExpression left = translateConjunction(axiom.getLeft());
		OWLClassExpression right = translateConjunction(axiom.getRight());
		boolean subclassof = (axiom instanceof Subsumption) || (axiom instanceof Dissubsumption)
				|| ((axiom instanceof Definition) && (((Definition) axiom).isPrimitive()));
		OWLAxiom newAxiom = subclassof ? dataFactory.getOWLSubClassOfAxiom(left, right)
				: dataFactory.getOWLEquivalentClassesAxiom(left, right);
		axioms.add(newAxiom);
		return axioms;
	}

	@Override
	protected Set<OWLAxiom> translateAxiom(OWLAxiom axiom, boolean positive) {
		axioms.add(axiom);
		return axioms;
	}

	@Override
	protected OWLClassExpression translateClass(OWLClass cls) {
		return cls;
	}

	@Override
	protected <T, S> OWLClassExpression translateExistentialRestriction(T role, S filler,
			Function<T, OWLObjectProperty> roleRenderer, Function<S, OWLClassExpression> fillerRenderer) {
		OWLObjectProperty property = roleRenderer.apply(role);
		OWLClassExpression child = fillerRenderer.apply(filler);
		expr = dataFactory.getOWLObjectSomeValuesFrom(property, child);
		return expr;
	}

	@Override
	protected OWLClassExpression translateName(Integer atomId) {
		expr = dataFactory.getOWLClass(IRI.create(renderName(atomId)));
		return expr;
	}

	@Override
	protected OWLObjectProperty translateObjectProperty(OWLObjectProperty prop) {
		return prop;
	}

	@Override
	protected OWLObjectProperty translateRole(Integer roleId) {
		return dataFactory.getOWLObjectProperty(IRI.create(renderRole(roleId)));
	}

	@Override
	protected OWLClassExpression translateRoleList(String description, Collection<Integer> roleIds) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected OWLClassExpression translateTop() {
		expr = dataFactory.getOWLThing();
		return expr;
	}

	@Override
	protected <T> OWLClassExpression translateTrueConjunction(Collection<T> conjuncts,
			Function<T, OWLClassExpression> conjunctTranslator) {
		Set<OWLClassExpression> classExpressions = conjuncts.stream().map(conjunctTranslator)
				.collect(Collectors.toSet());
		expr = dataFactory.getOWLObjectIntersectionOf(classExpressions);
		return expr;
	}
}
