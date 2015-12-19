package to.kit.scenario.edit.component;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.commons.lang3.StringUtils;

import to.kit.scenario.edit.info.MapAnim;
import to.kit.scenario.edit.info.MapCell;
import to.kit.scenario.edit.info.MapEvent;
import to.kit.scenario.edit.info.MapInfo;
import to.kit.scenario.edit.info.Square;

/**
 * マップ表示パネル.
 * @author Hidetaka Sasai
 */
public final class MapPanel extends JPanel implements ActionListener {
	/** Command name of the timer. */
	private static final String TIMER_CMD = "timer";

	private final Timer timer = new Timer(500, this);

	private ActorPanel actor = new ActorPanel("/chr001.png", 32, 32);
	private BrickChooser brick;
	private MapInfo mapInfo;
	private Dimension imgSize;
	private MapCell[][] mapData;
	private BufferedImage bgImage;
	private BufferedImage stairImage;
	private boolean isShowStair;
	private boolean isShowWall;
	private boolean isShowEvent;
	private int animCount;

	private int calcNumber(final int val) {
		int num = val;

		if (8 * 12 <= num) {
			int cnt = this.animCount % 4;
			if (cnt == 3) {
				cnt = 1;
			}
			num = num + 8 * cnt;
		} else if (8 * 9 <= num) {
			num = num + 8 * (this.animCount % 3);
		} else if (8 * 7 <= num) {
			num = num + 8 * (this.animCount % 2);
		}
		return num;
	}

	private void drawImage() {
		Graphics2D bgg = (Graphics2D) this.bgImage.getGraphics();
		Graphics2D stg = (Graphics2D) this.stairImage.getGraphics();
		int y = 0;

		for (MapCell[] line : this.mapData) {
			int by = y * BrickChooser.BRICK_WIDTH;

			for (MapCell cell : line) {
				int bx = cell.getX() * BrickChooser.BRICK_WIDTH;
				int bgData = cell.getBg();
				int stairData = cell.getStair();
				int ev = cell.getEv();

				if (bgData != 0) {
					Image bgBrick = this.brick.getImage(calcNumber(bgData));

					bgg.drawImage(bgBrick, bx, by, null);
				}
				if (stairData != 0) {
					Image stBrick = this.brick.getImage(calcNumber(stairData));

					stg.drawImage(stBrick, bx, by, null);
				}
				if (this.isShowWall && cell.isWall()) {
					int fy = by + 11;
					int width = BrickChooser.BRICK_WIDTH - 1;
					//String msg = String.format("%02X", Integer.valueOf(bgData));
					String msg = StringUtils.EMPTY;
					bgg.setColor(Color.BLACK);
					bgg.drawString(msg, bx, fy + 1);
					bgg.drawString(msg, bx + 1, fy + 1);
					bgg.setColor(Color.WHITE);
					bgg.drawString(msg, bx, fy);
					bgg.setColor(Color.BLACK);
					bgg.setBackground(Color.WHITE);
					bgg.draw3DRect(bx, by, width, width, false);
				}
				if (this.isShowEvent && ev != 0) {
					int fy = by + 10;
					String msg = String.format("%02X", Integer.valueOf(ev));
					bgg.setColor(Color.BLACK);
					bgg.drawString(msg, bx, fy + 1);
					bgg.drawString(msg, bx + 1, fy + 1);
					bgg.setColor(Color.RED);
					bgg.drawString(msg, bx, fy);
				}
			}
			y++;
		}
	}

