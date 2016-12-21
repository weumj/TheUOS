package com.uoscs09.theuos2.appwidget.libraryseat;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseAppWidgetProvider;
import com.uoscs09.theuos2.tab.libraryseat.SeatInfo;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LibrarySeatWidget extends BaseAppWidgetProvider {
    public static final String LIBRARY_SEAT_WIDGET_REFRESH = "com.uoscs09.theuos2.widget.libraryseat.REFRESH";
    public static final String LIBRARY_SEAT_WIDGET_DATA = "com.uoscs09.theuos2.widget.libraryseat.DATA";
    public static final String LIBRARY_SEAT_WIDGET_ACTIVITY = "com.uoscs09.theuos2.widget.libraryseat.ACTIVITY";

    private final static int REQUEST_REFRESH = 0;
    private final static int REQUEST_ITEM_CLICK = 1;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        setWidgetDefaultLayout(context, appWidgetManager, appWidgetIds, R.string.progress_while_loading);
        final PendingResult pendingResult = goAsync();
        AppRequests.LibrarySeats.widgetDataRequest().delayed()
                .result(result -> onBackgroundTaskResult(context, appWidgetManager, appWidgetIds, result))
                .error(error -> exceptionOccurred(context, appWidgetManager, appWidgetIds, error))
                .atLast(pendingResult::finish)
                .execute();
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        switch (intent.getAction()) {
            case LIBRARY_SEAT_WIDGET_REFRESH:
                callOnUpdate(context);
                break;

            case Intent.ACTION_BOOT_COMPLETED:
                // 처음 부팅시 인터넷 접속이 되지 않으므로, 기존 파일에서 읽어온다.
                List<SeatInfo> list;
                try {
                    list = AppRequests.LibrarySeats.readFile().get();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    return;
                }
                if (list == null)
                    return;

                onBackgroundTaskResult(context, AppWidgetManager.getInstance(context), new int[]{intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)}, list);

                break;
            default:
                break;
        }
    }

    void onBackgroundTaskResult(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, List<SeatInfo> result) {
        String dateTime = new SimpleDateFormat("a hh:mm:ss", Locale.getDefault()).format(new Date());
        for (int id : appWidgetIds) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_library_seat);

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(LIBRARY_SEAT_WIDGET_DATA, new ArrayList<>(result));

            Intent intent = new Intent(context, LibrarySeatListService.class)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    .putExtra(LIBRARY_SEAT_WIDGET_DATA, bundle);

            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            rv.setRemoteAdapter(android.R.id.list, intent);
            rv.setEmptyView(android.R.id.list, android.R.id.empty);


            // refresh button
            rv.setTextViewText(android.R.id.text1, dateTime);

            Intent clickIntent = new Intent(context, LibrarySeatWidget.class);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id).setAction(LIBRARY_SEAT_WIDGET_REFRESH);
            rv.setOnClickPendingIntent(android.R.id.selectedIcon, PendingIntent.getBroadcast(context, REQUEST_REFRESH, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            appWidgetManager.updateAppWidget(id, rv);
            appWidgetManager.notifyAppWidgetViewDataChanged(id, android.R.id.list);
        }
    }

    void exceptionOccurred(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Throwable e) {
        AppUtil.showErrorToast(context, e, true);
        setWidgetDefaultLayout(context, appWidgetManager, appWidgetIds, R.string.progress_fail);
    }

    /**
     * 위젯의 화면을 기본 형태로 설정한다.
     */
    private void setWidgetDefaultLayout(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, int textId) {
        for (int id : appWidgetIds) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_library_seat);
            rv.setTextViewText(android.R.id.text1, context.getText(textId));

            Intent clickIntent = new Intent(context, LibrarySeatWidget.class);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id).setAction(LIBRARY_SEAT_WIDGET_REFRESH);
            rv.setOnClickPendingIntent(android.R.id.selectedIcon, PendingIntent.getBroadcast(context, REQUEST_REFRESH, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            // Collection OnclickListener
            Intent intent = new Intent(context, LibrarySeatWidget.class);
            Intent collectionClickIntent = new Intent(LIBRARY_SEAT_WIDGET_ACTIVITY);
            collectionClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
            collectionClickIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            PendingIntent collectionClickPI = PendingIntent.getBroadcast(context, REQUEST_ITEM_CLICK, collectionClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(android.R.id.list, collectionClickPI);

            appWidgetManager.updateAppWidget(id, rv);
        }

    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "LibrarySeatWidget";
    }

}
