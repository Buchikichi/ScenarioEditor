package to.kit.scenario.edit.info;

/**
 * イベント.
 * @author Hidetaka Sasai
 */
public final class MapEvent {
	private final String position;
	private final int eventNum;

	/**
	 * イベント発生位置を取得.
	 * @return イベント発生位置
	 */
	public String getPosition() {
		return this.position;
	}

	/**
	 * イベント番号を取得.
	 * @return イベント番号
	 */
	public int getEventNum() {
		return this.eventNum;
	}

	/**
	 * インスタンス生成.
	 * @param position イベント発生位置
	 * @param eventNum イベント番号
	 */
	public MapEvent(final String position, final int eventNum) {
		this.position = position;
		this.eventNum = eventNum;
	}
}
