package in.altilogic.prayogeek.fragments;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ScrollableViewPager extends ViewPager {
    private boolean enabled;

    public ScrollableViewPager(Context context) {
        super(context);
        this.enabled = true;
    }

    public ScrollableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.enabled) {
            boolean isIntercept = false;
            try{
                isIntercept = super.onInterceptTouchEvent(event);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return isIntercept;
        }

        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
