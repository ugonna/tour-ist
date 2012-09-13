package com.ugo.android.tourmate.util.media;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/**
 * Utility class to lazy load images unto an {@link android.widget.ImageView}
 * from a URI.
 * @author ugo
 *
 */
public class ImageFactory {

	// TODO Cache images on SD card.
	
	private final static Map<String, Drawable> drawableMap;
	
	static {
		drawableMap = new HashMap<String, Drawable>();
	}
	
	public static Drawable fetchDrawable(final String url) {
		try {
			InputStream inStream = fetch(url);
			Drawable drawable = Drawable.createFromStream(inStream, "src");
			inStream.close();
			drawableMap.put(url, drawable);
			return drawable;
		} catch (Exception ex) {
			Log.e(ImageFactory.class.getName(), "Error fetching image", ex);
			return null;
		}

	}
	
	public static void fetchDrawableUntoImageView(final String url, final ImageView imageView) {
		if (drawableMap.containsKey(url)) {
			imageView.setImageDrawable(drawableMap.get(url));
			imageView.refreshDrawableState();
			return;
		}
		
		new ImageFactory().new ImageFetcherASync().execute(url, imageView);
	}
	
	private static InputStream fetch(final String url) throws MalformedURLException, IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response = httpClient.execute(request);
        return response.getEntity().getContent();
	}
	
	class ImageFetcherASync extends AsyncTask<Object, Void, Drawable> {

		ImageView imageView;
		@Override
		protected Drawable doInBackground(Object... params) {
			Drawable drawable = fetchDrawable((String)params[0]);
			imageView = (ImageView) params[1];
			return drawable;
		}
		
		@Override
		protected void onPostExecute(Drawable result) {
			imageView.setImageDrawable(result);
			imageView.refreshDrawableState();
		}
	}
}
