package com.ugo.android.tourmate.db;

import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_ACTIONS_TBL_NAME;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_ACTION_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_ACTION_VALUE_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_ADDRESS_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_DATE_ADDED_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_ICON_URL_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_ID_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_INTENDED_TBL_NAME;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_LOCATION_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_NAME_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_PHONE_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_REFERENCE_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_TYPES_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_VICINITY_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_VISITED_TBL_NAME;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_V_DATE_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_V_DURATION_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_V_TIME_COLUMN;
import static com.ugo.android.tourmate.util.TouristConstants.POI_STATUS_ADDED;
import static com.ugo.android.tourmate.util.TouristConstants.POI_STATUS_VISITED;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateFormat;

import com.ugo.android.tourmate.entities.PerformableAction;
import com.ugo.android.tourmate.entities.PointOfInterest;
import com.ugo.android.tourmate.util.TouristConstants;

public class TourDataAdapter {

	public static TourDataAdapter open(Context context) throws SQLException {
		return new TourDataAdapter(context);
	}

	private TourDatabaseHelper dbHelper;

	private SQLiteDatabase database;

	TourDataAdapter(Context context) {
		dbHelper = new TourDatabaseHelper(context);
	}

	public void close() {
		if (database.isOpen()) {
			database.close();
		}
	}

	/**
	 * Checks the POI to see if it has been visited or if it has been added to
	 * the intended visit list and returns a status.
	 * 
	 * @param poiID
	 *            The unique ID of the POI
	 * @return {@link TouristConstants.POI_STATUS_VISITED} (2) if it has been
	 *         visited, {@link TouristConstants.POI_STATUS_ADDED} (1) if it has
	 *         been added and 0 otherwise.
	 */
	public int checkPOIStatus(String poiID) {
		Cursor cur = null;
		database = dbHelper.getReadableDatabase();

		cur = database.query(POI_VISITED_TBL_NAME,
				new String[] { POI_ID_COLUMN }, POI_ID_COLUMN + "=?",
				new String[] { poiID }, null, null, null);

		if (cur == null || !cur.moveToFirst()) {
			cur = database.query(POI_INTENDED_TBL_NAME,
					new String[] { POI_ID_COLUMN }, POI_ID_COLUMN + "=?",
					new String[] { poiID }, null, null, null);

			if (cur != null && cur.moveToFirst()) {
				return POI_STATUS_ADDED;
			}
		} else {
			return POI_STATUS_VISITED;
		}
		return 0;
	}

	public Cursor fetchAllActionsForPOI(String poiId) {
		database = dbHelper.getReadableDatabase();
		return database.query(POI_ACTIONS_TBL_NAME, null, POI_ID_COLUMN + "=?",
				new String[] { poiId }, null, null, null);
	}

	/**
	 * Fetches all intended POIs
	 * 
	 * @return
	 */
	public Cursor fetchAllIntendedPOIs() {
		database = dbHelper.getReadableDatabase();
		return database.query(POI_INTENDED_TBL_NAME, null, null, null, null,
				null, null);
	}

	public Cursor fetchAllVisitedPOIs() {
		final String query = "SELECT i.* FROM " + POI_INTENDED_TBL_NAME
				+ " i INNER JOIN " + POI_VISITED_TBL_NAME + " v ON i."
				+ POI_ID_COLUMN + "=v." + POI_ID_COLUMN;
		database = dbHelper.getReadableDatabase();
		return database.rawQuery(query, null);
	}

	public long insertIntendedPOIAndActions(PointOfInterest poi,
			List<PerformableAction> actions) {
		long result = 0L;
		try {
			database = dbHelper.getWritableDatabase();
			database.beginTransaction();
			result = insertIntendedPOI(poi);
			if (insertNewActions(poi.getPlaceId(), actions)) {
				database.setTransactionSuccessful();
			}
		} finally {
			database.endTransaction();
		}
		return result;
	}

	public long insertVisitedPOI(PointOfInterest poi, int durationInMins) {

			ContentValues initialContentValues = new ContentValues(9);
			initialContentValues.put(POI_ID_COLUMN, poi.getPlaceId());
			initialContentValues.put(POI_V_DATE_COLUMN, getCurrentDateString()
					.toString());
			initialContentValues.put(POI_V_TIME_COLUMN, getCurrentTimeString()
					.toString());
			initialContentValues.put(POI_V_DURATION_COLUMN, durationInMins);
			initialContentValues.put(POI_ADDRESS_COLUMN, poi.getAddress());
			initialContentValues.put(POI_VICINITY_COLUMN, poi.getVicinity());
			initialContentValues.put(POI_ICON_URL_COLUMN, poi.getIconUrl());
			initialContentValues.put(POI_LOCATION_COLUMN, poi.getLocation()
					.getLatitude() + "," + poi.getLocation().getLongitude());
			initialContentValues.put(POI_ICON_URL_COLUMN,
					poi.getPlaceTypeCommaSeparated());

			database = dbHelper.getWritableDatabase();

			return database.insert(POI_INTENDED_TBL_NAME, null,
					initialContentValues);
	}

	private CharSequence getCurrentDateString() {
		return DateFormat.format("dd/MM/yyyy", System.currentTimeMillis());
	}

	private CharSequence getCurrentTimeString() {
		return DateFormat.format("hh:mm:ss.SSS", System.currentTimeMillis());
	}

	private long insertIntendedPOI(PointOfInterest poi) {
		ContentValues initialContentValues = new ContentValues(9);
		initialContentValues.put(POI_ID_COLUMN, poi.getPlaceId());
		initialContentValues.put(POI_REFERENCE_COLUMN, poi.getReferenceToken());
		initialContentValues.put(POI_NAME_COLUMN, poi.getName());
		initialContentValues.put(POI_PHONE_COLUMN, poi.getPhoneNumber());
		initialContentValues.put(POI_ADDRESS_COLUMN, poi.getAddress());
		initialContentValues.put(POI_VICINITY_COLUMN, poi.getVicinity());
		initialContentValues.put(POI_ICON_URL_COLUMN, poi.getIconUrl());
		initialContentValues.put(POI_LOCATION_COLUMN, poi.getLocation()
				.getLatitude() + "," + poi.getLocation().getLongitude());
		initialContentValues.put(POI_TYPES_COLUMN,
				poi.getPlaceTypeCommaSeparated());
		initialContentValues.put(POI_DATE_ADDED_COLUMN, getCurrentDateString()
				.toString());

		return database.insert(POI_INTENDED_TBL_NAME, null,
				initialContentValues);
	}

	private boolean insertNewActions(String poiID,
			List<PerformableAction> actions) {
		database.beginTransaction();
		boolean success = false;
		try {
			database.delete(POI_ACTIONS_TBL_NAME, POI_ID_COLUMN + "=?",
					new String[] { poiID });

			ContentValues values = new ContentValues(3);
			for (PerformableAction action : actions) {
				values.put(POI_ID_COLUMN, poiID);
				values.put(POI_ACTION_COLUMN, action.getAction());
				values.put(POI_ACTION_VALUE_COLUMN, action.getValue());

				database.insert(POI_ACTIONS_TBL_NAME, null, values);
			}
			database.setTransactionSuccessful();
			// Free for GC
			// values = null;
			success = true;
		} finally {
			database.endTransaction();
		}
		return success;
	}
}
