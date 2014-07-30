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
	private AnimationDrawable mLoadingAnimation;
	@ReleaseWhenDestroy
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

	@Override
	protected void onTransactPostExcute() {
		if (getActivity() != null && mIsMenuRefresh)
			getActivity().invalidateOptionsMenu();
		animationStop();
	}

	protected final void animationStart() {
		if (mLoadingView != null) {
			mLoadingView.setVisibility(View.VISIBLE);
		}
		getActivity().runOnUiThread(mStartAction);
	}

	protected final void animationStop() {
		if (mLoadingView != null) {
			mLoadingView.setVisibility(View.INVISIBLE);
		}
		getActivity().runOnUiThread(mStopAction);
	}

	private final Runnable mStartAction = new Runnable() {
		@Override
		public void run() {
			if (mLoadingAnimation != null)
				mLoadingAnimation.start();
		}
	};

	private final Runnable mStopAction = new Runnable() {
		@Override
		public void run() {
			if (mLoadingAnimation != null)
				mLoadingAnimation.stop();
		}
	};

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
		MenuItem refreshItem = getLoadingMenuItem(menu);
		if (refreshItem != null) {
			if (isRunning()) {
				if (mIsMenuRefresh)
					refreshItem.setIcon(mLoadingAnimation);
				animationStart();
			} else {
				animationStop();
			}
		}
	}

	@Override
	public void onDetach() {
		if (mLoadingAnimation != null) {
			mLoadingAnimation.setCallback(null);
			mLoadingAnimation = null;
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
