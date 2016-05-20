package de.tudresden.inf.lat.uel.sat.solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import de.tudresden.inf.lat.uel.sat.type.SatInput;
import de.tudresden.inf.lat.uel.sat.type.SatOutput;
import de.tudresden.inf.lat.uel.sat.type.SatSolver;

/**
 * An object of this class uses the MiniSat solver to solve a SAT problem.
 * 
 * @author Julian Mendez
 */
public class MiniSatSolver implements SatSolver {

	private static final String minisatCommand = "MiniSat";
	private static final String tempPrefix = "uelMiniSat";
	private static final String tempSuffix = ".tmp";

	/**
	 * Constructs a new solver.
	 */
	public MiniSatSolver() {
	}

	public void cleanup() {
	}

	private void runMiniSat(File satinput, File satoutput) throws IOException {
		try {
			ProcessBuilder pb = new ProcessBuilder(minisatCommand, satinput.toString(), satoutput.toString());
			Process p = pb.start();
			p.waitFor();
			p.destroy();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public SatOutput solve(SatInput input) throws IOException {
		if (input == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		File satinput = File.createTempFile(tempPrefix, tempSuffix);
		File satoutput = File.createTempFile(tempPrefix, tempSuffix);

		PrintWriter satinputWriter = new PrintWriter(new FileWriter(satinput));
		satinputWriter.println(input.toCNF());
		satinputWriter.flush();
		satinputWriter.close();

		runMiniSat(satinput, satoutput);

		BufferedReader satoutputReader = new BufferedReader(new FileReader(satoutput));
		String line = satoutputReader.readLine();
		boolean satisfiable = false;
		Set<Integer> clause = new HashSet<Integer>();
		if (line.trim().equals(SatSolver.SAT)) {
			satisfiable = true;
			StringTokenizer stok = new StringTokenizer(satoutputReader.readLine());
			while (stok.hasMoreTokens()) {
				clause.add(Integer.parseInt(stok.nextToken()));
			}
			clause.remove(SatSolver.END_OF_CLAUSE);
		}

		satinput.delete();
		satoutput.delete();
		satoutputReader.close();

		return new SatOutput(satisfiable, clause);
	}

	@Override
	public SatOutput update(Set<Integer> clause) throws IOException {
		throw new UnsupportedOperationException();
	}

}
