package com.ugo.android.tourmate.ui;

import static com.ugo.android.tourmate.util.TouristConstants.POI_STATUS_ADDED;
import static com.ugo.android.tourmate.util.TouristConstants.POI_STATUS_VISITED;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.db.TourDataAdapter;
import com.ugo.android.tourmate.entities.PointOfInterest;
import com.ugo.android.tourmate.util.POIHelper;
import com.ugo.android.tourmate.util.media.ImageFactory;

public class POIArrayAdapter extends ArrayAdapter<PointOfInterest> {

	List<PointOfInterest> pois;
	/**
	 * Location from which all POIs are obtained from.
	 * Used to compute distance between this point
	 * and each POI
	 */
	private Location locationObtainedFrom;
	private TourDataAdapter dataAdapter;
	
	public POIArrayAdapter(Context context, int textViewResourceId,
			List<PointOfInterest> pois) {
		super(context, textViewResourceId, pois);
		this.pois = pois;
		dataAdapter = TourDataAdapter.open(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = 
				(LayoutInflater)this.getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.poi_item_row, null);
		}
		
		PointOfInterest poi = pois.get(position);
		if (poi != null) {
			// Get the name, vicinity and distance between
			((TextView) v.findViewById(R.id.poiName)).setText(poi.getName());
			((TextView) v.findViewById(R.id.poiDistance)).setText(
					POIHelper.getDistanceBetween(locationObtainedFrom, poi.getLocation()));
			((TextView) v.findViewById(R.id.poiVicinity)).setText(poi.getVicinity());
			// Get the image in a different thread after loading the list
			if (poi.getIconUrl() != null && poi.getIconUrl() != "") {
				ImageFactory.fetchDrawableUntoImageView(poi.getIconUrl(),
						((ImageView) v.findViewById(R.id.poiIcon)));
			}
			
			ImageView statusNotification = (ImageView) v.findViewById(R.id.statusNotification);
			int poiStatus = dataAdapter.checkPOIStatus(poi.getPlaceId());
			poi.setVisitStatus(poiStatus);
			switch (poiStatus) {
			case POI_STATUS_ADDED:
				statusNotification.setVisibility(View.VISIBLE);
				break;
			case POI_STATUS_VISITED:
				statusNotification.setVisibility(View.VISIBLE);
				statusNotification.setImageResource(R.drawable.star_visited);
				break;
			default:
				statusNotification.setVisibility(View.INVISIBLE);
			}
		}
		return v;
	}
	
	/**
	 * Sets the location from which all POIs are obtained from.
	 * Used to compute distance between this point
	 * and each POI.
	 * 
	 * @param loc Location from which all POIs are generated.
	 */
	public void setLocationObtainedFrom(Location loc) {
		this.locationObtainedFrom = loc;
	}
	
	

}
