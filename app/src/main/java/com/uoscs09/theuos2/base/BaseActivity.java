package com.uoscs09.theuos2.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

public abstract class BaseActivity extends AppCompatActivity {

    private TrackerUtil trackerUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppUtil.applyTheme(this);

        super.onCreate(savedInstanceState);

        trackerUtil = new TrackerUtil(this);
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

    public abstract String getScreenNameForTracker();

    public TrackerUtil getTrackerUtil() {
        return trackerUtil;
    }

    public void sendTrackerEvent(String action, String label) {
        getTrackerUtil().sendEvent(getScreenNameForTracker(), action, label);
    }

    public void sendTrackerEvent(String action, String label, long value) {
        getTrackerUtil().sendEvent(getScreenNameForTracker(), action, label, value);
    }

    public void sendClickEvent(String label) {
        getTrackerUtil().sendClickEvent(getScreenNameForTracker(), label);
    }

    public void sendClickEvent(String label, long value) {
        getTrackerUtil().sendClickEvent(getScreenNameForTracker(), label, value);
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
