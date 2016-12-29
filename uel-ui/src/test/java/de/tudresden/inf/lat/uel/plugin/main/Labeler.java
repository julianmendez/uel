package de.tudresden.inf.lat.uel.plugin.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * An object implementing this class adds labels to entities in an OWL ontology.
 * 
 * @author Julian Mendez
 */
public class Labeler {

	private static final String classEnd = "\n    </owl:Class>";
	private static final String classRdfAbout = "<owl:Class rdf:about=\"#";
	private static final String objectPropertyEnd = "\n    </owl:ObjectProperty>";
	private static final String objectPropertyRdfAbout = "<owl:ObjectProperty rdf:about=\"#";
	private static final String quotes = "\">";
	private static final String quotesAndBar = "\"/>";
	private static final String rdfsLabelLeft = "\n        <rdfs:label>";
	private static final String rdfsLabelRight = "</rdfs:label>";
	private static final String symbol01key = "&";
	private static final String symbol01val = "&amp;";
	private static final String symbol02key = "<";
	private static final String symbol02val = "&lt;";
	private static final String symbol03key = ">";
	private static final String symbol03val = "&gt;";
	private static final String symbol04key = "\"";
	private static final String symbol04val = "&quot;";
	private static final String symbol05key = "'";
	private static final String symbol05val = "&apos;";

	public static void main(String[] args) throws IOException {
		if (args.length == 3) {
			(new Labeler()).run(new File(args[0]), new File(args[1]), new File(args[2]));
		} else {
			System.out.println("Parameters: <input owl file>  <name map file> <output owl file>");
		}
	}

	public Labeler() {
	}

	private String processTag(String line, Map<String, String> map, String header, String footer) {
		String ret = line;
		int leftMarker = line.indexOf(header);
		if (leftMarker != -1) {
			leftMarker += header.length();

			int rightMarker = line.indexOf(quotesAndBar, leftMarker);
			if (rightMarker != -1) {
				line = line.substring(0, rightMarker) + quotes + footer;
			}

			rightMarker = line.indexOf(quotes, leftMarker);
			if (rightMarker != -1) {
				String key = line.substring(leftMarker, rightMarker);
				String value = map.get(key);
				if (value != null) {
					int endMarker = rightMarker + quotes.length();
					ret = line.substring(0, endMarker) + rdfsLabelLeft + value + rdfsLabelRight
							+ line.substring(endMarker);
				}
			}
		}
		return ret;
	}

	private Map<String, String> readMap(File mapFile) throws IOException {
		Map<String, String> ret = new TreeMap<>();
		BufferedReader reader = new BufferedReader(new FileReader(mapFile));
		reader.lines().forEach(line -> {
			StringTokenizer stok = new StringTokenizer(line, "\t");
			if (stok.hasMoreTokens()) {
				String key = stok.nextToken();
				if (stok.hasMoreTokens()) {
					String value = stok.nextToken();
					ret.put(toXMLEncoding(key), toXMLEncoding(value));
				}
			}
		});
		reader.close();
		return ret;
	}

	public void run(File inputFile, File mapFile, File outputFile) throws IOException {
		Map<String, String> nameMap = readMap(mapFile);
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			line = processTag(line, nameMap, classRdfAbout, classEnd);
			line = processTag(line, nameMap, objectPropertyRdfAbout, objectPropertyEnd);
			writer.write(line + "\n");
		}
		writer.flush();
		reader.close();
		writer.close();
	}

	private String toXMLEncoding(String input) {
		String ret = input;
		ret = ret.replaceAll(symbol01key, symbol01val);
		ret = ret.replaceAll(symbol02key, symbol02val);
		ret = ret.replaceAll(symbol03key, symbol03val);
		ret = ret.replaceAll(symbol04key, symbol04val);
		ret = ret.replaceAll(symbol05key, symbol05val);
		return ret;
	}

}
