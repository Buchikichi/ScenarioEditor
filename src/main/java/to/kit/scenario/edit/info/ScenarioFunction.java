package to.kit.scenario.edit.info;

/**
 * シナリオファンクション.
 * @author Hidetaka Sasai
 */
public final class ScenarioFunction {
	private String name;
	private String contents;

	/**
	 * @return the title
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the contents
	 */
	public String getContents() {
		return this.contents;
	}
	/**
	 * @param contents the contents to set
	 */
	public void setContents(String contents) {
		this.contents = contents;
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
	/**
	 * インスタンス生成.
	 */
	public ScenarioFunction() {
		// nop
	}
}
