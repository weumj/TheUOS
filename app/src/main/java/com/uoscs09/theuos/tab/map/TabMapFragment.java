package com.uoscs09.theuos.tab.map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.CustomWebViewClient;
import com.uoscs09.theuos.common.NonLeakingWebView;
import com.uoscs09.theuos.common.impl.BaseFragment;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;

@SuppressLint("ClickableViewAccessibility")
public class TabMapFragment extends BaseFragment implements OnTouchListener {
	@ReleaseWhenDestroy
	private NonLeakingWebView mWebView;
	private final static String URL = "http://m.uos.ac.kr/mkor/html/01_auos/05_location/location.do";

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		if (mWebView == null)
			mWebView = new NonLeakingWebView(getActivity());
		mWebView.setWebViewClient(new CustomWebViewClient());
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setUseWideViewPort(true);

		mWebView.loadUrl(URL);
		return mWebView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (mWebView != null) {
			if (isVisibleToUser) {
				// mWebView.setVisibility(View.VISIBLE);
				mWebView.setOnTouchListener(null);
			} else {
				mWebView.setOnTouchListener(this);
				// mWebView.setVisibility(View.INVISIBLE);
			}
		}
		super.setUserVisibleHint(isVisibleToUser);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	@Override
	public void onDestroyView() {
		if (mWebView != null) {
			mWebView.clearCache(true);
			mWebView.loadUrl("about:blank");
			AppUtil.unbindDrawables(mWebView);
			mWebView.destroy();
			mWebView = null;
			System.gc();
		}
		super.onDestroyView();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.tab_map, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_map:
			Activity activity = getActivity();
			startActivity(new Intent(activity, SubMapActivity.class));
			AppUtil.overridePendingTransition(activity, 0);
			return true;
		case R.id.action_refresh:
			mWebView.loadUrl(URL);
			return true;
		case R.id.action_backward:
			mWebView.goBack();
			return true;
		case R.id.action_forward:
			mWebView.goForward();
			return true;
		default:
			return false;
		}
	}
}
