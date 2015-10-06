package to.kit.scenario.edit.component;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public final class ActorPanel extends JPanel {
	private BufferedImage image;
	private final int width;
	private final int height;
	private int x;
	private int y;
	private int steps;
	private int direction;

	@Override
	public void paint(Graphics g) {
		int ix = (this.steps % 2) * this.width;
		int left = this.x * BrickChooser.BRICK_WIDTH;
		int top = this.y * BrickChooser.BRICK_WIDTH;

		g.drawImage(this.image, -ix, 0, this.width * 8, this.height, null);
		setBounds(left, top, this.width, this.height);
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}
	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}
	/**
	 * @param steps the steps to set
	 */
	public void setSteps(int steps) {
		this.steps = steps;
	}
	/**
	 * @param direction the direction to set
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

	/**
	 * インスタンス生成.
	 * @param resource リソース名
	 * @param width 幅
	 * @param height 高さ
	 */
	public ActorPanel(String resource, int width, int height) {
		URL url = ActorPanel.class.getResource(resource);
		try {
			this.image = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.width = width;
		this.height = height;
		setBounds(0, 0, width, height);
		setOpaque(false);
	}
}
