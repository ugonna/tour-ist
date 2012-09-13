package com.ugo.android.tourmate.ui;

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.entities.PointOfInterest;
import com.ugo.android.tourmate.util.POIHelper;

public class RecommendationBarFragment extends Fragment {

	private static ViewFlipper flipper;
	private static boolean loaded;
	private ArrayList<PointOfInterest> pois;
	private static final int _FLIP_INTERVAL = 5000;
	private LocationListener locationListener;
	private POIRecommendationTask recommendationFetcherTask;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		recommendationFetcherTask = new POIRecommendationTask();
		
		return (ViewGroup) inflater.inflate(R.layout.fragment_recommend_bar,
				container);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!loaded) {
			setupRecommendationBar();
		}
	}

	private void setupRecommendationBar() {
		flipper = (ViewFlipper) getView().findViewById(
				R.id.recommendationFlipper);
		flipper.setInAnimation(AnimationUtils.loadAnimation(this.getActivity(),
				android.R.anim.slide_in_left));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(
				this.getActivity(), android.R.anim.slide_out_right));

		getCoarseLocation();
	}

	private void setupViewFlipper(Location currLocation) {
		if (pois == null || pois.isEmpty()) {
			// TODO Show the empty view
			return;
		}
		
		// Remove the loading view from the flipper
		flipper.removeViewAt(0);
		
		flipper.setFlipInterval(_FLIP_INTERVAL);
		//flipper.removeViewAt(0);
		
		if (getView() == null) {
			return;
		}
		// Setup first recommend view
		ViewGroup firstRecommendView = (ViewGroup) getView().findViewById(
				R.id.firstRecommend);
		if (firstRecommendView != null && pois.get(0) != null) {
			((TextView) firstRecommendView.findViewById(R.id.poiRecommendName))
					.setText(pois.get(0).getName());
			((TextView) firstRecommendView
					.findViewById(R.id.poiRecommendVicinity)).setText(pois.get(
					0).getVicinity());
			((TextView) firstRecommendView
					.findViewById(R.id.poiRecommendDistance))
					.setText(POIHelper.getDistanceBetween(currLocation, pois
							.get(0).getLocation()));
		}

		// Setup second recommend view
		ViewGroup secondRecommendView = (ViewGroup) getView().findViewById(
				R.id.secondRecommend);
		if (secondRecommendView != null && pois.get(1) != null) {
			((TextView) secondRecommendView.findViewById(R.id.poiRecommendName))
					.setText(pois.get(1).getName());
			((TextView) secondRecommendView
					.findViewById(R.id.poiRecommendVicinity)).setText(pois.get(
					1).getVicinity());
			((TextView) secondRecommendView
					.findViewById(R.id.poiRecommendDistance))
					.setText(POIHelper.getDistanceBetween(currLocation, pois
							.get(1).getLocation()));
		}

		// And so on...
		ViewGroup thirdRecommendView = (ViewGroup) getView().findViewById(
				R.id.thirdRecommend);
		if (firstRecommendView != null && pois.get(2) != null) {
			((TextView) thirdRecommendView.findViewById(R.id.poiRecommendName))
					.setText(pois.get(2).getName());
			((TextView) thirdRecommendView
					.findViewById(R.id.poiRecommendVicinity)).setText(pois.get(
					2).getVicinity());
			((TextView) thirdRecommendView
					.findViewById(R.id.poiRecommendDistance))
					.setText(POIHelper.getDistanceBetween(currLocation, pois
							.get(2).getLocation()));
		}

		// ...and so forth
		ViewGroup fourthRecommendView = (ViewGroup) getView().findViewById(
				R.id.fourthRecommend);
		if (firstRecommendView != null && pois.get(0) != null) {
			((TextView) fourthRecommendView.findViewById(R.id.poiRecommendName))
					.setText(pois.get(3).getName());
			((TextView) fourthRecommendView
					.findViewById(R.id.poiRecommendVicinity)).setText(pois.get(
					3).getVicinity());
			((TextView) fourthRecommendView
					.findViewById(R.id.poiRecommendDistance))
					.setText(POIHelper.getDistanceBetween(currLocation, pois
							.get(3).getLocation()));
		}

		// We get it by now
		ViewGroup fifthRecommendView = (ViewGroup) getView().findViewById(
				R.id.fifthRecommend);
		if (firstRecommendView != null && pois.get(0) != null) {
			((TextView) fifthRecommendView.findViewById(R.id.poiRecommendName))
					.setText(pois.get(4).getName());
			((TextView) fifthRecommendView
					.findViewById(R.id.poiRecommendVicinity)).setText(pois.get(
					4).getVicinity());
			((TextView) fifthRecommendView
					.findViewById(R.id.poiRecommendDistance))
					.setText(POIHelper.getDistanceBetween(currLocation, pois
							.get(4).getLocation()));
		}

		flipper.startFlipping();
	}

	public static void moveFlipperNext() {
		if (flipper != null && loaded && flipper.isFlipping()) {
			flipper.stopFlipping();
			flipper.showNext();
			flipper.startFlipping();
		}
	}

	/*public static void moveFlipperPrevious() {
		if (flipper != null && loaded && flipper.isFlipping()) {
			flipper.stopFlipping();
			
			flipper.showPrevious();
			
			flipper.startFlipping();
		}
	}*/

	private void getCoarseLocation() {
		// Acquire a reference to the system Location Manager
		final LocationManager locationManager = (LocationManager) getActivity()
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				if (recommendationFetcherTask.getStatus() == AsyncTask.Status.FINISHED) {
					// Restart the damn thing
					recommendationFetcherTask = new POIRecommendationTask();
				}
				if (recommendationFetcherTask.getStatus() == AsyncTask.Status.PENDING) {
					recommendationFetcherTask.execute(location);
				}

				locationManager.removeUpdates(locationListener);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	}

	class POIRecommendationTask extends AsyncTask<Location, Location, Location> {

		final int CONN_TIMEOUT_EXCEPTION = 1;
		final int UNKNOWN_HOST_EXCEPTION = 2;
		int errorCode = 0;

		private void checkError() {
			// TODO Flip to empty view

			switch (errorCode) {
			case CONN_TIMEOUT_EXCEPTION:
				Toast.makeText(getActivity(), R.string.err_msg_connect_timeout,
						Toast.LENGTH_SHORT).show();
				break;
			case UNKNOWN_HOST_EXCEPTION:
				Toast.makeText(getActivity(), R.string.err_msg_cannot_connect,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

		@Override
		protected Location doInBackground(Location... params) {
			if (pois != null) {
				pois.clear();
				pois = null;
			}

			try {
				pois = POIHelper
						.fetchPOIRecommendations(
								RecommendationBarFragment.this.getActivity(),
								params[0]);
			} catch (UnknownHostException ex) {
				errorCode = UNKNOWN_HOST_EXCEPTION;
				cancel(true);
				return null;
			} catch (ConnectTimeoutException ex) {
				errorCode = CONN_TIMEOUT_EXCEPTION;
				cancel(true);
				return null;
			}

			return params[0];
		}

		@Override
		protected void onPostExecute(Location currLocation) {
			super.onPostExecute(currLocation);

			if (errorCode != 0) {
				checkError();
				return;
			}
			
			loaded = true;
			setupViewFlipper(currLocation);
		}

	}
}