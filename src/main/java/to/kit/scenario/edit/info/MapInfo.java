package to.kit.scenario.edit.info;

import java.util.ArrayList;
import java.util.List;

/**
 * マップ情報.
 * @author Hidetaka Sasai
 */
public final class MapInfo {
	private final Square mapSize;
	private final Location pos;
	private int[][] wall;
	private final List<MapEvent> eventList = new ArrayList<>();

	/**
	 * マップサイズを取得.
	 * @return マップサイズ
	 */
	public Square getMapSize() {
		return this.mapSize;
	}
	/**
	 * 位置を取得.
	 * @return 位置
	 */
	public Location getPos() {
		return this.pos;
	}
	/**
	 * @return the wall
	 */
	public int[][] getWall() {
		return this.wall;
	}
	/**
	 * @param wall the wall to set
	 */
	public void setWall(int[][] wall) {
		this.wall = wall;
	}
	/**
	 * イベント一覧を取得.
	 * @return イベント一覧
	 */
	public List<MapEvent> getEventList() {
		return this.eventList;
	}

	/**
	 * インスタンス生成.
	 * @param width マップの幅
	 * @param height マップの高さ
	 * @param x 位置X
	 * @param y 位置Y
	 */
	public MapInfo(int width, int height, int x, int y) {
		this.mapSize = new Square(width, height);
		this.pos = new Location(x, y);
	}
}
