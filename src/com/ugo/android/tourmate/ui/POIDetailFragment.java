package com.ugo.android.tourmate.ui;

import static com.ugo.android.tourmate.util.TouristConstants.POI_ACTION_TYPE_FB;
import static com.ugo.android.tourmate.util.TouristConstants.POI_ACTION_TYPE_NOTE;
import static com.ugo.android.tourmate.util.TouristConstants.POI_ACTION_TYPE_VOLUME;
import static com.ugo.android.tourmate.util.TouristConstants.POI_STATUS_ADDED;
import static com.ugo.android.tourmate.util.TouristConstants.POI_STATUS_VISITED;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.db.TourDataAdapter;
import com.ugo.android.tourmate.db.TourDatabaseHelper;
import com.ugo.android.tourmate.entities.PerformableAction;
import com.ugo.android.tourmate.entities.PointOfInterest;
import com.ugo.android.tourmate.services.ProximityMonitorService;
import com.ugo.android.tourmate.ui.animation.ExpandAnimation;
import com.ugo.android.tourmate.util.GAnalyticsUtil;
import com.ugo.android.tourmate.util.POIHelper;
import com.ugo.android.tourmate.util.POITrackingUtil;
import com.ugo.android.tourmate.util.TouristConstants;
import com.ugo.android.tourmate.util.media.ImageFactory;

public class POIDetailFragment extends Fragment {

	private TextView clickableExposeText;
	private LinearLayout controlPanel;
	MenuItem addPoiMenuItem, removePoiMenuItem;

	private boolean controlPanelExpanded;
	private int currPOIStatus;
	private TourDataAdapter dbAdapter;
	private ImageButton exposeButton;
	private PointOfInterest poi;
	private POIDetailsFetcherTask poiFetcherTask;
	Location currLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = BaseActivity
				.fragmentArgumentsToIntent(getArguments());

		final String poiReference = intent
				.getStringExtra(TouristConstants.EXTRA_POI_REFERENCE);

		final String poiId = intent
				.getStringExtra(TouristConstants.EXTRA_POI_ID);

		setHasOptionsMenu(true);

		currPOIStatus = intent
				.getIntExtra(TouristConstants.EXTRA_POI_STATUS, 0);

		currLocation = intent
				.getParcelableExtra(TouristConstants.EXTRA_CURR_LOCATION);

		POITrackingUtil.initialize(getActivity());

		dbAdapter = TourDataAdapter.open(getActivity());

		poiFetcherTask = new POIDetailsFetcherTask();
		poiFetcherTask.execute(poiReference, poiId);
		
