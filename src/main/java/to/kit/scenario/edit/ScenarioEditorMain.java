package to.kit.scenario.edit;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.arnx.jsonic.JSON;

/**
 * エディターメイン.
 * @author Hidetaka Sasai
 */
public class ScenarioEditorMain extends EditorFrame {
	private static final String FILE_EXT = ".dat";

	private JFileChooser chooser = new JFileChooser();

	@Override
	protected void save() {
		BufferedImage bgImage = this.mapPane.getBgImage();

		if (bgImage == null) {
			return;
		}
		int res = this.chooser.showSaveDialog(this);
		if (res != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = this.chooser.getSelectedFile();
		String filename = file.getAbsolutePath();
		String bgName = filename.replaceAll(FILE_EXT, "bg.png");
		String stName = filename.replaceAll(FILE_EXT, "st.png");
		BufferedImage stImage = this.mapPane.getStairImage();

		try {
			ImageIO.write(bgImage, "png", new File(bgName));
			ImageIO.write(stImage, "png", new File(stName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		String wallName = filename.replaceAll(FILE_EXT, ".map");
		String wallData = JSON.encode(this.mapPane.getMapInfo());

		try (FileWriter out = new FileWriter(new File(wallName))) {
			out.write(wallData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void open() {
		int res = this.chooser.showOpenDialog(this);

		if (res != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = this.chooser.getSelectedFile();
		String filename = file.getAbsolutePath();

		filename = filename.replaceAll(FILE_EXT, ".png");
		try {
			BufferedImage image = ImageIO.read(new File(filename));

			this.bricks.setImage(image);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.mapPane.load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.scrollPane.revalidate();
		repaint();
	}

	/**
	 * インスタンス生成.
	 * @param currentDirectory デフォルト選択ディレクトリ
	 */
	public ScenarioEditorMain(String currentDirectory) {
		this.chooser.setFileFilter(new FileFilter(){
			@Override
			public String getDescription() {
				return "Map.";
			}
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(FILE_EXT);
			}
		});
		if (currentDirectory != null) {
			this.chooser.setCurrentDirectory(new File(currentDirectory));
		}
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				String currentDirectory = null;

				if (0 < args.length) {
					currentDirectory = args[0];
				}
				try {
					ScenarioEditorMain frame = new ScenarioEditorMain(currentDirectory);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
