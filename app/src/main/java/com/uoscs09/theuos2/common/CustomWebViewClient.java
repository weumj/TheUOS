package com.uoscs09.theuos2.common;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/** URI 창이 아닌 직접 URL을 load하는 역할 */
public class CustomWebViewClient extends WebViewClient {

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		view.loadUrl(url);
		return true;
	}
}
