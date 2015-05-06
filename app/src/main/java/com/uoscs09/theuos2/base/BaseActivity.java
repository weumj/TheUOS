package com.uoscs09.theuos2.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.uoscs09.theuos2.common.UOSApplication;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

/**
 * onCreate에서 테마 설정을 하는 fragment액티비티
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        AppUtil.applyTheme(this);
        super.onCreate(arg0);

        if (!UOSApplication.DEBUG) {
            Tracker t = TrackerUtil.getInstance(this).getTracker();
            t.setScreenName(getScreenName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }
    }

    @NonNull
    protected abstract String getScreenName();

    @Override
    public void onStart() {
        super.onStart();

        if (!UOSApplication.DEBUG)
            GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (!UOSApplication.DEBUG)
            GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onDestroy() {
        AppUtil.unbindDrawables(getWindow().getDecorView());
        super.onDestroy();
        System.gc();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

}
