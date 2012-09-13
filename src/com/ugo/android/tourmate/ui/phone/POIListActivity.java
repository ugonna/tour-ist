package com.ugo.android.tourmate.ui.phone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ugo.android.tourmate.ui.BaseSinglePaneActivity;
import com.ugo.android.tourmate.ui.POIListFragment;
import com.ugo.android.tourmate.util.TouristConstants;

public class POIListActivity extends BaseSinglePaneActivity {

	@Override
	protected void handleActivityReturn(int requestCode, int resultCode,
			Intent data) {
		switch (requestCode) {
		case TouristConstants.DIALOG_NO_FINE_PROVIDER:
			((POIListFragment) getFragmentInstance()).checkForFineLocationProvider();
			break;
		default:
			break;
		}
	}

	@Override
	/** { @inheritDoc }*/
	protected Fragment onCreatePane() {
		return new POIListFragment();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupSubActivity();
	}
}
