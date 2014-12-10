package de.tudresden.inf.lat.uel.core.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.core.type.AtomManager;
import de.tudresden.inf.lat.uel.core.type.AtomManagerImpl;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;

/**
 * An object of this class connects the graphical user interface with the
 * processor.
 * 
 * @author Julian Mendez
 */
public class UelModel {

	private AtomManager atomManager = new AtomManagerImpl();
	private DynamicOntology ontology = null;
	private String processorName;
	private UelProcessor uelProcessor = null;
	private List<Set<Equation>> unifierList = new ArrayList<Set<Equation>>();
	private Set<Set<Equation>> unifierSet = new HashSet<Set<Equation>>();
	private PluginGoal pluginGoal = null;

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

	/**
	 * Computes the next unifier. This unifier can be equivalent to another one
	 * already computed.
	 * 
	 * @return <code>true</code> if and only if more unifiers can be computed
	 */
	public boolean computeNextUnifier() throws InterruptedException {
		while (this.uelProcessor.computeNextUnifier()) {
			Set<Equation> result = this.uelProcessor.getUnifier()
					.getEquations();
			if (!this.unifierSet.contains(result)) {
				this.unifierList.add(result);
				this.unifierSet.add(result);
				return true;
			}
		}
		return false;
	}

	public void configure(Set<String> input) {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Iterator<String> it = input.iterator();
		String leftStr = it.next();
		String rightStr = it.next();

		this.pluginGoal = new PluginGoal(this.atomManager, this.ontology,
				leftStr, rightStr);
	}

	public PluginGoal getPluginGoal() {
		return this.pluginGoal;
	}

	public void configureUelProcessor(UelProcessor processor) {
		this.uelProcessor = processor;
	}

	public AtomManager getAtomManager() {
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
