package org.frugalscience.measure.mood.ui;

import org.frugalscience.measure.mood.MoodMeasureModel;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static java.lang.Math.PI;

class MapAnnotation extends JPanel {

	private final int width;
	private final int height;
	private int pointSize = 10;

	private int posX(double input) {
		return (int) ((input + 180) * ((float) width / 360));
	}

	private int posY(double input) {
		double latRad = input * PI / 180;
		double mercN = Math.log(Math.tan((PI / 4) + (latRad / 2)));
		return (int) ((height/2)-(width*mercN/(2*PI)));
	}

	MapAnnotation(int width, int height) {
		this.width = width;
		this.height = height;
	}

	ArrayList<DataPoint> entities = new ArrayList<>();

	void add(MoodMeasureModel measure) {
		int x = posX(measure.getLongitude());
		int y = posY(measure.getLatitude());
		Color color = toColor(measure.getMood());
		addEntity(x, y, pointSize, pointSize, color);
	}

	private Color toColor(int mood) {
		float hue = (float) mood / (float) MoodMeasureModel.getMaxMood();
		int saturation = 1;
		int brightness = 1;
		return Color.getHSBColor(hue, saturation, brightness);
	}

	private void addEntity(int x, int y, int w, int h, Color c) {
		entities.add(new DataPoint(x, y, w, h, c));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for (DataPoint entity : entities) {
			g.setColor(entity.getColor());
			g.fillOval((int) entity.x, (int) entity.y, (int) entity.width, (int) entity.height);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	void clear() {
		entities.clear();
	}

	class DataPoint extends Rectangle2D.Double {

		Color color;

		public DataPoint(double x, double y, double w, double h, Color c) {
			super(x, y, w, h);
			color = c;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public Color getColor() {
			return color;
		}
	}

}
