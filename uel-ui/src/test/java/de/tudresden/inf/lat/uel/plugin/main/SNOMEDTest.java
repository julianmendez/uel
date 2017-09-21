/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.util.Scanner;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.google.common.base.Stopwatch;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import de.tudresden.inf.lat.uel.core.main.UnifierIterator;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UelOptions;
import de.tudresden.inf.lat.uel.plugin.main.SNOMEDAlgorithmResult.SNOMEDAlgorithmStatus;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDTest implements Runnable {

	private final UnifierIterator iterator;
	private final UelOptions options;
	private final OWLOntology pos;
	private final OWLOntology neg;
	private final OWLAxiom goalAxiom;

	public SNOMEDAlgorithmResult result;

	public SNOMEDTest(UnifierIterator iterator, UelOptions options, OWLOntology pos, OWLOntology neg,
			OWLAxiom goalAxiom) {
		this.iterator = iterator;
		this.options = options;
		this.pos = pos;
		this.neg = neg;
		this.goalAxiom = goalAxiom;
		this.result = new SNOMEDAlgorithmResult(options.unificationAlgorithmName);
	}

	@Override
	public void run() {
		try {
			computeUnifiers();
		} catch (Exception ex) {
			ex.printStackTrace();
			result.status = SNOMEDAlgorithmStatus.ERROR;
		} finally {
			if (iterator != null) {
				iterator.cleanup();
			}
		}
	}

	private void computeUnifiers() throws OWLOntologyCreationException, InterruptedException {
		Stopwatch timer = Stopwatch.createStarted();

		UelModel model = iterator.getUelModel();
		Set<OWLAxiom> background = model.renderDefinitions();
		model.getUnificationAlgorithm().setCallbackPreprocessing(() -> {
			result.preprocessing = SNOMEDEvaluation.output(timer, "Time for preprocessing", false);
		});

		// System.out.println("Unifiers:");

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

				Set<OWLEquivalentClassesAxiom> unifier = iterator.next();

				if (Thread.interrupted()) {
					break;
				}

				OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
				OWLOntology extendedOntology = ontologyManager.createOntology();
				ontologyManager.addAxioms(extendedOntology, background);
				ontologyManager.addAxioms(extendedOntology, unifier);

				OWLReasoner reasoner = new JcelReasonerFactory().createNonBufferingReasoner(extendedOntology);
				reasoner.precomputeInferences();

				if (SNOMEDEvaluation.CHECK_UNIFIERS) {
					boolean solution = true;
					for (OWLAxiom a : pos.getAxioms(AxiomType.SUBCLASS_OF)) {
						if (!reasoner.isEntailed(a)) {
							solution = false;
						}
					}
					for (OWLAxiom a : neg.getAxioms(AxiomType.SUBCLASS_OF)) {
						if (reasoner.isEntailed(a)) {
							solution = false;
						}
					}
					if (!solution) {
						System.out.println("This is not a real solution!");
					}
				}

				if (reasoner.isEntailed(goalAxiom)) {
					System.out.println("----------------- This is the wanted solution! ------------------");
					result.totalTime = result.goalUnifier = SNOMEDEvaluation.output(timer,
							"Time to compute the wanted solution", false);
					result.status = SNOMEDAlgorithmStatus.SUCCESS;
					break;
				} else {
					if (i == 1) {
						result.firstUnifier = SNOMEDEvaluation.output(timer, "Time to compute first solution", false);
					}
				}

				reasoner.dispose();
			} else {
				System.out.println("No more unifiers.");
				result.totalTime = result.allUnifiers = SNOMEDEvaluation.output(timer, "Time to compute all solutions",
						false);
				break;
			}

			if (!skip) {
				System.out.println("Press RETURN to start computing the next unifier (input 'a' for all unifiers) ...");
			}
		}
		in.close();
	}

}
