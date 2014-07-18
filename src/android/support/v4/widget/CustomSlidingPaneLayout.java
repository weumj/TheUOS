package android.support.v4.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomSlidingPaneLayout extends SlidingPaneLayout {
	// PagerInterface l;

	public CustomSlidingPaneLayout(Context context) {
		super(context);
		// l = (PagerInterface) context;
	}

	public CustomSlidingPaneLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// l = (PagerInterface) context;
	}

	public CustomSlidingPaneLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// l = (PagerInterface) context;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (/* l.sendCommand(Type.INDEX, null).equals(0) || */isOpen())
			return super.onInterceptTouchEvent(arg0);
		else
			return false;
	}
}
