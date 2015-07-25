package com.uoscs09.theuos2.common;


import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.Tracker;
import com.uoscs09.theuos2.util.TrackerUtil;

public class UOSApplication extends Application {
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private TrackerUtil mTrackerUtil;

    public static final boolean DEBUG = true;


    public TrackerUtil getTrackerUtil() {
        if (mTrackerUtil == null)
            mTrackerUtil = TrackerUtil.newInstance(this);

        return mTrackerUtil;
    }


    @Override
    public void onCreate() {
        super.onCreate();

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

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {

        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(getRequestQueue(), new LruBitmapCache());
        }

        return mImageLoader;
    }

}
