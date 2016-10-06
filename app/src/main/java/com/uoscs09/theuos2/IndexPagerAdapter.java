package com.uoscs09.theuos2;

import android.content.Context;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

import com.uoscs09.theuos2.util.AppUtil;

import java.util.ArrayList;

class IndexPagerAdapter extends FixedFragmentStatePagerAdapter {
    private final Context mContext;
    private ArrayList<AppUtil.TabInfo> list;
    //private WeakReference<Fragment> mCurrentFragmentCache;

    IndexPagerAdapter(FragmentManager fm, ArrayList<AppUtil.TabInfo> indexList, Context context) {
        super(fm);

        this.list = indexList;
        this.mContext = context;
    }

    private AppUtil.TabInfo tabInfo(int position){
        return list.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getText(tabInfo(position).titleResId);
    }

    @Override
    public Fragment getItem(int position) {
        return tabInfo(position).getFragment();
    }

    @Override
    public int getCount() {
        return list.size();
    }
/*
    public Fragment getCurrentFragment() {
        if (mCurrentFragmentCache != null && mCurrentFragmentCache.get() != null)
            return mCurrentFragmentCache.get();

        return null;
    }
*/
    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        //mCurrentFragmentCache = new WeakReference<>((Fragment)object);


    }
}
