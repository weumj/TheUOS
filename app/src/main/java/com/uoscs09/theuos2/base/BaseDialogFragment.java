package com.uoscs09.theuos2.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.PermissionChecker;

import com.uoscs09.theuos2.util.TrackerUtil;


public abstract class BaseDialogFragment extends DialogFragment {

    private TrackerUtil trackerUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getBaseActivity() != null)
            trackerUtil = getBaseActivity().getTrackerUtil();
        else
            trackerUtil = new TrackerUtil(getActivity());
    }

    private TrackerUtil getTrackerUtil() {
        if (trackerUtil == null) {
            if (getBaseActivity() != null)
                return getBaseActivity().getTrackerUtil();
            else
                return new TrackerUtil(getActivity());
        }
        return trackerUtil;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        trackerUtil = null;
    }

    public abstract String getScreenNameForTracker();

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

    protected final BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }


    @PermissionChecker.PermissionResult
    protected boolean checkSelfPermission(String permission) {
        return getBaseActivity().checkSelfPermissionCompat(permission);
    }

    // String...
    @PermissionChecker.PermissionResult
    protected boolean checkSelfPermissions(String[] permission) {
        return getBaseActivity().checkSelfPermissionCompat(permission);
    }

    protected boolean checkPermissionResultAndShowToastIfFailed(@NonNull String[] permissions, @NonNull int[] grantResults, String message) {
        return getBaseActivity().checkPermissionResultAndShowToastIfFailed(permissions, grantResults, message);
    }

    protected boolean checkPermissionResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
        return getBaseActivity().checkPermissionResult(permissions, grantResults);
    }
}
