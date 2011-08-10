package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.main.Atom;
import de.tudresden.inf.lat.uel.main.Equation;
import de.tudresden.inf.lat.uel.ontmanager.Ontology;

/**
 * An object implementing this class can build a UEL ontology using an OWL
 * ontology.
 * 
 * @author Julian Mendez
 */
public class OntologyBuilder {

	private static final Logger logger = Logger.getLogger(OntologyBuilder.class
			.getName());

	private Ontology ontology = null;

	public OntologyBuilder(Ontology ontology) {
		if (ontology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.ontology = ontology;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof OntologyBuilder) {
			OntologyBuilder other = (OntologyBuilder) o;
			ret = this.ontology.equals(other.ontology);
		}
		return ret;
	}

	@Override
	public int hashCode() {
		return this.ontology.hashCode();
	}

	public void loadOntology(OWLOntology owlOntology) {
		if (owlOntology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		for (OWLAxiom axiom : owlOntology.getAxioms()) {
			if (axiom instanceof OWLSubClassOfAxiom) {
				processSubClassOf((OWLSubClassOfAxiom) axiom);
			} else if (axiom instanceof OWLEquivalentClassesAxiom) {
				processEquivalentClasses((OWLEquivalentClassesAxiom) axiom);
			}
		}

		for (OWLClass cls : owlOntology.getClassesInSignature()) {
			String id = cls.toStringID();
			if (this.ontology.getDefinition(id) == null
					&& this.ontology.getPrimitiveDefinition(id) == null) {
				Atom atom = new Atom(id, false);
				this.ontology.putPrimitiveDefinition(atom.getName(),
						makeEquation(atom, new HashSet<Atom>()));
			}
		}
	}

	private Equation makeEquation(Atom definiendum, Set<Atom> definiens) {
		Map<String, Atom> leftMap = new HashMap<String, Atom>();
		leftMap.put(definiendum.toString(), definiendum);

		Map<String, Atom> rightMap = new HashMap<String, Atom>();
		for (Atom atom : definiens) {
			rightMap.put(atom.toString(), atom);
		}

		Equation equation = new Equation();
		equation.setLeft(leftMap);
		equation.setRight(rightMap);
		return equation;
	}

	private Set<Atom> processClass(OWLClass cls) {
		Set<Atom> ret = new HashSet<Atom>();
		Atom newAtom = new Atom(cls.toStringID(), false);
		ret.add(newAtom);
		this.ontology.putAtom(newAtom.toString(), newAtom);
		return ret;
	}

	private Set<Atom> processClassExpression(OWLClassExpression classExpr) {
		Set<Atom> ret = null;
		if (classExpr instanceof OWLClass) {
			ret = processClass((OWLClass) classExpr);
		} else if (classExpr instanceof OWLObjectIntersectionOf) {
			ret = processIntersection((OWLObjectIntersectionOf) classExpr);
		} else if (classExpr instanceof OWLObjectSomeValuesFrom) {
			ret = processSomeValuesRestriction((OWLObjectSomeValuesFrom) classExpr);
		} else {
			logger.info("Ignoring class expression '" + classExpr + "'.");
		}
		return ret;
	}

	private void processEquivalentClasses(OWLEquivalentClassesAxiom axiom) {
		Set<OWLClassExpression> elements = axiom.getClassExpressions();
		if (elements.size() == 2) {

			Iterator<OWLClassExpression> it = axiom.getClassExpressions()
					.iterator();
			OWLClassExpression class00 = it.next();
			OWLClassExpression class01 = it.next();
			if (class00 instanceof OWLClass && class01 instanceof OWLClass) {
				logger.warning("Definition '" + axiom
						+ "' is ambiguous, assuming '" + class00 + "':='"
						+ class01 + "'.");
			}
			if (!(class00 instanceof OWLClass) && class01 instanceof OWLClass) {
				OWLClassExpression t = class00;
				class00 = class01;
				class01 = t;
			}
			if (class00 instanceof OWLClass) {
				Atom definiendum = processClassExpression(class00).iterator()
						.next();
				Set<Atom> definiens = processClassExpression(class01);
				this.ontology.putDefinition(definiendum.getName(),
						makeEquation(definiendum, definiens));

			} else {
				processClassExpression(class00);
				processClassExpression(class01);
			}
		}
	}

	private Set<Atom> processIntersection(OWLObjectIntersectionOf intersection) {
		Set<Atom> ret = new HashSet<Atom>();
		for (OWLClassExpression operand : intersection.getOperands()) {
			ret.addAll(processClassExpression(operand));
		}
		return ret;
	}

	private Set<Atom> processSomeValuesRestriction(
			OWLObjectSomeValuesFrom someValuesRestriction) {
		Set<Atom> ret = new HashSet<Atom>();
		Map<String, Atom> map = new HashMap<String, Atom>();
		Set<Atom> atomSet = processClassExpression(someValuesRestriction
				.getFiller());
		for (Atom atom : atomSet) {
			map.put(atom.toString(), atom);
		}
		Atom prop = new Atom(someValuesRestriction.getProperty()
				.getNamedProperty().toStringID(), true);
		Atom newAtom = new Atom(prop.getName(), true, map);
		this.ontology.putAtom(newAtom.toString(), newAtom);
		ret.add(newAtom);
		return ret;
	}

	private void processSubClassOf(OWLSubClassOfAxiom axiom) {
		Set<Atom> subClassSet = processClassExpression(axiom.getSubClass());
		Set<Atom> superClassSet = processClassExpression(axiom.getSuperClass());

		if (axiom.getSubClass() instanceof OWLClass) {
			this.ontology.putPrimitiveDefinition(subClassSet.iterator().next()
					.getName(),
					makeEquation(subClassSet.iterator().next(), superClassSet));
		}
	}

}
