package de.tudresden.inf.lat.uel.plugin.processor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

import de.tudresden.inf.lat.uel.core.sat.Sat4jSolver;
import de.tudresden.inf.lat.uel.core.sat.SatInput;
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
		StringWriter result = new StringWriter();
		String satoutputStr = null;
		try {
			if (getUnifierList().isEmpty()) {
				this.satinput = this.translator.getSatInput();
			} else {
				this.satinput.add(this.translator.getUpdate().toString());
			}
			satoutputStr = solver.solve(this.satinput.toString());
			this.translator.reset();
			unifiable = this.translator.toTBox(new StringReader(satoutputStr),
					result);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		result.flush();
		String res = result.toString();
		if (this.unifierSet.contains(res)) {
			unifiable = false;
		}
		if (unifiable) {
			this.unifierList.add(res);
			this.unifierSet.add(res);
		}
		return unifiable;
	}

	public Goal configure(Set<String> input) {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Goal ret = createGoal(this.ontology, input, this.candidates);
		this.translator = new Translator(ret);
		return ret;
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

	public Set<String> getCandidates() {
		return Collections.unmodifiableSet(this.candidates);
	}

	public Ontology getOntology() {
		return this.ontology;
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
