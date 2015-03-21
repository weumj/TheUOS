package com.uoscs09.theuos2;

import android.content.Context;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.uoscs09.theuos2.util.AppUtil;

import java.util.ArrayList;

public class IndexPagerAdapter extends FixedFragmentStatePagerAdapter {
    private final Context mContext;
    private ArrayList<Integer> list;

    public IndexPagerAdapter(FragmentManager fm, ArrayList<Integer> indexList, Context context) {
        super(fm);
        this.list = indexList;
        this.mContext = context;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getText(list.get(position));
    }

    @Override
    public Fragment getItem(int position) {
        Class<? extends Fragment> clz = AppUtil.getPageClass(list.get(position));
        if (clz == null) {
            list = AppUtil.loadEnabledPageOrder(mContext);
            clz = AppUtil.getPageClass(list.get(position));
        }
        return Fragment.instantiate(mContext, clz.getName());
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
