package com.ugo.android.tourmate.ui.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import com.ugo.android.tourmate.R;

public class POIHeaderTextViewFactory implements ViewFactory {

	private Context context;
	
	public POIHeaderTextViewFactory(Context context) {
		this.context = context;
	}
	
	@Override
	public View makeView() {
		TextView t = new TextView(context);
        t.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        t.setTextSize(
        		context.getResources().getDimension(
        				R.dimen.text_size_tiny));
        t.setSingleLine(false);
        return t;
	}

}
