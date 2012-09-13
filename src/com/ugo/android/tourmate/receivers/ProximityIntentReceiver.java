package com.ugo.android.tourmate.receivers;

import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.util.TouristConstants;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;

public class ProximityIntentReceiver extends BroadcastReceiver {
	
	private static final int _NOTIFICATION_ID = 0x1001;

	@Override
	public void onReceive(Context context, Intent intent) {
		String enteringKey = LocationManager.KEY_PROXIMITY_ENTERING;
		String poiName = intent.getStringExtra(TouristConstants.EXTRA_POI_NAME);
		String poiReference = intent.getStringExtra(TouristConstants.EXTRA_POI_REFERENCE);
		String poiId = intent.getStringExtra(TouristConstants.EXTRA_POI_ID);

		boolean entering = intent.getBooleanExtra(enteringKey, false);

		if (entering) {
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			// TODO Make this PendingIntent point to the details activity
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					null, 0);

			String notificationTitle = context.getString(R.string.notification_proximity_entered_title);
			String notificationText = context.getString(R.string.notification_proximity_entered_text)
					+ " " + poiName;

			// Notification.Builder only works on API level > 11
			/*Notification notification = new Notification.Builder(context)
					.setContentIntent(pendingIntent)
					.setContentTitle(notificationTitle)
					.setContentText(notificationText)
					.setAutoCancel(true)
					.setDefaults(
							Notification.DEFAULT_VIBRATE
									| Notification.DEFAULT_LIGHTS)
					.getNotification();*/
			
			Notification notification = configureNotification(new Notification());

			configureNotification(notification);
			notification.setLatestEventInfo(context, notificationTitle, notificationText, pendingIntent);

			notificationManager.notify(_NOTIFICATION_ID, notification);
		}
	}

	private Notification configureNotification(Notification notification) {

		notification.icon = R.drawable.notification_location;
		notification.when = System.currentTimeMillis();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_LIGHTS;

		notification.ledARGB = Color.GREEN;
		notification.ledOnMS = 1500;
		notification.ledOffMS = 1500;

		return notification;
	}
}
