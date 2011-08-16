package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.core.type.Atom;
import de.tudresden.inf.lat.uel.core.type.Equation;
import de.tudresden.inf.lat.uel.core.type.Ontology;

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

	/**
	 * Constructs a new ontology builder.
	 * 
	 * @param ontology
	 *            ontology to build
	 */
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

	private Map<OWLClass, OWLClassExpression> getDefinitions(
			OWLOntology owlOntology) {
		Map<OWLClass, OWLClassExpression> ret = new HashMap<OWLClass, OWLClassExpression>();
		Set<OWLEquivalentClassesAxiom> setOfAxioms = owlOntology
				.getAxioms(AxiomType.EQUIVALENT_CLASSES);
		for (OWLEquivalentClassesAxiom axiom : setOfAxioms) {
			Set<OWLClassExpression> operands = axiom.getClassExpressions();
			if (operands.size() == 2) {
				Iterator<OWLClassExpression> it = operands.iterator();
				OWLClassExpression first = it.next();
				OWLClassExpression second = it.next();
				if (!(first instanceof OWLClass) && second instanceof OWLClass) {
					OWLClassExpression other = first;
					first = second;
					second = other;
				}
				if (first instanceof OWLClass && second instanceof OWLClass) {
					logger.warning("Definition '" + axiom
							+ "' is ambiguous, including only '" + first
							+ "':='" + second);
				}
				if (first instanceof OWLClass) {
					ret.put((OWLClass) first, second);
				}
			}
		}
		return ret;
	}

	private Map<OWLClass, Set<OWLClassExpression>> getPrimitiveDefinitions(
			OWLOntology owlOntology) {
		Map<OWLClass, Set<OWLClassExpression>> ret = new HashMap<OWLClass, Set<OWLClassExpression>>();
		Set<OWLSubClassOfAxiom> setOfAxioms = owlOntology
				.getAxioms(AxiomType.SUBCLASS_OF);
		for (OWLSubClassOfAxiom axiom : setOfAxioms) {
			if (axiom.getSubClass() instanceof OWLClass) {
				OWLClass definiendum = (OWLClass) axiom.getSubClass();
				Set<OWLClassExpression> definiensSet = ret.get(definiendum);
				if (definiensSet == null) {
					definiensSet = new HashSet<OWLClassExpression>();
					ret.put(definiendum, definiensSet);
				}
				definiensSet.add(axiom.getSuperClass());
			}
		}
		return ret;
	}

	@Override
	public int hashCode() {
		return this.ontology.hashCode();
	}

	/**
	 * Loads an OWL ontology.
	 * 
	 * @param owlOntology
	 *            OWL ontology
	 */
	public void loadOntology(OWLOntology owlOntology) {
		if (owlOntology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Map<OWLClass, OWLClassExpression> definitions = getDefinitions(owlOntology);
		for (OWLClass key : definitions.keySet()) {
			processDefinition(key, definitions.get(key));
		}

		Map<OWLClass, Set<OWLClassExpression>> primitiveDefinitions = getPrimitiveDefinitions(owlOntology);
		for (OWLClass key : primitiveDefinitions.keySet()) {
			processPrimitiveDefinition(key, primitiveDefinitions.get(key));
		}
	}

	private Equation makeEquation(Atom definiendum, Set<Atom> definiens,
			boolean primitive) {
		Map<String, Atom> leftMap = new HashMap<String, Atom>();
		leftMap.put(definiendum.toString(), definiendum);

		Map<String, Atom> rightMap = new HashMap<String, Atom>();
		for (Atom atom : definiens) {
			rightMap.put(atom.toString(), atom);
		}

		Equation ret = new Equation(leftMap, rightMap, primitive);
		return ret;
	}

	private Set<Atom> processClass(OWLClass cls) {
		Set<Atom> ret = new HashSet<Atom>();
		Atom newAtom = new Atom(cls.toStringID(), false);
		ret.add(newAtom);
		this.ontology.putAtom(newAtom.toString(), newAtom);
		return ret;
	}

	private Set<Atom> processClassExpression(OWLClassExpression classExpr) {
		Set<Atom> ret = new HashSet<Atom>();
		if (classExpr instanceof OWLClass) {
			ret = processClass((OWLClass) classExpr);
		} else if (classExpr instanceof OWLObjectIntersectionOf) {
			ret = processIntersection((OWLObjectIntersectionOf) classExpr);
		} else if (classExpr instanceof OWLObjectSomeValuesFrom) {
			ret = processSomeValuesRestriction((OWLObjectSomeValuesFrom) classExpr);
		} else {
			logger.warning("Ignoring class expression '" + classExpr + "'.");
		}
		return ret;
	}

	private void processDefinition(OWLClass definiendum,
			OWLClassExpression definiens) {
		Set<Atom> left = processClass(definiendum);
		Set<Atom> right = processClassExpression(definiens);
		this.ontology.putDefinition(left.iterator().next().toString(),
				makeEquation(left.iterator().next(), right, false));
	}

	private Set<Atom> processIntersection(OWLObjectIntersectionOf intersection) {
		Set<Atom> ret = new HashSet<Atom>();
		for (OWLClassExpression operand : intersection.getOperands()) {
			ret.addAll(processClassExpression(operand));
		}
		return ret;
	}

	private void processPrimitiveDefinition(OWLClass definiendum,
			Set<OWLClassExpression> definiensSet) {
		Set<Atom> subClassSet = processClass(definiendum);
		Set<Atom> superClassSet = new HashSet<Atom>();
		for (OWLClassExpression clsExpr : definiensSet) {
			superClassSet.addAll(processClassExpression(clsExpr));
		}
		this.ontology
				.putPrimitiveDefinition(
						subClassSet.iterator().next().toString(),
						makeEquation(subClassSet.iterator().next(),
								superClassSet, true));
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
		Atom newAtom = new Atom(prop.toString(), true, map);
		this.ontology.putAtom(newAtom.toString(), newAtom);
		ret.add(newAtom);
		return ret;
	}

}
