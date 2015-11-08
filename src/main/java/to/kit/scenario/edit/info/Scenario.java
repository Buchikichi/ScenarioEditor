package to.kit.scenario.edit.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * シナリオデータ.
 * @author Hidetaka Sasai
 */
public final class Scenario {
	private String firstEvent;
	private Map<String, ScenarioFunction> functionMap = new HashMap<>();
	private List<Item> itemList = new ArrayList<>();

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
	/**
	 * ファンクションマップを取得.
	 * @return ファンクションマップ
	 */
	public Map<String, ScenarioFunction> getFunctionMap() {
		return this.functionMap;
	}
	/**
	 * ファンクションマップを設定.
	 * @param functionMap ファンクションマップ
	 */
	public void setFunctionMap(Map<String, ScenarioFunction> functionMap) {
		this.functionMap = functionMap;
	}
	/**
	 * アイテム一覧を取得.
	 * @return アイテム一覧
	 */
	public List<Item> getItemList() {
		return this.itemList;
	}
	/**
	 * アイテム一覧を設定.
	 * @param itemList アイテム一覧
	 */
	public void setItemList(List<Item> itemList) {
		this.itemList = itemList;
	}
}
