package com.ugo.android.tourmate.entities;

import java.util.regex.Pattern;

import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelablePOI implements Parcelable {
	
	private String name;
	private String vicinity;
	private String iconUrl;
	private Location location;
	
	public static final Parcelable.Creator<ParcelablePOI> CREATOR = new Parcelable.Creator<ParcelablePOI>() {
		public ParcelablePOI createFromParcel(Parcel in) {
			return new ParcelablePOI(in);
		}

		public ParcelablePOI[] newArray(int size) {
			return new ParcelablePOI[size];
		}
	};
	
	public ParcelablePOI() { }
	
	public ParcelablePOI(Parcel in) {
		String[] data = new String[3];
		in.readStringArray(data);
		this.setName((String) data[0]);
		this.setVicinity((String) data[1]);
		this.setIconUrl((String) data[2]);
		setLocation((Location) in.readParcelable(Location.class
				.getClassLoader()));
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(new String[] { this.getName(), this.getVicinity(),
				this.getIconUrl() });
		dest.writeParcelable(this.getLocation(), flags);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param vicinity the vicinity to set
	 */
	public void setVicinity(String vicinity) {
		this.vicinity = vicinity;
	}

	/**
	 * @return the vicinity
	 */
	public String getVicinity() {
		return vicinity;
	}

	/**
	 * @param iconUrl the iconUrl to set
	 */
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	/**
	 * @return the iconUrl
	 */
	public String getIconUrl() {
		return iconUrl;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}
	
	public void setLocationFromString(String loc) {
		String[] locationArr = loc.split(Pattern.quote(","));
		Location location = new Location(LocationManager.PASSIVE_PROVIDER);
		location.setLatitude(Integer.parseInt(locationArr[0]));
		location.setLongitude(Integer.parseInt(locationArr[1]));
		this.setLocation(location);
	}
}