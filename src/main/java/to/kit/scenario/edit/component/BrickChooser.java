package to.kit.scenario.edit.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.plaf.metal.MetalBorders;

/**
 * ブロック選択.
 * @author Hidetaka Sasai
 */
public final class BrickChooser extends JPanel {
	/** ブロックの幅. */
	public static final int BRICK_WIDTH = 16;

	/** 表示するイメージ. */
	private BufferedImage image;
	/** カーソル. */
	private JPanel cursor = new JPanel();

	/**
	 * イメージを取得.
	 * @param num ブロック番号
	 * @return イメージ
	 */
	public BufferedImage getImage(final int num) {
		int blockNum = num & 0x7f;
		int x = (blockNum % 8) * BRICK_WIDTH;
		int y = (blockNum / 8) * BRICK_WIDTH;

		return this.image.getSubimage(x, y, BRICK_WIDTH, BRICK_WIDTH);
	}

	/**
	 * 表示するイメージを設定.
	 * @param image 表示するイメージ
	 */
	public void setImage(final BufferedImage image) {
		this.image = image;
	}

	protected void mousePressed(MouseEvent e) {
		if (this.image == null) {
			return;
		}
		int x = e.getX() / BRICK_WIDTH;
		int y = e.getY() / BRICK_WIDTH;
		Rectangle rect = this.cursor.getBounds();

		rect.x = x * BRICK_WIDTH;
		rect.y = y * BRICK_WIDTH;
		this.cursor.setBounds(rect);
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		if (this.image != null) {
			g.drawImage(this.image, 0, 0, Color.BLACK, this);
		}
		super.paint(g);
	}

	/**
	 * インスタンス生成.
	 */
	public BrickChooser() {
		setMinimumSize(new Dimension(128, 144));
		setLayout(null);
		setOpaque(false);
		add(this.cursor);
		this.cursor.setBorder(new MetalBorders.PaletteBorder());
		this.cursor.setBounds(0, 0, BRICK_WIDTH, BRICK_WIDTH);
		this.cursor.setOpaque(false);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				BrickChooser.this.mousePressed(e);
			}
		});
	}
}
