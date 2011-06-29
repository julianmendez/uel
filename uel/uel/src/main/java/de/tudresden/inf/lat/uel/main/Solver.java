package de.tudresden.inf.lat.uel.main;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import de.tudresden.inf.lat.uel.sattranslator.Translator;

public interface Solver {

	/**
	 * This method is the basic unification procedure. It returns true if the
	 * goal is unifiable and false otherwise.
	 * 
	 * It creates an input file <satinput> for a SAT solver, by using the method
	 * of Translator. It calls a solver with this file. The output is saved in
	 * file <satoutput>. It calls Translator again to detect if the solver
	 * returned "satisfiable" and to translate the output into a result.
	 * 
	 * @param translator
	 *            translator
	 * @param satinput
	 *            input file
	 * @param satoutput
	 *            output file
	 * @throws IOException
	 * @return true if and only if the goal is unifiable
	 */
	public boolean unify(Translator translator, File satinput, File satoutput,
			Writer result) throws IOException;

}
