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
    AnimationDrawable mLoadingAnimation;
	@ReleaseWhenDestroy
	View mLoadingView;
	private boolean mIsMenuRefresh = true;
	boolean mMenu = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLoadingView = View.inflate(getActivity(), R.layout.view_loading, null);
		ImageView iv = (ImageView) mLoadingView
				.findViewById(R.id.view_loading_image);
		if (!mMenu) {
			iv.setBackgroundResource(AppUtil.getStyledValue(getActivity(),
					R.attr.ic_loading));
		}
		mLoadingView.setTag(R.id.view_loading_image, iv);
		mLoadingAnimation = (AnimationDrawable) iv.getBackground();
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

	public final void setDrawableForMenu(boolean isMenu) {
		mMenu = isMenu;
	}

	@Override
	protected void onTransactPostExecute() {
		if (getActivity() != null && mIsMenuRefresh)
			getActivity().invalidateOptionsMenu();
		animationStop();
	}

	protected final void animationStart() {
		if (mLoadingView != null) {
			mLoadingView.setVisibility(View.VISIBLE);
		}
		if (getActivity() != null)
			getActivity().runOnUiThread(mStartAction);
	}

	protected final void animationStop() {
		if (mLoadingView != null) {
			mLoadingView.setVisibility(View.GONE);
		}
		if (getActivity() != null) {
			getActivity().runOnUiThread(mStopAction);
		}

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
			if (mLoadingAnimation != null) {
				mLoadingAnimation.stop();

				ImageView iv = (ImageView) mLoadingView
						.getTag(R.id.view_loading_image);
				iv.setBackgroundResource(0);
				iv.setBackgroundResource(AppUtil.getStyledValue(getActivity(),
						mMenu ? R.attr.menu_ic_loading : R.attr.ic_loading));

				mLoadingAnimation = (AnimationDrawable) iv.getBackground();
			}
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
	protected void execute() {
		if (getActivity() != null && mIsMenuRefresh)
			getActivity().invalidateOptionsMenu();
		animationStart();
		super.execute();
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
