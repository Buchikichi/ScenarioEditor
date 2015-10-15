package to.kit.scenario.edit;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import net.arnx.jsonic.JSON;
import to.kit.scenario.edit.info.MapEvent;
import to.kit.scenario.edit.info.MapInfo;
import to.kit.scenario.edit.io.ScenarioFile;

/**
 * エディターメイン.
 * @author Hidetaka Sasai
 */
public class ScenarioEditorMain extends EditorFrame {
	private static final String FILE_EXT = ".dat";

	private JFileChooser chooser = new JFileChooser();
	private ScenarioFile scenario = new ScenarioFile();

	private void listMapFiles() {
		File dir = this.chooser.getCurrentDirectory();

		this.listBox.removeAllItems();
		for (File file : dir.listFiles()) {
			String name = file.getName();

			if (!name.endsWith(".dat")) {
				continue;
			}
			this.listBox.addItem(name);
		}
	}

	private void saveMap(String filename) {
		String wallName = filename.replaceAll(FILE_EXT, ".map");
		MapInfo mapInfo = this.mapPane.getMapInfo();

		for (MapEvent event : mapInfo.getEventList()) {
			String eventId = this.scenario.getEventId(event.getEventNum());

			event.setEventId(eventId);
		}
		try (FileWriter out = new FileWriter(new File(wallName))) {
			out.write(JSON.encode(mapInfo));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveImage(String filename) {
		BufferedImage bgImage = this.mapPane.getBgImage();
		BufferedImage stImage = this.mapPane.getStairImage();
		String bgName = filename.replaceAll(FILE_EXT, "bg.png");
		String stName = filename.replaceAll(FILE_EXT, "st.png");

		try {
			ImageIO.write(bgImage, "png", new File(bgName));
			ImageIO.write(stImage, "png", new File(stName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void save() {
		if (this.mapPane.getBgImage() == null) {
			return;
		}
		int res = this.chooser.showSaveDialog(this);
		if (res != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File dir = this.chooser.getSelectedFile();

		for (int ix = 0; ix < this.listBox.getItemCount(); ix++) {
			String name = this.listBox.getItemAt(ix);
			File file = new File(dir, name);
			String filename = file.getAbsolutePath();

			saveImage(filename);
			saveMap(filename);
		}
		System.out.println("saved.");
	}

	@Override
	protected void open() {
		int res = this.chooser.showOpenDialog(this);

		if (res != JFileChooser.APPROVE_OPTION) {
			return;
		}
	}

	@Override
	protected void mapChanged(String name) {
		File dir = this.chooser.getCurrentDirectory();
		File file = new File(dir, name);
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
		this.chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (currentDirectory == null) {
			return;
		}
		File dir = new File(currentDirectory);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		this.chooser.setCurrentDirectory(dir);
		listMapFiles();
		File scenarioFile = new File(dir, "scene.xml");

		this.scenario.load(scenarioFile);
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
