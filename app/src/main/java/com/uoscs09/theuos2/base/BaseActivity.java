package com.uoscs09.theuos2.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

public abstract class BaseActivity extends AppCompatActivity implements TrackerUtil.TrackerScreen {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppUtil.applyTheme(this);
        super.onCreate(savedInstanceState);

        if (!UOSApplication.DEBUG) {
            Tracker t = TrackerUtil.getInstance(this).getTracker();
            t.setScreenName(getScreenNameForTracker());
            t.send(new HitBuilders.ScreenViewBuilder().build());
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

    @PermissionChecker.PermissionResult
    protected boolean checkSelfPermissionCompat(@NonNull String permission) {
        return AppUtil.checkSelfPermissionCompat(this, permission);
    }

    // String...
    @PermissionChecker.PermissionResult
    protected boolean checkSelfPermissionCompat(@NonNull String... permissions) {
        return AppUtil.checkSelfPermissionCompat(this, permissions);
    }

    protected void requestPermissionsCompat(int requestCode, String... permissions) {
        AppUtil.requestPermissionsCompat(this,requestCode,permissions);
    }

    protected boolean checkPermissionResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
        return AppUtil.checkPermissionResult(permissions, grantResults);
    }

    protected boolean checkPermissionResultAndShowToastIfFailed(@NonNull String[] permissions, @NonNull int[] grantResults, String message) {
        return AppUtil.checkPermissionResultAndShowToastIfFailed(this, permissions, grantResults, message);
    }

    protected boolean checkPermissionResultAndShowToastIfFailed(@NonNull String[] permissions, @NonNull int[] grantResults, @StringRes int res) {
        return AppUtil.checkPermissionResultAndShowToastIfFailed(this, permissions, grantResults, res);
    }
}
