package com.uoscs09.theuos2.base;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.uoscs09.theuos2.util.TrackerUtil;


public abstract class BaseDialogFragment extends DialogFragment implements TrackerUtil.TrackerScreen {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TrackerUtil.getInstance(this).sendVisibleEvent(getScreenNameForTracker());
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
