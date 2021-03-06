package com.uoscs09.theuos2.appwidget.timetable;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseAppWidgetProvider;
import com.uoscs09.theuos2.tab.timetable.Timetable2;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class TimeTableWidget extends BaseAppWidgetProvider {
    private final static String WIDGET_TIMETABLE_REFRESH_INTERNAL = "com.uoscs09.theuos2.widget.timetable.refresh_internal";
    public final static String WIDGET_TIMETABLE_REFRESH = "com.uoscs09.theuos2.widget.timetable";

    public static void sendRefreshIntent(Context context) {
        // fix?
        sendRefreshIntentInternal(context, TimeTableWidget5x4.class);
        sendRefreshIntentInternal(context, TimeTableWidget4x4.class);
    }

    private static void sendRefreshIntentInternal(Context context, Class<? extends TimeTableWidget> clz) {
        Intent intent = new Intent(context, clz);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, clz));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }


    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final PendingResult pendingResult = goAsync();
        AppRequests.TimeTables.readFile()
                .subscribe(
                        result -> onResult(context, appWidgetManager, appWidgetIds, result),
                        t -> {
                            t.printStackTrace();
                            pendingResult.finish();
                        },
                        pendingResult::finish
                );

    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context, getListServiceClass()));
    }

    void onResult(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Timetable2 timeTable) {
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
                    .setAction(WIDGET_TIMETABLE_REFRESH_INTERNAL)
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
    }


    protected abstract RemoteViews getRemoteViews(Context context);

    //fix
    protected abstract Class<? extends WidgetTimeTableListService2> getListServiceClass();

    protected abstract Class<? extends TimeTableWidget> getWidgetClass();


    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        switch (intent.getAction()) {
            case WIDGET_TIMETABLE_REFRESH_INTERNAL:
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
            case WIDGET_TIMETABLE_REFRESH:
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
