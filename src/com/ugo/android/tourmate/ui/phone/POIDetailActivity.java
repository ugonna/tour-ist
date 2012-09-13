package com.ugo.android.tourmate.ui.phone;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ugo.android.tourmate.ui.BaseSinglePaneActivity;
import com.ugo.android.tourmate.ui.POIDetailFragment;

public class POIDetailActivity extends BaseSinglePaneActivity {

	@Override
	protected Fragment onCreatePane() {
		// TODO Auto-generated method stub
		return new POIDetailFragment();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupSubActivity();
	}
}
