package de.tudresden.inf.lat.uel.plugin.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

/**
 * 
 * @author Julian Mendez
 */
public class XMLFlattener {

	private static final String dotSepStr = ".";
	private static final String extraStr = "0";
	private static final String newLineSepStr = "\n";
	private static final String spaceSepStr = " ";
	private static final String tabSepStr = "\t";
	private static final String textStr = "#text";

	public static final void main(String[] args) throws SAXException,
			IOException {
		if (args.length == 2) {
			(new XMLFlattener()).run(new File(args[0]), new File(args[1]));
		}
	}

	private Set<String> setOfFields = new TreeSet<String>();

	public XMLFlattener() {
	}

	private String getNodeName(String prefix, Node node,
			Map<String, String> nodeInfo) {
		String ret = node.getNodeName();
		if (ret.equals(textStr)) {
			ret = node.getParentNode().getNodeName();
		}
		ret = prefix + dotSepStr + ret;
		String infoKey = ret;
		String infoValue = nodeInfo.get(infoKey);
		while (infoValue != null) {
			infoKey += extraStr;
			infoValue = nodeInfo.get(infoKey);
		}
		ret = infoKey;
		return ret;
	}

	private void preProcessDocument(Document doc) {
		NodeList mainNodeList = doc.getChildNodes();
		if (mainNodeList.getLength() > 0) {
			Node mainNode = mainNodeList.item(0);
			NodeList nodeList = mainNode.getChildNodes();

			for (int i = 0; i < nodeList.getLength(); i++) {
				Map<String, String> nodeInfo = new TreeMap<String, String>();
				processNode("", nodeList.item(i), nodeInfo);
			}
		}
	}

	private void processDocument(Document doc, Writer writer)
			throws IOException {
		NodeList mainNodeList = doc.getChildNodes();
		if (mainNodeList.getLength() > 0) {
			Node mainNode = mainNodeList.item(0);
			NodeList nodeList = mainNode.getChildNodes();

			for (int i = 0; i < nodeList.getLength(); i++) {
				Map<String, String> nodeInfo = new TreeMap<String, String>();
				processNode("", nodeList.item(i), nodeInfo);
				renderRecord(nodeInfo, writer);
			}
		}
	}

	private void processNode(String prefix, Node node,
			Map<String, String> nodeInfo) {

		String name = getNodeName(prefix, node, nodeInfo);
		this.setOfFields.add(name);
		String value = node.getNodeValue();

		if (value != null) {
			nodeInfo.put(name, value.trim());
		}

		if (node.hasAttributes()) {
			NamedNodeMap nodes = node.getAttributes();
			for (int i = 0; i < nodes.getLength(); i++) {
				processNode(name, nodes.item(i), nodeInfo);
			}
		}

		if (node.hasChildNodes()) {
			NodeList nodes = node.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				processNode(name, nodes.item(i), nodeInfo);
			}
		}
	}

	private void renderHeader(Writer writer) throws IOException {
		for (String key : this.setOfFields) {
			writer.write(key.replace(tabSepStr, spaceSepStr) + tabSepStr);
		}
		writer.write(newLineSepStr);
	}

	private void renderRecord(Map<String, String> nodeInfo, Writer writer)
			throws IOException {
		for (String key : this.setOfFields) {
			String value = nodeInfo.get(key);
			if (value == null) {
				value = "";
			}
			writer.write(value.replace(tabSepStr, spaceSepStr) + tabSepStr);
		}
		writer.write(newLineSepStr);

	}

	public void run(File input, File output) throws SAXException, IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		DOMParser parser = new DOMParser();
		parser.parse(input.getCanonicalPath());
		preProcessDocument(parser.getDocument());
		renderHeader(writer);
		processDocument(parser.getDocument(), writer);
		writer.flush();
	}

}
