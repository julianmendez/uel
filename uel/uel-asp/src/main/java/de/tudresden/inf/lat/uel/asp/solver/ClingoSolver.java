package de.tudresden.inf.lat.uel.asp.solver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Stefan Borgwardt
 * 
 */
public class ClingoSolver implements AspSolver {

	private static String UNIFICATION_PROGRAM = "/unification.lp";
	private static String DISUNIFICATION_PROGRAM = "/disunification.lp";
	private static String TYPES_PROGRAM = "/types.lp";
	private static String FINAL_PROGRAM = "/final.lp";
	private static String CLINGO_COMMAND = "clingo";
	// TODO: multi-threading?
	private static String COMMON_ARGUMENTS = "--project --outf=2"; // --enum-mode=domRec";
	private static String HEURISTIC_ARGUMENTS = "--dom-mod=5,16 --heu=Domain";

	private boolean disequations;
	private boolean types;
	private boolean minimize;
	private int maxSolutions = 1;
	private String program;
	private File outputFile;

	public ClingoSolver(boolean disequations, boolean types, boolean minimize) {
		this.disequations = disequations;
		this.types = types;
		this.minimize = minimize;
		try {
			this.outputFile = File.createTempFile("uel-asp-clingo", ".tmp");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		// System.out.println(minimize);
	}

	private List<String> getClingoArguments() {
		List<String> arguments = new ArrayList<String>();
		arguments.add(CLINGO_COMMAND);
		arguments.add(Integer.toString(maxSolutions));
		maxSolutions *= 2;
		arguments.addAll(Arrays.asList(COMMON_ARGUMENTS.split(" ")));
		if (minimize) {
			arguments.addAll(Arrays.asList(HEURISTIC_ARGUMENTS.split(" ")));
		}
		// System.out.println(arguments.toString());
		return arguments;
	}

	@Override
	public AspOutput solve(AspInput input) throws IOException {
		StringBuilder programBuilder = new StringBuilder();
		appendResource(UNIFICATION_PROGRAM, programBuilder);
		if (disequations) {
			appendResource(DISUNIFICATION_PROGRAM, programBuilder);
		}
		if (types) {
			appendResource(TYPES_PROGRAM, programBuilder);
		}
		appendResource(FINAL_PROGRAM, programBuilder);
		programBuilder.append(input.getProgram());
		this.program = programBuilder.toString();
		// System.out.println(program);
		return new ClingoOutput(this, input.getAtomManager());
	}

	public boolean computeMoreSolutions() {
		try {
			// call clingo
			ProcessBuilder pbClingo = new ProcessBuilder(getClingoArguments());
			Process pClingo = pbClingo.start();

			// pipe .lp files and input.getProgram() as input
			OutputStream clingoInput = pClingo.getOutputStream();
			pipe(new ByteArrayInputStream(program.getBytes()), clingoInput);
			clingoInput.close();

			// write the json output
			OutputStream output = new FileOutputStream(outputFile);
			pipe(pClingo.getInputStream(), output);
			output.close();

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

			return clingoReturnCode > 10;
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public InputStream getCurrentSolutions() {
		try {
			InputStream stream = new FileInputStream(outputFile);
			return stream;
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void appendResource(String resourceName, StringBuilder output)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				AspProcessor.class.getResourceAsStream(resourceName)));
		String line = null;
		while ((line = reader.readLine()) != null) {
			output.append(line);
			output.append(System.lineSeparator());
		}
	}

	private void pipe(InputStream input, OutputStream output)
			throws IOException {
		pipe(new BufferedReader(new InputStreamReader(input)),
				new BufferedWriter(new OutputStreamWriter(output)));
	}

	private void pipe(BufferedReader input, BufferedWriter output)
			throws IOException {
		String line;
		while ((line = input.readLine()) != null) {
			// System.out.println(line);
			output.write(line);
			output.newLine();
		}
		output.flush();
	}

}
