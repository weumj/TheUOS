package com.uoscs09.theuos2.appwidget.timetable;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseAppWidgetProvider;
import com.uoscs09.theuos2.tab.timetable.TimeTable;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mj.android.utils.task.Tasks;

public abstract class TimeTableWidget extends BaseAppWidgetProvider {
    public final static String WIDGET_TIMETABLE_REFRESH = "com.uoscs09.theuos2.widget.timetable.refresh";

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final PendingResult pendingResult = goAsync();
        Tasks.execute(() -> {
            TimeTable timeTable = null;
            try {
                timeTable = AppRequests.TimeTables.readFromFile(context).get();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            RemoteViews views = getRemoteViews(context);

            for (int appWidgetId : appWidgetIds) {
                // adapter
                Intent adapterIntent = new Intent(context, getListServiceClass())
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                adapterIntent.setData(Uri.parse(adapterIntent.toUri(Intent.URI_INTENT_SCHEME)));

                views.setRemoteAdapter(R.id.widget_timetable_listview, adapterIntent);
                views.setEmptyView(R.id.widget_timetable_listview, R.id.widget_timetable_empty);

                // refresh button
                Intent refreshIntent = new Intent(context, getWidgetClass())
                        .setAction(WIDGET_TIMETABLE_REFRESH)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

                PendingIntent p = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.widget_time_refresh, p);

                // title
                SimpleDateFormat date = new SimpleDateFormat("yyyy MMM dd E", Locale.getDefault());
                views.setTextViewText(R.id.widget_time_date, date.format(new Date()));

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

            pendingResult.finish();
        });
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context, getListServiceClass()));
    }

    protected abstract RemoteViews getRemoteViews(Context context);

    protected abstract Class<? extends WidgetTimeTableListService2> getListServiceClass();

    protected abstract Class<? extends TimeTableWidget> getWidgetClass();


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

                //context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE));

                callOnUpdate(context);
                // AppUtil.showToast(context, "새로고침...", true);
                //appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widget_timetable_listview);
                break;

            case Intent.ACTION_TIMEZONE_CHANGED:
            case Intent.ACTION_DATE_CHANGED:
            case Intent.ACTION_BOOT_COMPLETED:
                callOnUpdate(context);
                break;

            default:
                break;
        }

    }


}
