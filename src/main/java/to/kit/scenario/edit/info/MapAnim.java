package to.kit.scenario.edit.info;

/**
 * マップアニメーション.
 * @author Hidetaka Sasai
 *
 */
public final class MapAnim {
	private final int x;
	private final int y;
	private final int ox;
	private final int oy;
	private final int layer;
	private final int pat;

	/**
	 * @return the x
	 */
	public int getX() {
		return this.x;
	}
	/**
	 * @return the y
	 */
	public int getY() {
		return this.y;
	}
	/**
	 * @return the ox
	 */
	public int getOx() {
		return this.ox;
	}
	/**
	 * @return the oy
	 */
	public int getOy() {
		return this.oy;
	}
	/**
	 * @return the layer
	 */
	public int getLayer() {
		return this.layer;
	}
	/**
	 * @return the pat
	 */
	public int getPat() {
		return this.pat;
	}

	/**
	 * インスタンス生成.
	 * @param x
	 * @param y
	 * @param ox オフセットX
	 * @param oy オフセットY
	 * @param layer 層
	 * @param pat パターン
	 */
	public MapAnim(final int x, final int y, final int ox, final int oy, final int layer, final int pat) {
		this.x = x;
		this.y = y;
		this.ox = ox;
		this.oy = oy;
		this.layer = layer;
		this.pat = pat;
	}
}
