package com.uoscs09.theuos2;


import android.app.Application;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.Tracker;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

public class UOSApplication extends Application {
    private TrackerUtil mTrackerUtil;

    public static final boolean DEBUG = BuildConfig.DEBUG;


    public TrackerUtil getTrackerUtil() {
        if (mTrackerUtil == null)
            mTrackerUtil = TrackerUtil.newInstance(this);

        return mTrackerUtil;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        AppUtil.init(this);

        mTrackerUtil = TrackerUtil.newInstance(this);

        if (!DEBUG) {
            Tracker t = mTrackerUtil.getTracker();
            t.enableAdvertisingIdCollection(true);
            t.enableAutoActivityTracking(true);
            t.enableExceptionReporting(true);

            Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
            if (uncaughtExceptionHandler instanceof ExceptionReporter) {
                ExceptionReporter exceptionReporter = (ExceptionReporter) uncaughtExceptionHandler;
                exceptionReporter.setExceptionParser(new TrackerUtil.AnalyticsExceptionParser());
            }

        }
    }

}
