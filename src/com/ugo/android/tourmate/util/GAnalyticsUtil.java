package com.ugo.android.tourmate.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class GAnalyticsUtil {
	
	private static final String TAG = GAnalyticsUtil.class.getSimpleName();
	
	GoogleAnalyticsTracker tracker;
	private Context applicationContext;
	
	/**
     * The analytics tracking code for the app.
     */
    private static final String UACODE = "YOUR_UA_CODE";

    private static final int VISITOR_SCOPE = 1;
    private static final String FIRST_RUN_KEY = "firstRun";

    private static GAnalyticsUtil instance;

    /**
     * Empty instance for use when Analytics is disabled or there was no Context available.
     */
    private static GAnalyticsUtil emptyAnalyticsUtils = new GAnalyticsUtil(null) {
        @Override
        public void trackEvent(String category, String action, String label, int value) {}

        @Override
        public void trackPageView(String path) {}
    };

    /**
     * Returns the global {@link AnalyticsUtils} singleton object, creating one if necessary.
     */
    public static GAnalyticsUtil getInstance(Context context) {

        if (instance == null) {
            if (context == null) {
                return emptyAnalyticsUtils;
            }
            instance = new GAnalyticsUtil(context);
        }

        return instance;
    }

    private GAnalyticsUtil(Context context) {
        if (context == null) {
            // This should only occur for the empty Analytics utils object.
            return;
        }

        applicationContext = context.getApplicationContext();
        tracker = GoogleAnalyticsTracker.getInstance();

        // Unfortunately this needs to be synchronous.
        tracker.start(UACODE, 300, applicationContext);

        Log.d(TAG, "Initializing Analytics");

        // Since visitor CV's should only be declared the first time an app runs, check if
        // it's run before. Add as necessary.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                applicationContext);
        final boolean firstRun = prefs.getBoolean(FIRST_RUN_KEY, true);
        if (firstRun) {
            Log.d(TAG, "Analytics firstRun");

            String apiLevel = Integer.toString(Build.VERSION.SDK_INT);
            String model = Build.MODEL;
            tracker.setCustomVar(1, "apiLevel", apiLevel, VISITOR_SCOPE);
            tracker.setCustomVar(2, "model", model, VISITOR_SCOPE);

            // Close out so we never run this block again, unless app is removed & =
            // reinstalled.
            prefs.edit().putBoolean(FIRST_RUN_KEY, false).commit();
        }
    }

    public void trackEvent(final String category, final String action, final String label,
            final int value) {
        // We wrap the call in an AsyncTask since the Google Analytics library writes to disk
        // on its calling thread.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    tracker.trackEvent(category, action, label, value);
                    Log.d(TAG, "tour.ist Analytics trackEvent: "
                            + category + " / " + action + " / " + label + " / " + value);
                } catch (Exception e) {
                    // We don't want to crash if there's an Analytics library exception.
                    Log.w(TAG, "tour.ist Analytics trackEvent error: "
                            + category + " / " + action + " / " + label + " / " + value, e);
                }
                return null;
            }
        }.execute();
    }

    public void trackPageView(final String path) {
        // We wrap the call in an AsyncTask since the Google Analytics library writes to disk
        // on its calling thread.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    tracker.trackPageView(path);
                    Log.d(TAG, "tour.IST Analytics trackPageView: " + path);
                } catch (Exception e) {
                    // We don't want to crash if there's an Analytics library exception.
                    Log.w(TAG, "tour.IST Analytics trackPageView error: " + path, e);
                }
                return null;
            }
        }.execute();
    }

}
