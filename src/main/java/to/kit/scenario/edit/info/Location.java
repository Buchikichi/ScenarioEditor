package to.kit.scenario.edit.info;

/**
 * 位置.<br/>
 * (Pointの代わり)
 * @author Hidetaka Sasai
 */
public final class Location {
	/** 座標X. */
	public final int x;
	/** 座標Y. */
	public final int y;

	/**
	 * インスタンス生成.
	 * @param x 座標X
	 * @param y 座標Y
	 */
	public Location(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
}
