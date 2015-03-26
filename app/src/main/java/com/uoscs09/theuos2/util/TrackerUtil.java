package com.uoscs09.theuos2.util;


import android.content.Context;
import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.common.UOSApplication;

import java.util.HashMap;

public class TrackerUtil {

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    private final HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();


    private static TrackerUtil sInstance;
    private Tracker mTracker;

    private static final String APP_VERSION = "App Version";
    private final String appVersion;

    private static final String USER_VISIBLE = "User Visible";

    private TrackerUtil(Context context){
        mTracker = getTracker(context, TrackerName.APP_TRACKER);
        appVersion = context.getString(R.string.setting_app_version_name);
    }

    public static TrackerUtil getInstance(Context context){
        if(sInstance == null)
            sInstance = new TrackerUtil(context);

        return sInstance;
    }

    public static  TrackerUtil getInstance(Fragment fragment){
        return getInstance(fragment.getActivity());
    }

    public static TrackerUtil getInstance(android.app.Fragment fragment){
        return getInstance(fragment.getActivity());
    }

   synchronized Tracker getTracker(Context context, TrackerName trackerId) {

        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);

            analytics.setDryRun(UOSApplication.DEBUG);

            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(context.getString(R.string.google_analytics_PROPERTY_ID)) :
                    (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker) :
                            analytics.newTracker(R.xml.ecommerce_tracker);

            mTrackers.put(trackerId, t);
        }

        return mTrackers.get(trackerId);
    }


    public Tracker getTracker(){
        return mTracker;
    }

    public void sendEvent(String category, String action){
        if(UOSApplication.DEBUG)
            return;

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .set(APP_VERSION, appVersion)
                .build());
    }

    public void sendEvent(String category, String action, String label) {
        if(UOSApplication.DEBUG)
            return;

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .set(APP_VERSION, appVersion)
                .build());
    }

    public void sendEvent(String category, String action, String label, long value) {
        if(UOSApplication.DEBUG)
            return;

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

    public void sendVisibleEvent(String category){
        sendEvent(category, USER_VISIBLE);
    }
}
