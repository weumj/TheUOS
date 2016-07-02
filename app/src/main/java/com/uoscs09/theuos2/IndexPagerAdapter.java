package com.uoscs09.theuos2;

import android.content.Context;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

import com.uoscs09.theuos2.util.AppUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class IndexPagerAdapter extends FixedFragmentStatePagerAdapter {
    private final Context mContext;
    private ArrayList<Integer> list;
    private WeakReference<Fragment> mCurrentFragmentCache;

    public IndexPagerAdapter(FragmentManager fm, ArrayList<Integer> indexList, Context context) {
        super(fm);

        this.list = indexList;
        this.mContext = context;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        //return AppUtil.TabInfo.find(list.get(position)).getTitle(mContext);
        return mContext.getText(list.get(position));
    }

    @Override
    public Fragment getItem(int position) {

        //return AppUtil.TabInfo.find(list.get(position)).getFragment();

        Class<? extends Fragment> clz = AppUtil.getPageClass(list.get(position));
        if (clz == null) {
            list = AppUtil.loadEnabledPageOrder2();
            clz = AppUtil.getPageClass(list.get(position));
        }

        return Fragment.instantiate(mContext, clz.getName());

    }

    @Override
    public int getCount() {
        return list.size();
    }

    public Fragment getCurrentFragment() {
        if (mCurrentFragmentCache != null && mCurrentFragmentCache.get() != null)
            return mCurrentFragmentCache.get();

        return null;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        mCurrentFragmentCache = new WeakReference<>((Fragment)object);


    }
}
