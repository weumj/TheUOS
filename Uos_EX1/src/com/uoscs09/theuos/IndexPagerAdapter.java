package com.uoscs09.theuos;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.uoscs09.theuos.common.util.AppUtil;

public class IndexPagerAdapter extends FixedFragmentStatePagerAdapter {
	private WeakReference<Context> contextRef;
	private List<Integer> list;

	public IndexPagerAdapter(FragmentManager fm, List<Integer> indexList,
			Context context) {
		super(fm);
		this.list = indexList;
		this.contextRef = new WeakReference<Context>(context);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return contextRef.get().getText(list.get(position));
	}

	@Override
	public Fragment getItem(int position) {
		Class<? extends Fragment> clz = AppUtil
				.getPageClass(list.get(position));
		if (clz == null) {
			list = AppUtil.loadDefaultPageOrder();
			clz = AppUtil.getPageClass(list.get(position));
		}
		return Fragment.instantiate(contextRef.get(), clz.getName());
	}

	@Override
	public int getCount() {
		return list.size();
	}
}
