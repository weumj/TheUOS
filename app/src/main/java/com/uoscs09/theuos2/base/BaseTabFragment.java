package com.uoscs09.theuos2.base;


import android.app.Activity;
import android.view.ViewGroup;

import com.uoscs09.theuos2.UosMainActivity;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;

public abstract class BaseTabFragment extends BaseFragment {
    @ReleaseWhenDestroy
    private ViewGroup mTabParent;

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

    /*
    public final void resetNestedScrollPosition() {
        ((UosMainActivity) getActivity()).resetAppBar();
    }
    */

    protected ViewGroup getToolbarParent() {
        return ((UosMainActivity) getActivity()).getToolbarParent();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mTabParent != null && getUserVisibleHint()) {
            addTabMenu();
        }

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isAdded()) {
            if (isVisibleToUser)
                addTabMenu();
            else
                removeTabMenu();
        }

    }


    public void addTabMenu() {

        ViewGroup toolBarParent = getToolbarParent();

        if (toolBarParent == null || mTabParent == null)
            return;

        if (mTabParent.getParent() == null)
            toolBarParent.addView(mTabParent);

    }


    public void removeTabMenu() {
        ViewGroup toolBarParent = getToolbarParent();

        if (toolBarParent == null || mTabParent == null)
            return;

        if (toolBarParent.getChildCount() > 1) {
            toolBarParent.removeViews(1, toolBarParent.getChildCount() - 1);
        }
        /*
        if (toolBarParent.indexOfChild(mTabParent) > 0) {
            toolBarParent.removeView(mTabParent);

        }
        */
    }

}
