/**
 * 
 */
package de.tudresden.inf.lat.uel.asp.solver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Stefan Borgwardt
 *
 */
public class OutputStreamBuilder {

	private byte[] buffer = new byte[1024];
	private int length;
	private OutputStream output;

	public OutputStreamBuilder() {
		this(new ByteArrayOutputStream());
	}

	public OutputStreamBuilder(OutputStream output) {
		this.output = output;
	}

	public OutputStreamBuilder append(InputStream input) {
		// http://stackoverflow.com/a/35446009
		try {
			while ((length = input.read(buffer)) != -1) {
				// System.out.write(buffer, 0, length);
				output.write(buffer, 0, length);
			}
			input.close();
			return this;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public OutputStreamBuilder append(Integer n) {
		return append(n.toString());
	}

	public OutputStreamBuilder append(String input) {
		byte[] data = input.getBytes();
		if (data.length <= buffer.length) {
			try {
				// System.out.write(data);
				output.write(data);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		} else {
			append(new ByteArrayInputStream(input.getBytes()));
		}
		return this;
	}

	public OutputStreamBuilder appendResource(String resourceName) {
		return append(OutputStreamBuilder.class.getResourceAsStream(resourceName));
	}

	public void close() {
		try {
			output.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public OutputStreamBuilder newLine() {
		return append(System.lineSeparator());
	}

	public String toString() {
		return output.toString();
	}

}
