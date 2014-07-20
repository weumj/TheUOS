package com.uoscs09.theuos.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import com.uoscs09.theuos.R;
/** drag and drop이 가능한 {@code ListView} <br>
 * stackoverflow에서 검색함*/
public class DropAndDragListView extends ListView {
	private Context mContext;
	private ImageView mDragView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private int mDragPos;      // 드래그 아이템의 위치
    private int mFirstDragPos; // 드래그 아이템의 원래 위치
    private int mDragPoint;
    private int mCoordOffset;  // 스크린에서의 위치와 뷰내에서의 위치의 차이
    private DragListener mDragListener;
    private DropListener mDropListener;
    private int mUpperBound;
    private int mLowerBound;
    private int mHeight;
    private Rect mTempRect = new Rect();
    private Bitmap mDragBitmap;
    private final int mTouchSlop;
    private int mItemHeightNormal;
   // private int mItemHeightExpanded;
    private int dndViewId;
    private int dragImageX = 0;
    
	public DropAndDragListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}
	
	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDragListener != null || mDropListener != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    int itemnum = pointToPosition(x, y);
                    if (itemnum == AdapterView.INVALID_POSITION) {
                        break;
                    }
                    View item = getChildAt(itemnum - getFirstVisiblePosition()); // 드래그 아이템
                    mItemHeightNormal = item.getHeight(); // 아이템의 높이
                    //mItemHeightExpanded = mItemHeightNormal * 2; // 아이템이 드래그 할때 벌어지질 높이
                    mDragPoint = y - item.getTop();
                    mCoordOffset = ((int)ev.getRawY()) - y;
                    View dragger = item.findViewById(dndViewId); // 드래그 이벤트를 할 아이템내에서의 뷰
                    if(dragger == null)
                    	dragger = item;
                    Rect r = mTempRect;
                    dragger.getDrawingRect(r);
                    if (x < r.right * 2) {
                        item.setDrawingCacheEnabled(true);
                        // 드래그 하는 아이템의 이미지 캡쳐
                        Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache(true));
                        startDragging(bitmap, y);
                        mDragPos = itemnum;
                        mFirstDragPos = mDragPos;
                        mHeight = getHeight();
                        // 스크롤링을 위한 값 획득
                        int touchSlop = mTouchSlop;
                        mUpperBound = Math.min(y - touchSlop, mHeight / 3);
                        mLowerBound = Math.max(y + touchSlop, mHeight * 2 /3);
                        return false;
                    }
                    mDragView = null;
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
        if ((mDragListener != null || mDropListener != null) && mDragView != null) {
            int action = ev.getAction(); 
            switch (action) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Rect r = mTempRect;
                    mDragView.getDrawingRect(r);
                    stopDragging();
                    if (mDropListener != null && mDragPos >= 0 && mDragPos < getCount()) {
                        mDropListener.drop(mFirstDragPos, mDragPos);
                    }
                    unExpandViews(false);
                    break;
                    
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    dragView(x, y);
                    int itemnum = getItemForPosition(y);
                    if (itemnum >= 0) {
                        if (action == MotionEvent.ACTION_DOWN || itemnum != mDragPos) {
                            if (mDragListener != null) {
                                mDragListener.drag(mDragPos, itemnum);
                            }
                            mDragPos = itemnum;
                            doExpansion(); // 처음 드래그한 아이템과 다른 위치에 있을 경우 펼쳐지게 한다.
                        }
                        int speed = 0;
                        adjustScrollBounds(y); // 스크롤 계산
                        if (y > mLowerBound) {
                            // 스크롤 최상위
                            speed = y > (mHeight + mLowerBound) / 2 ? 16 : 4;
                        } else if (y < mUpperBound) {
                            // 스크롤 최하위
                            speed = y < mUpperBound / 2 ? -16 : -4;
                        }
                        if (speed != 0) {
                            int ref = pointToPosition(0, mHeight / 2);
                            if (ref == AdapterView.INVALID_POSITION) {
                                //we hit a divider or an invisible view, check somewhere else
                                ref = pointToPosition(0, mHeight / 2 + getDividerHeight() + 64);
                            }
                            View v = getChildAt(ref - getFirstVisiblePosition());
                            if (v!= null) {
                                int pos = v.getTop();
                                setSelectionFromTop(ref, pos - speed);
                            }
                        }
                    }
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }
	
	private int getItemForPosition(int y) {
        int adjustedy = y - mDragPoint - 32;
        int pos = myPointToPosition(0, adjustedy);
        if (pos >= 0) {
            if (pos <= mFirstDragPos) {
                pos += 1;
            }
        } else if (adjustedy < 0) {
            pos = 0;
        }
        return pos;
    }
	
	private int myPointToPosition(int x, int y) {
        Rect frame = mTempRect;
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            child.getHitRect(frame);
            if (frame.contains(x, y)) {
                return getFirstVisiblePosition() + i;
            }
        }
        return INVALID_POSITION;
    }
	
	private void adjustScrollBounds(int y) {
        if (y >= mHeight / 3) {
            mUpperBound = mHeight / 3;
        }
        if (y <= mHeight * 2 / 3) {
            mLowerBound = mHeight * 2 / 3;
        }
    }
	
	private void doExpansion() {
        int childnum = mDragPos - getFirstVisiblePosition();
        if (mDragPos > mFirstDragPos) {
            childnum++;
        }

        View first = getChildAt(mFirstDragPos - getFirstVisiblePosition());

        for (int i = 0;; i++) {
            View vv = getChildAt(i);
            if (vv == null) {
                break;
            }
            int height = mItemHeightNormal;
            int visibility = View.VISIBLE;
            if (vv.equals(first)) {
                
                if (mDragPos == mFirstDragPos) {
                    visibility = View.INVISIBLE;
                } else {
                    height = 1;
                }
            } else if (i == childnum) {
                if (mDragPos < getCount() - 1) {
                    //height = mItemHeightExpanded;
                }
            }
            ViewGroup.LayoutParams params = vv.getLayoutParams();
            params.height = height;
            vv.setLayoutParams(params);
            vv.setVisibility(visibility);
        }
    }
    
	private void unExpandViews(boolean deletion) {
        for (int i = 0;; i++) {
            View v = getChildAt(i);
            if (v == null) {
                if (deletion) {
                    int position = getFirstVisiblePosition();
                    int y = getChildAt(0).getTop();
                    setAdapter(getAdapter());
                    setSelectionFromTop(position, y);
                }
                layoutChildren();
                v = getChildAt(i);
                if (v == null) {
                    break;
                }
            }
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = mItemHeightNormal;
            v.setLayoutParams(params);
            v.setVisibility(View.VISIBLE);
        }
    }
    
	// 드래그 시작
	private void startDragging(Bitmap bm, int y) {
        stopDragging();

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP;
        mWindowParams.x = dragImageX;
        mWindowParams.y = y - mDragPoint + mCoordOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;
        
        ImageView v = new ImageView(mContext);
        int backGroundColor = getResources().getColor(R.color.rosy_brown);
        v.setBackgroundColor(backGroundColor);
        v.setImageBitmap(bm);
        mDragBitmap = bm;

        mWindowManager = (WindowManager)mContext.getSystemService("window");
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
    }
	
	// 드래그를 위해 만들어 준 뷰의 이동
	private void dragView(int x, int y) {
        mWindowParams.y = y - mDragPoint + mCoordOffset;
        mWindowManager.updateViewLayout(mDragView, mWindowParams);
    }
    
	// 드래그 종료 처리
    private void stopDragging() {
        if (mDragView != null) {
            WindowManager wm = (WindowManager)mContext.getSystemService("window");
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
        if (mDragBitmap != null) {
            mDragBitmap.recycle();
            mDragBitmap = null;
        }
    }
    
    /**
     * 드래그 이벤트 리스너 등록
     * @param l 드래그 이벤트 리스너
     */
	public void setDragListener(DragListener l) {
        mDragListener = l;
    }
    
	/**
	 * 드랍 이벤트 리스너 등록
	 * @param l 드랍 이벤트 리스너
	 */
    public void setDropListener(DropListener l) {
        mDropListener = l;
    }
    
    /**
     * 리스트 아이템에 있는 뷰들 중 드래그 드랍 이벤트를 발생시킬 뷰의 아이 
     * @param id 드래그 드랍 이벤트를 발생시킬 뷰의 아이디
     */
    public void setDndView(int id){
    	dndViewId = id;
    }
    
    /**
     * 드래그시 표시되는 뷰의 스크린에서의 left padding
     * @param x 스크린에서의 left padding, 설정안하면 0
     */
    public void setDragImageX(int x){
    	dragImageX = x;
    }
    
    public interface DragListener {
        void drag(int from, int to);
    }
    public interface DropListener {
        void drop(int from, int to);
    }
}
