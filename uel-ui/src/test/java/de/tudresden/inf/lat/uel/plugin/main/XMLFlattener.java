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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * An object of this class flattens an XML file returning a tab-separated file.
 * This recognizes a "main node", and creates a row for each sub node of this
 * main node.
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

	public static final void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		if (args.length == 2) {
			(new XMLFlattener()).run(new File(args[0]), new File(args[1]));
		} else {
			System.out.println(
					"two parameters are required:  \n(1) input file name, RDF/OWL file  \n(2) output file name, a tab-separated file");
		}
	}

	/**
	 * Constructs a new XML flattener.
	 */
	public XMLFlattener() {
	}

	private Node getMainNode(Document doc) {
		NodeList mainNodeList = doc.getChildNodes();
		int n = mainNodeList.getLength();
		Node ret = null;
		if (n > 0) {
			Node mainNode = mainNodeList.item(0);
			for (int k = 0; mainNode.getNodeType() != Node.ELEMENT_NODE && k < n; k++) {
				mainNode = mainNodeList.item(k);
			}
			if (mainNode.getNodeType() == Node.ELEMENT_NODE) {
				ret = mainNode;
			}
		}
		return ret;
	}

	private String getNodeName(String prefix, Node node, Map<String, String> nodeInfo) {
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

	private Set<String> preProcessDocument(Document doc) {
		Set<String> ret = new TreeSet<>();
		Node mainNode = getMainNode(doc);
		if (mainNode != null) {
			NodeList nodeList = mainNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Map<String, String> nodeInfo = new TreeMap<>();
				processNode("", nodeList.item(i), nodeInfo);
				ret.addAll(nodeInfo.keySet());
			}
		}
		return ret;
	}

	private void processDocument(Set<String> keySet, Document doc, Writer writer) throws IOException {
		Node mainNode = getMainNode(doc);
		if (mainNode != null) {
			NodeList nodeList = mainNode.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Map<String, String> nodeInfo = new TreeMap<>();
				processNode("", nodeList.item(i), nodeInfo);
				renderRecord(keySet, nodeInfo, writer);
			}
		}
	}

	private void processNode(String prefix, Node node, Map<String, String> nodeInfo) {

		String name = getNodeName(prefix, node, nodeInfo);
		String value = node.getNodeValue();

		if (value != null) {
			if (!value.trim().isEmpty()) {
				nodeInfo.put(name, value);
			}
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

	private void renderHeader(Set<String> keySet, Writer writer) throws IOException {
		for (String key : keySet) {
			writer.write(key.replace(tabSepStr, spaceSepStr) + tabSepStr);
		}
		writer.write(newLineSepStr);
	}

	private void renderRecord(Set<String> keySet, Map<String, String> nodeInfo, Writer writer) throws IOException {
		StringBuffer sbuf = new StringBuffer();
		for (String key : keySet) {
			String value = nodeInfo.get(key);
			if (value == null) {
				value = "";
			}

			sbuf.append(value.replace(tabSepStr, spaceSepStr));
			sbuf.append(tabSepStr);
		}
		String line = sbuf.toString();
		if (!line.trim().isEmpty()) {
			writer.write(line);
			writer.write(newLineSepStr);
		}
	}

	public void run(File input, File output) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document document = docBuilder.parse(input);
		document.getDocumentElement().normalize();

		Set<String> keySet = preProcessDocument(document);
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		renderHeader(keySet, writer);
		processDocument(keySet, document, writer);
		writer.flush();
	}

}
