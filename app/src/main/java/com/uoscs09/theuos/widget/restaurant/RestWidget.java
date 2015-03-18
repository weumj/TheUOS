package com.uoscs09.theuos.widget.restaurant;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsAsyncWidgetProvider;
import com.uoscs09.theuos.common.util.IOUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.tab.restaurant.RestItem;
import com.uoscs09.theuos.tab.restaurant.TabRestaurantFragment;

import java.util.ArrayList;

public class RestWidget extends AbsAsyncWidgetProvider<ArrayList<RestItem>> {
	public static final String REST_WIDGET_NEXT_ACTION = "com.uoscs09.theuos.widget.restaurant.NEXT";
	public static final String REST_WIDGET_PREV_ACTION = "com.uoscs09.theuos.widget.restaurant.PREV";
	public static final String REST_WIDGET_POSITION = "REST_WIDGET_POSITION";
	public static final String REST_WIDGET_ITEM = "REST_WIDGET_ITEM";

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		setWidgetDefaultLayout(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(@NonNull Context context, @NonNull Intent intent) {
		final String action = intent.getAction();
        switch (action) {
            case REST_WIDGET_NEXT_ACTION: {
                int position = intent.getIntExtra(REST_WIDGET_POSITION, 0) + 1;
                PrefUtil.getInstance(context).put(REST_WIDGET_POSITION, position);
                int[] ids = new int[]{intent.getIntExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID)};
                onUpdate(context, AppWidgetManager.getInstance(context), ids);
                context.sendBroadcast(new Intent(
                        AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID, ids[0]));
                break;
            }

            case REST_WIDGET_PREV_ACTION: {
                int position = intent.getIntExtra(REST_WIDGET_POSITION, 0) - 1;
                PrefUtil.getInstance(context).put(REST_WIDGET_POSITION, position);
                int[] ids = new int[]{intent.getIntExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID)};
                onUpdate(context, AppWidgetManager.getInstance(context), ids);
                context.sendBroadcast(new Intent(
                        AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID, ids[0]));
                break;
            }

            case Intent.ACTION_BOOT_COMPLETED:
                // 처음 부팅시 인터넷 접속이 되지 않으므로, 기존 파일에서 읽어온다.
                ArrayList<RestItem> list = IOUtil.readFromFileSuppressed(context,
                        IOUtil.FILE_REST);
                if (list == null)
                    return;
                onBackgroundTaskResult(context,
                        AppWidgetManager.getInstance(context),
                        new int[]{intent.getIntExtra(
                                AppWidgetManager.EXTRA_APPWIDGET_ID,
                                AppWidgetManager.INVALID_APPWIDGET_ID)}, list);
                break;
            default:
                super.onReceive(context, intent);
                break;
        }
	}

	@Override
	protected ArrayList<RestItem> doInBackGround(Context context,
			AppWidgetManager appWidgetManager, int[] appWidgetIds)
			throws Exception {
		if (OApiUtil.getDateTime()
				- PrefUtil.getInstance(context).get(
						PrefUtil.KEY_REST_DATE_TIME, 0) < 3) {
			ArrayList<RestItem> list = IOUtil.readFromFileSuppressed(context,
					IOUtil.FILE_REST);
			if (list == null)
				list = TabRestaurantFragment.getRestListFromWeb(context);
			return list;
		} else {
			return TabRestaurantFragment.getRestListFromWeb(context);
		}
	}

	@Override
	protected void onBackgroundTaskResult(Context context,
			AppWidgetManager appWidgetManager, int[] appWidgetIds,
			ArrayList<RestItem> result) {
		RemoteViews rv = new RemoteViews(context.getPackageName(),
				R.layout.widget_rest);
		RestItem item;

		int position = PrefUtil.getInstance(context).get(REST_WIDGET_POSITION,
				0);
		for (int id : appWidgetIds) {
			// position은 이미 onReceive()에서 값이 변경되어 기록된다.
			// 여기서는 값의 범위를 검사한다 (유효성 검사)
			if (position >= result.size()) {
				position = 0;
			} else if (position < 0) {
				position = result.size() - 1;
			}
			PrefUtil.getInstance(context).put(REST_WIDGET_POSITION, position);
			item = result.get(position);

			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList(REST_WIDGET_ITEM, result);
			Intent intent = new Intent(context, RestListService.class)
					.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
					.putExtra(REST_WIDGET_POSITION, position)
					.putExtra(REST_WIDGET_ITEM, bundle);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

			rv.setRemoteAdapter(R.id.widget_rest_listview, intent);
			rv.setTextViewText(R.id.widget_rest_main_title, item.title);

			rv.setOnClickPendingIntent(
					R.id.widget_rest_btn_next,
					getMoveIntent(context, id, REST_WIDGET_NEXT_ACTION,position));
			rv.setOnClickPendingIntent(
					R.id.widget_rest_btn_prev,
					getMoveIntent(context, id, REST_WIDGET_PREV_ACTION,position));

			appWidgetManager.updateAppWidget(id, rv);
			appWidgetManager.notifyAppWidgetViewDataChanged(id,R.id.widget_rest_listview);
		}
	}

	/** 다음 메뉴로 움직일 버튼의 눌렸을 때 사용 될 PendingIntent를 설정한다. */
	private PendingIntent getMoveIntent(Context context, int id,			final String action, int position) {
		final Intent i = new Intent(context, RestWidget.class);
		i.setAction(action);
		i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
		i.putExtra(REST_WIDGET_POSITION, position);
        return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	protected void exceptionOccurred(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Exception e) {
		super.exceptionOccurred(context, appWidgetManager, appWidgetIds, e);
		setWidgetDefaultLayout(context, appWidgetManager, appWidgetIds);
	}

	/** 식단표 위젯의 화면을 기본 형태로 설정한다. */
	private void setWidgetDefaultLayout(Context context,			AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews rv = new RemoteViews(context.getPackageName(),R.layout.widget_rest);
		int position = PrefUtil.getInstance(context).get(REST_WIDGET_POSITION,0);

		for (int id : appWidgetIds) {
			rv.setOnClickPendingIntent(
					R.id.widget_rest_btn_next,
					getMoveIntent(context, id, REST_WIDGET_NEXT_ACTION,position));

			rv.setOnClickPendingIntent(
					R.id.widget_rest_btn_prev,
					getMoveIntent(context, id, REST_WIDGET_PREV_ACTION,position));

			appWidgetManager.updateAppWidget(id, rv);
		}
	}
}
