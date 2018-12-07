package com.outreach.interviews.map.resource;

import com.google.gson.JsonObject;

public class Geocode {
	
	/**
	 * The latitude of the geocode.
	 */
	String lat;
	
	/**
	 * The longitude of the geocode.
	 */
	String lng;
	
	public Geocode(JsonObject jsonGeocode) {
		JsonObject location = jsonGeocode.get("location").getAsJsonObject();
		lat = location.get("lat").getAsString();
		lng = location.get("lng").getAsString();
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}
	
	@Override
	public String toString() {
		return "Geocode [lat=" + lat + ", lng=" + lng + "]";
	}
}
