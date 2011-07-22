package de.tudresden.inf.lat.uel.plugin.processor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tudresden.inf.lat.uel.main.Equation;
import de.tudresden.inf.lat.uel.main.FAtom;
import de.tudresden.inf.lat.uel.main.Goal;
import de.tudresden.inf.lat.uel.main.Sat4jSolver;
import de.tudresden.inf.lat.uel.main.Solver;
import de.tudresden.inf.lat.uel.ontmanager.Ontology;
import de.tudresden.inf.lat.uel.sattranslator.SatInput;
import de.tudresden.inf.lat.uel.sattranslator.Translator;
import de.uulm.ecs.ai.owlapi.krssrenderer.KRSS2OWLSyntaxRenderer;

/**
 * This class connects with UEL.
 * 
 * @author Julian Mendez
 */
public class UelProcessor {

	private Set<String> candidates = new HashSet<String>();
	private Ontology ontology = null;
	private SatInput satinput = null;
	private Translator translator = null;
	private List<String> unifierList = new ArrayList<String>();

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
		this.ontology = new Ontology();
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
		if (unifiable) {
			this.unifierList.add(result.toString());
		}
		return unifiable;
	}

	public void configure(Set<String> input) {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Goal goal = createGoal(ontology, input, this.candidates);
		this.translator = new Translator(goal);
	}

	private Goal createGoal(Ontology ont, Set<String> input, Set<String> vars) {
		Goal goal = new Goal(ont);
		StringBuffer sbuf = new StringBuffer();
		for (String cls : input) {
			Equation eq = ont.getPrimitiveDefinition(cls);
			sbuf.append(eq.toString());
			sbuf.append("\n");
		}

		try {
			goal.initialize(new StringReader(sbuf.toString()), vars);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return goal;
	}

	public Set<String> getCandidates() {
		return Collections.unmodifiableSet(this.candidates);
	}

	public List<String> getUnifierList() {
		return Collections.unmodifiableList(this.unifierList);
	}

	public void loadOntology(OWLOntologyManager owlOntologyManager,
			OWLOntology owlOntology) {
		if (owlOntologyManager == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (owlOntology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		StringWriter writer = new StringWriter();
		KRSS2OWLSyntaxRenderer renderer = new KRSS2OWLSyntaxRenderer(
				owlOntologyManager);
		try {
			renderer.render(owlOntology, writer);
		} catch (OWLRendererException e) {
			throw new RuntimeException(e);
		}
		writer.flush();

		try {
			this.ontology.loadOntology(new StringReader(writer.toString()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void recalculateCandidates(Set<String> input) {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Goal goal = createGoal(this.ontology, input, new HashSet<String>());
		Map<String, FAtom> consMap = goal.getConstants();
		Set<String> varSet = new HashSet<String>();
		for (Iterator<String> it = consMap.keySet().iterator(); it.hasNext();) {
			varSet.add(consMap.get(it.next()).toString());
		}
		this.candidates = varSet;
		this.unifierList.clear();
	}

}
