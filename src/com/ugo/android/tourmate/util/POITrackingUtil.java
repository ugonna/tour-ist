package com.ugo.android.tourmate.util;


public class POITrackingUtil {

	private static boolean initialized;
	private static android.media.AudioManager audio;
	
	public static int getRingtoneMaximum() throws IllegalStateException {
		if (audio != null) {
			return audio.getStreamMaxVolume(android.media.AudioManager.STREAM_RING);
		} else {
			throw new IllegalStateException("POITrackingUtil has not been initialized." +
					" First call POITrackingUtil.initialize()");
		}
	}
	
	public static int getRingtoneVolume() {
		if (audio != null) {
			return audio.getStreamVolume(android.media.AudioManager.STREAM_RING);
		} else {
			throw new IllegalStateException("POITrackingUtil has not been initialized." +
					" First call POITrackingUtil.initialize()");
		}
	}
	
	public static void initialize(android.content.Context context) {
		
		if (!initialized) {
			audio = (android.media.AudioManager) context.getSystemService(
				android.content.Context.AUDIO_SERVICE);
			initialized = true;
		}
	}
}
