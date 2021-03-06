package to.kit.scenario.edit.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.util.DOMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.arnx.jsonic.JSON;
import to.kit.scenario.edit.info.Actor;
import to.kit.scenario.edit.info.Item;
import to.kit.scenario.edit.info.Scenario;

/**
 * シナリオファイルロード.
 * @author Hidetaka Sasai
 */
public final class ScenarioFile {
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(ScenarioFile.class);

	private static final String FUNC_PREFIX = "mng.";
	private static final String VARIABLE_PATTERN = "@[$*a-zA-Z0-9]+[ ]*";
	private final Pattern variablePattern = Pattern.compile(VARIABLE_PATTERN);

	private final Scenario scenario = new Scenario();
	private final List<String> variableList = new ArrayList<>();
	private final Map<String, String> choiceMap = new HashMap<>();
	private final Map<String, Actor> actorMap = new HashMap<>();
	private final Map<String, String> placeMap = new HashMap<>();
	private final Map<Integer, String> eventNumberMap = new HashMap<>();
	private int yieldStep;

	private String span(final String styleClass, final String text) {
		return "<span class=\"" + styleClass + "\">" + text + "</span>";
	}

	private String quote(String value) {
		return "'" + value + "'";
	}

	private String makeYield() {
		this.yieldStep++;
		return "yield " + this.yieldStep + ";\n";
	}

	private String convertNames(final String str) {
		String result = str;
		Matcher matcher = this.variablePattern.matcher(str);
		Set<String> variables = new HashSet<>();
		boolean quote = str.contains("{");

		while (matcher.find()) {
			variables.add(matcher.group());
		}
		for (String var : variables) {
			String key = var.substring(1);
			String value;

			if ("Save".equals(key)) {
				value = StringUtils.chop(makeFunction(0, "save"));
			} else if ("*".equals(key)) {
				value = span("savePoint", "日記帳");
			} else if (key.startsWith("$")) {
				String gold = key.substring(1);
				value = span("currency", gold + "Gold");
			} else if (this.choiceMap.containsKey(key)) {
				value = span("item", this.choiceMap.get(key));
			} else if (this.actorMap.containsKey(key)) {
				Actor actor = this.actorMap.get(key);
				value = span("chara", actor.getName());
			} else if (this.placeMap.containsKey(key)) {
				value = span("place", this.placeMap.get(key));
			} else {
				//System.out.println("***" + var);
				value = "";
			}
			if (quote) {
				value = quote(value);
			}
			result = result.replace(var, value);
		}
		return result;
	}

