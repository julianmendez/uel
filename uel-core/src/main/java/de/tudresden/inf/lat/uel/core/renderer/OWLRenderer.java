package de.tudresden.inf.lat.uel.core.renderer;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Dissubsumption;
import de.tudresden.inf.lat.uel.type.api.Subsumption;

public class OWLRenderer extends Renderer<OWLClassExpression, Set<OWLAxiom>> {

	private final OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
	private Set<OWLAxiom> axioms;
	private OWLClassExpression expr;

	public OWLRenderer(AtomManager atomManager, Set<Definition> background) {
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
		axioms = new HashSet<OWLAxiom>();
		expr = null;
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
	protected OWLClassExpression translateExistentialRestriction(String roleName, Integer childId) {
		OWLClassExpression child = translateChild(childId);
		OWLObjectProperty property = dataFactory.getOWLObjectProperty(IRI.create(roleName));
		expr = dataFactory.getOWLObjectSomeValuesFrom(property, child);
		return expr;
	}

	@Override
	protected OWLClassExpression translateName(Integer atomId) {
		expr = dataFactory.getOWLClass(IRI.create(renderName(atomId)));
		return expr;
	}

	@Override
	protected OWLClassExpression translateTop() {
		expr = dataFactory.getOWLThing();
		return expr;
	}

	@Override
	protected OWLClassExpression translateTrueConjunction(Set<Integer> atomIds) {
		Set<OWLClassExpression> classExpressions = atomIds.stream().map(atomId -> translateAtom(atomId))
				.collect(Collectors.toSet());
		expr = dataFactory.getOWLObjectIntersectionOf(classExpressions);
		return expr;
	}

}
