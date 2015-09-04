package util.compatlibrary.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.ListView;


public class NestedListView extends ListView implements NestedScrollingChild {

    private interface NestedTouchHelper {
        void onInterceptTouchEvent(@NonNull MotionEvent ev);

        void onTouchEvent(@NonNull MotionEvent ev);

        void requestDisallowInterceptTouchEvent(boolean disallowIntercept);
    }

    private static class NestedTouchHelperLollipop implements NestedTouchHelper {

        @Override
        public void onInterceptTouchEvent(@NonNull MotionEvent ev) {
        }

        @Override
        public void onTouchEvent(@NonNull MotionEvent ev) {
        }

        @Override
        public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

    private static class NestedTouchHelperBase implements NestedTouchHelper {
        NestedListView listView;

        public NestedTouchHelperBase(NestedListView listView) {
            this.listView = listView;

            final ViewConfiguration configuration = ViewConfiguration.get(listView.getContext());
            mTouchSlop = configuration.getScaledTouchSlop();
            mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
            mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
            //mOverscrollDistance = configuration.getScaledOverscrollDistance();
            //mOverflingDistance = configuration.getScaledOverflingDistance();

            //mDensityScale = getContext().getResources().getDisplayMetrics().density;
        }

        private int mTouchSlop;
        private int mMinimumVelocity;
        private int mMaximumVelocity;


        private int mActivePointerId = INVALID_POINTER;


        private final int[] mScrollOffset = new int[2];
        private final int[] mScrollConsumed = new int[2];
        private int mNestedYOffset;
        private int mLastMotionY;

        private boolean mIsBeingDragged = false;


        private static final int INVALID_POINTER = -1;
        private VelocityTracker mVelocityTracker;

        private void initOrResetVelocityTracker() {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            } else {
                mVelocityTracker.clear();
            }
        }

        private void initVelocityTrackerIfNotExists() {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
        }

        private void recycleVelocityTracker() {
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
        }

        @Override
        public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (disallowIntercept) {
                recycleVelocityTracker();
            }
        }

        private boolean inChild(int x, int y) {
            if (listView.getChildCount() > 0) {
                final int scrollY = listView.getScrollY();
                final View child = listView.getChildAt(findRow(y) - listView.getFirstVisiblePosition());

                return child != null &&
                        !(y < child.getTop() - scrollY
                                || y >= child.getBottom() - scrollY
                                || x < child.getLeft()
                                || x >= child.getRight()
                        );
            }
            return false;
        }

        int findRow(int y) {
            int childCount = listView.getChildCount();
            if (childCount > 0) {
                if (!listView.isStackFromBottom()) {
                    for (int i = 0; i < childCount; i++) {
                        View v = listView.getChildAt(i);
                        if (y <= v.getBottom()) {
                            return listView.getFirstVisiblePosition() + i;
                        }
                    }
                } else {
                    for (int i = childCount - 1; i >= 0; i--) {
                        View v = listView.getChildAt(i);
                        if (y >= v.getTop()) {
                            return listView.getFirstVisiblePosition() + i;
                        }
                    }
                }
            }
            return INVALID_POSITION;
        }


        @Override
        public void onInterceptTouchEvent(@NonNull MotionEvent ev) {
            final int action = ev.getAction();
            if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
                return;
            }

            if (listView.getScrollY() == 0 && !ViewCompat.canScrollVertically(listView, 1)) {
                return;
            }

            switch (action & MotionEventCompat.ACTION_MASK) {
                case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                * Locally do absolute value. mLastMotionY is set to the y value
                * of the down event.
                */
                    final int activePointerId = mActivePointerId;
                    if (activePointerId == INVALID_POINTER) {
                        // If we don't have a valid id, the touch down wasn't on content.
                        break;
                    }

                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);


                    final int y = (int) MotionEventCompat.getY(ev, pointerIndex);
                    final int yDiff = Math.abs(y - mLastMotionY);
                    if (yDiff > mTouchSlop
                            /*&& (listView.getNestedScrollAxes() & ViewCompat.SCROLL_AXIS_VERTICAL) == 0*/) {
                        mIsBeingDragged = true;
                        mLastMotionY = y;
                        initVelocityTrackerIfNotExists();
                        mVelocityTracker.addMovement(ev);
                        mNestedYOffset = 0;
                        final ViewParent parent = listView.getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    break;
                }

                case MotionEvent.ACTION_DOWN: {
                    final int y = (int) ev.getY();
                    if (!inChild((int) ev.getX(), y)) {
                        mIsBeingDragged = false;
                        recycleVelocityTracker();
                        break;
                    }

                    mLastMotionY = y;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                    initOrResetVelocityTracker();
                    mVelocityTracker.addMovement(ev);

                    listView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                    break;
                }


                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:

