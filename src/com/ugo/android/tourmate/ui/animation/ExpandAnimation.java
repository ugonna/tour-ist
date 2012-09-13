package com.ugo.android.tourmate.ui.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Animation that expands/collapses a view downward/upward.
 * @author ugo
 *
 */
public class ExpandAnimation extends Animation {

	View view;
	int targetHeight;
	boolean expand;
	
	/**
	 * Constructor to use when creating an expanding or collapsing animation
	 * for a view.
	 *  
	 * @param view The {@link View } to animate
	 * @param targetHeight The target height of the <code>View</code>
	 * @param expand <code>true</code> if the view is expanding, <code>false</code>
	 * if it's being collapsed
	 * 
	 * @see android.view.animation.Animation
	 */
	public ExpandAnimation(View view, int targetHeight, boolean expand) {
		super();
		
		this.view = view;
		this.targetHeight = targetHeight;
		this.expand = expand;
	}
	
	@Override
	public boolean willChangeBounds() {
		return true;
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		int newHeight;
		
		if (expand) {
			newHeight = (int)(targetHeight*interpolatedTime);	
		} else {
			newHeight = (int)(targetHeight*(1-interpolatedTime));
		}
		
		view.getLayoutParams().height = newHeight;
		view.requestLayout();
	}
}
