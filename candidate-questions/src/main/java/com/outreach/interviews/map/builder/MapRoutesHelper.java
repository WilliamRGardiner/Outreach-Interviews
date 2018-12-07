package com.outreach.interviews.map.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.outreach.interviews.map.enums.MapModes;
import com.outreach.interviews.map.enums.MapOperations;
import com.outreach.interviews.map.enums.MapRegions;
import com.outreach.interviews.map.resource.Geocode;

public class MapRoutesHelper
{
	public static class RoutesBuilder {

		private String origin;
		private String destination;
		private MapRegions region;
		private MapOperations operation;
		private MapModes mode;
		private JsonObject result;

		private final String URL = "https://maps.googleapis.com/maps/api/";
		private CloseableHttpClient httpclient = HttpClients.createDefault();
		
		/**
		 * Set the starting point
		 * @param origin String representing the starting point
		 * @return {@link RoutesBuilder}
		 */
		public RoutesBuilder setOrigin(String origin)  {
			this.origin = origin;
			return this;
		}
		
		/**
		 * Set the destination point
		 * @param destination String representing the destination point
		 * @return {@link RoutesBuilder}
		 */
		public RoutesBuilder setDestination(String destination) {
			this.destination = destination;
			return this;
		}
		
		/**
		 * Set the region {@link MapRegions}
		 * @param region Allows for en, es
		 * @return {@link RoutesBuilder}
		 */
		public RoutesBuilder setRegion(MapRegions region) {
			this.region = region;
			return this;
		}
		
		/**
		 * Set the region {@link MapModes}
		 * @param mode Allows for walking, driving, transit, biking
		 * @return {@link RoutesBuilder}
		 */
		public RoutesBuilder setMapMode(MapModes mode) {
			this.mode = mode;
			return this;
		}
		
		/**
		 * Create the URL to communicate with the Google Maps API
		 * @param type URL to provide to Apache HttpClient
		 * @return {@link RoutesBuilder}
		 */
		public RoutesBuilder setURL(MapOperations type) {
			this.operation = type;
			return this;

		}
		
		/**
		 * Perform the HTTP request and retrieve the data from the HttpClient object
		 * @return {@link RoutesBuilder}  
		 * @throws UnsupportedOperationException
		 * @throws IOException
		 * @throws IllegalArgumentException
		 */
		public RoutesBuilder build() throws UnsupportedOperationException, IOException, IllegalArgumentException {
			String requestURL = this.getURL();
			if(this.operation == MapOperations.directions) {
				requestURL += "&origin=" + getOrigin() + "&destination=" + getDestination() + "&region="
						+ getRegion() + "&key=" + this.getAPIKey();
				if(getMode() != null) {
					requestURL = requestURL + "&mode=" + this.getMode();
				}
			} else if(this.operation == MapOperations.geocode) {
				requestURL += "&address=" + getOrigin() + "&key=" + this.getAPIKey();;
			}
			
			HttpGet httpGet = new HttpGet(requestURL);
			
			CloseableHttpResponse response = httpclient.execute(httpGet);
			
			try {
				HttpEntity entity = response.getEntity();
				String result = IOUtils.toString(entity.getContent(), "UTF-8");
				this.result = new JsonParser().parse(result).getAsJsonObject();
			}
			finally {
				response.close();
			}
			
			return this;
		}
		
		/**
		 * Retrieve the steps required to get from the source to the destination
		 * @return List of String containing the steps to get to the destination
		 */
		public List<String> getDirections() {
			if(this.operation.equals(MapOperations.directions) && zeroResults(this.result)) {
				List<String> list = new ArrayList<String>();
				JsonArray steps = this.result.get("routes").getAsJsonArray().get(0).getAsJsonObject()
					.get("legs").getAsJsonArray().get(0).getAsJsonObject()
					.get("steps").getAsJsonArray();
				
				Iterator<JsonElement> i = steps.iterator();
				while(i.hasNext()) {
					JsonObject step = (JsonObject) i.next();
					list.add(step.get("html_instructions").getAsString());
				}
				return list;
			} else {
				throw new IllegalArgumentException("Does not support " + MapOperations.geocode.name());
			}
		}
		
		/**
		 * Retrieves the geocodes for all locations matching the search criteria.
		 * 
		 * @return a List of Geocode Objects for all matching locations
		 */
		public List<Geocode> getGeocode() {
			if(this.operation.equals(MapOperations.geocode) && zeroResults(this.result)) {
				JsonArray resultArray = this.result.get("results").getAsJsonArray();
				Iterator<JsonElement> resultIterator = resultArray.iterator();
				List<Geocode> geocodes = new ArrayList<>();
				while (resultIterator.hasNext()) {
					geocodes.add(new Geocode(resultIterator.next().getAsJsonObject().get("geometry").getAsJsonObject()));
				}
				return geocodes;
			} else {
				throw new IllegalArgumentException("Does not support " + MapOperations.geocode.name());
			}
		}
		

		//*************************For Internal Use Only***********************************//
		private final String getURL() {
			return this.URL + this.operation.name() + "/json?";
		}

		private final String getAPIKey() {
			return "AIzaSyD0AXY5m13exK_uX-0MN66XjLP0e2R8YMQ";
		}
		
		private final String getOrigin() {
			if(this.origin == null)
				throw new IllegalArgumentException("Origin cannot be empty");
			
			return this.origin;
		}
		
		private final String getDestination() {
			if(this.destination == null)
				throw new IllegalArgumentException("Destination cannot be empty");
			
			return this.destination;
		}
		
		private final String getMode() {
			if(this.mode == null) {
				return null;
			}
			
			return this.mode.name();
		}
		
		private final String getRegion() {
			if(this.destination == null)
				throw new IllegalArgumentException("Region cannot be empty");
			
			return this.region.name();
		}
		
		private final boolean zeroResults(JsonObject obj) {
			return !obj.get("status").getAsString().equals("ZERO_RESULTS");
		}

	}
}