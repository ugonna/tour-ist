package com.ugo.android.tourmate.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.ugo.android.tourmate.R;

public class CurrentPositionOverlay extends Overlay {

	Context mContext;
	GeoPoint mPoint;
	float mAccuracy;

	public CurrentPositionOverlay(Context context, GeoPoint point,
			float accuracy) {
		super();
		this.mContext = context;
		this.mPoint = point;
		this.mAccuracy = accuracy;
	}

	void setCurrentPosition(GeoPoint point, float accuracy) {
		this.mPoint = point;
		this.mAccuracy = accuracy;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
		Projection projection = mapView.getProjection();
		Point center = new Point();

		int radius = (int) (projection.metersToEquatorPixels(mAccuracy));
		projection.toPixels(this.mPoint, center);

		// Draw the accuracy-radius circle
		Paint accuracyPaint = new Paint();
		accuracyPaint.setAntiAlias(true);
		accuracyPaint.setStrokeWidth(2.0f);
		accuracyPaint.setColor(0xff6666ff);
		accuracyPaint.setStyle(Style.STROKE);

		canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

		accuracyPaint.setColor(0x186666ff);
		accuracyPaint.setStyle(Style.FILL);
		canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

		// Draw the marker
		Paint markerPaint = new Paint();
		accuracyPaint.setStrokeWidth(1.0f);
		markerPaint.setStyle(Style.STROKE);
		accuracyPaint.setColor(0xff000000);

		Bitmap markerBitmap = BitmapFactory.decodeResource(
				mContext.getResources(), R.drawable.map_marker_curr_loc);
		canvas.drawBitmap(markerBitmap, center.x, center.y, markerPaint);
	}
}
