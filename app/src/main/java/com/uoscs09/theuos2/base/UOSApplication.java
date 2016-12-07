package com.uoscs09.theuos2.base;


import android.app.Application;
import android.os.StrictMode;

import com.uoscs09.theuos2.BuildConfig;
import com.uoscs09.theuos2.util.AppUtil;

import mj.android.utils.task.TaskQueue;

public class UOSApplication extends Application {
    public static final boolean DEBUG = BuildConfig.DEBUG;
    private TaskQueue taskQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG) {
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
        }

        AppUtil.init(this);

        taskQueue = new TaskQueue();
    }

    final TaskQueue taskQueue() {
        return taskQueue;
    }

}
