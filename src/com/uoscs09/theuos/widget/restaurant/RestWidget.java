package com.uoscs09.theuos.widget.restaurant;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.IOUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.tab.restaurant.RestItem;
import com.uoscs09.theuos.tab.restaurant.TabRestaurantFragment;

public class RestWidget extends AppWidgetProvider {
	public static final String REST_WIDGET_NEXT_ACTION = "com.uoscs09.theuos.widget.restaurant.NEXT";
	public static final String REST_WIDGET_PREV_ACTION = "com.uoscs09.theuos.widget.restaurant.PREV";
	public static final String REST_WIDGET_POSITION = "REST_WIDGET_POSITION";
	public static final String REST_WIDGET_ITEM = "REST_WIDGET_ITEM";

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		new AsyncLoader<ArrayList<RestItem>>().excute(
				new Callable<ArrayList<RestItem>>() {

					@Override
					public ArrayList<RestItem> call() throws Exception {
						if (OApiUtil.getDateTime()
								- PrefUtil.getInstance(context).get(
										PrefUtil.KEY_REST_DATE_TIME, 0) < 3) {
							ArrayList<RestItem> list = IOUtil
									.readFromFileSuppressed(context,
											IOUtil.FILE_REST);
							if (list == null)
								list = TabRestaurantFragment
										.getRestListFromWeb(context);
							return list;
						} else {
							return TabRestaurantFragment
									.getRestListFromWeb(context);
						}
					}
				}, new AsyncLoader.OnTaskFinishedListener() {
					@Override
					public void onTaskFinished(boolean isExceptionOccoured,
							Object data) {
						if (isExceptionOccoured) {
							AppUtil.showErrorToast(context, (Exception) data,
									true);
						} else {
							RemoteViews rv = new RemoteViews(context
									.getPackageName(), R.layout.widget_rest);
							@SuppressWarnings("unchecked")
							ArrayList<RestItem> list = (ArrayList<RestItem>) data;
							RestItem item;
							int position = PrefUtil.getInstance(context).get(
									REST_WIDGET_POSITION, 0);
							for (int id : appWidgetIds) {
								if (position >= list.size()) {
									position = 0;
								} else if (position < 0) {
									position = list.size() - 1;
								}
								PrefUtil.getInstance(context).put(
										REST_WIDGET_POSITION, position);
								item = list.get(position);
								Bundle bundle = new Bundle();
								bundle.putParcelableArrayList(REST_WIDGET_ITEM,
										list);
								Intent intent = new Intent(context,
										RestListService.class)
										.putExtra(
												AppWidgetManager.EXTRA_APPWIDGET_ID,
												id)
										.putExtra(REST_WIDGET_POSITION,
												position)
										.putExtra(REST_WIDGET_ITEM, bundle);
								intent.setData(Uri.parse(intent
										.toUri(Intent.URI_INTENT_SCHEME)));
								rv.setRemoteAdapter(R.id.widget_rest_listview,
										intent);
								rv.setTextViewText(R.id.widget_rest_main_title,
										item.title);
								rv.setOnClickPendingIntent(
										R.id.widget_rest_btn_next,
										getMoveIntent(context, id,
												REST_WIDGET_NEXT_ACTION,
												position));
								rv.setOnClickPendingIntent(
										R.id.widget_rest_btn_prev,
										getMoveIntent(context, id,
												REST_WIDGET_PREV_ACTION,
												position));
								appWidgetManager.updateAppWidget(id, rv);
								appWidgetManager
										.notifyAppWidgetViewDataChanged(id,
												R.id.widget_rest_listview);
							}
						}
					}
				});
		RemoteViews rv = new RemoteViews(context.getPackageName(),
				R.layout.widget_rest);
		for (int id : appWidgetIds) {
			rv.setTextViewText(R.id.widget_rest_main_title,
					context.getText(R.string.progress_while_loading));
			rv.setOnClickPendingIntent(R.id.widget_rest_btn_next, null);
			rv.setOnClickPendingIntent(R.id.widget_rest_btn_prev, null);
			appWidgetManager.updateAppWidget(id, rv);
		}
	}

	private PendingIntent getMoveIntent(Context context, int id, String action,
			int position) {
		final Intent i = new Intent(context, RestWidget.class);
		i.setAction(action);
		i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
		i.putExtra(REST_WIDGET_POSITION, position);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pi;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (REST_WIDGET_NEXT_ACTION.equals(action)) {
			int position = intent.getIntExtra(REST_WIDGET_POSITION, 0) + 1;
			PrefUtil.getInstance(context).put(REST_WIDGET_POSITION, position);
			int[] ids = new int[] { intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID) };
			onUpdate(context, AppWidgetManager.getInstance(context), ids);
			context.sendBroadcast(new Intent(
					AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, ids[0]));
		} else if (REST_WIDGET_PREV_ACTION.equals(action)) {
			int position = intent.getIntExtra(REST_WIDGET_POSITION, 0) - 1;
			PrefUtil.getInstance(context).put(REST_WIDGET_POSITION, position);
			int[] ids = new int[] { intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID) };
			onUpdate(context, AppWidgetManager.getInstance(context), ids);
			context.sendBroadcast(new Intent(
					AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, ids[0]));
		}
		super.onReceive(context, intent);
	}
}
