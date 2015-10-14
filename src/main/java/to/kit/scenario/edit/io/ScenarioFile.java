package to.kit.scenario.edit.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ScenarioFile {

	private String indent(final int depth, final String values) {
		StringBuilder buff = new StringBuilder();
		String indent = StringUtils.repeat("\t", depth);

		try (BufferedReader reader = new BufferedReader(new StringReader(values))) {
			String line;

			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (StringUtils.isBlank(line)) {
					continue;
				}
				buff.append(indent);
				buff.append(line);
				buff.append('\n');
			}
		} catch (IOException e) {
			// nop
		}
		return buff.toString();
	}

	private String makeFunction(final int depth, final String name, final String ... args) {
		String func = String.format("%s(%s);", name, StringUtils.join(args, ", "));

		return indent(depth, func);
	}

	private String analyzeIfElseStatement(final int depth, final Element element) {
		String result;
		String name = element.getNodeName();
		NamedNodeMap nodeMap = element.getAttributes();
		Node expNode = nodeMap.getNamedItem("true");

		if ("if".equals(name)) {
			StringBuilder buff = new StringBuilder();
			buff.append("if (");
			buff.append(expNode.getNodeValue());
			buff.append(") {");
			result = indent(depth, buff.toString());
		} else {
			StringBuilder buff = new StringBuilder();
			buff.append("} else ");
			if (expNode != null) {
				buff.append("if (");
				buff.append(expNode.getNodeValue());
				buff.append(") ");
			}
			buff.append("{");
			result = indent(depth - 1, buff.toString());
		}
		return result;
	}

	private String analyzeChild(final int depth, Node parentNode) {
		StringBuilder buff = new StringBuilder();
		NodeList childList = parentNode.getChildNodes();

		for (int ix = 0; ix < childList.getLength(); ix++) {
			Node node = childList.item(ix);
			int type = node.getNodeType();

			if (type == Node.TEXT_NODE) {
				String str = node.getTextContent();

				buff.append(indent(depth, str));
				continue;
			}
			Element element = (Element) node;
			String name = node.getNodeName();
			boolean isIf = "if".equals(name);

			if (isIf) {
				buff.append(analyzeIfElseStatement(depth, element));
			} else if ("else".equals(name)) {
				buff.append(analyzeIfElseStatement(depth, element));
			} else if ("nop".equals(name)) {
				continue;
			} else if ("call".equals(name)) {
				String id = DOMUtil.getAttrValue(element, "id");

				buff.append(makeFunction(depth, id));
			} else if ("jump".equals(name)) {
				String mapId = DOMUtil.getAttrValue(element, "map");
				String x = DOMUtil.getAttrValue(element, "x");
				String y = DOMUtil.getAttrValue(element, "y");

				buff.append(makeFunction(depth, "jump", mapId, x, y));
			} else if ("set".equals(name)) {
				String to = DOMUtil.getAttrValue(element, "to");
				String val = DOMUtil.getAttrValue(element, "val");
				String stmt = String.format("%s = %s;", to, val);

				buff.append(indent(depth, stmt));
			} else {
				buff.append('\n');
				buff.append('【');
				buff.append(name);
				buff.append('】');
				buff.append(indent(depth, node.toString()));
			}
			String child = analyzeChild(depth + 1, node);
			buff.append(child);
			if (isIf) {
				buff.append(indent(depth, "}"));
			}
		}
		return buff.toString();
	}

	private void loadFunctions(Document doc) {
		NodeList functionList = doc.getElementsByTagName("function");

		for (int ix = 0; ix < functionList.getLength(); ix++) {
			Node node = functionList.item(ix);
			NamedNodeMap nodeMap = node.getAttributes();
			String id = nodeMap.getNamedItem("id").getNodeValue();
			String title = nodeMap.getNamedItem("title").getNodeValue();
			String func = String.format("▼%02x:[%s]%s", Integer.valueOf(ix + 1), id, title);

			System.out.println(func);
			String contents = analyzeChild(1, node);
			System.out.println(contents);
		}
	}

	public void load(File file) {
		Document doc;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.parse(file);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return;
		}
		loadFunctions(doc);
	}
}
