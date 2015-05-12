package com.uoscs09.theuos2.base;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

import com.uoscs09.theuos2.widget.CustomToolbar;

public class TabHidingScrollListener implements View.OnSystemUiVisibilityChangeListener {

    public static class ForAbsListView implements AbsListView.OnScrollListener {
        TabHidingScrollListener delegate;
        private int mLastFirstVisibleItemPosition;

        public ForAbsListView(Toolbar toolbar) {
            this.delegate = new TabHidingScrollListener(toolbar);
        }


        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            /*
            if (totalItemCount == 0)
                return;

            delegate.onScroll(firstVisibleItem >= mLastFirstVisibleItemPosition);

            mLastFirstVisibleItemPosition = firstVisibleItem;
            */

        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

            if (view.getCount() == 0)
                return;

            int firstVisiblePosition = view.getFirstVisiblePosition();
            delegate.onScroll(firstVisiblePosition >= mLastFirstVisibleItemPosition);

            mLastFirstVisibleItemPosition = firstVisiblePosition;

            /*
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    int currentFirstVisiblePosition = view.getFirstVisiblePosition();

                    if (currentFirstVisiblePosition < mLastFirstVisibleItemPosition) {
                        //scroll down
                        delegate.show();


                    } else if (currentFirstVisiblePosition > mLastFirstVisibleItemPosition) {
                        // scroll up
                        delegate.hide();
                    }
                    mLastFirstVisibleItemPosition = currentFirstVisiblePosition;

                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    break;
            }
            */
        }
    }

    public static class ForRecyclerView extends RecyclerView.OnScrollListener {
        TabHidingScrollListener delegate;

        public ForRecyclerView(Toolbar toolbar) {
            this.delegate = new TabHidingScrollListener(toolbar);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (recyclerView.getAdapter() == null || recyclerView.getAdapter().getItemCount() == 0)
                return;

            delegate.onScroll(dx > 0);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            delegate.onScrollStateChanged();
        }
    }

    private CustomToolbar toolbar;

    TabHidingScrollListener(Toolbar toolbar) {
        this.toolbar = (CustomToolbar) toolbar;
        this.toolbar.setOnVisibilityChangedListener(this);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {

        Log.d("visibility inform : ", "" + visibility);
        isVisible = visibility == View.VISIBLE;

    }

    //private boolean needGuard = false;
    private boolean isVisible = true;

    void onScroll(boolean upward) {
        Log.d("scroll", upward ? "up" : "down");

        // if (needGuard)
        //      return;

        //  needGuard = true;

        if (upward) {
            if (isVisible) {
                hide();
                Log.d("scroll", "hide");
            }

        } else {
            if (!isVisible) {
                show();
                Log.d("scroll", "show");
            }

        }

        // needGuard = false;

    }

    void onScrollStateChanged() {
    }

    public void hide() {
        toolbar.setVisibility(View.GONE);
    }

    public void show() {
        toolbar.setVisibility(View.VISIBLE);
    }

}
