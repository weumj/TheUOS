package com.uoscs09.theuos2.base;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;

public class TabHidingScrollListener {
    public static class ForAbsListView implements AbsListView.OnScrollListener {
        TabHidingScrollListener delegate;
        private int mLastFirstVisibleItemPosition;

        public ForAbsListView(Toolbar toolbar) {
            this.delegate = new TabHidingScrollListener(toolbar);
        }


        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
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

            if (dx == 0)
                return;

            delegate.onScroll(dx > 0);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            delegate.onScrollStateChanged();
        }
    }

    private Toolbar toolbar;

    TabHidingScrollListener(Toolbar toolbar) {
        this.toolbar = toolbar;
    }

    //private boolean needGuard = false;

    void onScroll(boolean upward) {

        // if (needGuard)
        //      return;

        //  needGuard = true;

        if (upward) hide();
        else show();

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
