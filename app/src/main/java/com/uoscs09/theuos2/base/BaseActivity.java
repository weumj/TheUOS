package com.uoscs09.theuos2.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.uoscs09.theuos2.UOSApplication;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

public abstract class BaseActivity extends AppCompatActivity implements TrackerUtil.TrackerScreen {

    @Override
    protected void onCreate(Bundle arg0) {
        AppUtil.applyTheme(this);
        super.onCreate(arg0);

        if (!UOSApplication.DEBUG) {
            Tracker t = TrackerUtil.getInstance(this).getTracker();
            t.setScreenName(getScreenNameForTracker());
            t.send(new HitBuilders.AppViewBuilder().build());
        }
    }

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

    public void sendTrackerEvent(String action, String label) {
        TrackerUtil.getInstance(this).sendEvent(getScreenNameForTracker(), action, label);
    }

    public void sendTrackerEvent(String action, String label, long value) {
        TrackerUtil.getInstance(this).sendEvent(getScreenNameForTracker(), action, label, value);
    }

    public void sendClickEvent(String label) {
        TrackerUtil.getInstance(this).sendClickEvent(getScreenNameForTracker(), label);
    }

    public void sendClickEvent(String label, long value) {
        TrackerUtil.getInstance(this).sendClickEvent(getScreenNameForTracker(), label, value);
    }

}
