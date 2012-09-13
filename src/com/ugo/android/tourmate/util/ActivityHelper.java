package com.ugo.android.tourmate.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.ui.HomeActivity;

public class ActivityHelper {
	
	public static ActivityHelper createInstance(Activity activity) {
		boolean isHoneyComb = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
		
		return isHoneyComb ? new ActivityHelperHoneycomb(activity) :
			new ActivityHelper(activity);
	}
	
	protected Activity activity;
	
	public ActivityHelper(Activity activity) {
		this.activity = activity;
	}
	
	/**
	 * Gets the compatible action bar for API Level < 11
	 * @return {@link ViewGroup} for Action bar for API Level < 11
	 */
	public ViewGroup getActionBarCompat() {
        return (ViewGroup) activity.findViewById(R.id.actionbar_compat);
    }
	
	public boolean onCreateOptionsMenu(Menu menu) {
		//TODO The below will put the default MenuItem on the action bar. Implement
        activity.getMenuInflater().inflate(R.menu.default_menu_items, menu);
        return false;
    }
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return false;
    }
	
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //goHome(); TODO Implement for long press to go home
            return true;
        }
        return false;
    }
	
	public boolean onOptionsItemSelected(MenuItem item) {
		//TODO Implement for each default MenuItem. See above
        switch (item.getItemId()) {
            case R.id.menu_search: 
                //goSearch();
            	Toast.makeText(activity, "Under Construction", Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }
	
	public void onPostCreate(Bundle savedInstanceState) {
        // Create the action bar
        SimpleMenu menu = new SimpleMenu(activity);
        activity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, menu);

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            addActionButtonCompatFromMenuItem(item);
        }
    }

    public void setActionBarTitle(CharSequence title) {
        ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return;
        }

        TextView titleText = (TextView) actionBar.findViewById(R.id.actionbar_compat_text);
        if (titleText != null) {
            titleText.setText(title);
        }
    }
	
    public void setupActionBar(CharSequence title, int color) {
        final ViewGroup actionBarCompat = getActionBarCompat();
        //Will return null if API level >= 11
        if (actionBarCompat == null) {
            return;
        }

        LinearLayout.LayoutParams springLayoutParams = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.FILL_PARENT);
        springLayoutParams.weight = 1;

        View.OnClickListener homeClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                goHome();
            }
        };

        if (title != null) {
            // Add Home button
            addActionButtonCompat(R.drawable.ic_title_home, R.string.description_home,
                    homeClickListener, true);

            // Add title text
            TextView titleText = new TextView(activity, null, R.attr.actionbarCompatTextStyle);
            titleText.setLayoutParams(springLayoutParams);
            titleText.setText(title);
            actionBarCompat.addView(titleText);

        } else {
            // Add logo
            ImageButton logo = new ImageButton(activity, null, R.attr.actionbarCompatLogoStyle);
            logo.setOnClickListener(homeClickListener);
            actionBarCompat.addView(logo);

            // Add spring (dummy view to align future children to the right)
            View spring = new View(activity);
            spring.setLayoutParams(springLayoutParams);
            actionBarCompat.addView(spring);
        }

        //setActionBarColor(color); TODO Set color
    }

    /**
     * Method, to be called in <code>onPostCreate</code> for Honeycomb, that sets up this 
     * activity as the home activity for the app.
     */
    public void setupHomeActivity() {
    	//Do nothing. Not Honeycomb
    }
    
	/**
     * Method, to be called in <code>onPostCreate</code> for Honeycomb, that sets up this activity as a
     * sub-activity in the app.
     */
    public void setupSubActivity() {
    	//Do nothing. Not Honeycomb
    }
	
	private View addActionButtonCompat(int iconResId, int textResId,
            View.OnClickListener clickListener, boolean separatorAfter) {
        final ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return null;
        }

        // Create the separator
        ImageView separator = new ImageView(activity, null, R.attr.actionbarCompatSeparatorStyle);
        separator.setLayoutParams(
                new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.FILL_PARENT));

        // Create the button
        ImageButton actionButton = new ImageButton(activity, null,
                R.attr.actionbarCompatButtonStyle);
        actionButton.setLayoutParams(new ViewGroup.LayoutParams(
                (int) activity.getResources().getDimension(R.dimen.actionbar_compat_height),
                ViewGroup.LayoutParams.FILL_PARENT));
        actionButton.setImageResource(iconResId);
        actionButton.setScaleType(ImageView.ScaleType.CENTER);
        actionButton.setContentDescription(activity.getResources().getString(textResId));
        actionButton.setOnClickListener(clickListener);

        // Add separator and button to the action bar in the desired order

        if (!separatorAfter) {
            actionBar.addView(separator);
        }

        actionBar.addView(actionButton);

        if (separatorAfter) {
            actionBar.addView(separator);
        }

        return actionButton;
    }
	
	private View addActionButtonCompatFromMenuItem(final MenuItem item) {
        final ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return null;
        }

        // Create the separator
        ImageView separator = new ImageView(activity, null, R.attr.actionbarCompatSeparatorStyle);
        separator.setLayoutParams(
                new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.FILL_PARENT));

        // Create the button
        ImageButton actionButton = new ImageButton(activity, null,
                R.attr.actionbarCompatButtonStyle);
        actionButton.setId(item.getItemId());
        actionButton.setLayoutParams(new ViewGroup.LayoutParams(
                (int) activity.getResources().getDimension(R.dimen.actionbar_compat_height),
                ViewGroup.LayoutParams.FILL_PARENT));
        actionButton.setImageDrawable(item.getIcon());
        actionButton.setScaleType(ImageView.ScaleType.CENTER);
        actionButton.setContentDescription(item.getTitle());
        actionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                activity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
            }
        });

        actionBar.addView(separator);
        actionBar.addView(actionButton);

        return actionButton;
    }
	
	public void goHome() {
		if (activity instanceof HomeActivity) {
            return;
        }
		
		final Intent intent = new Intent(activity, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
	}

}
