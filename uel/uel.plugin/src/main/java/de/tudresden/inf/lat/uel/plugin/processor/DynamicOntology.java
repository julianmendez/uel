package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.core.type.Equation;
import de.tudresden.inf.lat.uel.core.type.Ontology;

/**
 * An object of this class is a UEL ontology that reuses a previously build OWL
 * ontology.
 * 
 * @author Julian Mendez
 */
public class DynamicOntology implements Ontology {

	private Map<Integer, Equation> definitionCache = new HashMap<Integer, Equation>();
	private Map<Integer, OWLClass> nameMap = new HashMap<Integer, OWLClass>();
	private OntologyBuilder ontologyBuilder = null;
	private OWLDefinitionSet owlDefinitionSet;
	private Map<Integer, Equation> primitiveDefinitionCache = new HashMap<Integer, Equation>();

	/**
	 * Constructs a new dynamic ontology.
	 */
	public DynamicOntology(OntologyBuilder builder) {
		if (builder == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.ontologyBuilder = builder;
	}

	/**
	 * Clears this ontology.
	 */
	public void clear() {
		this.nameMap.clear();
		this.definitionCache.clear();
		this.primitiveDefinitionCache.clear();
	}

	@Override
	public boolean containsDefinition(Integer id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		boolean ret = true;
		Equation eq = this.definitionCache.get(id);
		if (eq == null) {
			OWLClass cls = this.nameMap.get(id);
			ret = this.owlDefinitionSet.getDefinition(cls) != null;
		}
		return ret;
	}

	@Override
	public boolean containsPrimitiveDefinition(Integer id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		boolean ret = true;
		Equation eq = this.primitiveDefinitionCache.get(id);
		if (eq == null) {
			OWLClass cls = this.nameMap.get(id);
			ret = this.owlDefinitionSet.getPrimitiveDefinition(cls) != null;
		}
		return ret;
	}

	@Override
	public Equation getDefinition(Integer id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Equation ret = this.definitionCache.get(id);
		if (ret == null) {
			OWLClass cls = this.nameMap.get(id);
			if (cls != null) {
				OWLClassExpression clExpr = this.owlDefinitionSet
						.getDefinition(cls);
				if (clExpr != null) {
					updateCache(getOntologyBuilder().processDefinition(cls,
							clExpr));
					ret = this.definitionCache.get(id);
				}
			}
		}

		return ret;
	}

	@Override
	public Set<Integer> getDefinitionIds() {
		return Collections.unmodifiableSet(nameMap.keySet());
	}

	public OntologyBuilder getOntologyBuilder() {
		return this.ontologyBuilder;
	}

	@Override
	public Equation getPrimitiveDefinition(Integer id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Equation ret = this.primitiveDefinitionCache.get(id);
		if (ret == null) {
			OWLClass cls = this.nameMap.get(id);
			if (cls != null) {
				Set<OWLClassExpression> clExprSet = this.owlDefinitionSet
						.getPrimitiveDefinition(cls);
				if (clExprSet != null) {
					updateCache(getOntologyBuilder()
							.processPrimitiveDefinition(cls, clExprSet));
					ret = this.primitiveDefinitionCache.get(id);
				}
			}
		}
		return ret;
	}

	/**
	 * Loads an OWL ontology.
	 * 
	 * @param owlOntology
	 *            OWL ontology
	 */
	public void load(OWLOntology owlOntology01, OWLOntology owlOntology02) {
		if (owlOntology01 == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (owlOntology02 == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.owlDefinitionSet = new OWLDefinitionSet(owlOntology01,
				owlOntology02);
		Set<OWLClass> toVisit = new HashSet<OWLClass>();
		toVisit.addAll(this.owlDefinitionSet.getDefinedConcepts());
		toVisit.addAll(this.owlDefinitionSet.getPrimitiveDefinedConcepts());
		this.nameMap.putAll(getOntologyBuilder().processNames(toVisit));
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Definitions ");
		sbuf.append(this.owlDefinitionSet.toString());
		sbuf.append("\n");
		return sbuf.toString();
	}

	private void updateCache(Set<Equation> equations) {
		for (Equation equation : equations) {
			if (equation.isPrimitive()) {
				this.primitiveDefinitionCache.put(equation.getLeft(), equation);
			} else {
				this.definitionCache.put(equation.getLeft(), equation);
			}
		}
	}

	@Override
	public Set<Equation> getModule(Integer id) {
		Set<Equation> ret = new HashSet<Equation>();
		Set<Integer> visited = new HashSet<Integer>();
		Set<Integer> toVisit = new HashSet<Integer>();
		toVisit.add(id);
		while (!toVisit.isEmpty()) {
			Integer e = toVisit.iterator().next();
			visited.add(e);

			Equation eq = getDefinition(e);
			if (eq == null) {
				eq = getPrimitiveDefinition(e);
			}
			if (eq != null) {
				ret.add(eq);
				toVisit.addAll(eq.getRight());
			}

			toVisit.removeAll(visited);
		}
		return ret;
	}

}
