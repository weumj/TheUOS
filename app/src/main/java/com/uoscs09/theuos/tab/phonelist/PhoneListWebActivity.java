package com.uoscs09.theuos.tab.phonelist;

import android.os.Bundle;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.CustomWebViewClient;
import com.uoscs09.theuos.common.WebViewActivity;

public class PhoneListWebActivity extends WebViewActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.title_section6_tel);

		mWebView.loadUrl("http://m.uos.ac.kr/mkor/html/01_auos/04_tel/tel.do");
		mWebView.setWebViewClient(new CustomWebViewClient());
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setUseWideViewPort(true);
	}
}
