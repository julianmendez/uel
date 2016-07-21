package de.tudresden.inf.lat.uel.sat.solver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import de.tudresden.inf.lat.uel.sat.type.SatInput;
import de.tudresden.inf.lat.uel.sat.type.SatOutput;
import de.tudresden.inf.lat.uel.sat.type.SatSolver;

/**
 * This class runs an external MaxSAT solver on a constructed WCNF input file.
 *
 * @author Stefan Borgwardt
 */
public class CNFMaxSatSolver implements SatSolver {

	/** the type constant for the AKMAXSAT solver */
	public static final int AKMAXSAT = 2;
	/** the command to execute the AKMAXSAT solver */
	public static final String AKMAXSAT_COMMAND = "akmaxsat";
	/** the options for the AKMAXSAT solver */
	public static final String[] AKMAXSAT_OPTIONS = new String[] {};
	/** the type constant for the CLASP solver */
	public static final int CLASP = 1;
	/** the command to execute the CLASP solver */
	public static final String CLASP_COMMAND = "clasp";
	/** the options for the CLASP solver */
	public static final String[] CLASP_OPTIONS = new String[] { "--quiet=1,1" };

	private String[] commandOptions;

	private final File inputFile;
	// private File outputFile;
	private Integer nbVars;

	/**
	 * Construct a new MaxSat solver of the given type.
	 * 
	 * @param solver
	 *            the type of the solver (AKMAXSAT or CLASP)
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public CNFMaxSatSolver(int solver) throws IOException {
		this.inputFile = File.createTempFile("WCNFinput", ".tmp");

		switch (solver) {
		case CLASP:
			initCommandOptions(CLASP_COMMAND, CLASP_OPTIONS);
			break;
		case AKMAXSAT:
			initCommandOptions(AKMAXSAT_COMMAND, AKMAXSAT_OPTIONS);
			break;
		}
	}

	public void cleanup() {
	}

	private SatOutput convertToSatOutput(Process p) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

		Set<Integer> clause = new HashSet<Integer>();
		String line = reader.readLine();
		boolean satisfiable = false;

		while (line != null) {
			if (line.startsWith("v")) {
				satisfiable = true;
				StringTokenizer stok = new StringTokenizer(line.substring(2));
				while (stok.hasMoreTokens()) {
					clause.add(Integer.parseInt(stok.nextToken()));
				}
			}
			line = reader.readLine();
		}
		reader.close();
		p.destroy();
		clause.remove(SatSolver.END_OF_CLAUSE);

		return new SatOutput(satisfiable, clause);
	}

	private void initCommandOptions(String command, String[] options) {
		this.commandOptions = new String[2 + CLASP_OPTIONS.length];
		this.commandOptions[0] = CLASP_COMMAND;
		for (int i = 0; i < CLASP_OPTIONS.length; i++) {
			this.commandOptions[i + 1] = CLASP_OPTIONS[i];
		}
		this.commandOptions[CLASP_OPTIONS.length + 1] = this.inputFile.getPath();
	}

	private Process solve() throws IOException {
		try {
			ProcessBuilder pb = new ProcessBuilder(this.commandOptions);
			pb.redirectErrorStream();
			Process p = pb.start();
			p.waitFor();
			return p;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public SatOutput solve(SatInput input) throws IOException {

		this.nbVars = input.getLastId();
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.inputFile));
		writer.write(input.toWCNF((int) Math.pow(this.nbVars + 1, 3)));
		writer.close();

		return convertToSatOutput(solve());
	}

	@Override
	public SatOutput update(Set<Integer> clause) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(this.inputFile));
		StringTokenizer stok = new StringTokenizer(reader.readLine());
		stok.nextToken();
		stok.nextToken();
		stok.nextToken();
		int nbClauses = Integer.parseInt(stok.nextToken());
		reader.close();

		BufferedWriter writer = new BufferedWriter(new FileWriter(this.inputFile));
		StringBuffer sbuf = new StringBuffer();
		SatInput.appendWCNFLine(sbuf, nbVars, nbClauses + 1, nbVars + 1);
		SatInput.appendWCNFClause(sbuf, clause, nbVars + 1);
		writer.write(sbuf.toString());
		writer.close();

		return convertToSatOutput(solve());
	}
}
