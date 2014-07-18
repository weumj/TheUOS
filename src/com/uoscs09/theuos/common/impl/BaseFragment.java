package com.uoscs09.theuos.common.impl;

import com.uoscs09.theuos.common.util.AppUtil;

import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {

	@Override
	public void onDetach() {
		AppUtil.releaseResource(this);
		super.onDetach();
		System.gc();
	}
}
