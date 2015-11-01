package to.kit.scenario.edit;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.arnx.jsonic.JSON;
import to.kit.scenario.edit.info.MapEvent;
import to.kit.scenario.edit.info.MapInfo;
import to.kit.scenario.edit.io.ScenarioFile;

/**
 * エディターメイン.
 * @author Hidetaka Sasai
 */
public class ScenarioEditorMain extends EditorFrame {
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(ScenarioEditorMain.class);
	private static final String FILE_EXT = ".dat";

	private JFileChooser chooser = new JFileChooser();
	private ScenarioFile scenario = new ScenarioFile();

	private void listMapFiles() {
		File dir = this.chooser.getSelectedFile();

		this.listBox.removeAllItems();
		for (File file : dir.listFiles()) {
			String name = file.getName();

			if (!name.endsWith(".dat")) {
				continue;
			}
			this.listBox.addItem(name);
		}
	}

	private void saveMap(String filename) throws IOException {
		String wallName = filename.replaceAll(FILE_EXT, ".map");
		MapInfo mapInfo = this.mapPane.getMapInfo();

		for (MapEvent event : mapInfo.getEventList()) {
			String eventId = this.scenario.getEventId(event.getEventNum());

			event.setEventId(eventId);
		}
		try (FileWriter out = new FileWriter(new File(wallName))) {
			out.write(JSON.encode(mapInfo));
		}
	}

	private void saveImage(String filename) throws IOException {
		BufferedImage bgImage = this.mapPane.getBgImage();
		BufferedImage stImage = this.mapPane.getStairImage();
		String bgName = filename.replaceAll(FILE_EXT, "bg.png");
		String stName = filename.replaceAll(FILE_EXT, "st.png");

		ImageIO.write(bgImage, "png", new File(bgName));
		ImageIO.write(stImage, "png", new File(stName));
	}

	private void openScenario() {
		File dir = this.chooser.getSelectedFile();
		File scenarioFile = new File(dir, "scene.xml");

		this.scenario.load(scenarioFile);
		listMapFiles();
	}

	@Override
	protected void save() throws IOException {
		if (this.mapPane.getBgImage() == null) {
			return;
		}
		int res = this.chooser.showSaveDialog(this);
		if (res != JFileChooser.APPROVE_OPTION) {
			return;
		}

		// image/map
		File dir = this.chooser.getSelectedFile();

		for (int ix = 0; ix < this.listBox.getItemCount(); ix++) {
			String name = this.listBox.getItemAt(ix);
			File file = new File(dir, name);
			String filename = file.getAbsolutePath();

			mapChanged(name);
			saveImage(filename);
			saveMap(filename);
		}

		// scene
		this.scenario.save(new File(dir, "scene.json"));
		this.scenario.archive(dir, "whjr000s.jar");
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
		File dir = this.chooser.getSelectedFile();
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
		this.chooser.setSelectedFile(dir);
		openScenario();
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
