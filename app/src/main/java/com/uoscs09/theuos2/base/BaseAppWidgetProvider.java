package com.uoscs09.theuos2.base;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
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

    protected void callOnUpdate(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

        onUpdate(context, appWidgetManager, ids);
    }

    @NonNull
    public abstract String getScreenNameForTracker();

    protected boolean isApplication(Context context) {
        return context.getApplicationContext() instanceof UOSApplication;
    }

    protected TrackerUtil getTrackerUtil(Context context) {
        return isApplication(context) ? ((UOSApplication) context.getApplicationContext()).getTrackerUtil() : TrackerUtil.newInstance(context);
    }

    public void sendTrackerEvent(Context context, String action) {
        getTrackerUtil(context).sendEvent(getScreenNameForTracker(), action);
    }

    public void sendTrackerEvent(Context context, String action, String label) {
        getTrackerUtil(context).sendEvent(getScreenNameForTracker(), action, label);
    }

    public void sendTrackerEvent(Context context, String action, String label, long value) {
        getTrackerUtil(context).sendEvent(getScreenNameForTracker(), action, label, value);
    }

    public void sendClickEvent(Context context, String label) {
        getTrackerUtil(context).sendClickEvent(getScreenNameForTracker(), label);
    }

    public void sendClickEvent(Context context, String label, long value) {
        getTrackerUtil(context).sendClickEvent(getScreenNameForTracker(), label, value);
    }
}
