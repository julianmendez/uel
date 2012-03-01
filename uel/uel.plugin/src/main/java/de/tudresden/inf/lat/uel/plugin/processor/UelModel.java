package de.tudresden.inf.lat.uel.plugin.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.plugin.type.SatAtom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.IndexedSetImpl;

/**
 * An object implementing this class connects with the UEL core and uses it to
 * unify.
 * 
 * @author Julian Mendez
 */
public class UelModel {

	private IndexedSet<SatAtom> atomManager = new IndexedSetImpl<SatAtom>();
	private DynamicOntology ontology = null;
	private String processorName;
	private UelProcessor uelProcessor = null;
	private List<Set<Equation>> unifierList = new ArrayList<Set<Equation>>();
	private Set<Set<Equation>> unifierSet = new HashSet<Set<Equation>>();

	/**
	 * Constructs a new processor.
	 */
	public UelModel() {
		this.ontology = new DynamicOntology(new OntologyBuilder(
				getAtomManager()));
	}

	public void clearOntology() {
		this.ontology.clear();
		this.unifierList.clear();
		this.unifierSet.clear();
	}

	public boolean computeNextUnifier() {
		boolean ret = this.uelProcessor.computeNextUnifier();
		if (ret) {
			Set<Equation> result = this.uelProcessor.getUnifier()
					.getEquations();
			if (result != null && !this.unifierSet.contains(result)) {
				this.unifierList.add(result);
				this.unifierSet.add(result);
			}
		}
		return ret;
	}

	/**
	 * Computes the next unifier. This unifier can be equivalent to another one
	 * already computed.
	 * 
	 * @return <code>true</code> if and only if more unifiers can be computed
	 */

	public PluginGoal configure(Set<String> input) {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Iterator<String> it = input.iterator();
		String leftStr = it.next();
		String rightStr = it.next();

		return new PluginGoal(this.atomManager, this.ontology, leftStr,
				rightStr);
	}

	public void configureUelProcessor(UelProcessor processor) {
		this.uelProcessor = processor;
	}

	public IndexedSet<SatAtom> getAtomManager() {
		return this.atomManager;
	}

	public Ontology getOntology() {
		return this.ontology;
	}

	public String getProcessorName() {
		return this.processorName;
	}

	public UelProcessor getUelProcessor() {
		return this.uelProcessor;
	}

	public List<Set<Equation>> getUnifierList() {
		return Collections.unmodifiableList(this.unifierList);
	}

	public void loadOntology(OWLOntology ontology01, OWLOntology ontology02) {
		this.ontology.load(ontology01, ontology02);
	}

	public void setProcessorName(String name) {
		if (!UelProcessorFactory.getProcessorNames().contains(name)) {
			throw new IllegalArgumentException("Processor name is invalid: '"
					+ name + "'.");
		}
		this.processorName = name;
	}

}
