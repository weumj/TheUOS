package com.uoscs09.theuos2.tab.phonelist;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.common.CustomWebViewClient;
import com.uoscs09.theuos2.common.WebViewActivity;

public class PhoneListWebActivity extends WebViewActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(R.string.title_tab_phone);

		mWebView.loadUrl("http://m.uos.ac.kr/mkor/html/01_auos/04_tel/tel.do");
		mWebView.setWebViewClient(new CustomWebViewClient());
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setUseWideViewPort(true);
	}

    @NonNull
    @Override
	public String getScreenNameForTracker() {
        return "PhoneListWebActivity";
    }
}
