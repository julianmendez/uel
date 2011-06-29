package de.tudresden.inf.lat.uel.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * An object of this class uses the MiniSat solver to solve a SAT problem.
 */
public class MiniSatSolver implements Solver {

	public static final String minisatCommand = "MiniSat";
	private static final String tempPrefix = "uelMiniSat";
	private static final String tempSuffix = ".tmp";

	private void runMiniSat(File satinput, File satoutput) throws IOException {
		try {
			ProcessBuilder pb = new ProcessBuilder(minisatCommand,
					satinput.toString(), satoutput.toString());
			Process p = pb.start();
			p.waitFor();
			p.destroy();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public String solve(String input) throws IOException {
		String ret = null;
		File satinput = File.createTempFile(tempPrefix, tempSuffix);
		File satoutput = File.createTempFile(tempPrefix, tempSuffix);

		PrintWriter satinputWriter = new PrintWriter(new FileWriter(satinput));
		satinputWriter.println(input);
		satinputWriter.flush();
		satinputWriter.close();

		runMiniSat(satinput, satoutput);

		BufferedReader satoutputReader = new BufferedReader(new FileReader(
				satoutput));
		StringBuffer sbuf = new StringBuffer();
		String line = "";
		while (line != null) {
			line = satoutputReader.readLine();
			if (line != null) {
				sbuf.append(line);
				sbuf.append("\n");
			}
		}
		ret = sbuf.toString();

		satinput.delete();
		satoutput.delete();

		return ret;
	}

}
