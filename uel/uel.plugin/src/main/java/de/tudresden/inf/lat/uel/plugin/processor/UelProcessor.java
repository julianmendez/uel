package de.tudresden.inf.lat.uel.plugin.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.core.sat.Sat4jSolver;
import de.tudresden.inf.lat.uel.core.sat.SatInput;
import de.tudresden.inf.lat.uel.core.sat.SatOutput;
import de.tudresden.inf.lat.uel.core.sat.Solver;
import de.tudresden.inf.lat.uel.core.sat.Translator;
import de.tudresden.inf.lat.uel.core.type.Goal;
import de.tudresden.inf.lat.uel.core.type.Ontology;
import de.tudresden.inf.lat.uel.core.type.SatAtom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.IndexedSet;
import de.tudresden.inf.lat.uel.type.impl.IndexedSetImpl;

/**
 * An object implementing this class connects with the UEL core and uses it to
 * unify.
 * 
 * @author Julian Mendez
 */
public class UelProcessor {

	private IndexedSet<SatAtom> atomManager = new IndexedSetImpl<SatAtom>();
	private DynamicOntology ontology = null;
	private SatInput satinput = null;
	private Translator translator = null;
	private List<Set<Equation>> unifierList = new ArrayList<Set<Equation>>();
	private Set<Set<Equation>> unifierSet = new HashSet<Set<Equation>>();

	/**
	 * Constructs a new processor.
	 */
	public UelProcessor() {
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
	public boolean computeNextUnifier() {
		boolean hasMoreUnifiers = true;
		Solver solver = new Sat4jSolver();
		Set<Equation> result = null;
		SatOutput satoutput = null;
		try {
			if (!getUnifierList().isEmpty()) {
				Set<Integer> update = this.translator.getUpdate();
				if (update.isEmpty()) {
					hasMoreUnifiers = false;
				} else {
					this.satinput.add(update);
				}
			}
			satoutput = solver.solve(this.satinput);
			boolean unifiable = satoutput.isSatisfiable();
			hasMoreUnifiers = hasMoreUnifiers && unifiable;
			this.translator.reset();
			if (unifiable) {
				result = this.translator.toTBox(satoutput.getOutput());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (result != null && !this.unifierSet.contains(result)) {
			this.unifierList.add(result);
			this.unifierSet.add(result);
		}
		return hasMoreUnifiers;
	}

	public void computeSatInput() {
		this.satinput = this.translator.computeSatInput();
	}

	public Goal configure(Set<String> input) {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Iterator<String> it = input.iterator();
		String leftStr = it.next();
		String rightStr = it.next();

		return new Goal(this.atomManager, this.ontology, leftStr, rightStr);
	}

	public void createTranslator(Goal g) {
		this.translator = new Translator(g, true);
	}

	public IndexedSet<SatAtom> getAtomManager() {
		return this.atomManager;
	}

	public Ontology getOntology() {
		return this.ontology;
	}

	public SatInput getSatInput() {
		return this.satinput;
	}

	public Translator getTranslator() {
		return this.translator;
	}

	public List<Set<Equation>> getUnifierList() {
		return Collections.unmodifiableList(this.unifierList);
	}

	public void loadOntology(OWLOntology ontology01, OWLOntology ontology02) {
		this.ontology.load(ontology01, ontology02);
	}

}
