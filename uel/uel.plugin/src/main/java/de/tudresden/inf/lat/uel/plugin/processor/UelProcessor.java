package de.tudresden.inf.lat.uel.plugin.processor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.tudresden.inf.lat.uel.main.Equation;
import de.tudresden.inf.lat.uel.main.FAtom;
import de.tudresden.inf.lat.uel.main.Goal;
import de.tudresden.inf.lat.uel.ontmanager.Ontology;
import de.uulm.ecs.ai.owlapi.krssrenderer.KRSS2OWLSyntaxRenderer;

/**
 * This class connects with UEL.
 * 
 * @author Julian Mendez
 */
public class UelProcessor {

	private Set<String> candidates = new HashSet<String>();
	private OWLWorkspace owlWorkspace = null;

	public UelProcessor(OWLWorkspace workspace) {
		if (workspace == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.owlWorkspace = workspace;
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

	private Goal createGoal(Ontology ont, Set<OWLClass> input) {
		Goal goal = new Goal(ont);
		StringBuffer sbuf = new StringBuffer();
		for (OWLClass cls : input) {
			String key = cls.toStringID();
			Equation eq = ont.getPrimitiveDefinition(key);
			sbuf.append(eq.toString());
			sbuf.append("\n");
		}

		try {
			goal.initialize(new StringReader(sbuf.toString()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return goal;
	}

	public Set<String> getCandidates() {
		return Collections.unmodifiableSet(this.candidates);
	}

	private Ontology getOntology(OWLModelManager modelManager) {
		OWLOntologyManager owlOntologyManager = modelManager
				.getOWLOntologyManager();
		OWLOntology owlOntology = modelManager.getActiveOntology();
		StringWriter writer = new StringWriter();
		KRSS2OWLSyntaxRenderer renderer = new KRSS2OWLSyntaxRenderer(
				owlOntologyManager);
		try {
			renderer.render(owlOntology, writer);
		} catch (OWLRendererException e) {
			throw new RuntimeException(e);
		}
		writer.flush();

		Ontology ret = new Ontology();
		try {
			ret.loadOntology(new StringReader(writer.toString()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public OWLWorkspace getOWLWorkspace() {
		return this.owlWorkspace;
	}

	public Set<String> recalculateCandidates(Set<OWLClass> input) {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		Ontology ont = getOntology(getOWLWorkspace().getOWLModelManager());
		Goal goal = createGoal(ont, input);
		Map<String, FAtom> consMap = goal.getConstants();
		Set<String> varSet = new HashSet<String>();
		for (Iterator<String> it = consMap.keySet().iterator(); it.hasNext();) {
			varSet.add(consMap.get(it.next()).toString());
		}
		return Collections.unmodifiableSet(varSet);
	}

}
