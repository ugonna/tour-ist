/**
 * 
 */
package com.ugo.android.tourmate.util;

/**
 * @author Ugonna Nwakama
 *
 */
public interface TouristConstants {
	/**
	 * Constant to use as name in the name/value pair put in Intent.putExtra()
	 * to describe the list mode to be created in the tourmate list
	 */
	public static final String EXTRA_LIST_MODE = "com.ugo.android.tourmate.listmode";
	
	/**
	 * Constant to use as name in the name/value pair put in Intent.putExtra()
	 * to indicate the reference key of a particular POI
	 */
	public static final String EXTRA_POI_REFERENCE = "com.ugo.android.tourmate.poi.reference";
	
	/**
	 * Constant to use as name in the name/value pair put in Intent.putExtra()
	 * to indicate the ID of a particular POI
	 */
	public static final String EXTRA_POI_ID = "com.ugo.android.tourmate.poi.id";
	
	/**
	 * Constant to use as name in the name/value pair put in Intent.putExtra()
	 * to indicate the name of a particular POI
	 */
	public static final String EXTRA_POI_NAME = "com.ugo.android.tourmate.poi.name";
	
	/**
	 * Constant to use as name in the name/value pair put in Intent.putExtra()
	 * to indicate the visit status of a particular POI
	 */
	public static final String EXTRA_POI_STATUS = "com.ugo.android.tourmate.poi.status";
	
	/**
	 * Constant to use as name in the name/value pair put in Intent.putExtra()
	 * to indicate the location of a particular POI
	 */
	public static final String EXTRA_POI_LOCATION = "com.ugo.android.tourmate.poi.location";
	
	/**
	 * Constant to use as name in the name/value pair put in Intent.putExtra()
	 * to hold the current Location of the device
	 */
	public static final String EXTRA_CURR_LOCATION = "com.ugo.android.tourmate.current.location";
	
	/**
	 * Constant to use as name in the name/value pair put in Intent.putExtra()
	 * to hold the POI details
	 */
	public static final String EXTRA_POI_DETAILS_ARR = "com.ugo.android.tourmate.poi.details";
	
	/**
	 * Constant to use as name in the name/value pair put in Intent.putExtra()
	 * to hold the POI details
	 */
	public static final String INTENT_POI_PROXIMITY_ALERT = "com.ugo.android.tourmate.proximityalert";

	/**
	 * The status a POI has when it has been added to the intended visit list
	 */
	public static final int POI_STATUS_ADDED = 1;
	/**
	 * The status a POI has when it has been visited
	 */
	public static final int POI_STATUS_VISITED = 2;
	/**
	 * Constant to use as value in the name/value pair put in Intent.putExtra()
	 * 
	 * If the value from POIListFragment.EXTRA_LIST_MODE matches this, the list
	 * will enter explore mode.
	 */
	public static final String LIST_MODE_EXPLORE = "explore";
	/**
	 * Constant to use as value in the name/value pair put in Intent.putExtra()
	 * 
	 * If the value from POIListFragment.EXTRA_LIST_MODE matches this, the list
	 * will enter recommend mode.
	 */
	public static final String LIST_MODE_RECOMMEND = "recommend";
	/**
	 * Constant to use as value in the name/value pair put in Intent.putExtra()
	 * 
	 * If the value from POIListFragment.EXTRA_LIST_MODE matches this, the list
	 * will enter intended POI display mode.
	 */
	public static final String LIST_MODE_INTENDED = "intended";
	/**
	 * Constant to use as value in the name/value pair put in Intent.putExtra()
	 * 
	 * If the value from POIListFragment.EXTRA_LIST_MODE matches this, the list
	 * will enter history display mode.
	 */
	public static final String LIST_MODE_VISITED = "visited";
	
	/**
	 * Dialog id for the AlertDialog shown if no fine location
	 * provider (GPS) is available.
	 */
	public static final int DIALOG_NO_FINE_PROVIDER = 1;
	/**
	 * Action type that can occur when a user enters a location.
	 * Post to Facebook.
	 */
	public final static String POI_ACTION_TYPE_FB = "facebook";
	/**
	 * Action type that can occur when a user enters a location.
	 * Show a Note-to-Self.
	 */
	public final static String POI_ACTION_TYPE_NOTE = "note";
	/**
	 * Action type that can occur when a user enters a location.
	 * Change the Volume.
	 */
	public final static String POI_ACTION_TYPE_VOLUME = "volume";
}