                    mIsBeingDragged = false;
                    mActivePointerId = INVALID_POINTER;
                    recycleVelocityTracker();
                    listView.stopNestedScroll();
                    break;
                case MotionEventCompat.ACTION_POINTER_UP:
                    onSecondaryPointerUp(ev);
                    break;
            }


        }

        @Override
        public void onTouchEvent(@NonNull MotionEvent ev) {

            initVelocityTrackerIfNotExists();

            MotionEvent vtev = MotionEvent.obtain(ev);

            final int actionMasked = MotionEventCompat.getActionMasked(ev);

            if (actionMasked == MotionEvent.ACTION_DOWN) {
                mNestedYOffset = 0;
            }
            vtev.offsetLocation(0, mNestedYOffset);

            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN: {

                    mLastMotionY = (int) ev.getY();
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    listView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                    break;
                }
                case MotionEvent.ACTION_MOVE:
                    int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                    if (activePointerIndex < 0)
                        activePointerIndex = 0;

                    final int y = (int) MotionEventCompat.getY(ev, activePointerIndex);
                    int deltaY = mLastMotionY - y;
                    if (listView.dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                        deltaY -= mScrollConsumed[1];
                        vtev.offsetLocation(0, mScrollOffset[1]);
                        mNestedYOffset += mScrollOffset[1];
                    }
                    if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
                        final ViewParent parent = listView.getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                        mIsBeingDragged = true;
                        if (deltaY > 0) {
                            deltaY -= mTouchSlop;
                        } else {
                            deltaY += mTouchSlop;
                        }
                    }
                    if (mIsBeingDragged) {
                        mLastMotionY = y - mScrollOffset[1];

                        final int oldY = listView.getScrollY();

                        final int scrolledDeltaY = listView.getScrollY() - oldY;
                        final int unconsumedY = deltaY - scrolledDeltaY;
                        if (listView.dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, mScrollOffset)) {
                            mLastMotionY -= mScrollOffset[1];
                            vtev.offsetLocation(0, mScrollOffset[1]);
                            mNestedYOffset += mScrollOffset[1];
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mIsBeingDragged) {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        int initialVelocity = (int) VelocityTrackerCompat.getYVelocity(velocityTracker, mActivePointerId);

                        if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                            flingWithNestedDispatch(-initialVelocity);
                        }

                        mActivePointerId = INVALID_POINTER;
                        endDrag();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if (mIsBeingDragged && listView.getChildCount() > 0) {
                        mActivePointerId = INVALID_POINTER;
                        endDrag();
                    }
                    break;
                case MotionEventCompat.ACTION_POINTER_DOWN: {
                    final int index = MotionEventCompat.getActionIndex(ev);
                    mLastMotionY = (int) MotionEventCompat.getY(ev, index);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                    break;
                }
                case MotionEventCompat.ACTION_POINTER_UP:
                    onSecondaryPointerUp(ev);
                    mLastMotionY = (int) MotionEventCompat.getY(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                    break;
            }

            if (mVelocityTracker != null) {
                mVelocityTracker.addMovement(vtev);
            }
            vtev.recycle();

        }


        private void onSecondaryPointerUp(MotionEvent ev) {
            final int pointerIndex = (ev.getAction() &
                    MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
            final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
            if (pointerId == mActivePointerId) {
                // This was our active pointer going up. Choose a new
                // active pointer and adjust accordingly.
                // TODO: Make this decision more intelligent.
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mLastMotionY = (int) MotionEventCompat.getY(ev, newPointerIndex);
                mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                if (mVelocityTracker != null) {
                    mVelocityTracker.clear();
                }
            }
        }

        private void endDrag() {
            mIsBeingDragged = false;
        }

        private void flingWithNestedDispatch(int velocityY) {
            final int scrollY = listView.getScrollY();
            final boolean canFling = (scrollY > 0 || velocityY > 0) && (velocityY < 0);
            if (!listView.dispatchNestedPreFling(0, velocityY)) {
                listView.dispatchNestedFling(0, velocityY, canFling);
            /*
            if (canFling) {
               fling(velocityY);
            }
            */
            }
        }

    }


    private final NestedScrollingChildHelper mScrollingChildHelper;
    private final NestedTouchHelper mNestedTouchHelper;

    public NestedListView(Context context) {
        this(context, null);
    }

    public NestedListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);

        mNestedTouchHelper = Build.VERSION.SDK_INT > 20 ? new NestedTouchHelperLollipop() : new NestedTouchHelperBase(this);

    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent ev) {
        mNestedTouchHelper.onInterceptTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        mNestedTouchHelper.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }


    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        mNestedTouchHelper.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
/*
    @Override
    public int getNestedScrollAxes() {
        if (Build.VERSION.SDK_INT < 21)
            return ViewCompat.SCROLL_AXIS_VERTICAL;
        else
            return super.getNestedScrollAxes();
    }
*/

}
