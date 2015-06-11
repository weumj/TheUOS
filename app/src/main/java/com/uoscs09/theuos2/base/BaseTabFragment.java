package com.uoscs09.theuos2.base;


import android.app.Activity;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewCompat;
import android.view.ViewGroup;

import com.uoscs09.theuos2.UosMainActivity;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;

public abstract class BaseTabFragment extends BaseFragment {
    @ReleaseWhenDestroy
    private ViewGroup mTabParent;
    @ReleaseWhenDestroy
    private NestedScrollingChild mNestedScrollingChild;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof UosMainActivity))
            throw new RuntimeException("Activity != UosMainActivity");
    }


    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (Build.VERSION.SDK_INT <= 20) {
            ColorFilter cf = new PorterDuffColorFilter(AppUtil.getAttrColor(getActivity(), R.attr.color_actionbar_title), PorterDuff.Mode.SRC_ATOP);
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                item.getIcon().setColorFilter(cf);
            }
        }

    }
    */

    protected void registerTabParentView(ViewGroup tabParent) {
        this.mTabParent = tabParent;
    }


    protected ViewGroup getTabParentView() {
        return mTabParent;
    }

    public final void resetNestedScrollPosition() {
        if(mNestedScrollingChild != null){
            mNestedScrollingChild.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
            mNestedScrollingChild.dispatchNestedPreScroll(0, -Integer.MAX_VALUE, null, null);
            mNestedScrollingChild.stopNestedScroll();
        }
    }

    protected void registerNestedScrollingChild(NestedScrollingChild child){
        mNestedScrollingChild = child;
    }


    protected ViewGroup getToolbarParent() {
        return ((UosMainActivity) getActivity()).getToolbarParent();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mTabParent != null && getUserVisibleHint()) {
            addOrRemoveTabMenu(true);
        }

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isAdded())
            addOrRemoveTabMenu(isVisibleToUser);

    }

    private void addOrRemoveTabMenu(boolean visible) {

        ViewGroup toolBarParent = getToolbarParent();

        if (toolBarParent == null || mTabParent == null)
            return;

        if (visible) {
            if (mTabParent.getParent() == null)
                toolBarParent.addView(mTabParent);

        } else if (toolBarParent.indexOfChild(mTabParent) > 0) {
            toolBarParent.removeView(mTabParent);

        }
    }

}
