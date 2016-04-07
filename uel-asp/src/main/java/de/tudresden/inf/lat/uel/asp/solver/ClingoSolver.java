package de.tudresden.inf.lat.uel.asp.solver;

import java.io.BufferedReader;
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
	private static String HEURISTIC_ARGUMENTS = "--enum-mode=domRec --dom-mod=5,16 --heu=Domain";

	private boolean hasNegativePart;
	private boolean types;
	private boolean minimize;
	private int maxSolutions = 1;
	private String program;
	private File outputFile;

	public ClingoSolver(boolean hasNegativePart, boolean types, boolean minimize) {
		this.hasNegativePart = hasNegativePart;
		this.types = types;
		this.minimize = minimize;
		try {
			this.outputFile = File.createTempFile("uel-asp-clingo", ".tmp");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void cleanup() {
		// TODO: implement asynchronous execution of clingo and stop the solver
		// here if it is currently running
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
		return arguments;
	}

	@Override
	public AspOutput solve(AspInput input) throws IOException {
		StringBuilder programBuilder = new StringBuilder();
		appendResource(UNIFICATION_PROGRAM, programBuilder);
		if (hasNegativePart) {
			appendResource(DISUNIFICATION_PROGRAM, programBuilder);
		}
		if (types) {
			appendResource(TYPES_PROGRAM, programBuilder);
		}
		appendResource(FINAL_PROGRAM, programBuilder);
		programBuilder.append(input.getProgram());
		this.program = programBuilder.toString();
		return new ClingoOutput(this, input.getAtomManager());
	}

	public boolean computeMoreSolutions() {
		Process pClingo = null;

		try {
			// call clingo
			ProcessBuilder pbClingo = new ProcessBuilder(getClingoArguments());
			pClingo = pbClingo.start();

			// pipe .lp files and input.getProgram() as input
			OutputStream clingoInput = pClingo.getOutputStream();
			syncPipe(new ByteArrayInputStream(program.getBytes()), clingoInput);
			clingoInput.close();

			// start writing the json output
			OutputStream output = new FileOutputStream(outputFile);
			AsyncPipe pipeOut = new AsyncPipe(pClingo.getInputStream(), output);
			pipeOut.start();

			// TODO: implement asynchronous execution of clingo and return
			// solution on demand?
			int clingoReturnCode = pClingo.waitFor();
			pipeOut.join();
			if (pipeOut.exception != null) {
				throw pipeOut.exception;
			}
			output.close();

			// successful if there was no exception (lsb=0) and either a model
			// was found (10) or the search space was exhausted (20)
			if (((clingoReturnCode & 1) == 1) || (((clingoReturnCode & 10) == 0) && ((clingoReturnCode & 20) == 0))) {
				ByteArrayOutputStream error = new ByteArrayOutputStream();
				syncPipe(pClingo.getErrorStream(), error);
				throw new IOException("clingo error (return code " + clingoReturnCode + "):\n" + error.toString());
			}
			pClingo.destroy();

			return clingoReturnCode > 10;
		} catch (InterruptedException | IOException ex) {
			if (pClingo != null) {
				pClingo.destroy();
			}
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
				return true;
			} else {
				throw new RuntimeException(ex);
			}
		}
	}

	private void syncPipe(InputStream input, OutputStream output) throws InterruptedException, IOException {
		AsyncPipe pipe = new AsyncPipe(input, output);
		pipe.start();
		pipe.join();
		if (pipe.exception != null) {
			throw pipe.exception;
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

	private void appendResource(String resourceName, StringBuilder output) throws IOException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(AspUnificationAlgorithm.class.getResourceAsStream(resourceName)));
		String line = null;
		while ((line = reader.readLine()) != null) {
			output.append(line);
			output.append(System.lineSeparator());
		}
		reader.close();
	}

}
