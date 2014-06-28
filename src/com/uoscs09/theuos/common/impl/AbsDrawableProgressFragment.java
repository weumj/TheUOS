package com.uoscs09.theuos.common.impl;

import com.uoscs09.theuos.R;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public abstract class AbsDrawableProgressFragment<T> extends
		AbsAsyncFragment<T> {
	private AnimationDrawable mLoadingAnimation, mLoadingAnimForMenu;
	private View mLoadingView;
	private boolean mIsMenuRefresh = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLoadingView = View.inflate(getActivity(),
				R.layout.footer_loading_view, null);
		mLoadingAnimation = (AnimationDrawable) ((ImageView) mLoadingView
				.findViewById(R.id.iv_list_footer_loading)).getBackground();
		mLoadingView.setVisibility(View.INVISIBLE);
		mLoadingAnimForMenu = (AnimationDrawable) getActivity().getResources()
				.getDrawable(R.anim.loading_animation);
	}

	public View getLoadingView() {
		return mLoadingView;
	}

	public AnimationDrawable getLoaAnimationDrawable() {
		return mLoadingAnimation;
	}

	protected void setMenuRefresh(boolean isRefresh) {
		mIsMenuRefresh = isRefresh;
	}

	@Override
	public void onPostExcute() {
		super.onPostExcute();
		mLoadingAnimation.stop();
		mLoadingAnimForMenu.stop();
		if (getActivity() != null && mIsMenuRefresh)
			getActivity().invalidateOptionsMenu();
		mLoadingView.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void excute() {
		super.excute();
		if (getActivity() != null && mIsMenuRefresh)
			getActivity().invalidateOptionsMenu();
		mLoadingView.setVisibility(View.VISIBLE);
		mLoadingAnimForMenu.start();
		mLoadingAnimation.start();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (isRunning())
			getLoadingMenuItem(menu).setIcon(mLoadingAnimForMenu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	/** 로딩 이이콘으로 변경할 MenuItem을 반환한다. */
	abstract protected MenuItem getLoadingMenuItem(Menu menu);
}
