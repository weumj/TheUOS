package com.uoscs09.theuos2.base;


import android.app.Activity;
import android.support.v7.widget.Toolbar;
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
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem item = menu.getItem(i);
                    item.setIcon(ImageUtil.getTintDrawableForMenu(getActivity(), item.getIcon()));
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


    protected ViewGroup getToolbarParent() {
        return ((UosMainActivity) getActivity()).getToolbarParent();
    }

    protected Toolbar getToolBar() {
        return ((UosMainActivity) getActivity()).getToolbar();
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
