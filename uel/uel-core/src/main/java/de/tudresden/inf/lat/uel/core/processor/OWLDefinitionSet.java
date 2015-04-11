package de.tudresden.inf.lat.uel.core.processor;

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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * This has a set of definitions extracted from an OWL ontology and contains OWL
 * classes.
 * 
 * @author Julian Mendez
 */
public class OWLDefinitionSet {

	private static final Logger logger = Logger
			.getLogger(OWLDefinitionSet.class.getName());

	private final Map<OWLClass, OWLClassExpression> definitions;
	private final Map<OWLClass, Set<OWLClassExpression>> primitiveDefinitions;

	public OWLDefinitionSet(Set<OWLOntology> ontologies, OWLClass owlThingAlias) {
		this.definitions = new HashMap<OWLClass, OWLClassExpression>();
		this.primitiveDefinitions = new HashMap<OWLClass, Set<OWLClassExpression>>();
		for (OWLOntology ontology : ontologies) {
			this.definitions.putAll(getDefinitions(ontology));
			this.primitiveDefinitions.putAll(getPrimitiveDefinitions(ontology,
					owlThingAlias));
		}
	}

	public Set<OWLClass> getDefinedConcepts() {
		return this.definitions.keySet();
	}

	public OWLClassExpression getDefinition(OWLClass cls) {
		return this.definitions.get(cls);
	}

	/**
	 * Returns all the definitions present in an OWL ontology.
	 * 
	 * @param owlOntology
	 *            OWL ontology
	 * @return all definitions present in an OWL ontology
	 */
	private Map<OWLClass, OWLClassExpression> getDefinitions(
			OWLOntology owlOntology) {
		if (owlOntology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

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

	public Set<OWLClass> getPrimitiveDefinedConcepts() {
		return this.primitiveDefinitions.keySet();
	}

	public Set<OWLClassExpression> getPrimitiveDefinition(OWLClass cls) {
		return this.primitiveDefinitions.get(cls);
	}

	/**
	 * Returns all the primitive definitions present in an OWL ontology unless
	 * OWL Thing is the super class.
	 * 
	 * @param owlOntology
	 *            OWL ontology
	 * @return all the primitive definitions present in an OWL ontology unless
	 *         OWL Thing is the super class
	 */
	private Map<OWLClass, Set<OWLClassExpression>> getPrimitiveDefinitions(
			OWLOntology owlOntology, OWLClass owlThingAlias) {
		if (owlOntology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Map<OWLClass, Set<OWLClassExpression>> ret = new HashMap<OWLClass, Set<OWLClassExpression>>();
		Set<OWLSubClassOfAxiom> setOfAxioms = owlOntology
				.getAxioms(AxiomType.SUBCLASS_OF);
		for (OWLSubClassOfAxiom axiom : setOfAxioms) {
			if (!axiom.getSuperClass().isOWLThing()
					&& !axiom.getSuperClass().equals(owlThingAlias)) {
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
		}
		return ret;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(this.definitions.toString());
		sbuf.append("\n");
		sbuf.append(this.primitiveDefinitions.toString());
		sbuf.append("\n");
		return sbuf.toString();
	}

}
