package to.kit.scenario.edit.info;

/**
 * Item.
 * @author Hidetaka Sasai
 */
public final class Item {
	private String id;
	private String name;
	private String src;

	/**
	 * @return the ID
	 */
	public String getId() {
		return this.id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the name
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
	 * @return the src
	 */
	public String getSrc() {
		return this.src;
	}
	/**
	 * @param src the src to set
	 */
	public void setSrc(String src) {
		this.src = src;
	}

	/**
	 * インスタンス生成.
	 * @param id ID
	 * @param name 名前
	 * @param src 画像
	 */
	public Item(String id, String name, String src) {
		this.id = id;
		this.name = name;
		this.src = src;
	}

	/**
	 * インスタンス生成.
	 */
	public Item() {
		// nop
	}
}
