package com.ugo.android.tourmate.util;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.ugo.android.tourmate.entities.PointOfInterest;
import com.ugo.android.tourmate.util.io.RestClient;
import com.ugo.android.tourmate.util.io.RestClient.RequestMethod;

public class POIHelper {

	public static final String API_KEY = "YOUR_API_KEY";

	public static DecimalFormat distanceFormat = new DecimalFormat("#######.##");

	public static final String POI_CATEGORIES = "amusement_park|aquarium|art_gallery|bakery|bar|book_store|"
			+ "cafe|casino|city_hall|clothing_store|establishment|food|grocery_or_supermarket|liquor_store|"
			+ "lodging|meal_delivery|meal_takeaway|movie_rental|movie_theater|musuem|natural_feature|night_club|"
			+ "park|point_of_interest|restaurant|shoe_store|stadium|store|zoo";

	public static final String STAT_INVALID_REQUEST = "INVALID_REQUEST";
	public static final String STAT_OK = "OK";
	public static final String STAT_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
	public static final String STAT_REQUEST_DENIED = "REQUEST_DENIED";
	public static final String STAT_ZERO_RESULTS = "ZERO_RESULTS";
	
	private final static String TAG = POIHelper.class.getSimpleName();

	public static boolean checkInToLocation(String reference)
			throws ConnectTimeoutException, UnknownHostException {
		RestClient gPlacesClient = new RestClient(
				"https://maps.googleapis.com/maps/api/place/check-in/json");
		gPlacesClient.addParam("sensor", "true");
		gPlacesClient.addParam("key", API_KEY);

		JSONObject data = new JSONObject();
		try {
			data.put("reference", reference);
			gPlacesClient
					.setPostStringEntity(new StringEntity(data.toString()));
			gPlacesClient.execute(RequestMethod.POST);

			String response = "";
			JSONObject gPlacesResponse;

			if (gPlacesClient.getResponseCode() == 200) {
				response = gPlacesClient.getResponse();
			} else {
				// TODO throw exception
			}
			
			if (!(response == "")) {
				gPlacesResponse = new JSONObject(response);
				
				String gPlacesStatus = gPlacesResponse.getString("status");
				
				if (gPlacesStatus.equals(STAT_OK)) {
					return true;
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static ArrayList<PointOfInterest> fetchPOIAroundLocation(
			Context context, Location location) throws ConnectTimeoutException,
			UnknownHostException {
		ArrayList<PointOfInterest> fetchedPOIs = new ArrayList<PointOfInterest>();

		String locationString = location.getLatitude() + ","
				+ location.getLongitude();

		String radiusString = location.getAccuracy() < 20 ? "1000" : "2000";

		RestClient gPlacesClient = new RestClient(
				"https://maps.googleapis.com/maps/api/place/nearbysearch/json");
		gPlacesClient.addParam("location", locationString);
		gPlacesClient.addParam("radius", radiusString);
		gPlacesClient.addParam("sensor", "true");
		// gPlacesClient.addParam("types", POI_CATEGORIES);
		gPlacesClient.addParam("key", API_KEY);

		try {
			gPlacesClient.execute(RequestMethod.GET);

			String response = "";
			JSONObject gPlacesResponse;

			if (gPlacesClient.getResponseCode() == 200) {
				response = gPlacesClient.getResponse();
			} else {
				// TODO throw exception
			}

			if (!(response == "")) {
				gPlacesResponse = new JSONObject(response);
				String gPlacesStatus = gPlacesResponse.getString("status");

				if (gPlacesStatus.equals(STAT_OK)) {
					JSONArray placesResult = gPlacesResponse
							.getJSONArray("results");

					for (int i = 0; i < placesResult.length(); i++) {
						// Create a Location object to store a parsed lat and
						// long with
						Location poiLoc = new Location(
								LocationManager.PASSIVE_PROVIDER);

						// Get the geometry object from the JSON
						JSONObject geometry = placesResult.getJSONObject(i)
								.getJSONObject("geometry");

						poiLoc.setLatitude(Double.parseDouble(geometry
								.getJSONObject("location").getString("lat")));
						poiLoc.setLongitude(Double.parseDouble(geometry
								.getJSONObject("location").getString("lng")));

						String poiName = placesResult.getJSONObject(i)
								.getString("name");

						PointOfInterest poi = setPOIExtraData(context,
								placesResult, i, poiLoc, poiName);

						// Fetch the URL for the icon representing this POI
						poi.setIconUrl(placesResult.getJSONObject(i).getString(
								"icon"));

						// Add POI to POI list
						fetchedPOIs.add(poi);
					}
				} else if (gPlacesStatus.equals(STAT_OVER_QUERY_LIMIT)) {
					// TODO Handle
				} else if (gPlacesStatus.equals(STAT_REQUEST_DENIED)) {
					// TODO Handle
					Log.e(TAG, "Request Denied " + gPlacesClient.getFullUrl()
							+ " is denied");
				} else if (gPlacesStatus.equals(STAT_ZERO_RESULTS)) {
					// TODO Handle
					Log.e(TAG, "Zero results " + gPlacesClient.getFullUrl()
							+ " has no results");
				} else if (gPlacesStatus.equals(STAT_INVALID_REQUEST)) {
					// TODO Handle my error
					Log.w(TAG, "URL request " + gPlacesClient.getFullUrl()
							+ " is malformed");
				}
			}
		} catch (ConnectTimeoutException ex) {
			throw ex;
		} catch (UnknownHostException ex) {
			throw ex;
		} catch (UnsupportedEncodingException ex) {
			Log.e(TAG, "Error while fetching places data from Google Places",
					ex);
		} catch (JSONException e) {
			Log.e(TAG, "Error while parsing JSON data from Google Places", e);
		} catch (Exception ex) {
			Log.e(TAG, "Error while fetching places data from Google Places",
					ex);
		}
		return fetchedPOIs;
	}
	
	public static PointOfInterest fetchPOIDetails(Context context,
			String reference) throws ConnectTimeoutException,
			UnknownHostException {
		PointOfInterest poi = new PointOfInterest(context, "");

		RestClient gPlacesClient = new RestClient(
				"https://maps.googleapis.com/maps/api/place/details/json");

		gPlacesClient.addParam("reference", reference);
		gPlacesClient.addParam("sensor", "true");
		gPlacesClient.addParam("key", API_KEY);

		try {
			gPlacesClient.execute(RequestMethod.GET);

			String response = "";
			JSONObject gPlacesResponse;

			if (gPlacesClient.getResponseCode() == 200) {
				response = gPlacesClient.getResponse();
			} else {
				// TODO Throw Exception
			}

			if (!(response == "")) {
				gPlacesResponse = new JSONObject(response);
				String gPlacesStatus = gPlacesResponse.getString("status");

				if (gPlacesStatus.equals(STAT_OK)) {
					JSONObject placeDetails = gPlacesResponse
							.getJSONObject("result");

					poi.setPlaceId(placeDetails.getString("id"));

					poi.setName(placeDetails.getString("name"));

					if (!placeDetails.isNull("vicinity")) {
						poi.setVicinity(placeDetails.getString("vicinity"));
					}
					if (!placeDetails.isNull("formatted_phone_number")) {
						poi.setPhoneNumber(placeDetails
								.getString("formatted_phone_number"));
					}
					if (!placeDetails.isNull("formatted_address")) {
						poi.setAddress(placeDetails
								.getString("formatted_address"));
					}

					JSONObject geometry = placeDetails
							.getJSONObject("geometry");

					Location poiLoc = new Location(
							LocationManager.PASSIVE_PROVIDER);

					poiLoc.setLatitude(geometry.getJSONObject("location")
							.getDouble("lat"));
					poiLoc.setLongitude(geometry.getJSONObject("location")
							.getDouble("lng"));

					if (!placeDetails.isNull("rating")) {
						poi.setRating((float) placeDetails.getDouble("rating"));
					}

					poi.setLocation(poiLoc);

					if (!placeDetails.isNull("types")) {
						JSONArray categories = placeDetails
								.getJSONArray("types");

						List<String> placeTypes = new ArrayList<String>(1);
						for (int i = 0; i < categories.length(); i++) {
							placeTypes.add(categories.getString(i));
						}
						poi.setPlaceTypes(placeTypes);
					}

					poi.setIconUrl(placeDetails.getString("icon"));

				} else if (gPlacesStatus.equals(STAT_OVER_QUERY_LIMIT)) {
					// TODO Handle
				} else if (gPlacesStatus.equals(STAT_REQUEST_DENIED)) {
					// TODO Handle
				} else if (gPlacesStatus.equals(STAT_ZERO_RESULTS)) {
					// TODO Handle
				} else if (gPlacesStatus.equals(STAT_INVALID_REQUEST)) {
					// TODO Handle my error
					Log.w(TAG, "URL request " + gPlacesClient.getFullUrl()
							+ " is malformed");
				}
			}
		} catch (UnknownHostException ex) {
			throw ex;
		} catch (ConnectTimeoutException ex) {
			throw ex;
		} catch (UnsupportedEncodingException ex) {
			Log.e(TAG, "Error while fetching places data from Google Places",
					ex);
		} catch (JSONException e) {
			Log.e(TAG, "Error while parsing JSON data from Google Places", e);
		} catch (Exception ex) {
			Log.e(TAG, "Error while fetching places data from Google Places",
					ex);
		}
		return poi;
	}

	public static ArrayList<PointOfInterest> fetchPOIRecommendations(
			Context context, Location location) throws ConnectTimeoutException,
			UnknownHostException {
		ArrayList<PointOfInterest> fetchedPOIs = new ArrayList<PointOfInterest>();

		String locationString = location.getLatitude() + ","
				+ location.getLongitude();

		String radiusString = location.getAccuracy() < 20 ? "1000" : "2000";

		RestClient gPlacesClient = new RestClient(
				"https://maps.googleapis.com/maps/api/place/search/json");
		gPlacesClient.addParam("location", locationString);
		gPlacesClient.addParam("radius", radiusString);
		gPlacesClient.addParam("sensor", "true");
		// gPlacesClient.addParam("types", POI_CATEGORIES);
		gPlacesClient.addParam("key", API_KEY);

		try {
			gPlacesClient.execute(RequestMethod.GET);

			String response = "";
			JSONObject gPlacesResponse;

			if (gPlacesClient.getResponseCode() == 200) {
				response = gPlacesClient.getResponse();
			} else {
				// TODO throw exception
			}

			if (!(response == "")) {
				gPlacesResponse = new JSONObject(response);
				String gPlacesStatus = gPlacesResponse.getString("status");

				if (gPlacesStatus.equals(STAT_OK)) {
					JSONArray placesResult = gPlacesResponse
							.getJSONArray("results");

					for (int i = 0; i < placesResult.length(); i++) {
						// Create a Location object to store a parsed lat and
						// long with
						Location poiLoc = new Location(
								LocationManager.PASSIVE_PROVIDER);

						// Get the geometry object from the JSON
						JSONObject geometry = placesResult.getJSONObject(i)
								.getJSONObject("geometry");

						poiLoc.setLatitude(Double.parseDouble(geometry
								.getJSONObject("location").getString("lat")));
						poiLoc.setLongitude(Double.parseDouble(geometry
								.getJSONObject("location").getString("lng")));

						String poiName = placesResult.getJSONObject(i)
								.getString("name");

						PointOfInterest poi = setPOIExtraData(context,
								placesResult, i, poiLoc, poiName);

						// Add POI to POI list
						fetchedPOIs.add(poi);
						
						// Fetch only five results
						if (i == 5) break;
					}
				} else if (gPlacesStatus.equals(STAT_OVER_QUERY_LIMIT)) {
					// TODO Handle
				} else if (gPlacesStatus.equals(STAT_REQUEST_DENIED)) {
					// TODO Handle
				} else if (gPlacesStatus.equals(STAT_ZERO_RESULTS)) {
					// TODO Handle
				} else if (gPlacesStatus.equals(STAT_INVALID_REQUEST)) {
					// TODO Handle my error
					Log.w(TAG, "URL request " + gPlacesClient.getFullUrl()
							+ " is malformed");
				}
			}
		} catch (ConnectTimeoutException ex) {
			throw ex;
		} catch (UnknownHostException ex) {
			throw ex;
		} catch (UnsupportedEncodingException ex) {
			Log.e(TAG, "Error while fetching places data from Google Places",
					ex);
		} catch (JSONException e) {
			Log.e(TAG, "Error while parsing JSON data from Google Places", e);
		} catch (Exception ex) {
			Log.e(TAG, "Error while fetching places data from Google Places",
					ex);
		}
		return fetchedPOIs;
	}

	public static String getDistanceBetween(Location loc1, Location loc2) {

		if (loc1 != null && loc2 != null) {
			float distance = loc1.distanceTo(loc2) / 1000;

			return distanceFormat.format(distance) + " km";
		}

		return "n/a";
	}

	/**
	 * @param context
	 * @param placesResult
	 * @param i
	 * @param poiLoc
	 * @param poiName
	 * @return
	 * @throws JSONException
	 */
	private static PointOfInterest setPOIExtraData(Context context,
			JSONArray placesResult, int i, Location poiLoc, String poiName)
			throws JSONException {
		PointOfInterest poi = new PointOfInterest(context,
				poiLoc, poiName);

		// Get a reference token for this POI
		poi.setReferenceToken(placesResult.getJSONObject(i)
				.getString("reference"));

		// Get a unique id for this POI
		poi.setPlaceId(placesResult.getJSONObject(i).getString(
				"id"));

		// If POI has vicinity, get it.
		// Not all POIs will have this.
		if (!placesResult.getJSONObject(i).isNull("vicinity")) {
			poi.setVicinity(placesResult.getJSONObject(i)
					.getString("vicinity"));
		}
		return poi;
	}
}
