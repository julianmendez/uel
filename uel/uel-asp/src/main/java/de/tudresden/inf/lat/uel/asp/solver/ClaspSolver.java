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
	private static String DISUNIFICATION_PROGRAM = "resources/disunification.lp";
	private static String TYPES_PROGRAM = "resources/types.lp";
	private static String HEURISTIC_PROGRAM = "resources/heuristic.lp";
	private static String FINAL_PROGRAM = "resources/final.lp";
	private static String GRINGO_COMMAND = "gringo";
	private static String CLASP_COMMAND = "clasp 0 --project --outf=2";
	private static String HCLASP_COMMAND = "hclasp 0 --project -e record --outf=2";

	private boolean disequations;
	private boolean types;
	private boolean minimize;

	public ClaspSolver(boolean disequations, boolean types, boolean minimize) {
		this.disequations = disequations;
		this.types = types;
		this.minimize = minimize;
	}

	@Override
	public AspOutput solve(AspInput input) throws IOException {
		try {
			// call gringo and (h)clasp
			ProcessBuilder pbGringo = new ProcessBuilder(
					GRINGO_COMMAND.split(" "));
			Process pGringo = pbGringo.start();

			String solverCommand = minimize ? HCLASP_COMMAND : CLASP_COMMAND;
			ProcessBuilder pbSolver = new ProcessBuilder(
					Arrays.asList(solverCommand.split(" ")));
			Process pSolver = pbSolver.start();

			// pipe .lp files and input.getProgram() as input
			OutputStream gringoInput = pGringo.getOutputStream();
			inputPrograms(gringoInput);
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
			// successful if there was no exception (lsb=0) and either a model
			// was found (10) or the search space was exhausted (20)
			if (((claspReturnCode & 1) == 1)
					|| (((claspReturnCode & 10) == 0) && ((claspReturnCode & 20) == 0))) {
				ByteArrayOutputStream error = new ByteArrayOutputStream();
				pipe(pSolver.getErrorStream(), error);
				throw new IOException("clasp error (return code "
						+ claspReturnCode + "):\n" + error.toString());
			}
			pSolver.destroy();

			// System.out.println(output.toString());
			return new ClaspOutput(output.toString(), claspReturnCode > 10,
					input.getAtomManager());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void inputPrograms(OutputStream input) throws IOException {
		pipeResource(input, UNIFICATION_PROGRAM);
		if (disequations) {
			pipeResource(input, DISUNIFICATION_PROGRAM);
		}
		if (types) {
			pipeResource(input, TYPES_PROGRAM);
		}
		pipeResource(input, FINAL_PROGRAM);
		if (minimize) {
			pipeResource(input, HEURISTIC_PROGRAM);
		}
	}

	private void pipeResource(OutputStream gringoInput, String resourceName)
			throws IOException {
		InputStream unificationProgram = AspProcessor.class
				.getResourceAsStream(resourceName);
		pipe(unificationProgram, gringoInput);
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
			if (line != null) {
				// System.out.println(line);
				output.write(line);
				output.newLine();
			}
		}
		output.flush();
	}

}
