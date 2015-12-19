package to.kit.scenario.edit.info;

/**
 * マップセル.
 * @author Hidetaka Sasai
 */
public final class MapCell {
	private final int x;
	private final int y;
	private final int bg;
	private final int bgType;
	private final int stair;
	private final int stairType;
	private final int ev;
	private final boolean wall;

	/**
	 * @param val
	 * @return animType
	 */
	private static int calcAnimType(final int val) {
		int type;

		if (8 * 12 <= val) {
			type = 4;
		} else if (8 * 9 <= val) {
			type = 3;
		} else if (8 * 7 <= val) {
			type = 2;
		} else {
			type = 0;
		}
		return type;
	}

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
	 * @return the bg
	 */
	public int getBg() {
		return this.bg;
	}
	/**
	 * @return the bgType
	 */
	public int getBgType() {
		return this.bgType;
	}
	/**
	 * @return the stair
	 */
	public int getStair() {
		return this.stair;
	}
	/**
	 * @return the ev
	 */
	public int getEv() {
		return this.ev;
	}
	/**
	 * @return the stairType
	 */
	public int getStairType() {
		return this.stairType;
	}
	/**
	 * @return wall
	 */
	public boolean isWall() {
		return this.wall;
	}

	/**
	 * インスタンス生成.
	 * @param x
	 * @param y
	 * @param bgData
	 * @param stairData
	 * @param ev
	 */
	public MapCell(int x, int y, final int bgData, final int stairData, final int ev) {
		this.x = x;
		this.y = y;
		this.bg = bgData & 0x7f;
		this.bgType = calcAnimType(this.bg);
		this.stair =stairData & 0x7f;
		this.stairType = calcAnimType(this.stair);
		this.ev = ev;
		this.wall = 0x7f < bgData;
	}
}
