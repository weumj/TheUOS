package com.uoscs09.theuos.tab.anounce;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.WebViewActivity;
import com.uoscs09.theuos.common.util.AppUtil;

public class SubAnounceWebActivity extends WebViewActivity {
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle(R.string.title_section1_announce);
		Intent intent = getIntent();
		String link = intent.getStringExtra(TabAnounceFragment.LIST_AN);
		int selection = intent.getIntExtra(TabAnounceFragment.PAGE_NUM, 0);
		switch (selection) {
		case 3:
			url = "http://scholarship.uos.ac.kr/scholarship.do?process=view&brdBbsseq=1&x=1&y=1&w=3&";
			break;
		case 2:
			url = "http://www.uos.ac.kr/korNotice/view.do?list_id=FA2&sort=1&seq=";
			break;
		case 1:
			url = "http://www.uos.ac.kr/korNotice/view.do?list_id=FA1&sort=1&seq=";
			break;
		default:
			finish();
			return;
		}
		url += link;
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView.setInitialScale(100);
		mWebView.loadUrl(url);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tab_anounce_sub, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_web:
			startActivity(AppUtil.setWebPageIntent(url));
			AppUtil.overridePendingTransition(this, 1);
			return true;
		default:
			return false;
		}
	}
}