	private String convertVariables(final String str) {
		String result = str;

		result = result.replace("$", "gold");
		result = result.replace("hp", "mng.hp");
		for (String key : this.choiceMap.keySet()) {
			result = result.replaceAll("\\b" + key + "\\b", "mng." + key);
		}
		return result;
	}

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
				line = convertNames(line);
				if (line.startsWith("{")) {
					line = line.replace("{", "[");
					line = line.replace("}", "]");
					buff.append(makeFunction(depth, "choose", line));
					buff.append(makeYield());
					continue;
				}
				if (":".equals(line)) {
					buff.append(makeFunction(depth, "wait"));
					buff.append(makeYield());
					continue;
				}
				if (line.startsWith(":")) {
					line = line.substring(1);
					for (String part : line.split(";")) {
						buff.append(makeFunction(depth, "print", quote(part + "\\n")));
					}
					continue;
				}
				buff.append(indent);
				buff.append(line);
				buff.append('\n');
			}
		} catch (@SuppressWarnings("unused") IOException e) {
			// nop
		}
		return buff.toString();
	}

	private String makeFunction(final int depth, final String name, final String ... args) {
		String func = String.format("%s%s(%s);", FUNC_PREFIX, name, StringUtils.join(args, ", "));

		return indent(depth, func);
	}

	private String analyzeIfElseStatement(final int depth, final Element element) {
		String result;
		String name = element.getNodeName();
		String exp = DOMUtil.getAttrValue(element, "true");

		exp = convertVariables(exp);
		if ("if".equals(name)) {
			StringBuilder buff = new StringBuilder();
			buff.append("if (");
			buff.append(exp);
			buff.append(") {");
			result = indent(depth, buff.toString());
		} else {
			StringBuilder buff = new StringBuilder();
			buff.append("} else ");
			if (StringUtils.isNotBlank(exp)) {
				buff.append("if (");
				buff.append(exp);
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

		if (depth == 1) {
			this.yieldStep = 0;
		}
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
			boolean isWhile = "while".equals(name);

			if (isIf) {
				buff.append(analyzeIfElseStatement(depth, element));
			} else if (isWhile) {
				String stmt = "while (true) {";
				buff.append(indent(depth, stmt));
			} else if ("else".equals(name)) {
				buff.append(analyzeIfElseStatement(depth, element));
			} else if ("game".equals(name)) {
				buff.append(makeFunction(depth, "gameOver"));
			} else if ("nop".equals(name)) {
				continue;
			} else if ("call".equals(name)) {
				String id = quote(DOMUtil.getAttrValue(element, "id"));

				buff.append(makeFunction(depth, "call", id));
				buff.append(makeYield());
			} else if ("jump".equals(name)) {
				String mapId = "map" + DOMUtil.getAttrValue(element, "map");
				String x = DOMUtil.getAttrValue(element, "x");
				String y = DOMUtil.getAttrValue(element, "y");

				buff.append(makeFunction(depth, "jump", quote(mapId), x, y));
			} else if ("check".equals(name)) {
				String mapId = "map" + DOMUtil.getAttrValue(element, "map");
				String x = DOMUtil.getAttrValue(element, "x");
				String y = DOMUtil.getAttrValue(element, "y");

				buff.append(makeFunction(depth, "check", quote(mapId), x, y));
			} else if ("effect".equals(name)) {
				String fade = DOMUtil.getAttrValue(element, "fade");

				buff.append(makeFunction(depth, "effect", fade));
			} else if ("set".equals(name)) {
				String to = DOMUtil.getAttrValue(element, "to");
				String val = DOMUtil.getAttrValue(element, "val");
				String str = convertVariables(to + " = " + val);

				buff.append(indent(depth, str));
			} else if ("add".equals(name)) {
				String to = DOMUtil.getAttrValue(element, "to");
				String val = DOMUtil.getAttrValue(element, "val");
				String str = convertVariables(to + " += " + val);

				buff.append(indent(depth, str));
			} else if ("sub".equals(name)) {
				String from = DOMUtil.getAttrValue(element, "from");
				String val = DOMUtil.getAttrValue(element, "val");
				String str = convertVariables(from + " -= " + val);

				buff.append(indent(depth, str));
			} else if ("img".equals(name)) {
				String src = quote(DOMUtil.getAttrValue(element, "src"));
				String alt = quote(DOMUtil.getAttrValue(element, "alt"));

				buff.append(makeFunction(depth, "img", src, alt));
			} else if ("actor".equals(name)) {
				String id = DOMUtil.getAttrValue(element, "id");
				Actor actor = this.actorMap.get(id);
				String src = actor.getSrc();
				String x = DOMUtil.getAttrValue(element, "x");
				String y = DOMUtil.getAttrValue(element, "y");
				String seq = DOMUtil.getAttrValue(element, "seq");
				String event = quote(DOMUtil.getAttrValue(element, "event"));

				buff.append(makeFunction(depth, "actor", quote(id), quote(src), x, y, seq, event));
			} else if ("enemy".equals(name)) {
				String id = DOMUtil.getAttrValue(element, "id");
				Actor actor = this.actorMap.get(id);
				String src = actor.getSrc();
				String x = DOMUtil.getAttrValue(element, "x");
				String y = DOMUtil.getAttrValue(element, "y");
				String seq = DOMUtil.getAttrValue(element, "seq");
				String level = DOMUtil.getAttrValue(element, "level");
				String event = quote(DOMUtil.getAttrValue(element, "event"));

				buff.append(makeFunction(depth, "enemy", quote(id), quote(src), x, y, seq, level, event));
			} else if ("bye".equals(name)) {
				String id = quote(DOMUtil.getAttrValue(element, "id"));

				buff.append(makeFunction(depth, "bye", id));
			} else {
				buff.append('\n');
				buff.append('【');
				buff.append(name);
				buff.append('】');
				buff.append(indent(depth, node.toString()));
			}
			String child = analyzeChild(depth + 1, node);
			buff.append(child);
			if (isIf || isWhile) {
				buff.append(indent(depth, "}"));
			}
		}
		return buff.toString();
	}

	private String getInitProcess() {
		StringBuilder buff = new StringBuilder();

		for (String name : this.variableList) {
			String exp = FUNC_PREFIX + name + " = 0";

			buff.append(indent(1, exp));
		}
		return buff.toString();
	}

	private void loadFunctions(Document doc) {
		NodeList nodeList = doc.getElementsByTagName("function");

		for (int ix = 0; ix < nodeList.getLength(); ix++) {
			Integer number = Integer.valueOf(ix + 1);
			Element element = (Element) nodeList.item(ix);
			String id = DOMUtil.getAttrValue(element, "id");
			String title = DOMUtil.getAttrValue(element, "title");
//			String func = String.format("▼%02x:[%s]%s", number, id, title);
			String contents = analyzeChild(1, element);
			boolean isFirstEvent = id.equals(this.scenario.getFirstEvent());

			if (isFirstEvent) {
				contents = getInitProcess() + contents;
			}
//			System.out.println(func);
//			System.out.println(contents);
			this.scenario.addFunction(id, title, contents);
			this.eventNumberMap.put(number, id);
		}
	}

	private void loadFirstEvent(Document doc) {
		NodeList startNodeList = doc.getElementsByTagName("start");

		if (0 < startNodeList.getLength()) {
			Element element = (Element) startNodeList.item(0);
			String event = DOMUtil.getAttrValue(element, "event");

			this.scenario.setFirstEvent(event);
		}
	}

	private void loadPlaces(Document doc) {
		NodeList nodeList = doc.getElementsByTagName("place");

		for (int ix = 0; ix < nodeList.getLength(); ix++) {
			Element element = (Element) nodeList.item(ix);
			String id = DOMUtil.getAttrValue(element, "id");
			String name = DOMUtil.getAttrValue(element, "name");

			this.placeMap.put(id, name);
		}
	}

	private void loadCharacters(Document doc) {
		NodeList nodeList = doc.getElementsByTagName("chara");

		for (int ix = 0; ix < nodeList.getLength(); ix++) {
			Element element = (Element) nodeList.item(ix);
			String id = DOMUtil.getAttrValue(element, "id");
			String name = DOMUtil.getAttrValue(element, "name");
			String src = "chr" + DOMUtil.getAttrValue(element, "src");

			this.actorMap.put(id, new Actor(name, src));
			this.variableList.add(id);
			this.choiceMap.put(id, name);
		}
	}

	private void loadItems(Document doc) {
		NodeList nodeList = doc.getElementsByTagName("item");
		List<Item> itemList = this.scenario.getItemList();

		for (int ix = 0; ix < nodeList.getLength(); ix++) {
			Element element = (Element) nodeList.item(ix);
			String id = DOMUtil.getAttrValue(element, "id");
			String name = DOMUtil.getAttrValue(element, "name");
			String src = DOMUtil.getAttrValue(element, "src");

			if (StringUtils.isNotBlank(src)) {
				itemList.add(new Item(id, name, "item" + src));
			}
			this.variableList.add(id);
			this.choiceMap.put(id, name);
		}
	}

	private void loadVariables(Document doc) {
		NodeList nodeList = doc.getElementsByTagName("count");

		for (int ix = 0; ix < nodeList.getLength(); ix++) {
			Element element = (Element) nodeList.item(ix);
			String id = DOMUtil.getAttrValue(element, "id");

			this.variableList.add(id);
			this.choiceMap.put(id, StringUtils.EMPTY);
		}
	}

	/**
	 * イベントIDを取得.
	 * @param number イベント番号
	 * @return イベントID
	 */
	public String getEventId(int number) {
		return this.eventNumberMap.get(Integer.valueOf(number));
	}

	/**
	 * シナリオデータをアーカイブ.
	 * @param dir ディレクトリー
	 * @param name ファイル名
	 * @throws IOException 入出力例外
	 */
	public void archive(File dir, String name) throws IOException {
		File zipFile = new File(dir, name);

		try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile))) {
			for (File file : dir.listFiles()) {
				String entryName = file.getName();
				boolean isTarget = entryName.endsWith(".png") || entryName.endsWith(".map")
						|| entryName.endsWith(".json");

				if (!file.isFile() || !isTarget) {
					continue;
				}
				ZipEntry entry = new ZipEntry(entryName);
				zip.putNextEntry(entry);
				try (InputStream input = new FileInputStream(file)) {
					IOUtils.copy(input, zip);
				}
			}
		}
		LOG.debug("saved[{}]", zipFile.getAbsolutePath());
	}

	/**
	 * シナリオファイルをセーブ.
	 * @param file ファイル
	 * @throws IOException 入出力例外
	 */
	public void save(File file) throws IOException {
		try (FileWriter out = new FileWriter(file)) {
			out.write(JSON.encode(this.scenario, false));
		}
	}

	/**
	 * シナリオファイルをロード.
	 * @param file ファイル
	 */
	public void load(File file) {
		LOG.debug("load[{}]", file.getAbsolutePath());
		Document doc;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.parse(file);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return;
		}
		loadVariables(doc);
		loadItems(doc);
		loadCharacters(doc);
		loadPlaces(doc);
		loadFirstEvent(doc);
		loadFunctions(doc);
	}
}
