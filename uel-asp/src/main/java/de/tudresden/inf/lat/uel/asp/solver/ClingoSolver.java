package de.tudresden.inf.lat.uel.asp.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tudresden.inf.lat.uel.type.impl.AbstractUnificationAlgorithm;

/**
 * @author Stefan Borgwardt
 * 
 */
public class ClingoSolver implements AspSolver {

	private static String UNIFICATION_PROGRAM = "/unification.lp";
	private static String DISUNIFICATION_PROGRAM = "/disunification.lp";
	private static String TYPES_PROGRAM = "/compatibility.lp";
	private static String FINAL_PROGRAM = "/final.lp";
	private static String CLINGO_COMMAND = "clingo";
	private static String COMMON_ARGUMENTS = "0 --project --outf=2"; // --enum-mode=domRec";
	private static String HEURISTIC_ARGUMENTS = "--enum-mode=domRec --dom-mod=5,16 --heu=Domain";

	private final boolean hasNegativePart;
	private final boolean types;
	private final boolean minimize;
	private Process pClingo = null;
	AbstractUnificationAlgorithm parent;

	public ClingoSolver(boolean hasNegativePart, boolean types, boolean minimize, AbstractUnificationAlgorithm parent) {
		this.hasNegativePart = hasNegativePart;
		this.types = types;
		this.minimize = minimize;
		this.parent = parent;
	}

	@Override
	public void cleanup() {
		if (pClingo != null) {
			if (pClingo.isAlive()) {
				// abort clingo computation
				pClingo.destroyForcibly();
			} else {
				// normal clingo exit
				handleClingoExitCode();
			}
		}
	}

	private void handleClingoExitCode() {
		int clingoReturnCode = pClingo.exitValue();
		// successful if there was no exception (lsb=0) and either a
		// model was found (10) or the search space was exhausted (20)
		if (((clingoReturnCode & 1) == 1) || (((clingoReturnCode & 10) == 0) && ((clingoReturnCode & 20) == 0))) {
			System.err.println("clingo error (return code " + clingoReturnCode + "):" + System.lineSeparator()
					+ new OutputStreamBuilder().append(pClingo.getErrorStream()).toString());
		}
	}

	private List<String> getClingoArguments() {
		List<String> arguments = new ArrayList<>();
		arguments.add(CLINGO_COMMAND);
		arguments.addAll(Arrays.asList(COMMON_ARGUMENTS.split(" ")));
		if (minimize) {
			arguments.addAll(Arrays.asList(HEURISTIC_ARGUMENTS.split(" ")));
		}
		return arguments;
	}

	@Override
	public AspOutput solve(AspInput input) throws IOException {
		// call clingo
		ProcessBuilder pbClingo = new ProcessBuilder(getClingoArguments());
		pClingo = pbClingo.start();

		// pipe .lp files and input.getProgram() as input
		OutputStreamBuilder clingoInput = new OutputStreamBuilder(pClingo.getOutputStream());
		clingoInput.appendResource(UNIFICATION_PROGRAM);
		if (hasNegativePart) {
			clingoInput.appendResource(DISUNIFICATION_PROGRAM);
		}
		if (types) {
			clingoInput.appendResource(TYPES_PROGRAM);
		}
		clingoInput.appendResource(FINAL_PROGRAM);
		input.appendProgram(clingoInput);
		clingoInput.close();

		// start parsing the output stream
		return new ClingoOutput(pClingo.getInputStream(), input.getAtomManager(), this);
	}

}
