package com.uoscs09.theuos.tab.libraryseat;

import android.os.Bundle;
import android.webkit.WebSettings;

import com.uoscs09.theuos.common.WebViewActivity;
import com.uoscs09.theuos.common.util.StringUtil;

public class SubSeatWebActivity extends WebViewActivity {
	private static final String TITLE_ = "ÁÂ¼® Á¤º¸";
	private static final String URL = "http://203.249.102.34:8080/seat/roomview5.asp";
	private static final String WHAT_ROOM = "?room_no=";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SeatItem item = getIntent().getParcelableExtra(
				TabLibrarySeatFragment.ITEM);
		getActionBar().setTitle(TITLE_);
		getActionBar().setSubtitle(item.roomName);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		mWebView.setInitialScale(100);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setDefaultTextEncodingName(StringUtil.ENCODE_EUC_KR);
		mWebView.loadUrl(URL + WHAT_ROOM + item.index);
	}
}
