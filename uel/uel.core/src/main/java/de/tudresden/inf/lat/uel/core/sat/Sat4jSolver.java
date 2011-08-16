package de.tudresden.inf.lat.uel.core.sat;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

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

	public static final int timeout = 3600;

	/**
	 * Constructs a new solver.
	 */
	public Sat4jSolver() {
	}

	@Override
	public String solve(String input) throws IOException {
		ByteArrayInputStream satinputInputStream = new ByteArrayInputStream(
				input.getBytes());
		StringWriter satoutputWriter = new StringWriter();

		ISolver solver = SolverFactory.newDefault();
		solver.setTimeout(timeout);
		DimacsReader reader = new DimacsReader(solver);
		try {
			IProblem problem = reader.parseInstance(satinputInputStream);
			if (problem.isSatisfiable()) {
				BufferedWriter output = new BufferedWriter(satoutputWriter);
				output.write(msgSat);
				output.newLine();
				output.write(reader.decode(problem.model()));
				output.newLine();
				output.flush();
			} else {
				BufferedWriter output = new BufferedWriter(satoutputWriter);
				output.write(msgUnsat);
				output.newLine();
				output.flush();
			}
		} catch (ContradictionException e) {
			BufferedWriter output = new BufferedWriter(satoutputWriter);
			output.write(msgUnsat);
			output.newLine();
			output.flush();
		} catch (ParseFormatException e) {
			throw new RuntimeException(e);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}

		return satoutputWriter.toString();
	}

}
