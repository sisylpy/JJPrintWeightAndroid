package com.swolo.lpy.pysx.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CanScrollViewPager extends ViewPager {
	
	private boolean isScrollable = true;  
	
	public CanScrollViewPager(Context context) {  
        super(context);  
    }  
  
    public CanScrollViewPager(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
    
    public void setCanScroll(boolean isCanScroll){  
        this.isScrollable = isCanScroll;  
    }  
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isScrollable == false) {
			return false;
		} else {
			return super.onTouchEvent(ev);
		}

	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (isScrollable == false) {
			return false;
		} else {
			return super.onInterceptTouchEvent(ev);
		}

	}
}
