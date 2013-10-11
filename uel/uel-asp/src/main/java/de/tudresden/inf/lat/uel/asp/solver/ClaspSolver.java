package de.tudresden.inf.lat.uel.asp.solver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

	private static String GRINGO_COMMAND = "gringo";
	// TODO: fix path
	private static String UNIFICATION_PROGRAM = "../uel-asp/src/main/resources/unification.lp";
	private static String CLASP_COMMAND = "clasp 0 -t 4 --outf=2";

	public ClaspSolver() {
	}

	@Override
	public AspOutput solve(AspInput input) throws IOException {
		try {
			// call gringo and clasp
			ProcessBuilder pbGringo = new ProcessBuilder(GRINGO_COMMAND);
			Process pGringo = pbGringo.start();

			ProcessBuilder pbClasp = new ProcessBuilder(
					Arrays.asList(CLASP_COMMAND.split(" ")));
			Process pClasp = pbClasp.start();

			// pipe unification.lp and input.getProgram() as input
			OutputStream gringoInput = pGringo.getOutputStream();
			File unificationProgram = new File(UNIFICATION_PROGRAM);
			pipe(new FileInputStream(unificationProgram), gringoInput);
			pipe(new ByteArrayInputStream(input.getProgram().getBytes()),
					gringoInput);
			gringoInput.close();
			pGringo.waitFor();

			// pipe the output to clasp
			pipe(pGringo.getInputStream(), pClasp.getOutputStream());
			pClasp.getOutputStream().close();

			// ByteArrayOutputStream error = new ByteArrayOutputStream();
			// pipe(pGringo.getErrorStream(), error);
			// System.out.println(error.toString());

			pGringo.destroy();
			pClasp.waitFor();

			// return the json output
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			pipe(pClasp.getInputStream(), output);

			// error = new ByteArrayOutputStream();
			// pipe(pClasp.getErrorStream(), error);
			// System.out.println(error.toString());

			pClasp.destroy();
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
			if (line != null) {
				output.write(line);
				output.newLine();
			}
		}
		output.flush();
	}

}
