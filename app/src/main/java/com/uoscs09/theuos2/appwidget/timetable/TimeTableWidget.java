package com.uoscs09.theuos2.appwidget.timetable;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsAppWidgetProvider;
import com.uoscs09.theuos2.tab.timetable.TimeTable;
import com.uoscs09.theuos2.tab.timetable.TimetableUtil;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class TimeTableWidget extends AbsAppWidgetProvider {
    public final static String WIDGET_TIMETABLE_REFRESH = "com.uoscs09.theuos2.widget.timetable.refresh";
    public final static String WIDGET_TIMETABLE_DAY = "WIDGET_TIMETABLE_DAY";

    private static final Handler HANDLER = new Handler();

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(getComponentName(context));

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_timetable_listview);

        HANDLER.post(new UpdateThread(context, appWidgetManager, appWidgetIds));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_timetable_listview);

        HANDLER.post(new UpdateThread(context, appWidgetManager, appWidgetIds));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context, getListServiceClass()));
    }

    protected class UpdateThread implements Runnable {
        private final Context context;
        private final AppWidgetManager appWidgetManager;
        private final int[] appWidgetIds;

        public UpdateThread(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            this.context = context;
            this.appWidgetManager = appWidgetManager;
            this.appWidgetIds = appWidgetIds;
        }

        @Override
        public void run() {
            RemoteViews views = getRemoteViews(context);

            for (int appWidgetId : appWidgetIds) {
                // adapter
                Intent adapterIntent = new Intent(context, getListServiceClass());
                adapterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                adapterIntent.setData(Uri.parse(adapterIntent.toUri(Intent.URI_INTENT_SCHEME)));

                views.setRemoteAdapter(R.id.widget_timetable_listview, adapterIntent);
                views.setEmptyView(R.id.widget_timetable_listview, R.id.widget_timetable_empty);

                // refresh button
                Intent refreshIntent = getIntent(context, WIDGET_TIMETABLE_REFRESH);
                refreshIntent.setAction(WIDGET_TIMETABLE_REFRESH);
                refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

                PendingIntent p = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.widget_time_refresh, p);

                // title
                SimpleDateFormat date = new SimpleDateFormat("yyyy MMM dd E", Locale.getDefault());
                views.setTextViewText(R.id.widget_time_date, date.format(new Date()));

                TimeTable timeTable = TimetableUtil.readTimetable(context);
                if (timeTable != null) {
                    views.setTextViewText(R.id.widget_time_term, timeTable.getYearAndSemester());

                } else {
                    views.setTextViewText(R.id.widget_time_term, "");
                }

                // Intent serviceIntent = new Intent(context,
                // getListServiceClass());
                // serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                // appWidgetId);
                // context.startService(serviceIntent);

                appWidgetManager.updateAppWidget(appWidgetId, views);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_timetable_listview);
            }
        }
    }

    protected abstract RemoteViews getRemoteViews(Context context);

    protected abstract Class<? extends WidgetTimeTableListService2> getListServiceClass();

    protected abstract Class<? extends TimeTableWidget> getWidgetClass();

    protected Intent getIntent(Context context, String action) {
        Intent intent = new Intent(context, getWidgetClass());
        intent.setComponent(getComponentName(context));
        intent.setAction(action);
        return intent;
    }

    private ComponentName getComponentName(Context context) {
        return new ComponentName(context, getWidgetClass());
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        switch (intent.getAction()) {
            case WIDGET_TIMETABLE_REFRESH:
                long z = 0;
                long waitTime = PrefUtil.getInstance(context).get("widget_timetable_refresh", z);
                long wait = System.currentTimeMillis();
                long diff = wait - waitTime;

                if (diff < 0 || diff > 5000) {
                    PrefUtil.getInstance(context).put("widget_timetable_refresh", wait);

                } else {
                    AppUtil.showToast(context, R.string.progress_wait, true);
                    return;

                }

                context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE));

                int[] ids = new int[]{intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)};

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                onUpdate(context, appWidgetManager, ids);
                // AppUtil.showToast(context, "새로고침...", true);
                appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widget_timetable_listview);

                break;

            case Intent.ACTION_BOOT_COMPLETED:
                context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE));
                break;
        }

    }

    @NonNull
    @Override
    protected String getTrackerName() {
        return "TimeTableWidget";
    }
}
