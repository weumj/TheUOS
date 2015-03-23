package com.uoscs09.theuos2.tab.libraryseat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.webkit.WebSettings;

import com.uoscs09.theuos2.common.WebViewActivity;
import com.uoscs09.theuos2.util.StringUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

public class SubSeatWebActivity extends WebViewActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SeatItem item = getIntent().getParcelableExtra(TabLibrarySeatFragment.ITEM);


        TrackerUtil.getInstance(this).sendEvent(getScreenName(), "view", item.roomName);

        getSupportActionBar().setTitle("좌석 정보");
        getSupportActionBar().setSubtitle(item.roomName);

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        mWebView.setInitialScale(100);

        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDefaultTextEncodingName(StringUtil.ENCODE_EUC_KR);

        mWebView.loadUrl("http://203.249.102.34:8080/seat/roomview5.asp?room_no=" + item.index);
    }

    @NonNull
    @Override
    protected String getScreenName() {
        return "SubSeatWebActivity";
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }
}
