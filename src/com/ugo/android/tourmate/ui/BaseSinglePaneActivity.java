package com.ugo.android.tourmate.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;

import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.util.TouristConstants;

/**
 * A {@link BaseActivity} that simply contains a single fragment. The intent used to invoke this
 * activity is forwarded to the fragment as arguments during fragment instantiation. Derived
 * activities should only need to implement
 * {@link BaseSinglePaneActivity#onCreatePane()}.
 */
public abstract class BaseSinglePaneActivity extends BaseActivity {
    private Fragment fragment;

    /**
	 * Gets the instance of the {@link Fragment} that is attached to this activity
	 * @return
	 */
	protected Fragment getFragmentInstance() {
		return fragment;
	}

    /**
     * Called when an activity that was called from this activity returns
     * and is expected to have some result that this activity acts on. Subclasses
     * override this method to provide their own implementation of the result.
     * @param requestCode
     */
    protected void handleActivityReturn(int requestCode, int resultCode, Intent data) {
    	// Do nothing by default
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.handleActivityReturn(requestCode, resultCode, data);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlepane_empty);
        getActivityHelper().setupActionBar(getTitle(), 0);

        final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        getActivityHelper().setActionBarTitle(customTitle != null ? customTitle : getTitle());

        if (savedInstanceState == null) {
            fragment = onCreatePane();
            fragment.setArguments(intentToFragmentArguments(getIntent()));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.singlepane_root, fragment)
                    .commit();
        }
    }
    
	@Override
    protected final Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		switch (id) {
		case TouristConstants.DIALOG_NO_FINE_PROVIDER:
			dialog = new AlertDialog.Builder(this)
					.setMessage(R.string.dialog_fine_location_disabled)
					.setCancelable(false)
					.setPositiveButton(R.string.dialog_yes_button,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(
											Settings.ACTION_LOCATION_SOURCE_SETTINGS);

									startActivityForResult(
											intent,
											TouristConstants.DIALOG_NO_FINE_PROVIDER);

									dialog.dismiss();
								}
							})
					.setNegativeButton(R.string.dialog_no_button,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							}).create();
			break;

		default:
			return super.onCreateDialog(id);
		}

		return dialog;
	}
	
    /**
     * Called in <code>onCreate</code> when the fragment constituting this activity is needed.
     * The returned fragment's arguments will be set to the intent used to invoke this activity.
     */
    protected abstract Fragment onCreatePane();
}
