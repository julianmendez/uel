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
import de.tudresden.inf.lat.uel.core.type.Equation;
import de.tudresden.inf.lat.uel.core.type.FAtom;
import de.tudresden.inf.lat.uel.core.type.Goal;
import de.tudresden.inf.lat.uel.core.type.Ontology;

/**
 * An object implementing this class connects with the UEL core and uses it to
 * unify.
 * 
 * @author Julian Mendez
 */
public class UelProcessor {

	private Set<String> candidates = new HashSet<String>();
	private DynamicOntology ontology = new DynamicOntology();
	private SatInput satinput = null;
	private Translator translator = null;
	private List<String> unifierList = new ArrayList<String>();
	private Set<String> unifierSet = new HashSet<String>();

	/**
	 * Constructs a new processor.
	 */
	public UelProcessor() {
	}

	public void addAll(Set<String> set) {
		if (set == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.candidates.addAll(set);
	}

	public void clearCandidates() {
		this.candidates.clear();
	}

	public void clearOntology() {
		this.ontology.clear();
		this.candidates.clear();
		this.unifierList.clear();
		this.unifierSet.clear();
	}

	public boolean computeNextUnifier() {
		boolean unifiable = false;
		Solver solver = new Sat4jSolver();
		String result = null;
		SatOutput satoutput = null;
		try {
			if (!getUnifierList().isEmpty()) {
				this.satinput.add(this.translator.getUpdate());
			}
			satoutput = solver.solve(this.satinput);
			unifiable = satoutput.isSatisfiable();
			this.translator.reset();
			result = this.translator.toTBox(satoutput.getOutput());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (this.unifierSet.contains(result)) {
			unifiable = false;
		}
		if (unifiable) {
			this.unifierList.add(result);
			this.unifierSet.add(result);
		}
		return unifiable;
	}

	public void computeSatInput() {
		this.satinput = this.translator.computeSatInput();
	}

	public Goal configure(Set<String> input) {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		return createGoal(this.ontology, input, this.candidates);
	}

	private Goal createGoal(Ontology ont, Set<String> input, Set<String> vars) {
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

		Goal ret = new Goal(ont);
		try {
			Iterator<String> inputIt = input.iterator();
			FAtom left = new FAtom(inputIt.next(), false, true, null);
			FAtom right = new FAtom(inputIt.next(), false, true, null);
			ret.initialize(equationList, left, right, vars);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return ret;
	}

	public void createTranslator(Goal g) {
		this.translator = new Translator(g, true);
	}

	public Set<String> getCandidates() {
		return Collections.unmodifiableSet(this.candidates);
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

	public List<String> getUnifierList() {
		return Collections.unmodifiableList(this.unifierList);
	}

	public void loadOntology(OWLOntology ontology) {
		this.ontology.load(ontology);
	}

}
