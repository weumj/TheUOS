package com.uoscs09.theuos2.base;


import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;
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
        /*
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDialog()
                .build()
        );
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        );
        */
        AppUtil.init(this);

        taskQueue = new TaskQueue();

        mTrackerUtil = TrackerUtil.newInstance(this);

        if (DEBUG) {
            mTrackerUtil.debugInit();
        }else {
            mTrackerUtil.init();
        }

        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false);
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
