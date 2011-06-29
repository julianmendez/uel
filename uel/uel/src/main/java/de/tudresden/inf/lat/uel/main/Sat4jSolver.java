package de.tudresden.inf.lat.uel.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import de.tudresden.inf.lat.uel.sattranslator.Translator;

/**
 * An object of this class uses the Sat4j solver to solve a SAT problem.
 */
public class Sat4jSolver implements Solver {

	@Override
	public boolean unify(Translator translator, File satinput, File satoutput,
			Writer result) throws IOException {

		translator.toDIMACS(new FileWriter(satinput));
		ISolver solver = SolverFactory.newDefault();
		solver.setTimeout(3600);
		DimacsReader reader = new DimacsReader(solver);
		try {
			IProblem problem = reader.parseInstance(new FileInputStream(
					satinput));
			if (problem.isSatisfiable()) {
				PrintStream output = new PrintStream(new FileOutputStream(
						satoutput));
				output.println("SAT");
				output.println(reader.decode(problem.model()));
				output.flush();
			} else {
				PrintStream output = new PrintStream(new FileOutputStream(
						satoutput));
				output.println("UNSAT");
				output.flush();
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (ParseFormatException e) {
			throw new RuntimeException(e);
		} catch (ContradictionException e) {
			PrintStream output = new PrintStream(
					new FileOutputStream(satoutput));
			output.println("UNSAT");
			output.flush();
		} catch (TimeoutException e) {
			PrintStream output = new PrintStream(
					new FileOutputStream(satoutput));
			output.println("TIMEOUT");
			output.flush();
		}
		boolean response = translator.toTBox(new FileReader(satoutput), result);
		return response;
	}

}
