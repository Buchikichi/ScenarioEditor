package to.kit.scenario.edit.info;

import java.util.HashMap;
import java.util.Map;

/**
 * シナリオデータ.
 * @author Hidetaka Sasai
 */
public final class Scenario {
	private String firstEvent;
	private final Map<String, ScenarioFunction> functionMap = new HashMap<>();

	/**
	 * ファンクションを追加.
	 * @param id ファンクションID
	 * @param name ファンクション名
	 * @param contents ファンクション内容
	 */
	public void addFunction(String id, String name, String contents) {
		this.functionMap.put(id, new ScenarioFunction(name, contents));
	}

	/**
	 * @return the functionMap
	 */
	public Map<String, ScenarioFunction> getFunctionMap() {
		return this.functionMap;
	}
	/**
	 * 最初のイベントを取得.
	 * @return 最初のイベント
	 */
	public String getFirstEvent() {
		return this.firstEvent;
	}
	/**
	 * 最初のイベントを設定.
	 * @param firstEvent 最初のイベント
	 */
	public void setFirstEvent(String firstEvent) {
		this.firstEvent = firstEvent;
	}

	class ScenarioFunction {
		private final String name;
		private final String contents;

		/**
		 * @return the title
		 */
		public String getName() {
			return this.name;
		}
		/**
		 * @return the contents
		 */
		public String getContents() {
			return this.contents;
		}
		/**
		 * インスタンス生成.
		 * @param name ファンクション名
		 * @param contents ファンクション内容
		 */
		public ScenarioFunction(String name, String contents) {
			this.name = name;
			this.contents = contents;
		}
	}
}
