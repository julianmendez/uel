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
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;

/**
 * @author Stefan Borgwardt
 * 
 */
public class ClaspSolver implements AspSolver {

	private static String GRINGO_COMMAND = "gringo";
	// TODO: fix path
	private static String UNIFICATION_PROGRAM = "resources/unification.lp";
	private static String CLASP_COMMAND = "clasp 0 --outf=2";

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
			InputStream unificationProgram = AspProcessor.class
					.getResourceAsStream(UNIFICATION_PROGRAM);
			pipe(unificationProgram, gringoInput);
			pipe(new ByteArrayInputStream(input.getProgram().getBytes()),
					gringoInput);
			System.out.println(input.getProgram());
			gringoInput.close();

			// pipe the output to clasp
			pipe(pGringo.getInputStream(), pClasp.getOutputStream());
			pClasp.getOutputStream().close();

			if (pGringo.waitFor() != 0) {
				ByteArrayOutputStream error = new ByteArrayOutputStream();
				pipe(pGringo.getErrorStream(), error);
				throw new IOException("gringo error:\n" + error.toString());
			}
			pGringo.destroy();

			// return the json output
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			pipe(pClasp.getInputStream(), output);

			int claspReturnCode = pClasp.waitFor();
			if ((claspReturnCode) != 20 && (claspReturnCode != 30)) {
				ByteArrayOutputStream error = new ByteArrayOutputStream();
				pipe(pClasp.getErrorStream(), error);
				throw new IOException("clasp error:\n" + error.toString());
			}
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
			// System.out.println(line);
			if (line != null) {
				output.write(line);
				output.newLine();
			}
		}
		output.flush();
	}

}
