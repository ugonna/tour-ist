package com.ugo.android.tourmate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.ui.phone.POIListActivity;
import com.ugo.android.tourmate.util.GAnalyticsUtil;
import com.ugo.android.tourmate.util.TouristConstants;

public class DashboardFragment extends Fragment {

	public void fireTrackerEvent(String label) {
		GAnalyticsUtil.getInstance(getActivity()).trackEvent(
				"Home Screen Dashboard", "Click", label, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View root = inflater.inflate(R.layout.fragment_dashboard, container);

		// Attach event handlers
		root.findViewById(R.id.home_btn_explore).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("Explore");
						// Launch explore mode
						/*
						 * if (UIUtils.isHoneycombTablet(getActivity())) {
						 * startActivity(new Intent(getActivity(),
						 * SessionsMultiPaneActivity.class)); } else { final
						 * Intent intent = new Intent(Intent.ACTION_VIEW,
						 * ScheduleContract.Tracks.CONTENT_URI);
						 * intent.putExtra(Intent.EXTRA_TITLE,
						 * getString(R.string.title_session_tracks));
						 * intent.putExtra(TracksFragment.EXTRA_NEXT_TYPE,
						 * TracksFragment.NEXT_TYPE_SESSIONS);
						 * startActivity(intent); }
						 */
						final Intent intent = new Intent(getActivity(),
								POIListActivity.class);
						intent.putExtra(TouristConstants.EXTRA_LIST_MODE,
								TouristConstants.LIST_MODE_EXPLORE);
						startActivity(intent);
					}
				});
		
		root.findViewById(R.id.home_btn_places).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("My_Places");
						/*
						 * if (UIUtils.isHoneycombTablet(getActivity())) {
						 * startActivity(new Intent(getActivity(),
						 * ScheduleMultiPaneActivity.class)); } else {
						 * startActivity(new Intent(getActivity(),
						 * ScheduleActivity.class)); }
						 */
						final Intent intent = new Intent(getActivity(),
								POIListActivity.class);
						intent.putExtra(TouristConstants.EXTRA_LIST_MODE,
								TouristConstants.LIST_MODE_INTENDED);
						startActivity(intent);

					}

				});

		root.findViewById(R.id.home_btn_history).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("History");
						// startActivity(new Intent(getActivity(),
						// StarredActivity.class));
						final Intent intent = new Intent(getActivity(),
								POIListActivity.class);
						intent.putExtra(TouristConstants.EXTRA_LIST_MODE,
								TouristConstants.LIST_MODE_VISITED);
						startActivity(intent);
					}
				});

		root.findViewById(R.id.home_btn_settings).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("Settings");
						/*
						 * if (UIUtils.isHoneycombTablet(getActivity())) {
						 * startActivity(new Intent(getActivity(),
						 * VendorsMultiPaneActivity.class)); } else { final
						 * Intent intent = new Intent(Intent.ACTION_VIEW,
						 * ScheduleContract.Tracks.CONTENT_URI);
						 * intent.putExtra(Intent.EXTRA_TITLE,
						 * getString(R.string.title_vendor_tracks));
						 * intent.putExtra(TracksFragment.EXTRA_NEXT_TYPE,
						 * TracksFragment.NEXT_TYPE_VENDORS);
						 * startActivity(intent); }
						 */
						Toast.makeText(getActivity(), "Under Construction", Toast.LENGTH_SHORT).show();
					}
				});

		return root;
	}

}
