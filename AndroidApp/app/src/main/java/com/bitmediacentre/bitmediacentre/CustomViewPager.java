package com.bitmediacentre.bitmediacentre;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class CustomViewPager extends ViewPager {
    private boolean isSwipeEnabled = true;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return isSwipeEnabled && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return isSwipeEnabled && super.onTouchEvent(event);
    }

    public void setSwipeEnabled(boolean enabled) {
        this.isSwipeEnabled = enabled;
    }
}
