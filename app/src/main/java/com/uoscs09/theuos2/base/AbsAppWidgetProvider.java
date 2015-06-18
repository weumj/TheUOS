package com.uoscs09.theuos2.base;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import com.uoscs09.theuos2.util.TrackerUtil;


public abstract class AbsAppWidgetProvider extends AppWidgetProvider{
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);


      TrackerUtil.newInstance(context).sendEvent(getTrackerName(), "onEnabled");

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        TrackerUtil.newInstance(context).sendEvent(getTrackerName(), "onDeleted");
    }

    @NonNull
    protected abstract String getTrackerName();
}
