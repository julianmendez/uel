package de.tudresden.inf.lat.uel.sat.solver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * An object of this class uses the Sat4j solver to solve a SAT problem.
 * 
 * @author Julian Mendez
 */
public class Sat4jSolver implements Solver {

	private static final int timeout = 3600;

	/**
	 * Constructs a new solver.
	 */
	public Sat4jSolver() {
	}

	@Override
	public SatOutput solve(SatInput input) throws IOException {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		ByteArrayInputStream satinputInputStream = new ByteArrayInputStream(
				input.toCNF().getBytes());
		ISolver solver = SolverFactory.newDefault();
		solver.setTimeout(timeout);
		boolean satisfiable = false;
		Set<Integer> clause = new TreeSet<Integer>();
		try {
			DimacsReader reader = new DimacsReader(solver);
			IProblem problem = reader.parseInstance(satinputInputStream);
			satisfiable = problem.isSatisfiable();
			if (satisfiable) {
				int[] model = problem.model();
				for (Integer e : model) {
					clause.add(e);
				}
				clause.remove(Solver.END_OF_CLAUSE);
			}
		} catch (ContradictionException e) {
			// unsatisfiable
		} catch (ParseFormatException e) {
			throw new RuntimeException(e);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}

		return new SatOutput(satisfiable, clause);
	}

}
