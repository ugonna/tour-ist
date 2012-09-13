package com.ugo.android.tourmate.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TourDatabaseHelper extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "tour.db";
	private final static int DATABASE_VERSION = 2;
	
	public final static String POI_INTENDED_TBL_NAME = "poi_intended";
	public final static String POI_VISITED_TBL_NAME = "poi_visited";
	public final static String POI_ACTIONS_TBL_NAME = "poi_actions";
	
	public final static String _ID_COLUMN = "_id";
	public final static String POI_ID_COLUMN = "poi_id";
	public final static String POI_REFERENCE_COLUMN = "reference_key";
	public final static String POI_NAME_COLUMN = "name";
	public final static String POI_PHONE_COLUMN = "phone_number";
	public final static String POI_ADDRESS_COLUMN = "address";
	public final static String POI_VICINITY_COLUMN = "vicinity";
	public final static String POI_ICON_URL_COLUMN = "img_url";
	public final static String POI_LOCATION_COLUMN = "lat_long";
	public final static String POI_TYPES_COLUMN = "types";
	public final static String POI_DATE_ADDED_COLUMN = "added_date";
	
	public final static String POI_V_DATE_COLUMN = "visit_date";
	public final static String POI_V_TIME_COLUMN = "visit_time";
	public final static String POI_V_DURATION_COLUMN = "visit_duration";
	public final static String POI_V_COUNT_COLUMN = "visit_count";
	
	public final static String POI_ACTION_COLUMN = "action";
	public final static String POI_ACTION_VALUE_COLUMN = "value";
	
	private final static String CREATE_POI_INTENDED_TABLE = 
		"CREATE TABLE " + POI_INTENDED_TBL_NAME + "(" +
			_ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			POI_ID_COLUMN + " TEXT UNIQUE," +
			POI_REFERENCE_COLUMN + " TEXT," +
			POI_NAME_COLUMN + " TEXT," +
			POI_PHONE_COLUMN + " TEXT," +
			POI_ADDRESS_COLUMN + " TEXT," +
			POI_VICINITY_COLUMN + " TEXT," +
			POI_ICON_URL_COLUMN + " TEXT," +
			POI_LOCATION_COLUMN + " TEXT," +
			POI_TYPES_COLUMN + " TEXT," +
			POI_DATE_ADDED_COLUMN + " TEXT" +
			");";
	private final static String CREATE_POI_VISITED_TABLE = 
		"CREATE TABLE " + POI_VISITED_TBL_NAME + "(" +
			_ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			POI_ID_COLUMN + " TEXT NOT NULL " +
					"REFERENCES " + POI_INTENDED_TBL_NAME + " (" + POI_ID_COLUMN + ")," +
			POI_V_DATE_COLUMN + " TEXT NOT NULL," +
			POI_V_TIME_COLUMN + " TEXT," +
			POI_V_DURATION_COLUMN + " TEXT," +
			POI_V_COUNT_COLUMN + " INTEGER," +
			"CONSTRAINT apples UNIQUE (" + POI_ID_COLUMN + ", "+ POI_V_DATE_COLUMN +")" +
			");";
	private final static String CREATE_POI_ACTIONS_TABLE = 
		"CREATE TABLE " + POI_ACTIONS_TBL_NAME + "(" +
			_ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			POI_ID_COLUMN + " TEXT NOT NULL " +
					"REFERENCES " + POI_INTENDED_TBL_NAME + " (" + POI_ID_COLUMN + ")," +
			POI_ACTION_COLUMN + " TEXT NOT NULL," +
			POI_ACTION_VALUE_COLUMN + " TEXT," +
			"CONSTRAINT mango UNIQUE (" + POI_ID_COLUMN + ", "+ POI_ACTION_COLUMN +")" +
			");";
	
	public TourDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_POI_INTENDED_TABLE);
		db.execSQL(CREATE_POI_VISITED_TABLE);
		db.execSQL(CREATE_POI_ACTIONS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + POI_INTENDED_TBL_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + POI_VISITED_TBL_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + POI_ACTIONS_TBL_NAME);
		
		this.onCreate(db);
	}
}
