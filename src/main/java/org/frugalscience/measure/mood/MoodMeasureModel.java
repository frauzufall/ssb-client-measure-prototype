package org.frugalscience.measure.mood;

import org.apache.tuweni.scuttlebutt.lib.model.ScuttlebuttMessageContent;

public class MoodMeasureModel implements ScuttlebuttMessageContent {

	private static int maxMood = 10;
	private float latitude;
	private float longitude;
	private String timepoint;
	private int mood;
	private String type = "measure-mood";

	public MoodMeasureModel() {
	}

	public MoodMeasureModel(int mood, float latitude, float longitude, String timepoint) {
		this.mood = mood;
		this.latitude = latitude;
		this.longitude = longitude;
		this.timepoint = timepoint;
	}

	public static int getMaxMood() {
		return maxMood;
	}

	@Override
	public String getType() {
		return type;
	}

	public float getLatitude() {
		return latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public String getTimepoint() {
		return timepoint;
	}

	@Override
	public String toString() {
		return "mood: " + mood + " latitude: " + latitude + " longitude: " + longitude + " timepoint: " + timepoint;
	}

	public int getMood() {
		return mood;
	}
}
