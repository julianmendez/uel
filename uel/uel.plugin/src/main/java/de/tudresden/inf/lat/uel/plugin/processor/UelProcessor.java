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
import de.tudresden.inf.lat.uel.core.type.Atom;
import de.tudresden.inf.lat.uel.core.type.Equation;
import de.tudresden.inf.lat.uel.core.type.FAtom;
import de.tudresden.inf.lat.uel.core.type.Goal;
import de.tudresden.inf.lat.uel.core.type.IndexedSet;
import de.tudresden.inf.lat.uel.core.type.Ontology;

/**
 * An object implementing this class connects with the UEL core and uses it to
 * unify.
 * 
 * @author Julian Mendez
 */
public class UelProcessor {

	private IndexedSet<Atom> atomManager = new IndexedSet<Atom>();
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

		return createGoal(this.ontology, input);
	}

	private Goal createGoal(Ontology ont, Set<String> input) {
		List<Equation> equationList = new ArrayList<Equation>();
		for (String cls : input) {
			Equation equation = ont.getDefinition(cls);
			if (equation == null) {
				equation = ont.getPrimitiveDefinition(cls);
			}
			if (equation != null) {
				equationList.add(equation);
			}
		}

		Goal ret = new Goal(ont, getAtomManager());
		try {
			Iterator<String> inputIt = input.iterator();
			FAtom left = new FAtom(inputIt.next(), false, true, null);
			FAtom right = new FAtom(inputIt.next(), false, true, null);
			ret.initialize(equationList, left, right);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return ret;
	}

	public void createTranslator(Goal g) {
		this.translator = new Translator(g, true);
	}

	public IndexedSet<Atom> getAtomManager() {
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

	public void loadOntology(OWLOntology ontology) {
		this.ontology.load(ontology);
	}

}
