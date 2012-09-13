package com.ugo.android.tourmate.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.view.View;

public class ARPointView extends View {

	private volatile float azimuth;
	private volatile float distance;
	private volatile float inclination;
	private Location location;
	protected Paint pointPaint;
	protected final int POINT_RADIUS = 5;
	
	public ARPointView(Context context) {
		super(context);
		
		pointPaint = new Paint();
		pointPaint.setColor(Color.WHITE);
		pointPaint.setStyle(Paint.Style.FILL);
		pointPaint.setAlpha(100);
	}
	
	public ARPointView(Context context, Location objectLocation) {
		this(context);
		this.location = objectLocation;
		
		pointPaint = new Paint();
		pointPaint.setColor(Color.WHITE);
		pointPaint.setStyle(Paint.Style.FILL);
	}

	/**
	 * Sets the azimuth between the device and magnetic north.
	 * @return the azimuth
	 */
	public float getAzimuth() {
		return azimuth;
	}
	
	/**
	 * Gets the distance between the device and the reference object.
	 * @return the distance
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * Gets the angle between the direction the phone is facing and the
	 * horizon.
	 * @return the inclination
	 */
	public float getInclination() {
		return inclination;
	}

	/**
	 * Gets the location of the reference object
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Sets the azimuth between the device and magnetic north.
	 * @param azimuth the azimuth to set
	 */
	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
	}

	/**
	 * Sets the distance between the device and the reference object.
	 * @param distance the distance to set
	 */
	public void setDistance(float distance) {
		this.distance = distance;
	}

	/**
	 * Sets the angle between the direction the phone is facing and the
	 * horizon.
	 * @param inclination the inclination to set
	 */
	public void setInclination(float inclination) {
		this.inclination = inclination;
	}

	/**
	 * Sets the location of the reference object
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawCircle(getLeft(), getTop(), POINT_RADIUS, pointPaint);
	}
}
