package com.uoscs09.theuos.common.impl;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;

public abstract class AbsDrawableProgressFragment<T> extends
		AbsAsyncFragment<T> {
	@ReleaseWhenDestroy
	private AnimationDrawable mLoadingAnimation, mLoadingAnimForMenu;
	@ReleaseWhenDestroy
	private View mLoadingView;
	private boolean mIsMenuRefresh = true;
	private boolean mIsViewEnable = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mIsViewEnable) {
			mLoadingView = View.inflate(getActivity(),
					R.layout.footer_loading_view, null);
			mLoadingAnimation = (AnimationDrawable) ((ImageView) mLoadingView
					.findViewById(R.id.iv_list_footer_loading)).getBackground();
			mLoadingView.setVisibility(View.INVISIBLE);
		}
		if (mIsMenuRefresh)
			mLoadingAnimForMenu = (AnimationDrawable) getActivity()
					.getResources().getDrawable(R.anim.loading_animation);
	}

	/** '로딩 중' 을 나타내는 View를 반환한다. */
	public final View getLoadingView() {
		return mLoadingView;
	}

	/** Loading View에 속한, Loading AnimationDrawable을 반환한다. */
	public final AnimationDrawable getLoadingAnimDrawable() {
		return mLoadingAnimation;
	}

	/** 메뉴 아이콘에 로딩 Animation을 적용할지의 여부를 설정한다. */
	protected final void setMenuRefresh(boolean isRefresh) {
		mIsMenuRefresh = isRefresh;
	}

	protected final void setLoadingViewEnable(boolean enable) {
		mIsViewEnable = enable;
	}

	@Override
	protected void onTransactPostExcute() {
		if (getActivity() != null && mIsMenuRefresh)
			getActivity().invalidateOptionsMenu();
		animationStop();
	}

	protected final void animationStart() {
		if (mLoadingView != null)
			mLoadingView.setVisibility(View.VISIBLE);
		if (mLoadingAnimForMenu != null)
			mLoadingAnimForMenu.start();
		if (mLoadingAnimation != null)
			mLoadingAnimation.start();
	}

	protected final void animationStop() {
		if (mLoadingAnimation != null)
			mLoadingAnimation.stop();
		if (mLoadingAnimForMenu != null)
			mLoadingAnimForMenu.stop();
		if (mLoadingView != null)
			mLoadingView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isRunning()) {
			animationStart();
		} else {
			animationStop();
		}
	}

	@Override
	protected void excute() {
		if (getActivity() != null && mIsMenuRefresh)
			getActivity().invalidateOptionsMenu();
		animationStart();
		super.excute();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (isRunning()) {
			getLoadingMenuItem(menu).setIcon(mLoadingAnimForMenu);
			animationStart();
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onDetach() {
		if (mLoadingAnimation != null) {
			mLoadingAnimation.setCallback(null);
			mLoadingAnimation = null;
		}
		if (mLoadingAnimForMenu != null) {
			mLoadingAnimForMenu.setCallback(null);
			mLoadingAnimForMenu = null;
		}
		if (mLoadingView != null) {
			AppUtil.unbindDrawables(mLoadingView);
			mLoadingView = null;
		}
		super.onDetach();
	}

	/** 로딩 아이콘으로 변경할 MenuItem을 반환한다. */
	abstract protected MenuItem getLoadingMenuItem(Menu menu);
}
