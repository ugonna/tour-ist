package com.ugo.android.tourmate.services;

import java.util.HashMap;
import java.util.Map;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.ugo.android.tourmate.receivers.ProximityIntentReceiver;
import com.ugo.android.tourmate.util.TouristConstants;

public class ProximityMonitorService extends Service {

	private static final int _PROXIMITY_ALERT_EXPIRY_TIME = 15000; // 20 seconds
	private static final int _PROXIMITY_CHECK_PERIOD = 900000;	// 15 minutes
	private static final int _PROXIMITY_ALERT_RADIUS = 1000;
	private HashMap<PendingIntent, Location> mProximityIntentMap;
	private ProximityIntentReceiver mProxIntentReceiver;
	private Handler mCheckProximityHandler;
	
	private Runnable mCheckProximityTask = new Runnable() {
		
		@Override
		public void run() {
			for (Map.Entry<PendingIntent, Location> entry : mProximityIntentMap.entrySet()) {
				// Removes the PendingAlert from...
				removeProximityAlert(entry.getKey());
				// ... and adds it again.
				addProximityAlert(entry.getKey(), entry.getValue());
			}
			
			mCheckProximityHandler.postDelayed(mCheckProximityTask, _PROXIMITY_CHECK_PERIOD);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mProximityIntentMap = new HashMap<PendingIntent, Location>(1);
		mProxIntentReceiver = new ProximityIntentReceiver();

		IntentFilter filter = new IntentFilter(
				TouristConstants.INTENT_POI_PROXIMITY_ALERT);
		this.getApplicationContext().registerReceiver(mProxIntentReceiver,
				filter);
		
		mCheckProximityHandler = new Handler();
		mCheckProximityHandler.removeCallbacks(mCheckProximityTask);
		mCheckProximityHandler.postDelayed(mCheckProximityTask, _PROXIMITY_CHECK_PERIOD);

		Log.i(ProximityMonitorService.class.getName(),
				ProximityMonitorService.class.getSimpleName() + " service started");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String poiId = intent.getStringExtra(TouristConstants.EXTRA_POI_ID);
		String poiReference = intent.getStringExtra(TouristConstants.EXTRA_POI_REFERENCE);
		String poiName = intent.getStringExtra(TouristConstants.EXTRA_POI_NAME);
		Location poiLocation = intent.getParcelableExtra(TouristConstants.EXTRA_POI_LOCATION);
		
		// All values must be not-null
		if (poiId != null && poiName != null && poiLocation != null) {
			addProximityAlert(poiId, poiReference, poiName, poiLocation);
		}
		return START_STICKY;
	}

	/**
	 * Add proximity alerts that have already been configured through
	 * the PendingAlert.
	 * 
	 * @param pendingIntent the already configured PendingAlert
	 * @param poiLocation the location that corresponds to the PendingAlert
	 */
	private void addProximityAlert(PendingIntent pendingIntent,
			Location poiLocation) {

		((LocationManager) this.getApplicationContext().getSystemService(
				Context.LOCATION_SERVICE)).addProximityAlert(
				poiLocation.getLatitude(), poiLocation.getLongitude(),
				_PROXIMITY_ALERT_RADIUS, _PROXIMITY_ALERT_EXPIRY_TIME,
				pendingIntent);
	}

	/**
	 * Configures a new PendingAlert putting the POI ID and name into it
	 * and using the given Location to add a proximity alert for that location
	 * 
	 * @param poiId the POI ID
	 * @param poiName the POI name
	 * @param poiLocation the POI Location
	 */
	private void addProximityAlert(String poiId, String poiReference, String poiName,
			Location poiLocation) {
		// Add the reference token to the intent
		Intent intent = new Intent(TouristConstants.INTENT_POI_PROXIMITY_ALERT);

		intent.putExtra(TouristConstants.EXTRA_POI_ID, poiId);
		intent.putExtra(TouristConstants.EXTRA_POI_REFERENCE, poiReference);
		intent.putExtra(TouristConstants.EXTRA_POI_NAME, poiName);

		PendingIntent proximityIntent = PendingIntent.getBroadcast(
				this.getApplicationContext(), 0, intent, 0);

		((LocationManager) this.getApplicationContext().getSystemService(
				Context.LOCATION_SERVICE)).addProximityAlert(
				poiLocation.getLatitude(), poiLocation.getLongitude(),
				_PROXIMITY_ALERT_RADIUS, _PROXIMITY_ALERT_EXPIRY_TIME,
				proximityIntent);

		mProximityIntentMap.put(proximityIntent, poiLocation);
	}
	
	private void removeProximityAlert(PendingIntent pendingIntent) {
		
		((LocationManager) this.getApplicationContext().getSystemService(
				Context.LOCATION_SERVICE)).removeProximityAlert(pendingIntent);
	}
}
