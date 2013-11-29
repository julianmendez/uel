package de.tudresden.inf.lat.uel.asp.solver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

/**
 * @author Stefan Borgwardt
 * 
 */
public class ClaspSolver implements AspSolver {

	private static String UNIFICATION_PROGRAM = "resources/unification.lp";
	private static String HEURISTIC_PROGRAM = "resources/heuristic.lp";
	private static String GRINGO_COMMAND = "gringo";
	private static String CLASP_COMMAND = "clasp 0 --outf=2";
	private static String HCLASP_COMMAND = "hclasp 0 -e record --outf=2";

	private boolean minimize;

	public ClaspSolver(boolean minimize) {
		this.minimize = minimize;
	}

	@Override
	public AspOutput solve(AspInput input) throws IOException {
		try {
			// call gringo and (h)clasp
			ProcessBuilder pbGringo = new ProcessBuilder(GRINGO_COMMAND);
			Process pGringo = pbGringo.start();

			String solverCommand = minimize ? HCLASP_COMMAND : CLASP_COMMAND;
			ProcessBuilder pbSolver = new ProcessBuilder(
					Arrays.asList(solverCommand.split(" ")));
			Process pSolver = pbSolver.start();

			// pipe unification.lp (heuristic.lp) and input.getProgram() as
			// input
			OutputStream gringoInput = pGringo.getOutputStream();
			InputStream unificationProgram = AspProcessor.class
					.getResourceAsStream(UNIFICATION_PROGRAM);
			pipe(unificationProgram, gringoInput);
			if (minimize) {
				InputStream heuristicProgram = AspProcessor.class
						.getResourceAsStream(HEURISTIC_PROGRAM);
				pipe(heuristicProgram, gringoInput);
			}
			pipe(new ByteArrayInputStream(input.getProgram().getBytes()),
					gringoInput);
			// System.out.println(input.getProgram());
			gringoInput.close();

			// pipe the output to (h)clasp
			pipe(pGringo.getInputStream(), pSolver.getOutputStream());
			pSolver.getOutputStream().close();

			if (pGringo.waitFor() != 0) {
				ByteArrayOutputStream error = new ByteArrayOutputStream();
				pipe(pGringo.getErrorStream(), error);
				throw new IOException("gringo error:\n" + error.toString());
			}
			pGringo.destroy();

			// return the json output
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			pipe(pSolver.getInputStream(), output);

			int claspReturnCode = pSolver.waitFor();
			if ((claspReturnCode) != 20 && (claspReturnCode != 30)) {
				ByteArrayOutputStream error = new ByteArrayOutputStream();
				pipe(pSolver.getErrorStream(), error);
				throw new IOException("clasp error:\n" + error.toString());
			}
			pSolver.destroy();

			return new ClaspOutput(output.toString(), input.getAtomManager());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void pipe(InputStream input, OutputStream output)
			throws IOException {
		pipe(new BufferedReader(new InputStreamReader(input)),
				new BufferedWriter(new OutputStreamWriter(output)));
	}

	private void pipe(BufferedReader input, BufferedWriter output)
			throws IOException {
		String line = "";
		while (line != null) {
			line = input.readLine();
			// System.out.println(line);
			if (line != null) {
				output.write(line);
				output.newLine();
			}
		}
		output.flush();
	}

}
