/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Stopwatch;

import de.tudresden.inf.lat.uel.core.main.AlternativeUelStarter;
import de.tudresden.inf.lat.uel.core.main.UnifierIterator;
import de.tudresden.inf.lat.uel.core.processor.UelOntology;
import de.tudresden.inf.lat.uel.core.processor.UelOptions;
import de.tudresden.inf.lat.uel.plugin.main.SNOMEDResult.SNOMEDGoalStatus;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.impl.AtomManagerImpl;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDTestInitialization implements Runnable {

	private final UelOptions options;
	private final OWLOntology snomed;
	private final Set<OWLOntology> bg;
	private final OWLClass goalClass;
	private final OWLClassExpression goalExpression;

	public UnifierIterator iterator;
	public OWLOntology pos;
	public OWLOntology neg;
	public OWLAxiom goalAxiom;
	public SNOMEDResult result;

	public SNOMEDTestInitialization(UelOptions options, OWLOntology snomed, Set<OWLOntology> bg, OWLClass goalClass,
			OWLClassExpression goalExpression) {
		this.options = options;
		this.snomed = snomed;
		this.bg = bg;
		this.goalClass = goalClass;
		this.goalExpression = goalExpression;
		this.result = new SNOMEDResult(goalClass, options);
	}

	@Override
	public void run() {
		try {
			buildGoal();
		} catch (Throwable t) {
			t.printStackTrace();
			result.goalStatus = SNOMEDGoalStatus.ERROR;
		}
	}

	private void buildGoal() throws OWLOntologyCreationException, InterruptedException {
		Stopwatch timer = Stopwatch.createStarted();

		if (options.verbosity.level > 1) {
			System.out.println("Goal expression: " + goalExpression);
		}

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();

		OWLClass x = SNOMEDEvaluation.cls("http://snomed.info/id/X");
		OWLClass top = SNOMEDEvaluation.cls(options.snomedCtConceptUri);
		Set<OWLClass> vars = new HashSet<OWLClass>(Arrays.asList(x));

		UelOntology ont = new UelOntology(new AtomManagerImpl(), Collections.singleton(snomed), top, true);
		Set<Integer> id = ont.processClassExpression(goalClass, new HashSet<Definition>(), false);
		Set<OWLClass> superclasses = ont.getDirectSuperclasses(goalClass);
		Set<OWLClass> siblings = ont.getSiblings(id.iterator().next(), false, options.numberOfSiblings, false);
		if (options.verbosity.level > 0) {
			System.out.println("Siblings: " + siblings.size());
		}
		if ((SNOMEDEvaluation.MAX_SIBLINGS > -1) && (siblings.size() > SNOMEDEvaluation.MAX_SIBLINGS)) {
			System.out.println("Too many siblings!");
			result.goalStatus = SNOMEDGoalStatus.TOO_LARGE;
			return;
		}
		if (options.verbosity.level > 1) {
			System.out.println("Siblings extracted");
		}

		pos = manager.createOntology();
		neg = manager.createOntology();
		manager.addAxioms(pos,
				superclasses.stream().map(cls -> factory.getOWLSubClassOfAxiom(x, cls)).collect(Collectors.toSet()));
		manager.addAxioms(neg,
				superclasses.stream().map(cls -> factory.getOWLSubClassOfAxiom(cls, x)).collect(Collectors.toSet()));
		manager.addAxioms(neg,
				siblings.stream().flatMap(
						cls -> Stream.of(factory.getOWLSubClassOfAxiom(x, cls), factory.getOWLSubClassOfAxiom(cls, x)))
						.collect(Collectors.toSet()));

		iterator = (UnifierIterator) AlternativeUelStarter.solve(bg, pos, neg, null, vars, options);
		goalAxiom = factory.getOWLEquivalentClassesAxiom(x, goalExpression);
		result.buildGoal = SNOMEDEvaluation.output(timer, "Building the unification problem", true);

		int size = iterator.getUelModel().getGoal().getAtomManager().size();
		result.goalSize = size;
		if (options.verbosity.level > 0) {
			System.out.println("Size: " + size);
		}
		if ((SNOMEDEvaluation.MAX_ATOMS > -1) && (size > SNOMEDEvaluation.MAX_ATOMS)) {
			System.out.println("Problem is too large!");
			result.goalStatus = SNOMEDGoalStatus.TOO_LARGE;
		} else {
			result.goalStatus = SNOMEDGoalStatus.SUCCESS;
		}
	}

}
