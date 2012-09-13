package com.ugo.android.tourmate.ui;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.entities.ParcelablePOI;
import com.ugo.android.tourmate.ui.widget.POIItemizedOverlay;
import com.ugo.android.tourmate.util.GAnalyticsUtil;
import com.ugo.android.tourmate.util.TouristConstants;

public class POIMapActivity extends MapActivity {

	private static final int E6 = 1000000;
	MyLocationOverlay locationOverlay;
	MapController mController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.poi_map_view);

		ArrayList<ParcelablePOI> pois = null;

		pois = getIntent().getParcelableArrayListExtra(
				TouristConstants.EXTRA_POI_DETAILS_ARR);

		MapView mapView = (MapView) findViewById(R.id.poiMapView);
		mapView.setBuiltInZoomControls(true);

		List<Overlay> mapOverlays = mapView.getOverlays();
		mController = mapView.getController();

		Drawable defaultMarkerdrawable = this.getResources().getDrawable(
				R.drawable.map_marker_poi);

		POIItemizedOverlay itemizedOverLay = new POIItemizedOverlay(
				defaultMarkerdrawable, getApplicationContext());
		itemizedOverLay.setOverlays(getOverLayListFromPOI(pois));
		
		Location currLocation = (Location) getIntent()
		.getParcelableExtra(TouristConstants.EXTRA_CURR_LOCATION);
		GeoPoint currGeoPoint = convertLocationToGeoPoint(currLocation);
		
		mapOverlays.add(itemizedOverLay);
		
		locationOverlay = new MyLocationOverlay(getApplicationContext(), mapView);
		
		//mapOverlays.add(new CurrentPositionOverlay(getApplicationContext(), currGeoPoint, currLocation.getAccuracy()));
		mapOverlays.add(locationOverlay);
		
		// Center on current location
		mController.animateTo(currGeoPoint);
		mController.setZoom(15);
		
		GAnalyticsUtil.getInstance(this).trackPageView("/MapMode");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		locationOverlay.enableMyLocation();
		locationOverlay.enableCompass();
	}
	@Override
	protected void onPause() {
		super.onPause();
		locationOverlay.disableMyLocation();
		locationOverlay.disableCompass();
	}
	
	private ArrayList<OverlayItem> getOverLayListFromPOI(
			ArrayList<ParcelablePOI> pois) {
		ArrayList<OverlayItem> overLays = new ArrayList<OverlayItem>();

		for (ParcelablePOI poi : pois) {

			overLays.add(new OverlayItem(convertLocationToGeoPoint(poi
					.getLocation()), poi.getName(), poi.getVicinity()));
		}

		return overLays;
	}

	private GeoPoint convertLocationToGeoPoint(Location location) {
		return new GeoPoint((int) (location.getLatitude() * E6),
				(int) (location.getLongitude() * E6));
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
