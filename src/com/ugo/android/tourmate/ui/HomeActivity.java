package com.ugo.android.tourmate.ui;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;

import com.ugo.android.tourmate.R;
import com.ugo.android.tourmate.util.GAnalyticsUtil;

public class HomeActivity extends BaseActivity {
    /** Called when the activity is first created. */
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
    	
        this.setContentView(R.layout.activity_main);

        this.getActivityHelper().setupActionBar(null, 0);
        
        GAnalyticsUtil.getInstance(this).trackPageView("/Home");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//TODO Add more relevant menu items
        //getMenuInflater().inflate(R.menu.refresh_menu_items, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if (item.getItemId() == R.id.menu_refresh) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupHomeActivity();
        
     // Gesture detection
        gestureDetector = new GestureDetector(new SwipeGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
        
        View slidingDrawerHandle = findViewById(R.id.recommend_sliding_drawer);
        
        // Will be null if in landscape mode. I removed it
        if (slidingDrawerHandle != null) {
        	// Add a left/right swipe listener to switch views in the ViewFlipper
        	// of the SlidingDrawer's handle.
        	slidingDrawerHandle.setOnTouchListener(gestureListener);
        }
    }
    
    class SwipeGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	// Left swipe
                	RecommendationBarFragment.moveFlipperNext();
                }  /*else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	// Right swipe
                	RecommendationBarFragment.moveFlipperPrevious();
                }*/ // No right swipe
            } catch (Exception e) {
                // nothing
            }
            return false;
		}
	}
}