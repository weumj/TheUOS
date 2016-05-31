package com.uoscs09.theuos2.base;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uoscs09.theuos2.UosMainActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import mj.android.utils.task.TaskQueue;

public abstract class BaseTabFragment extends BaseFragment {
    private ViewGroup mTabParent;
    private Unbinder unbinder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof UosMainActivity))
            throw new RuntimeException("Activity != UosMainActivity");
    }

    protected final UOSApplication getUosApplication() {
        return (UOSApplication) getActivity().getApplication();
    }

    protected final TaskQueue taskQueue() {
        return getUosApplication().taskQueue();
    }

    protected UosMainActivity getUosMainActivity() {
        return (UosMainActivity) getActivity();
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(layoutRes(), container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (unbinder != null) unbinder.unbind();

    }

    @LayoutRes
    protected abstract int layoutRes();

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

        if (isVisibleToUser)
            addTabMenu();
        else if (isAdded())
            removeTabMenu();

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
