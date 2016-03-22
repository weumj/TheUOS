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
import com.uoscs09.theuos2.base.AbsAsyncWidgetProvider;
import com.uoscs09.theuos2.tab.libraryseat.SeatItem;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.PrefHelper;
import com.uoscs09.theuos2.util.TimeUtil;

import java.util.ArrayList;
import java.util.Date;

public class LibrarySeatWidget extends AbsAsyncWidgetProvider<ArrayList<SeatItem>> {
    public static final String LIBRARY_SEAT_WIDGET_REFRESH = "com.uoscs09.theuos2.widget.libraryseat.REFRESH";
    public static final String LIBRARY_SEAT_WIDGET_DATA = "com.uoscs09.theuos2.widget.libraryseat.DATA";
    public static final String LIBRARY_SEAT_WIDGET_ACTIVITY = "com.uoscs09.theuos2.widget.libraryseat.ACTIVITY";

    private final static int[] STUDY_ROOM_NUMBER_ARRAY = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 23, 24, 25, 26, 27, 28};
    private final static int REQUEST_REFRESH = 0;
    private final static int REQUEST_ITEM_CLICK = 1;
    private final static String DATE_FILE = "LIB_SEAT_WIDGET_UPDATE_DATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        setWidgetDefaultLayout(context, appWidgetManager, appWidgetIds, R.string.progress_while_loading);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        switch (intent.getAction()) {
            case LIBRARY_SEAT_WIDGET_REFRESH:
                callOnUpdate(context);
                // } else if (LIBRARY_SEAT_WIDGET_ACTIVITY.equals(action)) {
                // SeatItem item = (SeatItem) intent.getExtras().getSerializable(
                // LIBRARY_SEAT_WIDGET_DATA);
                // if (item != null) {
                // Intent activityIntent = new Intent(context,
                // SubSeatWebActivity.class);
                // activityIntent.getExtras().setClassLoader(
                // SeatItem.class.getClassLoader());
                // activityIntent.putExtra(TabLibrarySeatFragment.ITEM,
                // (Parcelable) item);
                // activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // context.startActivity(activityIntent);
                // }
                break;

            case Intent.ACTION_BOOT_COMPLETED:
                // 처음 부팅시 인터넷 접속이 되지 않으므로, 기존 파일에서 읽어온다.
                ArrayList<SeatItem> list = IOUtil.readInternalFileSilent(IOUtil.FILE_LIBRARY_SEAT);
                if (list == null)
                    return;

                onBackgroundTaskResult(context, AppWidgetManager.getInstance(context), new int[]{intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)}, list);

                break;
            default:
                break;
        }
    }

    @Override
    protected ArrayList<SeatItem> doInBackGround(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) throws Throwable {

        ArrayList<SeatItem> list = AppRequests.LibrarySeats.request().get().seatItemList;
        ArrayList<SeatItem> newList = new ArrayList<>();

        if (PrefHelper.LibrarySeats.isShowingWidgetStudyRoom()) {
            for (int i : STUDY_ROOM_NUMBER_ARRAY) {
                SeatItem item = list.get(i);
                // if (Double.parseDouble(item.utilizationRateStr) < 50d)
                newList.add(item);
            }
        } else {
            newList = list;
        }

        // 파일 저장
        IOUtil.writeObjectToInternalFileSilent(IOUtil.FILE_LIBRARY_SEAT, newList);
        // 불러온 시간 기록
        IOUtil.writeObjectToInternalFileSilent(DATE_FILE, TimeUtil.getFormat_am_hms().format(new Date()));
        return newList;
    }

    @Override
    protected void onBackgroundTaskResult(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, ArrayList<SeatItem> result) {

        for (int id : appWidgetIds) {

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_library_seat);

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(LIBRARY_SEAT_WIDGET_DATA, result);

            Intent intent = new Intent(context, LibrarySeatListService.class)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    .putExtra(LIBRARY_SEAT_WIDGET_DATA, bundle);

            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            rv.setRemoteAdapter(android.R.id.list, intent);
            rv.setEmptyView(android.R.id.list, android.R.id.empty);

            String dateTime = IOUtil.readInternalFileSilent(DATE_FILE);
            if (dateTime == null)
                dateTime = "";

            // refresh button
            rv.setTextViewText(android.R.id.text1, dateTime);

            Intent clickIntent = new Intent(context, LibrarySeatWidget.class);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id).setAction(LIBRARY_SEAT_WIDGET_REFRESH);
            rv.setOnClickPendingIntent(android.R.id.selectedIcon, PendingIntent.getBroadcast(context, REQUEST_REFRESH, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            // Collection OnclickListener
            // Intent collectionClickIntent = new Intent(
            // LIBRARY_SEAT_WIDGET_ACTIVITY);
            // collectionClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
            // id);
            // collectionClickIntent.setData(Uri.parse(new Intent(context,
            // LibrarySeatWidget.class).toUri(Intent.URI_INTENT_SCHEME)));
            // PendingIntent collectionClickPI = PendingIntent.getBroadcast(
            // context, REQUEST_ITEM_CLICK, collectionClickIntent,
            // PendingIntent.FLAG_UPDATE_CURRENT);
            // rv.setPendingIntentTemplate(android.R.id.list,
            // collectionClickPI);

            appWidgetManager.updateAppWidget(id, rv);
            appWidgetManager.notifyAppWidgetViewDataChanged(id, android.R.id.list);
        }
    }

    @Override
    protected void exceptionOccurred(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Throwable e) {
        super.exceptionOccurred(context, appWidgetManager, appWidgetIds, e);
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
