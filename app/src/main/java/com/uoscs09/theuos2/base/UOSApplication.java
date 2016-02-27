package com.uoscs09.theuos2.base;


import android.app.Application;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.Tracker;
import com.uoscs09.theuos2.BuildConfig;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

import mj.android.utils.task.TaskQueue;

public class UOSApplication extends Application {
    private TrackerUtil mTrackerUtil;

    private TaskQueue taskQueue;

    public static final boolean DEBUG = BuildConfig.DEBUG;

    @Override
    public void onCreate() {
        super.onCreate();

        AppUtil.init(this);

        taskQueue = new TaskQueue();

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


    public final TrackerUtil getTrackerUtil() {
        if (mTrackerUtil == null)
            mTrackerUtil = TrackerUtil.newInstance(this);

        return mTrackerUtil;
    }


    final TaskQueue taskQueue() {
        return taskQueue;
    }

}
