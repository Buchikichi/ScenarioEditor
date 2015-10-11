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

import javax.swing.JPanel;
import javax.swing.Timer;

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
	private Dimension mapSize;
	private Dimension imgSize;
	private byte[] mapData;
	private BufferedImage bgImage;
	private BufferedImage stairImage;
	private boolean isShowStair;
	private boolean isShowWall;
	private boolean isShowEvent;
	private int animCount;

	private int calcNumber(final int val) {
		int num = val & 0x7f;

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

		for (int y = 0; y < this.mapSize.height; y++) {
			int by = y * BrickChooser.BRICK_WIDTH;
			int origin = y * this.mapSize.width * 4;

			for (int x = 0; x < this.mapSize.width; x++) {
				int bx = x * BrickChooser.BRICK_WIDTH;
				int ix = origin + x * 4;
				int bgData = this.mapData[ix] & 0xff;
				int stairData = this.mapData[ix + 1] & 0xff;
				byte ev = this.mapData[ix + 2];

				if (bgData != 0) {
					Image bgBrick = this.brick.getImage(calcNumber(bgData));

					bgg.drawImage(bgBrick, bx, by, null);
				}
				if (stairData != 0) {
					Image stBrick = this.brick.getImage(calcNumber(stairData));

					stg.drawImage(stBrick, bx, by, null);
				}
				if (this.isShowWall && 0x7f < bgData) {
					int num = bgData & 0x7f;
					int fy = by + 11;
					int width = BrickChooser.BRICK_WIDTH - 1;
					String msg = String.format("%02X", Integer.valueOf(bgData - num));
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
					String msg = String.format("%02X", Byte.valueOf(ev));
					bgg.setColor(Color.BLACK);
					bgg.drawString(msg, bx, fy + 1);
					bgg.drawString(msg, bx + 1, fy + 1);
					bgg.setColor(Color.RED);
					bgg.drawString(msg, bx, fy);
				}
			}
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

	private void loadHeader(DataInputStream data) throws IOException {
		short width = swapShort(data.readShort());
		short height = swapShort(data.readShort());
		this.mapSize = new Dimension(width, height);
		short posX = swapShort(data.readShort());
		short posY = swapShort(data.readShort());
		short block = swapShort(data.readShort());
		short object = swapShort(data.readShort());

		this.actor.setX(posX);
		this.actor.setY(posY);
		System.out.println("@" + posX + "," + posY);
		System.out.println("block:" + block);
		System.out.println("object:" + object);
		for (int ix = 0; ix < 8; ix++) {
			short chr = swapShort(data.readUnsignedShort());
			System.out.println("chr" + ix + ":" + chr);
		}
		this.imgSize = new Dimension(this.mapSize.width * BrickChooser.BRICK_WIDTH,
				this.mapSize.height * BrickChooser.BRICK_WIDTH);
		setPreferredSize(this.imgSize);
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	public void load(File file) throws IOException {
		try (InputStream in = new FileInputStream(file);
				DataInputStream data = new DataInputStream(in)) {
			byte[] b = new byte[4];
			data.read(b);
			String sig = new String(b);

			if (!"CMAP".equals(sig)) {
				return;
			}
			loadHeader(data);
			this.mapData = new byte[this.mapSize.width * this.mapSize.height * 4];
			data.read(this.mapData);
			//System.out.println("available:" + data.available());
		}
		this.actor.setVisible(true);
		this.timer.start();
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
	 * 壁情報を取得.
	 * @return 壁情報
	 */
	public int[][] getWallData() {
		int[][] result = new int[this.mapSize.height][this.mapSize.width];

		for (int y = 0; y < this.mapSize.height; y++) {
			int origin = y * this.mapSize.width * 4;

			for (int x = 0; x < this.mapSize.width; x++) {
				int ix = origin + x * 4;
				int bgData = this.mapData[ix] & 0xff;

				result[y][x] = bgData < 0x80 ? 0 : 1;
			}
		}
		return result;
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
