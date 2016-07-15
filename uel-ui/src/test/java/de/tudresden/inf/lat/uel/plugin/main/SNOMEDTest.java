/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.google.common.base.Stopwatch;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import de.tudresden.inf.lat.uel.core.main.AlternativeUelStarter;
import de.tudresden.inf.lat.uel.core.main.UnifierIterator;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UelOntology;
import de.tudresden.inf.lat.uel.core.processor.UelOptions;
import de.tudresden.inf.lat.uel.plugin.main.SNOMEDResult.SNOMEDStatus;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.impl.AtomManagerImpl;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDTest extends Thread {

	private UelOptions options;
	private OWLOntology snomed;
	private Set<OWLOntology> bg;
	private OWLClass goalClass;
	private OWLClassExpression goalExpression;

	private UnifierIterator iterator;
	private OWLOntology pos;
	private OWLOntology neg;
	private OWLAxiom goalAxiom;

	public SNOMEDResult result;

	public SNOMEDTest(UelOptions options, OWLOntology snomed, Set<OWLOntology> bg, OWLClass goalClass,
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
			runTest();
		} catch (Throwable t) {
			t.printStackTrace();
			result.status = SNOMEDStatus.ERROR;
		} finally {
			if (iterator != null) {
				iterator.cleanup();
			}
		}
	}

	private void runTest() throws OWLOntologyCreationException, InterruptedException {
		Stopwatch timer = Stopwatch.createStarted();

		if (options.verbosity.level > 1) {
			System.out.println("Goal expression: " + goalExpression);
		}

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();

		OWLClass x = SNOMEDEvaluation.cls("X");
		OWLClass top = SNOMEDEvaluation.cls("SCT_138875005");
		Set<OWLClass> vars = new HashSet<OWLClass>(Arrays.asList(x));

		UelOntology ont = new UelOntology(new AtomManagerImpl(), Collections.singleton(snomed), top, true);
		Set<Integer> id = ont.processClassExpression(goalClass, new HashSet<Definition>(), false);
		Set<OWLClass> superclasses = ont.getDirectSuperclasses(goalClass);
		Set<OWLClass> siblings = ont.getSiblings(id.iterator().next(), false, options.numberOfSiblings, false);
		if (options.verbosity.level > 0) {
			System.out.println("Siblings: " + siblings.size());
		}
		if (siblings.size() > SNOMEDEvaluation.MAX_SIBLINGS) {
			System.out.println("Too many siblings!");
			result.status = SNOMEDStatus.TOO_LARGE;
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
		if (size > SNOMEDEvaluation.MAX_ATOMS) {
			System.out.println("Problem is too large!");
			result.status = SNOMEDStatus.TOO_LARGE;
		} else {
			// iterator.getUelModel().printGoalInfo();
			computeUnifiers();
		}
	}

	private void computeUnifiers() throws OWLOntologyCreationException, InterruptedException {
		Stopwatch timer = Stopwatch.createStarted();

		UelModel model = iterator.getUelModel();
		Set<OWLAxiom> background = model.renderDefinitions();

		System.out.println("Unifiers:");

		Scanner in = new Scanner(System.in);
		int i = 0;
		boolean skip = true;

		while (skip || in.hasNextLine()) {
			if (!skip) {
				if (in.nextLine().equals("a")) {
					skip = true;
				}
			}
			// if (i == 0) {
			// skip = false;
			// }

			i++;
			// System.out.println();
			// System.out.println("--- " + i);

			if (iterator.hasNext()) {

				result.numberOfSolutions++;

				// System.out.println(model.printCurrentUnifier());
				// System.out.println(
				// model.getStringRenderer(null).renderUnifier(model.getCurrentUnifier(),
				// false, false, true));

				Set<OWLEquivalentClassesAxiom> unifier = iterator.next();

				if (Thread.interrupted()) {
					break;
				}

				OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
				OWLOntology extendedOntology = ontologyManager.createOntology();
				ontologyManager.addAxioms(extendedOntology, background);
				ontologyManager.addAxioms(extendedOntology, unifier);

				// ontologyManager.saveOntology(extendedOntology, new
				// FunctionalSyntaxDocumentFormat(), System.out);

				OWLReasoner reasoner = new JcelReasonerFactory().createNonBufferingReasoner(extendedOntology);
				reasoner.precomputeInferences();

				boolean solution = true;
				for (OWLAxiom a : pos.getAxioms(AxiomType.SUBCLASS_OF)) {
					// System.out.println(a + " (pos): " +
					// reasoner.isEntailed(a));
					if (!reasoner.isEntailed(a)) {
						solution = false;
					}
					// Assert.assertTrue(reasoner.isEntailed(a));
				}
				for (OWLAxiom a : neg.getAxioms(AxiomType.SUBCLASS_OF)) {
					// System.out.println(a + " (neg): " +
					// reasoner.isEntailed(a));
					if (reasoner.isEntailed(a)) {
						solution = false;
					}
					// Assert.assertTrue(!reasoner.isEntailed(a));
				}
				if (!solution) {
					System.out.println("This is not a real solution!");
				}

				// System.out.println(goalAxiom + " (goal): " +
				// reasoner.isEntailed(goalAxiom));
				if (reasoner.isEntailed(goalAxiom)) {
					System.out.println("----------------- This is the wanted solution! ------------------");
					result.goalUnifier = SNOMEDEvaluation.output(timer, "Time to compute the wanted solution", false);
					result.status = SNOMEDStatus.SUCCESS;
					break;
				} else {
					if (i == 1) {
						result.firstUnifier = SNOMEDEvaluation.output(timer, "Time to compute first solution", false);
					}
				}

				reasoner.dispose();
				System.out.println();
				System.out.flush();
			} else {
				System.out.println("No more unifiers.");
				result.allUnifiers = SNOMEDEvaluation.output(timer, "Time to compute all solutions", false);
				break;
			}

			if (!skip) {
				System.out.println("Press RETURN to start computing the next unifier (input 'a' for all unifiers) ...");
			}
		}
		in.close();
	}

}
