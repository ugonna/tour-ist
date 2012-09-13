package com.ugo.android.tourmate.entities;

import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;

import com.ugo.android.tourmate.ui.widget.ARPointView;

public class PointOfInterest extends ARPointView {

	private String name;
	private String vicinity;
	private String iconUrl;
	private String phoneNumber;
	private String address;
	private float rating;
	private List<String> placeTypes;
	private String placeTypeString;
	private String referenceToken;
	private String placeId;
	private int visitStatus;

	public PointOfInterest(Context context, Location location, String name) {
		super(context, location);
		this.name = name;
	}

	public PointOfInterest(Context context, String name) {
		super(context);
		this.name = name;
		placeTypeString = "";
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return the iconUrl
	 */
	public String getIconUrl() {
		return iconUrl;
	}

	/**
	 * Gets the name of the landmark
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the phoneNumber
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * @return the placeId
	 */
	public String getPlaceId() {
		return placeId;
	}

	public String getPlaceTypeCommaSeparated() {
		if (placeTypeString.equals("")) {
			int counter = 0;
			for (String place : placeTypes) {
				if (counter < placeTypes.size()) {
					placeTypeString += place + ", ";
				} else {
					placeTypeString += place;
				}
			}
		}
		return placeTypeString;
	}

	/**
	 * @return the placeTypes
	 */
	public List<String> getPlaceTypes() {
		return placeTypes;
	}

	/**
	 * @return the rating
	 */
	public float getRating() {
		return rating;
	}

	/**
	 * @return the reference
	 */
	public String getReferenceToken() {
		return referenceToken;
	}

	/**
	 * @return the vicinity
	 */
	public String getVicinity() {
		return vicinity;
	}

	public void setLocationFromString(String loc) {
		String[] locationArr = loc.split(Pattern.quote(","));
		Location location = new Location(LocationManager.PASSIVE_PROVIDER);
		location.setLatitude(Integer.parseInt(locationArr[0]));
		location.setLongitude(Integer.parseInt(locationArr[1]));
		this.setLocation(location);
	}
	/**
	 * @return the visitStatus
	 */
	public int getVisitStatus() {
		return visitStatus;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @param iconUrl
	 *            the iconUrl to set
	 */
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	/**
	 * Sets a name for the landmark
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param phoneNumber
	 *            the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @param placeId
	 *            the placeId to set
	 */
	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	/**
	 * @param placeTypes
	 *            the placeTypes to set
	 */
	public void setPlaceTypes(List<String> placeTypes) {
		this.placeTypes = placeTypes;
	}

	/**
	 * @param rating
	 *            the rating to set
	 */
	public void setRating(float rating) {
		this.rating = rating;
	}

	/**
	 * @param reference
	 *            the reference to set
	 */
	public void setReferenceToken(String reference) {
		this.referenceToken = reference;
	}

	/**
	 * @param vicinity
	 *            the vicinity to set
	 */
	public void setVicinity(String vicinity) {
		this.vicinity = vicinity;
	}

	/**
	 * @param visitStatus
	 *            the visitStatus to set
	 */
	public void setVisitStatus(int visitStatus) {
		this.visitStatus = visitStatus;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Draw the text 5 pixels to the right side of the point
		if (this.name != null) {
			canvas.drawText(name, getLeft() + (POINT_RADIUS * 2) + 5, getTop(),
					pointPaint);
			canvas.drawText(String.valueOf(getDistance()), getLeft()
					+ (POINT_RADIUS * 2) + 5, getTop() + 10, pointPaint);
		}
	}

}
