package com.ugo.android.tourmate.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.entities.ParcelablePOI;
import com.ugo.android.tourmate.entities.PointOfInterest;
import com.ugo.android.tourmate.ui.widget.ARSensorView;
import com.ugo.android.tourmate.ui.widget.CameraView;
import com.ugo.android.tourmate.util.GAnalyticsUtil;
import com.ugo.android.tourmate.util.TouristConstants;

public class ARModeActivity extends Activity {

	private CameraView cameraView;
	private ARSensorView arView;

	/*
	 * private void loadDebugLocation(Context context) { Location loc = new
	 * Location(LocationManager.GPS_PROVIDER); loc.setLatitude(9.040685892);
	 * loc.setLongitude(7.767666864); loc.setAltitude(3.1); PointOfInterest
	 * landmark = new PointOfInterest(context, loc, "Conference center");
	 * arView.addARView(landmark); }
	 */

	private void loadPoisUntoScreen(ArrayList<ParcelablePOI> pois) {

		for (ParcelablePOI parcelablePoi : pois) {
			// Sets a "fake" altitude just so it can appear above
			// the horizon
			parcelablePoi.getLocation().setAltitude(3);
			arView.addARView(new PointOfInterest(getApplicationContext(),
					parcelablePoi.getLocation(), parcelablePoi.getName()));
		}
	}

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create our Preview view and set it as the content of our activity.
		FrameLayout fLayout = new FrameLayout(this);
		cameraView = new CameraView(this);
		// Start the sensor view with the current location
		arView = new ARSensorView(this, (Location) getIntent()
				.getParcelableExtra(TouristConstants.EXTRA_CURR_LOCATION));
		Display display = getWindowManager().getDefaultDisplay();
		arView.setScreenWidth(display.getWidth());
		arView.setScreenHeight(display.getHeight());
		// Get the static view for the Explore AR mode
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View staticARView = li.inflate(R.layout.ar_static_view, null);
		staticARView.findViewById(R.id.goBackButton).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});

		fLayout.addView(cameraView, display.getWidth(), display.getHeight());
		fLayout.addView(arView, display.getWidth(), display.getHeight());
		fLayout.addView(staticARView);

		setContentView(fLayout);

		ArrayList<ParcelablePOI> pois = null;

		pois = getIntent().getParcelableArrayListExtra(
				TouristConstants.EXTRA_POI_DETAILS_ARR);

		// loadDebugLocation(this);
		loadPoisUntoScreen(pois);
		
		GAnalyticsUtil.getInstance(this).trackPageView("/AugmentedRealityMode");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		arView.finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		arView.finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		arView.start();
	}
}
