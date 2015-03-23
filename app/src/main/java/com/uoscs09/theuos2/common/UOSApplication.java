package com.uoscs09.theuos2.common;


import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.uoscs09.theuos2.R;

import java.util.HashMap;

public class UOSApplication extends Application{
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;


    @Override
    public void onCreate() {
        super.onCreate();

        Tracker t = getTracker(TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        t.enableAutoActivityTracking(true);
        t.enableExceptionReporting(true);
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

    private static final String PROPERTY_ID = "UA-61041818-1";

    public enum TrackerName {
        APP_TRACKER,           // 앱 별로 트래킹
        GLOBAL_TRACKER,        // 모든 앱을 통틀어 트래킹
        ECOMMERCE_TRACKER,     // 아마 유료 결재 트래킹 개념 같음
    }

    private final HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    public synchronized Tracker getTracker(TrackerName trackerId) {

        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID) :
                    (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker) :
                            analytics.newTracker(R.xml.ecommerce_tracker);

            mTrackers.put(trackerId, t);
        }

        return mTrackers.get(trackerId);
    }
}
