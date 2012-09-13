package com.ugo.android.tourmate.util;

import com.ugo.android.tourmate.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

public class ActivityHelperHoneycomb extends ActivityHelper {

	private Menu optionsMenu;
	
	protected ActivityHelperHoneycomb(Activity activity) {
        super(activity);
    }
	
	public ViewGroup getActionBarCompat() {
        return null;
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		optionsMenu = menu;
        return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//TODO Implement for each default MenuItem. See above
		switch (item.getItemId()) {
        case android.R.id.home:
            // Handle the HOME / UP affordance. Since the app is only two levels deep
            // hierarchically, UP always just goes home.
            goHome();
            return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		//TODO Do nothing. This is Honeycomb
	}
	
	/**
     * No-op on Honeycomb. The action bar title always remains the same.
     */
    @Override
    public void setActionBarTitle(CharSequence title) {
    }
    
    /** {@inheritDoc} */
    @Override
    public void setupHomeActivity() {
        super.setupHomeActivity();
        // NOTE: there needs to be a content view set before this is called, so this method
        // should be called in onPostCreate.
        activity.getActionBar().setDisplayOptions(
                0,
                ActionBar.DISPLAY_SHOW_TITLE);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setupSubActivity() {
        super.setupSubActivity();
        // NOTE: there needs to be a content view set before this is called, so this method
        // should be called in onPostCreate.
        activity.getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO,
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);
    }

    //TODO Not implemented in parent. But *might* be later.
    /**
     * No-op on Honeycomb. The action bar color always remains the same.
     *//*
    @Override
    public void setActionBarColor(int color) {
    }*/
}
