package com.uoscs09.theuos2.tab.announce;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.common.NonLeakingWebView;
import com.uoscs09.theuos2.common.WebViewActivity;
import com.uoscs09.theuos2.util.AppUtil;

public class SubAnnounceWebActivity extends WebViewActivity {
    private String url;
    private AnnounceItem mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_tab_announce);
        Intent intent = getIntent();

        mItem = intent.getParcelableExtra(TabAnnounceFragment.ITEM);
        int selection = intent.getIntExtra(TabAnnounceFragment.PAGE_NUM, 0);
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
                supportFinishAfterTransition();
                return;
        }

        url += mItem.onClickString;

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setInitialScale(100);

        settings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new AnnounceWebViewClient(this, selection));
        mWebView.loadUrl(url);

    }

    @NonNull
    @Override
    protected String getScreenName() {
        return "SubAnnounceActivity";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tab_anounce_sub, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_web:
                startActivity(AppUtil.setWebPageIntent(url));
                return true;

            case R.id.action_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mItem.title);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mItem.title + " - \'" + url + "\'");

                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.action_share)));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class AnnounceWebViewClient extends NonLeakingWebView.NonLeakingWebViewClient {

        private final int selection;
        private boolean firstLoading = true;
        public AnnounceWebViewClient(Activity activity, int selection) {
            super(activity);
            this.selection = selection;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if(firstLoading) {
                if (selection != 3) {
                    view.loadUrl(" javascript:(function() { " +
                            "var viewType = document.getElementsByClassName('viewType01')[0]; " +
                            "document.body.removeChild(document.getElementById('container'));" +
                            "document.body.removeChild(document.getElementById('footer'));" +
                            "document.body.style.backgroundImage = '';" +
                            "document.clear();" +
                            "document.body.appendChild(viewType);" +
                            "})()");
                } else {
                    view.loadUrl(" javascript:(function() { " +
                            "var viewType = document.getElementsByClassName('notice_tb')[0]; " +
                            "document.body.removeChild(document.getElementById('all_wrap'));" +
                            "document.body.style.backgroundImage = '';" +
                            "document.clear();" +
                            "document.body.appendChild(viewType);" +
                            "})()");
                    view.setInitialScale(100);
                }
                firstLoading = false;
            }

        }
    }
}
