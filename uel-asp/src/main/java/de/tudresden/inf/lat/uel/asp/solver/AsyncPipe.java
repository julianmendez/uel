/**
 * 
 */
package de.tudresden.inf.lat.uel.asp.solver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author stefborg
 *
 */
public class AsyncPipe extends Thread {
	private BufferedReader reader;
	private BufferedWriter writer;
	public IOException exception = null;

	public AsyncPipe(InputStream input, OutputStream output) {
		reader = new BufferedReader(new InputStreamReader(input));
		writer = new BufferedWriter(new OutputStreamWriter(output));
	}

	@Override
	public void run() {
		String line;

		try {
			while (!Thread.currentThread().isInterrupted() && ((line = reader.readLine()) != null)) {
				// System.out.println(line);
				writer.write(line);
				writer.newLine();
			}
			writer.flush();
		} catch (IOException ex) {
			this.exception = ex;
		}
	}
}
