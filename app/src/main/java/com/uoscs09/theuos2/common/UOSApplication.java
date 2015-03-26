package com.uoscs09.theuos2.common;


import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.Tracker;
import com.uoscs09.theuos2.util.TrackerUtil;

public class UOSApplication extends Application {
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    public static final boolean DEBUG = true;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!DEBUG) {
            Tracker t = TrackerUtil.getInstance(this).getTracker();
            t.enableAdvertisingIdCollection(true);
            t.enableAutoActivityTracking(true);
            t.enableExceptionReporting(true);
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
