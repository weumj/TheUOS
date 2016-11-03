package com.uoscs09.theuos2.util;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.analytics.ExceptionParser;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.UOSApplication;

import java.util.HashMap;

public class TrackerUtil {

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
    }

    private final HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    private Tracker mTracker;

    private static final String APP_VERSION = "App Version";
    private final String appVersion;

    private static final String USER_VISIBLE = "User Visible";

    private TrackerUtil(Context context) {
        if (context != null) {
            mTracker = getTracker(context, TrackerName.APP_TRACKER);
            appVersion = context.getString(R.string.setting_app_version_name);
        } else {
            appVersion = null;
            mTracker = null;
        }
    }

    public static TrackerUtil newInstance(Context context) {
        return new TrackerUtil(context);
    }

    public static TrackerUtil getInstance(Activity activity) {
        if (activity == null)
            return new NullTracker();
        else
            return ((UOSApplication) activity.getApplication()).getTrackerUtil();
    }

    public static TrackerUtil newInstance(UOSApplication application) {
        if (application == null)
            return new NullTracker();
        else
            return new TrackerUtil(application);
    }

    public static TrackerUtil getInstance(Fragment fragment) {
        if (fragment == null)
            return new NullTracker();
        else
            return getInstance(fragment.getActivity());
    }

    public static TrackerUtil getInstance(android.app.Fragment fragment) {
        if (fragment == null)
            return new NullTracker();
        else
            return getInstance(fragment.getActivity());
    }

    private synchronized Tracker getTracker(Context context, TrackerName trackerId) {

        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);

            analytics.setDryRun(UOSApplication.DEBUG);

            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(context.getString(R.string.google_analytics_PROPERTY_ID)) :
                    analytics.newTracker(R.xml.global_tracker);


            mTrackers.put(trackerId, t);
        }

        return mTrackers.get(trackerId);
    }


    public Tracker getTracker() {
        return mTracker;
    }

    public void init() {
        if (mTracker == null)
            return;

        mTracker.enableAdvertisingIdCollection(true);
        //t.enableAutoActivityTracking(true);
        mTracker.enableExceptionReporting(true);
        mTracker.setUseSecure(true);

        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (uncaughtExceptionHandler instanceof ExceptionReporter) {
            ExceptionReporter exceptionReporter = (ExceptionReporter) uncaughtExceptionHandler;
            exceptionReporter.setExceptionParser(new TrackerUtil.AnalyticsExceptionParser());
        }
    }

    public void debugInit() {
        if (mTracker == null)
            return;

        mTracker.enableAdvertisingIdCollection(false);
        //t.enableAutoActivityTracking(true);
        mTracker.enableExceptionReporting(false);
        mTracker.setUseSecure(true);
    }

    public void sendEvent(String category, String action) {
        if (UOSApplication.DEBUG)
            return;

        if (mTracker != null)
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .set(APP_VERSION, appVersion)
                    .build());
    }

    public void sendEvent(String category, String action, String label) {
        if (UOSApplication.DEBUG)
            return;

        if (mTracker != null)
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .setLabel(label)
                    .set(APP_VERSION, appVersion)
                    .build());
    }

    public void sendEvent(String category, String action, String label, long value) {
        if (UOSApplication.DEBUG)
            return;

        if (mTracker != null)
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .setLabel(label)
                    .setValue(value)
                    .set(APP_VERSION, appVersion)
                    .build());
    }

    public void sendClickEvent(String category, String label) {
        if (mTracker != null)
            sendEvent(category, "click", label);
    }

    public void sendClickEvent(String category, String label, long value) {
        if (mTracker != null)
            sendEvent(category, "click", label, value);
    }

    public void sendVisibleEvent(String category) {
        if (mTracker != null)
            sendEvent(category, USER_VISIBLE);
    }


    public static class AnalyticsExceptionParser implements ExceptionParser {

        @Override
        public String getDescription(String p_thread, Throwable p_throwable) {
            return "Thread: " + p_thread + ", Exception: " + Log.getStackTraceString(p_throwable);
        }
    }

    public interface TrackerScreen {

        @NonNull
        String getScreenNameForTracker();

        void sendTrackerEvent(String action, String label);

        void sendTrackerEvent(String action, String label, long value);

        void sendClickEvent(String label);

        void sendClickEvent(String label, long value);
    }

    private static class NullTracker extends TrackerUtil {

        private NullTracker() {
            super(null);
        }
    }
}
