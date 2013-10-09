package de.tudresden.inf.lat.uel.plugin.processor;

/**
 * @author Stefan Borgwardt
 * 
 */
public class ClaspSolver implements AspSolver {

	private static String GRINGO_COMMAND_LINE = "gringo";
	private static String UNIFICATION_PROGRAM = "unification.lp";
	private static String CLASP_COMMAND_LINE = "clasp 0 -t 4 --outf=2";

	public ClaspSolver() {
	}

	@Override
	public AspOutput solve(AspInput input) {
		// TODO: call gringo and clasp
		// call gringo with unification.lp and input.getProgram() as input files
		// pipe the output to clasp
		// return the json output
		return null;
	}

}
