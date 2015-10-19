package to.kit.scenario.edit.info;

/**
 * Actor.
 * @author Hidetaka Sasai
 */
public final class Actor {
	private final String name;
	private final String src;

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * @return the src
	 */
	public String getSrc() {
		return this.src;
	}

	/**
	 * インスタンス生成.
	 * @param name 名前
	 * @param src 画像
	 */
	public Actor(String name, String src) {
		this.name = name;
		this.src = src;
	}
}
