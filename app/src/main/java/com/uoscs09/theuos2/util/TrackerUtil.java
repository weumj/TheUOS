package com.uoscs09.theuos2.util;


import android.app.Activity;
import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.common.UOSApplication;

public class TrackerUtil {
    private static TrackerUtil sInstance;
    private Tracker mTracker;

    private static final String APP_VERSION = "App Version";
    private final String appVersion;

    private TrackerUtil(UOSApplication app){
        mTracker = app.getTracker(UOSApplication.TrackerName.APP_TRACKER);
        appVersion = app.getString(R.string.setting_app_version);
    }

    public static TrackerUtil getInstance(UOSApplication app){
        if(sInstance == null)
            sInstance = new TrackerUtil(app);

        return sInstance;
    }

    public static  TrackerUtil getInstance(Fragment fragment){
        return getInstance(fragment.getActivity());
    }

    public static TrackerUtil getInstance(Activity activity){
        return getInstance((UOSApplication) activity.getApplication());
    }

    public static TrackerUtil getInstance(android.app.Fragment fragment){
        return getInstance(fragment.getActivity());
    }


    public Tracker getTracker(){
        return mTracker;
    }

    public void sendEvent(String category, String action){
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .set(APP_VERSION, appVersion)
                .build());
    }

    public void sendEvent(String category, String action, String label) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .set(APP_VERSION, appVersion)
                .build());
    }

    public void sendEvent(String category, String action, String label, long value) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .set(APP_VERSION, appVersion)
                .build());
    }

    public void sendClickEvent(String category, String label) {
        sendEvent(category, "click", label);
    }

    public void sendClickEvent(String category, String label, long value) {
        sendEvent(category, "click", label, value);
    }
}
