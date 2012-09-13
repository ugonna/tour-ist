package com.ugo.android.tourmate.ui.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

public class ARSensorView extends View implements SensorEventListener {

	private final String TAG = ARSensorView.class.getSimpleName();
	private SensorManager sensorMan;
	private Location currLocation;
	private boolean locationChanged;
	public float direction;
	public double inclination;
	public double rollingX;
	public double rollingZ;
	public float kFilteringFactor;
	private final float xAngleWidth;
	private final float yAngleWidth;
	private int screenWidth;
	private int screenHeight;
	volatile List<ARPointView> arViews = new ArrayList<ARPointView>(0);

	public ARSensorView(Context context, Location currentLocation) {
		super(context);

		this.direction = (float) 22.4;
		this.rollingX = (float) 0;
		this.rollingZ = (float) 0;
		this.kFilteringFactor = (float) 0.05;
		this.xAngleWidth = 29;
		this.yAngleWidth = 19;
		this.currLocation = currentLocation;
		this.locationChanged = true;
	}

	public void addARView(ARPointView view) {
		arViews.add(view);
	}

	public void clearARViews() {
		arViews.clear();
	}

	public void start() {
		sensorMan = (SensorManager) getContext()
				.getSystemService(Context.SENSOR_SERVICE);
		sensorMan.registerListener(this,
				sensorMan.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorMan.registerListener(this,
				sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	public void finish() {
		sensorMan.unregisterListener(this);
	}

	// BEGIN SensorEventListener methods
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		float localDirection;
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			float tmp = event.values[0];

			if (tmp < 0)
				tmp = tmp + 360;

			direction = (float) ((tmp * kFilteringFactor) + (direction * (1.0 - kFilteringFactor)));

			if (direction < 0)
				localDirection = 360 + direction;
			else
				localDirection = direction;

			if (locationChanged) {
				updateLayouts(localDirection, (float) inclination, currLocation);
				// Never update with location again.
				locationChanged = false;
			} else {
				updateLayouts(localDirection, (float) inclination, null);
			}
		}

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			rollingZ = (event.values[2] * kFilteringFactor)
					+ (rollingZ * (1.0 - kFilteringFactor));
			rollingX = (event.values[0] * kFilteringFactor)
					+ (rollingX * (1.0 - kFilteringFactor));

			if (rollingZ != 0.0) {
				inclination = Math.atan(rollingX / rollingZ);// + Math.PI / 2.0;
			} else if (rollingX < 0) {
				inclination = Math.PI / 2.0;
			} else if (rollingX >= 0) {
				inclination = 3 * Math.PI / 2.0;
			}

			// convert to degrees
			inclination = inclination * (360 / (2 * Math.PI));

			// flip!
			if (inclination < 0) {
				inclination = inclination + 90;
			} else {
				inclination = inclination - 90;
			}

		}
		if (direction < 0) {
			localDirection = 360 + direction;
		} else {
			localDirection = direction;
		}

		if (locationChanged) {
			updateLayouts(localDirection, (float) inclination, currLocation);
			// Never update with location again.
			locationChanged = false;
		} else {
			updateLayouts(localDirection, (float) inclination, null);
		}

		this.postInvalidate();

	}

	// END SensorEventListener methods

	public void removeARView(ARPointView view) {
		arViews.remove(view);
	}

	/**
	 * @param screenHeight
	 *            the screenHeight to set
	 */
	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	/**
	 * @param screenWidth
	 *            the screenWidth to set
	 */
	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		finish();
	}

	private float calcXvalue(float leftArm, float rightArm, float az) {
		float offset;
		if (leftArm > rightArm) {
			if (az >= leftArm) {
				offset = az - leftArm;
			}
			if (az <= rightArm) {
				offset = 360 - leftArm + az;
			} else {
				offset = az - leftArm;
			}
		} else {
			offset = az - leftArm;
		}

		return (offset / xAngleWidth) * screenWidth;
	}

	private float calcYvalue(float lowerArm, float upperArm, float inc) {
		// distance in degrees to the lower arm
		float offset = ((upperArm - yAngleWidth) - inc) * -1;
		return screenHeight - ((offset / yAngleWidth) * screenHeight);
	}

	private void updateLayouts(float azi, float zAngle, Location l) {
		if (azi != -1) {
			// Process the accelerometer stuff
			float leftArm = azi - (xAngleWidth / 2);
			float rightArm = azi + (xAngleWidth / 2);
			if (leftArm < 0)
				leftArm = leftArm + 360;
			if (rightArm > 360)
				rightArm = rightArm - 360;

			float upperArm = zAngle + (yAngleWidth / 2);
			float lowerArm = zAngle - (yAngleWidth / 2);

			if (arViews.size() == 0) {
				return;
			}

			for (ARPointView pointView : arViews) {
				// If we have a location, and the view has one, update it's data
				try {
					if (l != null && pointView.getLocation() != null) {
						pointView.setAzimuth(l.bearingTo(pointView
								.getLocation()));
						if (pointView.getAzimuth() < 0) {
							pointView.setAzimuth(360 + pointView.getAzimuth());
						}
						if (l.hasAltitude()
								&& pointView.getLocation().hasAltitude()) {
							pointView
									.setInclination((float) Math
											.atan(((pointView.getLocation()
													.getAltitude() - l
													.getAltitude()) / l
													.distanceTo(pointView
															.getLocation()))));
						}
						pointView.setDistance(l.distanceTo(pointView
								.getLocation()));
					}

					int xValue = (int) calcXvalue(leftArm, rightArm,
							pointView.getAzimuth());
					int yValue = (int) calcYvalue(lowerArm, upperArm,
							pointView.getInclination());

					pointView.layout(xValue, yValue, pointView.getRight(),
							pointView.getBottom()); // TODO Check (l, t, r, b)
				} catch (Exception x) {
					Log.e(TAG, x.getMessage());
				}
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);
		// TODO Remove the above if not needed
		android.graphics.Paint p = new android.graphics.Paint();
		p.setColor(android.graphics.Color.WHITE);

		// Old
		/*canvas.drawText("Compass: " + String.valueOf(direction), 20, 20, p);

		canvas.drawText("Inclination: " + String.valueOf(inclination), 150, 20,
				p);

		if (currLocation != null) {
			canvas.drawText(
					"Location: Lat - "
							+ String.valueOf(currLocation.getLatitude())
							+ " Lon - "
							+ String.valueOf(currLocation.getLongitude()), 350,
					20, p);
		} else {
			canvas.drawText("Location: unavailable: ", 350, 20, p);
		}*/

		// Temporarily new
		if (currLocation != null) {
			canvas.drawText(
					"Location: Lat - "
							+ String.valueOf(currLocation.getLatitude())
							+ " Lon - "
							+ String.valueOf(currLocation.getLongitude()), 20,
					20, p);
		} else {
			canvas.drawText("Location: unavailable: ", 350, 20, p);
		}
		
		for (ARPointView arPoint : arViews) {
			arPoint.draw(canvas);
		}
	}
}
