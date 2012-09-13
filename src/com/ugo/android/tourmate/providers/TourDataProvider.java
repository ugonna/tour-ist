package com.ugo.android.tourmate.providers;

import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_ADDRESS_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_INTENDED_TBL_NAME;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_VISITED_TBL_NAME;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_V_DURATION_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper._ID_COLUMN;
import static com.ugo.android.tourmate.db.TourDatabaseHelper.POI_ID_COLUMN;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.ugo.android.tourmate.db.TourDatabaseHelper;

public class TourDataProvider extends ContentProvider {

	private TourDatabaseHelper dbHelper;

	private static final String AUTHORITY = "com.ugo.android.tourmate.providers.tourdataprovider";
	private static final String INTENDED_BASE_PATH = "intended";
	private static final String VISITED_BASE_PATH = "visited";
	public static final Uri INTENDED_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + INTENDED_BASE_PATH);
	public static final Uri VISITED_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + VISITED_BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/pointofinterest";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/pointofinterest";
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	private static final int INTENDED = 0;
	private static final int INTENDED_SINGLE = 1;
	private static final int VISITED = 2;
	private static final int VISITED_SINGLE = 3;
	static {
		sURIMatcher.addURI(AUTHORITY, INTENDED_BASE_PATH, INTENDED);
		sURIMatcher.addURI(AUTHORITY, INTENDED_BASE_PATH + "/#",
				INTENDED_SINGLE);
		sURIMatcher.addURI(AUTHORITY, VISITED_BASE_PATH, VISITED);
		sURIMatcher.addURI(AUTHORITY, VISITED_BASE_PATH + "/#", VISITED_SINGLE);
	}

	/**
	 * Not used within this app. Will be used externally (if ever)
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rowsAffected = 0;
		int uriType = sURIMatcher.match(uri);
		String id;

		switch (uriType) {
		case INTENDED:
			rowsAffected = db.delete(POI_INTENDED_TBL_NAME, selection,
					selectionArgs);
			break;
		case INTENDED_SINGLE:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = db.delete(POI_INTENDED_TBL_NAME, _ID_COLUMN
						+ "=" + id, null);
			} else {
				rowsAffected = db.delete(POI_INTENDED_TBL_NAME, selection
						+ " and " + _ID_COLUMN + "=" + id, selectionArgs);
			}
			break;
		case VISITED:
			rowsAffected = db.delete(POI_VISITED_TBL_NAME, selection,
					selectionArgs);
			break;
		case VISITED_SINGLE:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsAffected = db.delete(POI_VISITED_TBL_NAME, _ID_COLUMN + "="
						+ id, null);
			} else {
				rowsAffected = db.delete(POI_VISITED_TBL_NAME, selection
						+ " and " + _ID_COLUMN + "=" + id, selectionArgs);
			}
			break;
		default:
			// Close the database
			db.close();
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		// Close the database
		db.close();
		return rowsAffected;
	}

	@Override
	public String getType(Uri uri) {
		int uriType = sURIMatcher.match(uri);

		if (uriType == INTENDED || uriType == VISITED) {
			return CONTENT_TYPE;
		} else if (uriType == INTENDED || uriType == VISITED) {
			return CONTENT_ITEM_TYPE;
		}
		throw new IllegalArgumentException("Unknown URI " + uri);
	}

	/**
	 * Not used within this app. Will be used externally (if ever)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		long rowId;
		if (values == null) {
			// Empty values
			values = new ContentValues();
		}
		SQLiteDatabase db;

		switch (uriType) {
		case INTENDED:
			db = dbHelper.getWritableDatabase();
			rowId = db
					.insert(POI_INTENDED_TBL_NAME, POI_ADDRESS_COLUMN, values);
			if (rowId > 0) {
				Uri intendedUri = ContentUris.withAppendedId(
						INTENDED_CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(intendedUri,
						null);
				// Close the database
				db.close();
				return intendedUri;
			}
			break;
		case VISITED:
			db = dbHelper.getWritableDatabase();
			rowId = db.insert(POI_VISITED_TBL_NAME, POI_V_DURATION_COLUMN,
					values);
			if (rowId > 0) {
				Uri visitedUri = ContentUris.withAppendedId(
						VISITED_CONTENT_URI, rowId);
				getContext().getContentResolver()
						.notifyChange(visitedUri, null);
				// Close the database
				db.close();
				return visitedUri;
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		// Failed (rowId <= 0)
		throw new SQLiteException("Could not insert data");
	}

	@Override
	public boolean onCreate() {
		dbHelper = new TourDatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		int uriType = sURIMatcher.match(uri);

		switch (uriType) {
		case INTENDED:
			queryBuilder.setTables(POI_INTENDED_TBL_NAME);
			break;
		case INTENDED_SINGLE:
			queryBuilder.setTables(POI_INTENDED_TBL_NAME);
			queryBuilder.appendWhere(_ID_COLUMN + "="
					+ uri.getLastPathSegment());
			break;
		// With the VISITED queries, the data is obtained from
		// two tables. First that stores the POI details, second
		// that stores the Visit details
		case VISITED:
			queryBuilder.setTables(POI_VISITED_TBL_NAME + " INNER JOIN "
					+ POI_INTENDED_TBL_NAME + " ON " + POI_VISITED_TBL_NAME
					+ "." + POI_ID_COLUMN + "=" + POI_INTENDED_TBL_NAME + "."
					+ POI_ID_COLUMN);
			break;
		case VISITED_SINGLE:
			queryBuilder.setTables(POI_VISITED_TBL_NAME + " i INNER JOIN "
					+ POI_INTENDED_TBL_NAME + " ON " + POI_VISITED_TBL_NAME
					+ "." + POI_ID_COLUMN + "=" + POI_INTENDED_TBL_NAME + "."
					+ POI_ID_COLUMN);
			queryBuilder.appendWhere(_ID_COLUMN + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
				projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	/**
	 * Not used within this app. Will be used externally (if ever)
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;
		int uriType = sURIMatcher.match(uri);
		String id;

		switch (uriType) {
		case INTENDED:
			count = db.update(POI_INTENDED_TBL_NAME, values, selection,
					selectionArgs);
			break;
		case INTENDED_SINGLE:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				count = db.update(POI_INTENDED_TBL_NAME, values, _ID_COLUMN
						+ "=" + id, null);
			} else {
				count = db.update(POI_INTENDED_TBL_NAME, values, selection
						+ " and " + _ID_COLUMN + "=" + id, selectionArgs);
			}
			break;
		case VISITED:
			count = db.update(POI_VISITED_TBL_NAME, values, selection,
					selectionArgs);
			break;
		case VISITED_SINGLE:
			id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				count = db.update(POI_VISITED_TBL_NAME, values, _ID_COLUMN
						+ "=" + id, null);
			} else {
				count = db.update(POI_VISITED_TBL_NAME, values, selection
						+ " and " + _ID_COLUMN + "=" + id, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}
}
