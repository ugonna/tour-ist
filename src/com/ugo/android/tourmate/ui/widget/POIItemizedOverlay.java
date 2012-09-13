package com.ugo.android.tourmate.ui.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("rawtypes")
public class POIItemizedOverlay extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	public POIItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
	}

	@Override
	protected OverlayItem createItem(int index) {
		return mOverlays.get(index);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	public void setOverlays(ArrayList<OverlayItem> overLays) {
		this.mOverlays = overLays;
		populate();
	}

}
