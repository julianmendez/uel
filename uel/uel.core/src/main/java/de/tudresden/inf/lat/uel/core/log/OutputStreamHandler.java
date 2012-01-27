package de.tudresden.inf.lat.uel.core.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * This class is a <code>Handler</code> that logs some benchmarking data, such
 * as used memory and running time. (Copied from jcel, http://jcel.sf.net/)
 * 
 * @author Julian Mendez
 */
public class OutputStreamHandler extends Handler {

	private BufferedWriter output = null;
	private Long start = null;

	/**
	 * Constructs a new output stream handler.
	 * 
	 * @param output
	 *            output stream
	 */
	public OutputStreamHandler(OutputStream output) {
		if (output == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.output = new BufferedWriter(new OutputStreamWriter(output));
		this.start = new Date().getTime();
	}

	@Override
	public void close() throws SecurityException {
		try {
			this.output.close();
		} catch (IOException e) {
			throw new SecurityException(e);
		}

	}

	/**
	 * Creates a new message using the original one, and adding memory usage and
	 * running time.
	 * 
	 * @param originalMessage
	 *            original message
	 * @return the new message
	 */
	public String createMessage(String originalMessage) {
		if (originalMessage == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		if (start == null) {
			start = new Date().getTime();
		}
		long difference = (new Date()).getTime() - this.start;
		long totalMemory = Runtime.getRuntime().totalMemory() / 1048576;
		long freeMemory = Runtime.getRuntime().freeMemory() / 1048576;
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("(");
		sbuf.append("" + difference);
		sbuf.append(" ms) ([X]:");
		sbuf.append("" + (totalMemory - freeMemory));
		sbuf.append(" MB, [ ]:");
		sbuf.append("" + freeMemory);
		sbuf.append(" MB) ");
		sbuf.append(originalMessage);
		return sbuf.toString();
	}

	@Override
	public void flush() {
		try {
			this.output.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void publish(LogRecord record) {
		if (record == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		try {
			this.output.write(createMessage(record.getMessage()));
			this.output.newLine();
			this.output.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