	private void initImage() {
		int width = this.imgSize.width;
		int height = this.imgSize.height;

		this.bgImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		this.stairImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * バイトオーダーを変換.
	 * @param val 値
	 * @return 変換後の値
	 */
	private short swapShort(int val) {
		return (short) ((val & 0xff) << 8 | (val >> 8 & 0xff));
	}

	private void refillMapData(byte[] mapBytes) {
		Square mapSize = this.mapInfo.getMapSize();

		this.mapData = new MapCell[mapSize.height][mapSize.width];
		for (int y = 0; y < mapSize.height; y++) {
			int origin = y * mapSize.width * 4;

			for (int x = 0; x < mapSize.width; x++) {
				int ix = origin + x * 4;
				int bgData = mapBytes[ix] & 0xff;
				int stairData = mapBytes[ix + 1] & 0xff;
				int ev = mapBytes[ix + 2];

				this.mapData[y][x] = new MapCell(x, y, bgData, stairData, ev);
			}
		}
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	public void load(File file) throws IOException {
		byte[] mapBytes;

		try (InputStream in = new FileInputStream(file);
				DataInputStream data = new DataInputStream(in)) {
			byte[] b = new byte[4];
			data.read(b);
			String sig = new String(b);

			if (!"CMAP".equals(sig)) {
				return;
			}
			short width = swapShort(data.readShort());
			short height = swapShort(data.readShort());
			short posX = swapShort(data.readShort());
			short posY = swapShort(data.readShort());
			short block = swapShort(data.readShort());
			short object = swapShort(data.readShort());

			this.mapInfo = new MapInfo(width, height, posX, posY);
			this.actor.setX(posX);
			this.actor.setY(posY);
			System.out.println("@" + posX + "," + posY + "[" + width + "x" + height + "]");
			System.out.println("block:" + block);
			System.out.println("object:" + object);
			for (int ix = 0; ix < 8; ix++) {
				short chr = swapShort(data.readUnsignedShort());
				System.out.println("chr" + ix + ":" + chr);
			}
			this.imgSize = new Dimension(width * BrickChooser.BRICK_WIDTH, height * BrickChooser.BRICK_WIDTH);
			setPreferredSize(this.imgSize);
			mapBytes = new byte[width * height * 4];
			data.read(mapBytes);
			//System.out.println("available:" + data.available());
		}
		refillMapData(mapBytes);
		this.actor.setVisible(true);
		this.timer.start();
		// ここで描かないとsave出来ない
		initImage();
		drawImage();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (TIMER_CMD.equals(command)) {
			repaint();
		}
	}

	@Override
	public void paint(Graphics g) {
		if (this.mapData == null) {
			super.paint(g);
			return;
		}
		initImage();
		drawImage();
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
		((Graphics2D) g).setComposite(ac);
		g.drawImage(this.bgImage, 0, 0, this);
		if (this.isShowStair) {
			g.drawImage(this.stairImage, 0, 0, this);
		}
		this.animCount++;
		this.actor.setSteps(this.animCount);
		super.paint(g);
	}

	/**
	 * マップ情報を取得.
	 * @return マップ情報
	 */
	public MapInfo getMapInfo() {
		Square mapSize = this.mapInfo.getMapSize();
		int[][] wall = new int[mapSize.height][mapSize.width];
		List<MapEvent> eventList = this.mapInfo.getEventList();
		List<MapAnim> animList = this.mapInfo.getAnimList();

		for (MapCell[] line : this.mapData) {
			for (MapCell cell : line) {
				int x = cell.getX();
				int y = cell.getY();
				int bgData = cell.getBg();
				int bgType = cell.getBgType();
				int stair = cell.getStair();
				int stairType = cell.getStairType();
				int ev = cell.getEv();

				wall[y][x] = cell.isWall() ? 1 : 0;
				if (0 < ev) {
					String position = x + "-" + y;

					eventList.add(new MapEvent(position, ev));
				}
				if (0 < bgType) {
					int ox = bgData % 8;
					int oy = bgData / 8;
					animList.add(new MapAnim(x, y, ox, oy, 0, bgType));
				}
				if (0 < stairType) {
					int ox = stair % 8;
					int oy = stair / 8;
					animList.add(new MapAnim(x, y, ox, oy, 1, stairType));
				}
			}
		}
		this.mapInfo.setWall(wall);
		return this.mapInfo;
	}

	/**
	 * バックグラウンドイメージを取得.
	 * @return バックグラウンドイメージ
	 */
	public BufferedImage getBgImage() {
		return this.bgImage;
	}

	/**
	 * 上層イメージを取得.
	 * @return 上層イメージ
	 */
	public BufferedImage getStairImage() {
		return this.stairImage;
	}

	/**
	 * 上層を表示するかを設定.
	 * @param showStair 表示する場合はtrue
	 */
	public void setShowStair(boolean showStair) {
		this.isShowStair = showStair;
	}

	/**
	 * 壁を表示するかを設定.
	 * @param isShowWall 表示する場合はtrue
	 */
	public void setShowWall(boolean isShowWall) {
		this.isShowWall = isShowWall;
	}

	/**
	 * イベントを表示するかを設定.
	 * @param isShowEvent 表示する場合はtrue
	 */
	public void setShowEvent(boolean isShowEvent) {
		this.isShowEvent = isShowEvent;
	}

	/**
	 * インスタンス生成.
	 * @param brick ブロック選択
	 */
	public MapPanel(BrickChooser brick) {
		this.brick = brick;
		this.timer.setActionCommand(TIMER_CMD);
		setOpaque(false);
		this.actor.setVisible(false);
		add(this.actor);
	}
}
