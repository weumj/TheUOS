package com.uoscs09.theuos2.base;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;

import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.TrackerUtil;


public abstract class BaseAppWidgetProvider extends AppWidgetProvider {

    private TrackerUtil trackerUtil;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        trackerUtil = getTrackerUtil(context);

        sendTrackerEvent("enabled");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        sendTrackerEvent("disabled");
    }

    protected void callOnUpdate(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));

        onUpdate(context, appWidgetManager, ids);
    }

    @NonNull
    public abstract String getScreenNameForTracker();

    private TrackerUtil getTrackerUtil(Context context){
        if(trackerUtil == null){
            if(context == null) context = AppUtil.context();
            if(context == null) return null;
            trackerUtil = new TrackerUtil(context);
        }
        return trackerUtil;
    }


    public void sendTrackerEvent(String action) {
        if ((trackerUtil = getTrackerUtil(null)) != null)
            trackerUtil.sendEvent(getScreenNameForTracker(), action);
    }

    public void sendTrackerEvent(String action, String label) {
        if ((trackerUtil = getTrackerUtil(null)) != null)
            trackerUtil.sendEvent(getScreenNameForTracker(), action, label);
    }

    public void sendTrackerEvent(String action, String label, long value) {
        if ((trackerUtil = getTrackerUtil(null)) != null)
            trackerUtil.sendEvent(getScreenNameForTracker(), action, label, value);
    }

    public void sendClickEvent(String label) {
        if ((trackerUtil = getTrackerUtil(null)) != null)
            trackerUtil.sendClickEvent(getScreenNameForTracker(), label);
    }

    public void sendClickEvent(String label, long value) {
        if ((trackerUtil = getTrackerUtil(null)) != null)
            trackerUtil.sendClickEvent(getScreenNameForTracker(), label, value);
    }
}
