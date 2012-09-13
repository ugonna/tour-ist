package com.ugo.android.tourmate.ui;

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.http.conn.ConnectTimeoutException;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.db.TourDatabaseHelper;
import com.ugo.android.tourmate.entities.ParcelablePOI;
import com.ugo.android.tourmate.entities.PointOfInterest;
import com.ugo.android.tourmate.providers.TourDataProvider;
import com.ugo.android.tourmate.ui.phone.POIDetailActivity;
import com.ugo.android.tourmate.ui.widget.POIHeaderTextViewFactory;
import com.ugo.android.tourmate.util.GAnalyticsUtil;
import com.ugo.android.tourmate.util.POIHelper;
import com.ugo.android.tourmate.util.TouristConstants;

// Currently loads POI from the internet (Explore mode) using
// an AsyncTask, data from the local database (Visited or Intended mode)
// is loaded using a Loader<Cursor>.
// TODO Release resources when this activity stops
/**
 * List fragment that can display multiple lists.
 */
public class POIListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final int POI_LOADER_ID = 0x01;
	/**
	 * {@linkplain android.widget.ListAdapter} for showing in a list POIs
	 * obtained from the internet.
	 */
	private POIArrayAdapter poiArrayAdapter;
	/**
	 * {@linkplain android.widget.ListAdapter} for showing in a list POIs
	 * obtained from the database.
	 */
	private POICursorAdapter poiCursorAdapter;
	private POIFetcherTask poiFetcherASync;

	private volatile ArrayList<PointOfInterest> pois;

	private LocationManager locationMan;
	private Location currLocation;

	// TODO Reduce to fix
	private static final int BASE_FINE_ACCURACY = 10;
	private static final int BASE_COARSE_ACCURACY = 4500;

	private String listMode;

	private View headerView;
	private View footerView;
	// Button that allows user to enter AR mode on click.
	// Disabled by default, until a fine location is obtained.
	MenuItem arModeMenuButton;
	MenuItem mapModeMenuButton;

	// Needed for information on what's up.
	// Obtained from the list header after inflation
	// in onCreateView()
	private TextSwitcher infoTextSwitcher;

	// We need a handle to the location listener so as to be
	// able to remove or re-attach them later
	private LocationListener lowProvider;
	private LocationListener highProvider;

	/**
	 * Boolean representing if we're sticking with coarse or fine location
	 * provider. Default is <code>true</code>
	 */
	private boolean usingFineProvider = true;

	public void checkForFineLocationProvider() {
		if (!locationMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			this.getActivity().showDialog(
					TouristConstants.DIALOG_NO_FINE_PROVIDER);
		} else {
			requestFineLocationUpdates(locationMan.getProvider(locationMan
					.getBestProvider(createFineCriteria(), false)));
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		if (headerView != null)
			this.getListView().addHeaderView(headerView, null, false);

		if (listMode != null
				&& listMode.equals(TouristConstants.LIST_MODE_EXPLORE)) {

			if (footerView != null)
				this.getListView().addFooterView(footerView, null, false);

			this.setListAdapter(poiArrayAdapter);
		} else if (listMode != null) {
			if (listMode.equals(TouristConstants.LIST_MODE_INTENDED)) {

				((TextView) getListView().getEmptyView().findViewById(
						R.id.progText))
						.setText(R.string.poi_list_fetching_items_intended);

				this.getActivity().setTitle(R.string.intended_view_name);

				this.setListAdapter(poiCursorAdapter);
			} else if (listMode.equals(TouristConstants.LIST_MODE_VISITED)) {

				((TextView) getListView().getEmptyView().findViewById(
						R.id.progText))
						.setText(R.string.poi_list_fetching_items_visited);

				this.getActivity().setTitle(R.string.visited_view_name);

				this.setListAdapter(poiCursorAdapter);
			}

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = BaseActivity
				.fragmentArgumentsToIntent(getArguments());

		listMode = intent.getStringExtra(TouristConstants.EXTRA_LIST_MODE);

		if (listMode != null
				&& listMode.equals(TouristConstants.LIST_MODE_EXPLORE)) {

			setHasOptionsMenu(true);

			// Send an empty list so we can get the list from GPlaces
			this.poiArrayAdapter = new POIArrayAdapter(this.getActivity(),
					R.layout.poi_item_row,
					pois = new ArrayList<PointOfInterest>(0));

			// Initialize the POIFetcherTask with this context
			poiFetcherASync = new POIFetcherTask();
			
			GAnalyticsUtil.getInstance(getActivity()).trackPageView("/ExploreMode");

		} else if (listMode != null
				&& (listMode.equals(TouristConstants.LIST_MODE_INTENDED) || listMode
						.equals(TouristConstants.LIST_MODE_VISITED))) {

			setHasOptionsMenu(true);

			boolean isVisitedList = listMode
					.equals(TouristConstants.LIST_MODE_VISITED) ? true : false;

			// Initialize a Loader with an ID of 0x01
			this.getLoaderManager().initLoader(POI_LOADER_ID,
					savedInstanceState, this);

			// Pass a null cursor and load the data with a Loader
			this.poiCursorAdapter = new POICursorAdapter(this.getActivity(),
					null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER,
					isVisitedList);
			
			GAnalyticsUtil.getInstance(getActivity()).trackPageView("/VisitMode");
		}
		initializeLocationListeners();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.poi_list_menu_items, menu);
		arModeMenuButton = menu.findItem(R.id.menu_ar);
		mapModeMenuButton = menu.findItem(R.id.menu_map);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View list_root = inflater.inflate(R.layout.fragment_poi_list, null);
		// Get the list header - to be added later in the life-cycle
		// during onActivityCreated()
		headerView = inflater.inflate(R.layout.poi_list_header, null);

		// Get a handle to the TextSwitcher
		infoTextSwitcher = (TextSwitcher) headerView
				.findViewById(R.id.info_header_txt);
		// Set where it will get it's text view from
		// which is in POIHeaderTextView, using makeView()
		infoTextSwitcher.setFactory(new POIHeaderTextViewFactory(this
				.getActivity()));

		infoTextSwitcher.setCurrentText("");

		// If we're trying to obtain a fine location
		// show the blinking "fine tuning" text

		if (usingFineProvider) {
			infoTextSwitcher.setText(POIListFragment.this.getResources()
					.getText(R.string.poi_list_header_fine_tune));
			Animation blinkAnimation = AnimationUtils.loadAnimation(
					getActivity(), android.R.anim.fade_in);

			blinkAnimation.setRepeatMode(Animation.REVERSE);
			blinkAnimation.setRepeatCount(Animation.INFINITE);
			infoTextSwitcher.setAnimation(blinkAnimation);
		} else {
			// If it has been decided that we cannot get the fine location
			// tell the user.
			infoTextSwitcher.setText(this
					.getText(R.string.poi_list_header_using_coarse));
		}

		// Set the action for the AR mode button
		// Same with list footer (Powered by Google logo)
		footerView = inflater.inflate(R.layout.powered_by_google_footer, null);

		return list_root;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// super.onListItemClick(l, v, position, id);
		if (listMode != null
				&& listMode.equals(TouristConstants.LIST_MODE_EXPLORE)) {

			PointOfInterest poi = poiArrayAdapter.getItem(position - 1);

			final Intent intent = new Intent(getActivity(),
					POIDetailActivity.class);
			intent.putExtra(TouristConstants.EXTRA_POI_REFERENCE,
					poi.getReferenceToken());
			intent.putExtra(TouristConstants.EXTRA_POI_ID, poi.getPlaceId());
			intent.putExtra(TouristConstants.EXTRA_POI_STATUS,
					poi.getVisitStatus());
			intent.putExtra(TouristConstants.EXTRA_CURR_LOCATION, currLocation);

			((BaseActivity) getActivity()).openActivityOrFragment(intent);
		}

		getListView().setItemChecked(position, true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.menu_ar:
			if (item.isEnabled()) {
				intent = new Intent(getActivity(), ARModeActivity.class);

				// Fetch the POI data from the cursor (if non-empty) and
				// put into the list
				ArrayList<ParcelablePOI> parcelablePoi = fetchPoisIntoParcelableList(5);
				if (parcelablePoi == null) {
					return false;
				}

				intent.putParcelableArrayListExtra(
						TouristConstants.EXTRA_POI_DETAILS_ARR, parcelablePoi);
				intent.putExtra(TouristConstants.EXTRA_CURR_LOCATION,
						currLocation);
				startActivity(intent);
			} else {
				Toast.makeText(getActivity(), R.string.toast_no_ar_disabled,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.menu_map:
			if (item.isEnabled()) {
				intent = new Intent(getActivity(), POIMapActivity.class);

				// Fetch the POI data from the cursor (if non-empty) and
				// put into the list
				ArrayList<ParcelablePOI> parcelablePoi = fetchPoisIntoParcelableList(-1);
				if (parcelablePoi == null) {
					return false;
				}

				intent.putParcelableArrayListExtra(
						TouristConstants.EXTRA_POI_DETAILS_ARR, parcelablePoi);
				intent.putExtra(TouristConstants.EXTRA_CURR_LOCATION,
						currLocation);
				startActivity(intent);
			} else {
				Toast.makeText(getActivity(), R.string.toast_no_map_disabled,
						Toast.LENGTH_SHORT).show();
			}
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		super.onPause();

		stopLocationListeners();
	}

	/**
	 * 
	 */
	private void stopLocationListeners() {
		if (lowProvider != null) {
			locationMan.removeUpdates(lowProvider);
		}
		if (highProvider != null) {
			locationMan.removeUpdates(highProvider);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (listMode != null
				&& listMode.equals(TouristConstants.LIST_MODE_EXPLORE)) {
			if (poiArrayAdapter != null) {
				poiArrayAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		final Uri CONTENT_URI = listMode
				.equals(TouristConstants.LIST_MODE_INTENDED) ? TourDataProvider.INTENDED_CONTENT_URI
				: TourDataProvider.VISITED_CONTENT_URI;

		// If querying INTENDED table, fetch all columns, else (because the
		// other query does a join) fetch specified columns
		final String[] projection = listMode
				.equals(TouristConstants.LIST_MODE_INTENDED) ? null
				: new String[] {
						TourDatabaseHelper.POI_VISITED_TBL_NAME + "."
								+ TourDatabaseHelper.POI_ID_COLUMN,
						TourDatabaseHelper.POI_NAME_COLUMN,
						TourDatabaseHelper.POI_ADDRESS_COLUMN,
						TourDatabaseHelper.POI_PHONE_COLUMN,
						TourDatabaseHelper.POI_LOCATION_COLUMN,
						TourDatabaseHelper.POI_VICINITY_COLUMN,
						TourDatabaseHelper.POI_ICON_URL_COLUMN,
						TourDatabaseHelper.POI_TYPES_COLUMN };

		return new CursorLoader(getActivity(), CONTENT_URI, projection, null,
				null, null);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		poiCursorAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		if (cursor == null || !cursor.moveToFirst()) {
			// Show the empty indicator
			getListView().getEmptyView().findViewById(R.id.progBar)
					.setVisibility(View.GONE);
			if (listMode != null
					&& listMode == TouristConstants.LIST_MODE_INTENDED) {
				((TextView) getListView().getEmptyView().findViewById(
						R.id.progText))
						.setText(R.string.poi_list_no_marked_places);
			} else if (listMode != null
					&& listMode == TouristConstants.LIST_MODE_VISITED) {
				((TextView) getListView().getEmptyView().findViewById(
						R.id.progText))
						.setText(R.string.poi_list_no_visited_places);
			} else {
				((TextView) getListView().getEmptyView().findViewById(
						R.id.progText)).setText(R.string.poi_list_no_items);
			}
			stopLocationListeners();
		} else {
			poiCursorAdapter.swapCursor(cursor);
		}
	}

	private Criteria createCoarseCriteria() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		return criteria;
	}

	private Criteria createFineCriteria() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		return criteria;
	}

	private void initializeLocationListeners() {

		locationMan = (LocationManager) this.getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		// If last known GPS obtained location is valid, use it instead.
		boolean isPrevLocationValid = isLocationValid(locationMan
				.getLastKnownLocation(LocationManager.GPS_PROVIDER));
		if (isPrevLocationValid) {
			currLocation = locationMan
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			useNewLocation();
			return;
		}

		final LocationProvider locProvLow = locationMan.getProvider(locationMan
				.getBestProvider(createCoarseCriteria(), false));

		final LocationProvider locProvHigh = locationMan
				.getProvider(locationMan.getBestProvider(createFineCriteria(),
						true));

		// A coarse location provider will always be available.
		// Just check if it is enabled.
		if (!locationMan.isProviderEnabled(locProvLow.getName())) {
			// If Coarse location provider is not enabled notify the
			// user but still go on to use fine location provider.
			Toast.makeText(getActivity(), R.string.toast_no_coarse_provider,
					Toast.LENGTH_SHORT).show();
		} else {

			requestCoarseLocationUpdates(locProvLow);
		}

		if (locProvHigh == null) {
			// TODO Display message saying cannot get fine location.
			// then return

		} else if (locProvHigh.getName().equals(locProvLow.getName())) {
			// This means it couldn't get a different, finer location
			// provider so it fell back on a less accurate one.
			// Maybe a fine location provider is available but not enabled,
			// prompt the user to enable it.
			this.getActivity().showDialog(
					TouristConstants.DIALOG_NO_FINE_PROVIDER);
			// Carry on with network location's list, if any

			// onCreateView has not been called at this point.
			// Set the flag that we will stick with coarse
			// location.
			usingFineProvider = false;

		} else {
			requestFineLocationUpdates(locProvHigh);
		}
	}

	private boolean isLocationValid(Location location) {
		if (location == null) {
			return false;
		}

		// Test the location accuracy
		if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
			if (location.getAccuracy() > BASE_FINE_ACCURACY) {
				return false;
			}
		} else {
			// It has to be coarse location then
			if (location.getAccuracy() > BASE_COARSE_ACCURACY) {
				return false;
			}
		}

		// If the new location hasn't changed much, disregard it.
		if (currLocation != null
				&& currLocation.getLatitude() == location.getLatitude()
				&& currLocation.getLongitude() == location.getLongitude()) {
			return false;
		}

		// Test if the new location is older than a minute.
		if ((System.currentTimeMillis() - location.getTime()) > 60000) {
			return false;
		}

		return true;
	}

	/**
	 * @param locProvLow
	 */
	private void requestCoarseLocationUpdates(final LocationProvider locProvLow) {
		lowProvider = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (isLocationValid(location)) {
					currLocation = location;

					useNewLocation();

					// Stop listening for low criteria location updates
					if (lowProvider != null) {
						locationMan.removeUpdates(lowProvider);
					}
				}
			}

			@Override
			public void onProviderDisabled(String provider) {
				Toast.makeText(getActivity(),
						R.string.toast_no_coarse_provider, Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};

		locationMan.requestLocationUpdates(locProvLow.getName(), 60000, 1000,
				lowProvider);
	}

	/**
	 * @param locProvHigh
	 */
	private void requestFineLocationUpdates(final LocationProvider locProvHigh) {
		highProvider = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (isLocationValid(location)) {
					currLocation = location;

					useNewLocation();

					// Stop listening for high criteria location updates
					locationMan.removeUpdates(highProvider);
				} else {
					// Detach the location listener so we can re-attach it
					// again.
					// This will prevent it from going stale
					locationMan.removeUpdates(highProvider);
					locationMan.requestLocationUpdates(locProvHigh.getName(),
							60000, 1000, highProvider);
				}
			}

			@Override
			public void onProviderDisabled(String provider) {
				POIListFragment.this.getActivity().showDialog(
						TouristConstants.DIALOG_NO_FINE_PROVIDER);
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};

		locationMan.requestLocationUpdates(locProvHigh.getName(), 60000, 1000,
				highProvider);
	}

	/**
	 * 
	 */
	private void useNewLocation() {
		if (listMode != null
				&& listMode.equals(TouristConstants.LIST_MODE_EXPLORE)) {
			// Passes this location to the ListAdapter so it
			// can compute and display the distance between
			// this location and each POI
			poiArrayAdapter.setLocationObtainedFrom(currLocation);

			poiFetcherASync.cancel(true);
			if (poiFetcherASync.isCancelled()) {
				poiFetcherASync = new POIFetcherTask();
				poiFetcherASync.execute();
			}
			if (currLocation.getProvider().equals(LocationManager.GPS_PROVIDER)) {
				usingFineProvider = true;

				// Turn off the blinking animation
				if (infoTextSwitcher.getAnimation() != null) {
					infoTextSwitcher.getAnimation().setRepeatCount(0);
					infoTextSwitcher.getAnimation().cancel();
					infoTextSwitcher.setAnimation(null);
				}

				infoTextSwitcher.setText(POIListFragment.this.getResources()
						.getText(R.string.poi_list_header_obtained));
			}
		} else if (listMode != null
				&& (listMode.equals(TouristConstants.LIST_MODE_INTENDED) || listMode
						.equals(TouristConstants.LIST_MODE_VISITED))) {
			// Passes this location to the ListAdapter so it
			// can compute and display the distance between
			// this location and each POI
			poiCursorAdapter.setLocationObtainedFrom(currLocation);
			// Reload the Cursor to reflect the distances
			this.getLoaderManager().restartLoader(POI_LOADER_ID, null, this);
		}
	}

	/**
	 * Fetches the POIs into a parcelable list
	 * 
	 * @param limit
	 *            the number of ParcelabelPOIs to fetch. Use -1 for no limit
	 * @return
	 */
	private ArrayList<ParcelablePOI> fetchPoisIntoParcelableList(int limit) {
		if (listMode != null
				&& listMode.equals(TouristConstants.LIST_MODE_EXPLORE)) {
			if (pois != null) {
				ArrayList<ParcelablePOI> parcelablePois = new ArrayList<ParcelablePOI>();

				int count = 0;
				for (PointOfInterest poi : pois) {
					ParcelablePOI parcelablePoi = new ParcelablePOI();
					parcelablePoi.setName(poi.getName());
					parcelablePoi.setVicinity(poi.getVicinity());
					parcelablePoi.setIconUrl(poi.getIconUrl());
					parcelablePoi.setLocation(poi.getLocation());

					parcelablePois.add(parcelablePoi);

					// Fetch only ten POIs
					if (limit > 0 && ++count >= 9) {
						break;
					}
				}

				return parcelablePois;
			}
		} else if (listMode != null
				&& (listMode.equals(TouristConstants.LIST_MODE_INTENDED) || listMode
						.equals(TouristConstants.LIST_MODE_VISITED))) {

			if (poiCursorAdapter.getCursor() != null
					&& poiCursorAdapter.getCursor().moveToFirst()) {

				ArrayList<ParcelablePOI> parcelablePois = new ArrayList<ParcelablePOI>();

				final int nameColumnIndex = poiCursorAdapter.getCursor()
						.getColumnIndex(TourDatabaseHelper.POI_NAME_COLUMN);

				final int vicinityColumnIndex = poiCursorAdapter.getCursor()
						.getColumnIndex(TourDatabaseHelper.POI_VICINITY_COLUMN);

				final int iconUrlColumnIndex = poiCursorAdapter.getCursor()
						.getColumnIndex(TourDatabaseHelper.POI_ICON_URL_COLUMN);

				final int locationColumnIndex = poiCursorAdapter.getCursor()
						.getColumnIndex(TourDatabaseHelper.POI_LOCATION_COLUMN);

				while (poiCursorAdapter.getCursor().moveToNext()) {

					ParcelablePOI parcelablePoi = new ParcelablePOI();

					parcelablePoi.setName(poiCursorAdapter.getCursor()
							.getString(nameColumnIndex));
					parcelablePoi.setVicinity(poiCursorAdapter.getCursor()
							.getString(vicinityColumnIndex));
					parcelablePoi.setIconUrl(poiCursorAdapter.getCursor()
							.getString(iconUrlColumnIndex));
					parcelablePoi.setLocationFromString(poiCursorAdapter
							.getCursor().getString(locationColumnIndex));

					parcelablePois.add(parcelablePoi);
				}
				return parcelablePois;
			}
		}
		return null;
	}

	/**
	 * Fetches POIs from around a location
	 * 
	 * @author ugo
	 * 
	 */
	class POIFetcherTask extends AsyncTask<Void, Integer, Void> {

		final int CONN_TIMEOUT_EXCEPTION = 1;
		final int UNKNOWN_HOST_EXCEPTION = 2;
		int errorCode = 0;

		private void checkError() {
			getListView().getEmptyView().findViewById(R.id.progBar)
					.setVisibility(View.GONE);
			((TextView) getListView().getEmptyView()
					.findViewById(R.id.progText))
					.setText(R.string.poi_list_no_items);

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
		protected Void doInBackground(Void... nothing) {
			if (pois != null) {
				pois.clear();
				pois = null;
			}

			try {
				pois = POIHelper.fetchPOIAroundLocation(
						POIListFragment.this.getActivity(), currLocation);
			} catch (UnknownHostException ex) {
				errorCode = UNKNOWN_HOST_EXCEPTION;
				cancel(true);
				return null;
			} catch (ConnectTimeoutException ex) {
				errorCode = CONN_TIMEOUT_EXCEPTION;
				cancel(true);
				return null;
			}

			return null;
		}

		@Override
		protected void onCancelled(Void result) {
			checkError();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (errorCode != 0) {
				checkError();
				return;
			}

			if (pois != null && pois.size() > 0) {
				for (PointOfInterest poi : pois) {
					poiArrayAdapter.add(poi);
				}
			}
			poiArrayAdapter.notifyDataSetChanged();

			// If currLocation was obtained from GPS
			// enable the AR mode button
			if (currLocation.getProvider().equals(LocationManager.GPS_PROVIDER)) {
				arModeMenuButton.setEnabled(true);
			}
			arModeMenuButton.setEnabled(true); // TODO
			mapModeMenuButton.setEnabled(true);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pois = new ArrayList<PointOfInterest>(0);
		}
	}
}