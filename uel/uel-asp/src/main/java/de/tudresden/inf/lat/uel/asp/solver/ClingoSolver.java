package de.tudresden.inf.lat.uel.asp.solver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
public class ClingoSolver implements AspSolver {

	private static String UNIFICATION_PROGRAM = "/unification.lp";
	private static String DISUNIFICATION_PROGRAM = "/disunification.lp";
	private static String TYPES_PROGRAM = "/types.lp";
	private static String FINAL_PROGRAM = "/final.lp";
	private static String CLINGO_COMMAND = "clingo 0 --project --outf=2";
	private static String HCLINGO_COMMAND = "clingo 0 --project --dom-pref=32 --dom-mod=6 --heu=Domain --outf=2";

	private boolean disequations;
	private boolean types;
	private boolean minimize;

	public ClingoSolver(boolean disequations, boolean types, boolean minimize) {
		this.disequations = disequations;
		this.types = types;
		this.minimize = minimize;
		// System.out.println(minimize);
	}

	@Override
	public AspOutput solve(AspInput input) throws IOException {
		try {
			// call clingo
			String clingoCommand = minimize ? HCLINGO_COMMAND : CLINGO_COMMAND;
			ProcessBuilder pbClingo = new ProcessBuilder(
					Arrays.asList(clingoCommand.split(" ")));
			Process pClingo = pbClingo.start();

			// pipe .lp files and input.getProgram() as input
			OutputStream clingoInput = pClingo.getOutputStream();
			// System.out.println(clingoInput);
			inputPrograms(clingoInput);
			pipe(new ByteArrayInputStream(input.getProgram().getBytes()),
					clingoInput);
			// System.out.println(input.getProgram());
			clingoInput.close();

			// return the json output
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			pipe(pClingo.getInputStream(), output);

			int clingoReturnCode = pClingo.waitFor();
			// successful if there was no exception (lsb=0) and either a model
			// was found (10) or the search space was exhausted (20)
			if (((clingoReturnCode & 1) == 1)
					|| (((clingoReturnCode & 10) == 0) && ((clingoReturnCode & 20) == 0))) {
				ByteArrayOutputStream error = new ByteArrayOutputStream();
				pipe(pClingo.getErrorStream(), error);
				throw new IOException("clingo error (return code "
						+ clingoReturnCode + "):\n" + error.toString());
			}
			pClingo.destroy();

			// System.out.println(output.toString());
			return new ClingoOutput(output.toString(), clingoReturnCode > 10,
					input.getAtomManager());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void inputPrograms(OutputStream input) throws IOException {
		pipeResource(UNIFICATION_PROGRAM, input);
		if (disequations) {
			pipeResource(DISUNIFICATION_PROGRAM, input);
		}
		if (types) {
			pipeResource(TYPES_PROGRAM, input);
		}
		pipeResource(FINAL_PROGRAM, input);
	}

	private void pipeResource(String resourceName, OutputStream output)
			throws IOException {
		InputStream unificationProgram = AspProcessor.class
				.getResourceAsStream(resourceName);
		pipe(unificationProgram, output);
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
