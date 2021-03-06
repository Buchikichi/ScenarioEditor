package to.kit.scenario.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import to.kit.scenario.edit.component.BrickChooser;
import to.kit.scenario.edit.component.MapPanel;

/**
 * エディター.
 * @author Hidetaka Sasai
 */
public abstract class EditorFrame extends JFrame {
	private static final String ICON_NAME = "/b16au.png";
	protected JComboBox<String> listBox = new JComboBox<>();
	protected BrickChooser bricks = new BrickChooser();
	protected MapPanel mapPane = new MapPanel(this.bricks);
	protected JScrollPane scrollPane = new JScrollPane(this.mapPane);

	protected abstract void open();
	protected abstract void save() throws IOException;
	protected abstract void mapChanged(String name);

	/**
	 * Create the frame.
	 */
	public EditorFrame() {
		setType(Type.POPUP);
		setIconImage(Toolkit.getDefaultToolkit().getImage(EditorFrame.class.getResource(ICON_NAME)));
		setTitle("ScenarioEditor");
		setBounds(0, 0, 640, 480);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel contentPane = (JPanel) getContentPane();
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				open();
			}
		});
		btnOpen.setSize(40, 20);
		btnOpen.setIcon(new ImageIcon(EditorFrame.class.getResource("/javax/swing/plaf/metal/icons/ocean/directory.gif")));
		btnOpen.setBackground(Color.GRAY);
		toolBar.add(btnOpen);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					save();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		btnSave.setSize(40, 20);
		btnSave.setBackground(Color.GRAY);
		btnSave.setIcon(new ImageIcon(EditorFrame.class.getResource("/javax/swing/plaf/metal/icons/ocean/floppy.gif")));
		toolBar.add(btnSave);
		
		final JToggleButton showStairButton = new JToggleButton("ShowStair");
		showStairButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditorFrame.this.mapPane.setShowStair(showStairButton.isSelected());
				EditorFrame.this.repaint();
			}
		});
		showStairButton.setSelected(true);
		this.mapPane.setShowStair(showStairButton.isSelected());
		this.mapPane.setLayout(null);
		toolBar.add(showStairButton);
		
		final JToggleButton showWallButton = new JToggleButton("ShowWall");
		showWallButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditorFrame.this.mapPane.setShowWall(showWallButton.isSelected());
				EditorFrame.this.repaint();
			}
		});
		toolBar.add(showWallButton);
		
		final JToggleButton showEventButton = new JToggleButton("ShowEvent");
		showEventButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditorFrame.this.mapPane.setShowEvent(showEventButton.isSelected());
				EditorFrame.this.repaint();
			}
		});
		toolBar.add(showEventButton);

		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane, BorderLayout.CENTER);

		this.scrollPane.setWheelScrollingEnabled(true);
		JScrollBar verticalScrollBar = this.scrollPane.getVerticalScrollBar();
		verticalScrollBar.setUnitIncrement(16);
		splitPane.setRightComponent(this.scrollPane);

		JSplitPane mapSelectionSplitPane = new JSplitPane();
		mapSelectionSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setLeftComponent(mapSelectionSplitPane);
		this.listBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					mapChanged(String.valueOf(e.getItem()));
				}
			}
		});
		this.listBox.setPreferredSize(new Dimension(128, 32));
		mapSelectionSplitPane.setLeftComponent(this.listBox);
		mapSelectionSplitPane.setRightComponent(this.bricks);
	}
}