		GAnalyticsUtil.getInstance(getActivity()).trackPageView("/DetailView");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.poi_detail_menu_items, menu);
		addPoiMenuItem = menu.findItem(R.id.menu_add);
		removePoiMenuItem = menu.findItem(R.id.menu_remove);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View detailRoot = inflater.inflate(R.layout.fragment_poi_details,
				null);

		controlPanel = (LinearLayout) detailRoot
				.findViewById(R.id.controlPanel);

		switch (currPOIStatus) {
		case POI_STATUS_VISITED:
		case POI_STATUS_ADDED:
			controlPanel.setVisibility(View.GONE);
			detailRoot.findViewById(R.id.exposeControls).setVisibility(
					View.GONE);
			return detailRoot;
		}
		final SeekBar ringerVolumeSeekBar = (SeekBar) detailRoot
				.findViewById(R.id.ringtoneVolume);

		ringerVolumeSeekBar.setMax(POITrackingUtil.getRingtoneMaximum());
		ringerVolumeSeekBar.setProgress(POITrackingUtil.getRingtoneVolume());

		exposeButton = (ImageButton) detailRoot.findViewById(R.id.exposeButton);
		clickableExposeText = (TextView) detailRoot
				.findViewById(R.id.clickableExposeText);

		setupControlPanelExpander();

		return detailRoot;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			addToVisitList();
		}
		return super.onOptionsItemSelected(item);
	}

	private void addToVisitList() {
		List<PerformableAction> actions = new ArrayList<PerformableAction>(0);
		String outputMsg = getString(R.string.poi_details_successful_add);

		final CheckBox fbCheckBox = (CheckBox) getView().findViewById(
				R.id.postToFBCheck);
		// If the check box is checked...
		if (fbCheckBox.isChecked()) {
			actions.add(new PerformableAction(POI_ACTION_TYPE_FB, null));
			outputMsg += getString(R.string.poi_details_successful_add_facebook);
		}

		final EditText noteToSelfEdit = (EditText) getView().findViewById(
				R.id.noteToSelf);
		// If there is any text in the edit text...
		if (noteToSelfEdit.getText().length() > 0) {
			actions.add(new PerformableAction(POI_ACTION_TYPE_NOTE,
					noteToSelfEdit.getText().toString()));
			outputMsg += getString(R.string.poi_details_successful_add_note);
		}

		final SeekBar ringVolSeekBar = (SeekBar) getView().findViewById(
				R.id.ringtoneVolume);
		if (ringVolSeekBar.getProgress() > POITrackingUtil.getRingtoneVolume()) {
			actions.add(new PerformableAction(POI_ACTION_TYPE_VOLUME, Integer
					.toString(ringVolSeekBar.getProgress())));
			outputMsg += getString(R.string.poi_details_successful_add_volume_increase);
		} else if (ringVolSeekBar.getProgress() < POITrackingUtil
				.getRingtoneVolume()) {
			actions.add(new PerformableAction(POI_ACTION_TYPE_VOLUME, Integer
					.toString(ringVolSeekBar.getProgress())));
			outputMsg += getString(R.string.poi_details_successful_add_volume_decrease);
		}

		if (dbAdapter.insertIntendedPOIAndActions(poi, actions) > 0) {
			Toast.makeText(getActivity(), outputMsg, Toast.LENGTH_LONG).show();
			getActivity().finish();
		} else {
			Toast.makeText(getActivity(), R.string.poi_details_failed_add,
					Toast.LENGTH_SHORT).show();
		}
		
		// Add the POI location to proximity monitor
		Intent intent = new Intent(getActivity(), ProximityMonitorService.class);
		intent.putExtra(TouristConstants.EXTRA_POI_ID, poi.getPlaceId());
		intent.putExtra(TouristConstants.EXTRA_POI_REFERENCE, poi.getReferenceToken());
		intent.putExtra(TouristConstants.EXTRA_POI_NAME, poi.getName());
		intent.putExtra(TouristConstants.EXTRA_POI_LOCATION, poi.getLocation());
		
		// This call will start the service if not started
		// and pass the above Intent to Service.onStartCommand()
		// Easy way of passing data to this service
		this.getActivity().startService(intent);
		
		GAnalyticsUtil.getInstance(getActivity()).trackEvent("POI Details View", "marked POI", poi.getName(), 0);
	}

	private void loadPOIDetails() {
		
		final TextView nameText = (TextView) this.getView().findViewById(
				R.id.poiDetailName);
		final TextView categoryText = (TextView) this.getView().findViewById(
				R.id.poiDetailCategories);
		final RatingBar poiRatingBar = (RatingBar) this.getView().findViewById(
				R.id.poiRating);
		final TextView distanceTextView = (TextView) this.getView()
				.findViewById(R.id.poiDetailDistance);
		final TextView addressTextView = (TextView) this.getView()
				.findViewById(R.id.poiAddressText);
		final TextView phoneTextView = (TextView) this.getView().findViewById(
				R.id.poiPhoneText);
		// Display the name
		if (poi.getName() != null && !poi.getName().equals("")) {
			nameText.setText(poi.getName());
		}
		// Display the poi categories
		if (poi.getPlaceTypes() != null && poi.getPlaceTypes().size() > 0) {
			categoryText.setVisibility(View.VISIBLE);
			String categoryListString = "";

			for (String category : poi.getPlaceTypes()) {
				categoryListString += category + " ";
			}
			categoryText.setText(categoryText.getText() + categoryListString);
		} else {
			categoryText.setVisibility(View.GONE);
		}
		// Display the rating
		if (poi.getRating() > 0) {
			poiRatingBar.setRating(poi.getRating());
		}
		// Display the distance
		if (currLocation != null && poi.getLocation() != null) {

			distanceTextView.setVisibility(View.VISIBLE);
			distanceTextView.setText(POIHelper.getDistanceBetween(
					currLocation, poi.getLocation())
					+ " "
					+ getResources().getString(
							R.string.poi_details_distance_suffix));
		} else {
			distanceTextView.setVisibility(View.INVISIBLE);
		}
		// Display the address
		if (poi.getAddress() != null && !poi.getAddress().equals("")) {

			Spanned formattedText = Html.fromHtml("<b>" + getResources().getString(
					R.string.poi_details_address_head) + "</b>" + poi.getAddress());
			addressTextView.setText(formattedText);
			addressTextView.setVisibility(View.VISIBLE);
		} else {
			addressTextView.setVisibility(View.GONE);
		}
		// Display the phone number
		if (poi.getPhoneNumber() != null && !poi.getPhoneNumber().equals("")) {

			Spanned formattedText = Html.fromHtml("<b>" + getResources().getString(
					R.string.poi_details_phone_head) + "</b>" + poi.getPhoneNumber());
			phoneTextView.setText(formattedText);
			phoneTextView.setVisibility(View.VISIBLE);
		} else {
			phoneTextView.setVisibility(View.GONE);
		}
		
		if (poi.getIconUrl() != null && poi.getIconUrl() != "") {
			ImageFactory.fetchDrawableUntoImageView(poi.getIconUrl(),
					((ImageView) this.getView().findViewById(R.id.poiDetailIcon)));
		}
	}

	public void loadActionsSummary(List<PerformableAction> actions) {
		View importPanel = ((ViewStub) this.getView().findViewById(
				R.id.actionSummaryStub)).inflate();

		for (PerformableAction action : actions) {
			if (action.getAction().equals(POI_ACTION_TYPE_FB)) {

				importPanel.findViewById(R.id.actionSummaryfbText)
						.setVisibility(View.VISIBLE);
			} else if (action.getAction().equals(POI_ACTION_TYPE_NOTE)) {

				importPanel.findViewById(R.id.actionSummarynoteText)
						.setVisibility(View.VISIBLE);
				String textToSet = getString(R.string.poi_action_summary_note)
						+ " \"" + action.getValue() + "\"";
				((TextView) importPanel
						.findViewById(R.id.actionSummarynoteText))
						.setText(textToSet);
			} else if (action.getAction().equals(POI_ACTION_TYPE_VOLUME)) {

				importPanel.findViewById(R.id.actionSummaryvolumeText)
						.setVisibility(View.VISIBLE);
				String textToSet = getString(R.string.poi_action_summary_volume)
						+ " " + action.getValue();
				((TextView) importPanel
						.findViewById(R.id.actionSummaryvolumeText))
						.setText(textToSet);
			}
		}
	}

	/**
	 * Sets up the action and animation for the button to expose the control
	 * panel and the control panel itself respectively.
	 * 
	 * @param controlPanelFullHeight
	 * @param expandButton
	 */
	private void setupControlPanelExpander() {
		final int controlPanelFullHeight = 431; // Guessed for now...;
												// controlPanel.getLayoutParams().height;

		// Collapse the damn view
		controlPanel.getLayoutParams().height = 0;
		controlPanel.setVisibility(View.GONE);

		View.OnClickListener expandOnClick = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Animation anim;
				if (controlPanelExpanded) {
					anim = new ExpandAnimation(controlPanel,
							controlPanelFullHeight, false);
					anim.setInterpolator(new AccelerateDecelerateInterpolator());

					Drawable buttonDrawable = getResources().getDrawable(
							R.drawable.expander_ic_minimized);
					exposeButton.setImageDrawable(buttonDrawable);
					clickableExposeText
							.setText(R.string.poi_details_options_expand);

					// Do not set visibility as GONE till after animation

					controlPanelExpanded = false;
				} else {
					anim = new ExpandAnimation(controlPanel,
							controlPanelFullHeight, true);
					anim.setInterpolator(new AccelerateDecelerateInterpolator());

					Drawable buttonDrawable = getResources().getDrawable(
							R.drawable.expander_ic_maximized);
					exposeButton.setImageDrawable(buttonDrawable);
					clickableExposeText
							.setText(R.string.poi_details_options_collapse);

					controlPanel.setVisibility(View.VISIBLE);

					controlPanelExpanded = true;
				}
				anim.setDuration(750L);
				anim.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
						// If collapsing then set GONE only after collapse
						if (!controlPanelExpanded) {
							controlPanel.setVisibility(View.GONE);
						}
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationStart(Animation animation) {
					}
				});

				controlPanel.startAnimation(anim);
				controlPanel.invalidate();
				POIDetailFragment.this.getView().invalidate();
			}
		};
		clickableExposeText.setOnClickListener(expandOnClick);
		exposeButton.setOnClickListener(expandOnClick);
	}

	class POIDetailsFetcherTask extends AsyncTask<String, Void, Void> {

		List<PerformableAction> actions;
		int errorCode = 0;
		ProgressDialog progDialog;
		final int CONN_TIMEOUT_EXCEPTION = 1;
		final int UNKNOWN_HOST_EXCEPTION = 2;
		final int SQL_EXCEPTION = 3;

		private void checkError() {
			switch (errorCode) {
			case CONN_TIMEOUT_EXCEPTION:
				Toast.makeText(getActivity(), R.string.err_msg_connect_timeout,
						Toast.LENGTH_SHORT).show();
				break;
			case UNKNOWN_HOST_EXCEPTION:
				Toast.makeText(getActivity(), R.string.err_msg_cannot_connect,
						Toast.LENGTH_SHORT).show();
				break;
			case SQL_EXCEPTION:
				Toast.makeText(getActivity(),
						R.string.err_msg_cannot_fetch_actions,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

		@Override
		protected Void doInBackground(String... params) {
			try {
				Cursor actionCursor = dbAdapter
						.fetchAllActionsForPOI(params[1]);

				if (actionCursor != null && actionCursor.moveToFirst()) {
					// There is at least one performable action
					actions = new ArrayList<PerformableAction>(
							actionCursor.getCount());

					int actionColumn = actionCursor
							.getColumnIndex(TourDatabaseHelper.POI_ACTION_COLUMN);
					int actionValueColumn = actionCursor
							.getColumnIndex(TourDatabaseHelper.POI_ACTION_VALUE_COLUMN);

					do {
						actions.add(new PerformableAction(actionCursor
								.getString(actionColumn), actionCursor
								.getString(actionValueColumn)));
					} while (actionCursor.moveToNext());
					dbAdapter.close();
				}
			} catch (SQLException ex) {
				errorCode = SQL_EXCEPTION;
				// Do not cancel
			}

			try {
				poi = POIHelper.fetchPOIDetails(
						POIDetailFragment.this.getActivity(), params[0]);
			} catch (UnknownHostException ex) {
				errorCode = UNKNOWN_HOST_EXCEPTION;
				cancel(true);
			} catch (ConnectTimeoutException ex) {
				errorCode = CONN_TIMEOUT_EXCEPTION;
				cancel(true);
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
			}
			progDialog.dismiss();
			if (poi != null) {
				loadPOIDetails();
				if (actions != null && !actions.isEmpty()) {
					loadActionsSummary(actions);
				}
			}
			switch (currPOIStatus) {
			case POI_STATUS_ADDED:
				addPoiMenuItem.setVisible(false).setEnabled(false);
				break;
			default:
				removePoiMenuItem.setVisible(false).setEnabled(false);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			progDialog = new ProgressDialog(
					POIDetailFragment.this.getActivity());
			progDialog.setCancelable(true);
			progDialog.setMessage(getResources().getString(
					R.string.poi_details_progress_dialog_message));
			progDialog.setTitle(R.string.poi_details_progress_dialog_title);
			progDialog.show();
		}

	}
}
