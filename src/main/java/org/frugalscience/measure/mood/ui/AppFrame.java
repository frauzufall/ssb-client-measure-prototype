package org.frugalscience.measure.mood.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.miginfocom.swing.MigLayout;
import org.apache.tuweni.scuttlebutt.lib.model.FeedMessage;
import org.frugalscience.measure.mood.MoodMeasureModel;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class AppFrame extends JFrame {

	private final static String textSubmitButton = "Submit";
	private final static String textLongitudeField = "longitude";
	private final static String textLatitudeField = "latitude";
	private final static String textMoodSlider = "my mood today:";
	private final static String textOutputTitle = "output";
	private final static String textRefreshButton = "refresh";
	private JTextField latitudeInput;
	private JTextField longitudeInput;
	private JSlider moodSlider;
	private Consumer<MoodMeasureModel> submitConsumer = model -> {};
	private Runnable refreshRunnable = () -> {};
	private MapAnnotation mapPanel;

	public static AppFrame build() {
		AppFrame frame = new AppFrame();
		frame.pack();
		frame.setVisible(true);
		return frame;
	}

	private AppFrame() {
		setContentPane(createContentPane());
	}

	private Container createContentPane() {
		JPanel panel = new JPanel(new MigLayout("flowy"));
		panel.add(createInputPanel(), "grow");
		panel.add(createOutputPanel(), "grow");
		return panel;
	}

	private Component createInputPanel() {
		latitudeInput = createTextField();
		longitudeInput = createTextField();
		moodSlider = new JSlider(0, MoodMeasureModel.getMaxMood(), MoodMeasureModel.getMaxMood()/2);
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel(textMoodSlider));
		panel.add(moodSlider);
		panel.add(new JLabel(textLatitudeField), "newline");
		panel.add(latitudeInput, "grow");
		panel.add(new JLabel(textLongitudeField), "newline");
		panel.add(longitudeInput, "grow");
		JButton btn = new JButton(textSubmitButton);
		btn.addActionListener((evt) -> submitConsumer.accept(createModel()));
		panel.add(btn, "newline, span, grow");
		return panel;
	}

	private JTextField createTextField() {
		JTextField res = new JTextField();
		res.setColumns(10);
		return res;
	}

	private MoodMeasureModel createModel() {
		int mood = moodSlider.getValue();
		float latitude = Float.parseFloat(latitudeInput.getText());
		float longitude = Float.parseFloat(longitudeInput.getText());
		return new MoodMeasureModel(mood, latitude, longitude, new Date().toString());
	}

	private Component createOutputPanel() {
		mapPanel = createMapPanel();
		JButton btn = new JButton(textRefreshButton);
		btn.addActionListener((evt) -> refreshRunnable.run());
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel(textOutputTitle), "grow, push");
		panel.add(btn);
		panel.add(mapPanel, "newline, span, grow, push, hmin 200px");
		return panel;
	}

	private MapAnnotation createMapPanel() {
		ImageIcon image = new ImageIcon(getClass().getResource("/map.png"));
		MapAnnotation panel = new MapAnnotation(image.getIconWidth(), image.getIconHeight());
		panel.setLayout(new MigLayout("inset 0"));
		panel.add(new JLabel(image), "push, grow, span");
		return panel;
	}

	public void onSubmit(Consumer<MoodMeasureModel> consumer) {
		this.submitConsumer = consumer;
	}

	public void onRefresh(Runnable runnable) {
		this.refreshRunnable = runnable;
	}

	public void updateFeed(List<FeedMessage> messages) {
		mapPanel.clear();
		for (FeedMessage message : messages) {
			try {
				MoodMeasureModel content = message.getValue().getContentAs(new ObjectMapper(), MoodMeasureModel.class);
				mapPanel.add(content);
			} catch (IOException ignored) {
			}
		}
		mapPanel.repaint();
	}
}
