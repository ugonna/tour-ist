package com.ugo.android.tourmate.ui;

import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.db.TourDatabaseHelper;
import com.ugo.android.tourmate.util.POIHelper;
import com.ugo.android.tourmate.util.media.ImageFactory;

public class POICursorAdapter extends CursorAdapter {

	/**
	 * Location from which all POIs are obtained from. Used to compute distance
	 * between this point and each POI
	 */
	private Location locationObtainedFrom;
	/**
	 * Boolean that indicates if the list to be shown is from the added but not
	 * visited or from the visited table. <code>true</code> for former.
	 */
	private boolean visitedList;

	public POICursorAdapter(Context context, Cursor c, int flags,
			boolean visitedList) {
		super(context, c, flags);
		this.visitedList = visitedList;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Set the name of the POI
		((TextView) view.findViewById(R.id.poiName)).setText(cursor
				.getString(cursor
						.getColumnIndex(TourDatabaseHelper.POI_NAME_COLUMN)));
		Location poiLoc = new Location(LocationManager.PASSIVE_PROVIDER);
		// Split the stored location string at the comma.
		// The first String in the array is the latitude.
		String[] location = cursor.getString(
				cursor.getColumnIndex(TourDatabaseHelper.POI_LOCATION_COLUMN))
				.split(Pattern.quote(","));
		poiLoc.setLatitude(Double.parseDouble(location[0]));
		poiLoc.setLongitude(Double.parseDouble(location[1]));
		// Set the distance from current location up to POI
		((TextView) view.findViewById(R.id.poiDistance)).setText(POIHelper
				.getDistanceBetween(locationObtainedFrom, poiLoc));
		((TextView) view.findViewById(R.id.poiVicinity))
				.setText(cursor.getString(cursor
						.getColumnIndex(TourDatabaseHelper.POI_VICINITY_COLUMN)));
		// Set the image unto the ImageView on a background thread
		String iconUrl = cursor.getString(cursor
				.getColumnIndex(TourDatabaseHelper.POI_ICON_URL_COLUMN));
		if (iconUrl != null && iconUrl != "") {
			ImageFactory.fetchDrawableUntoImageView(iconUrl,
					((ImageView) view.findViewById(R.id.poiIcon)));
		}

		ImageView statusNotification = (ImageView) view
				.findViewById(R.id.statusNotification);
		// Make the statusNotification visible. It has a default
		statusNotification.setVisibility(View.VISIBLE);

		if (this.visitedList) {
			statusNotification.setImageResource(R.drawable.star_visited);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.poi_item_row, parent, false);
		bindView(v, context, cursor);
		return v;
	}

	/**
	 * Sets the location from which all POIs are obtained from. Used to compute
	 * distance between this point and each POI.
	 * 
	 * @param loc
	 *            Location from which all POIs are generated.
	 */
	public void setLocationObtainedFrom(Location loc) {
		this.locationObtainedFrom = loc;
	}

}
