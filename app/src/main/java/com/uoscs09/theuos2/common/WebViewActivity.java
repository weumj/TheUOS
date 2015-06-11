package com.uoscs09.theuos2.common;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebSettings;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.base.BaseActivity;
import com.uoscs09.theuos2.util.AppUtil;

/**
 * WebView가 포함된 액티비티, 액티비티 종료시(onDestroy) webView를 destory함
 */
public abstract class WebViewActivity extends BaseActivity {
    protected NonLeakingWebView mWebView;
    protected WebSettings settings;
    @ReleaseWhenDestroy
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mWebView = (NonLeakingWebView) findViewById(R.id.webview);

        setSupportActionBar(mToolbar);

        settings = mWebView.getSettings();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                supportFinishAfterTransition();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            settings = null;
            mWebView.clearCache(true);
            mWebView.loadUrl("about:blank");
            AppUtil.unbindDrawables(mWebView);
            //mWebView.destroy();
            mWebView = null;
            System.gc();
        }
        super.onDestroy();
    }

}