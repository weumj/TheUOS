package com.uoscs09.theuos2.base;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import com.uoscs09.theuos2.util.TrackerUtil;


public abstract class BaseAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        sendTrackerEvent(context, "enabled");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        sendTrackerEvent(context, "disabled");
    }

    @NonNull
    public abstract String getScreenNameForTracker();

    public void sendTrackerEvent(Context context, String action) {
        TrackerUtil.newInstance(context).sendEvent(getScreenNameForTracker(), action);
    }

    public void sendTrackerEvent(Context context, String action, String label) {
        TrackerUtil.newInstance(context).sendEvent(getScreenNameForTracker(), action, label);
    }

    public void sendTrackerEvent(Context context, String action, String label, long value) {
        TrackerUtil.newInstance(context).sendEvent(getScreenNameForTracker(), action, label, value);
    }

    public void sendClickEvent(Context context, String label) {
        TrackerUtil.newInstance(context).sendClickEvent(getScreenNameForTracker(), label);
    }

    public void sendClickEvent(Context context, String label, long value) {
        TrackerUtil.newInstance(context).sendClickEvent(getScreenNameForTracker(), label, value);
    }
}
