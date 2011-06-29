package de.tudresden.inf.lat.uel.main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import de.tudresden.inf.lat.uel.sattranslator.Translator;

/**
 * An object of this class uses the MiniSat solver to solve a SAT problem.
 */
public class MiniSatSolver implements Solver {

	public void runMiniSat(File satinput, File satoutput) throws IOException {
		try {
			ProcessBuilder pb = new ProcessBuilder("MiniSat",
					satinput.toString(), satoutput.toString());
			Process p = pb.start();
			p.waitFor();
			p.destroy();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean unify(Translator translator, File satinput, File satoutput,
			Writer result) throws IOException {
		translator.toDIMACS(new FileWriter(satinput));
		runMiniSat(satinput, satoutput);
		boolean response = translator.toTBox(new FileReader(satoutput), result);
		return response;
	}

}
