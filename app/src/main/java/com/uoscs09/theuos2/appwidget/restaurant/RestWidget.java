package com.uoscs09.theuos2.appwidget.restaurant;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.widget.RemoteViews;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseAppWidgetProvider;
import com.uoscs09.theuos2.tab.restaurant.RestItem;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;

public class RestWidget extends BaseAppWidgetProvider {
    public static final String REST_WIDGET_NEXT_ACTION = "com.uoscs09.theuos2.widget.restaurant.NEXT";
    public static final String REST_WIDGET_PREV_ACTION = "com.uoscs09.theuos2.widget.restaurant.PREV";
    public static final String REST_WIDGET_POSITION = "REST_WIDGET_POSITION";
    //public static final String REST_WIDGET_ITEM = "REST_WIDGET_ITEM";

    private static final String[] REST_TAB_MENU_STRING_LABEL = {"학생회관", "양식당", "자연과학관", "본관 8층", "생활관"};

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        setWidgetDefaultLayout(context, appWidgetManager, appWidgetIds);


        final PendingResult pendingResult = goAsync();
        AppRequests.Restaurants.request(false)
                .subscribe(
                        result -> onBackgroundTaskResult(context, appWidgetManager, appWidgetIds, result),
                        e -> {
                            exceptionOccurred(context, appWidgetManager, appWidgetIds, e);
                            pendingResult.finish();
                        },
                        pendingResult::finish
                );
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        switch (intent.getAction()) {

            case REST_WIDGET_NEXT_ACTION: {
                int position = intent.getIntExtra(REST_WIDGET_POSITION, 0) + 1;
                if (position > 4)
                    position = 0;

                PrefUtil.getInstance(context).put(REST_WIDGET_POSITION, position);

                callOnUpdate(context);
                break;
            }

            case REST_WIDGET_PREV_ACTION: {
                int position = intent.getIntExtra(REST_WIDGET_POSITION, 5) - 1;
                if (position < 0)
                    position = 4;

                PrefUtil.getInstance(context).put(REST_WIDGET_POSITION, position);

                callOnUpdate(context);
                break;
            }

            case Intent.ACTION_BOOT_COMPLETED:
                // 처음 부팅시 인터넷 접속이 되지 않으므로, 기존 파일에서 읽어온다.
                SparseArray<RestItem> map = AppRequests.Restaurants.readFromFile();
                if (map.size() == 0)
                    return;

                onBackgroundTaskResult(context, AppWidgetManager.getInstance(context), new int[]{intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)}, map);
                break;

            default:
                break;
        }
    }

    void onBackgroundTaskResult(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, SparseArray<RestItem> result) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_rest);

        int position = PrefUtil.getInstance(context).get(REST_WIDGET_POSITION, 0);

        for (int id : appWidgetIds) {
            PrefUtil.getInstance(context).put(REST_WIDGET_POSITION, position);

            //Bundle bundle = new Bundle();
            //bundle.putParcelableArrayList(REST_WIDGET_ITEM, result);

            Intent intent = new Intent(context, RestListService.class)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    .putExtra(REST_WIDGET_POSITION, position);
            //.putExtra(REST_WIDGET_ITEM, bundle);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            rv.setRemoteAdapter(R.id.widget_rest_listview, intent);
            rv.setTextViewText(R.id.widget_rest_main_title, REST_TAB_MENU_STRING_LABEL[position]);

            rv.setOnClickPendingIntent(
                    R.id.widget_rest_btn_next,
                    getMoveIntent(context, id, REST_WIDGET_NEXT_ACTION, position)
            );
            rv.setOnClickPendingIntent(
                    R.id.widget_rest_btn_prev,
                    getMoveIntent(context, id, REST_WIDGET_PREV_ACTION, position)
            );

            appWidgetManager.updateAppWidget(id, rv);
            appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.widget_rest_listview);
        }
    }

    void exceptionOccurred(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Throwable e) {
        AppUtil.showErrorToast(context, e, true);
        setWidgetDefaultLayout(context, appWidgetManager, appWidgetIds);
    }

    /**
     * 다음 메뉴로 움직일 버튼의 눌렸을 때 사용 될 PendingIntent를 설정한다.
     */
    private PendingIntent getMoveIntent(Context context, int id, final String action, int position) {
        final Intent i = new Intent(context, RestWidget.class);
        i.setAction(action);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        i.putExtra(REST_WIDGET_POSITION, position);
        return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 식단표 위젯의 화면을 기본 형태로 설정한다.
     */
    private void setWidgetDefaultLayout(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_rest);
        int position = PrefUtil.getInstance(context).get(REST_WIDGET_POSITION, 0);

        for (int id : appWidgetIds) {
            rv.setOnClickPendingIntent(
                    R.id.widget_rest_btn_next,
                    getMoveIntent(context, id, REST_WIDGET_NEXT_ACTION, position)
            );

            rv.setOnClickPendingIntent(
                    R.id.widget_rest_btn_prev,
                    getMoveIntent(context, id, REST_WIDGET_PREV_ACTION, position)
            );

            appWidgetManager.updateAppWidget(id, rv);
        }
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "RestaurantWidget";
    }

}
