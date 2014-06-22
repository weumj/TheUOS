package com.uoscs09.theuos.widget.restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.tab.restaurant.RestItem;
import com.uoscs09.theuos.tab.restaurant.TabRestaurantFragment;

public class RestWidget extends AppWidgetProvider {
	public static final String REST_WIDGET_NEXT_ACTION = "com.uoscs09.theuos.widget.restaurant.NEXT";
	public static final String REST_WIDGET_PREV_ACTION = "com.uoscs09.theuos.widget.restaurant.PREV";
	public static final String REST_WIDGET_POSITION = "REST_WIDGET_POSITION";
	public static final String REST_WIDGET_ITEM = "REST_WIDGET_ITEM";

	public static List<RestItem> getList(final Context context) {
		if (OApiUtil.getDateTime()
				- PrefUtil.getInstance(context).get(
						PrefUtil.KEY_REST_DATE_TIME, 0) < 3) {
			List<RestItem> list = AppUtil.readFromFile(context,
					AppUtil.FILE_REST);
			if (list == null)
				list = getRestListByThreading(context);
			return list;
		} else {
			return getRestListByThreading(context);
		}
	}

	protected static List<RestItem> getRestListByThreading(final Context context) {
		ExecutorService es = Executors.newFixedThreadPool(2);
		List<RestItem> list = null;
		try {
			list = es.submit(new Callable<List<RestItem>>() {
				@Override
				public List<RestItem> call() throws Exception {
					return TabRestaurantFragment.getRestListFromWeb(context);
				}
			}).get();
		} catch (Exception e) {
			e.printStackTrace();
			list = new ArrayList<RestItem>();
		} finally {
			es.shutdown();
		}
		return list;
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		new Handler().post(new UpdateThread(context, appWidgetManager,
				appWidgetIds));
	}

	protected class UpdateThread implements Runnable {
		private Context context;
		private AppWidgetManager appWidgetManager;
		private int[] appWidgetIds;

		public UpdateThread(Context context, AppWidgetManager appWidgetManager,
				int[] appWidgetIds) {
			this.context = context;
			this.appWidgetManager = appWidgetManager;
			this.appWidgetIds = appWidgetIds;
		}

		@Override
		public void run() {
			RemoteViews rv = new RemoteViews(context.getPackageName(),
					R.layout.widget_rest);
			List<RestItem> list = getList(context);
			RestItem item;
			int position = PrefUtil.getInstance(context).get(
					REST_WIDGET_POSITION, 0);
			for (int id : appWidgetIds) {
				try {
					item = list.get(position);
				} catch (Exception e) {
					e.printStackTrace();
					position = 0;
					try {
						list = getRestListByThreading(context);
						Log.w("restwidget", String.valueOf(list.size()));
						item = list.get(position);
					} catch (Exception e2) {
						Log.w("restwidget", e2);
						position = PrefUtil.getInstance(context).get(
								REST_WIDGET_POSITION, 0);
						list = AppUtil.readFromFile(context, AppUtil.FILE_REST);
						item = list.get(position);
					}
				}
				Intent intent = new Intent(context, RestListService.class)
						.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
						.putExtra(REST_WIDGET_POSITION, position);
				intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
				rv.setRemoteAdapter(R.id.widget_rest_listview, intent);
				rv.setTextViewText(R.id.widget_rest_main_title, item.title);
				rv.setOnClickPendingIntent(
						R.id.widget_rest_btn_next,
						getMoveIntent(context, id, REST_WIDGET_NEXT_ACTION,
								position));
				rv.setOnClickPendingIntent(
						R.id.widget_rest_btn_prev,
						getMoveIntent(context, id, REST_WIDGET_PREV_ACTION,
								position));
				appWidgetManager.updateAppWidget(id, rv);
				appWidgetManager.notifyAppWidgetViewDataChanged(id,
						R.id.widget_rest_listview);
			}
			return;
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
			List<RestItem> list = getList(context);
			int size = list.size();
			int position = intent.getIntExtra(REST_WIDGET_POSITION, 0);
			if (++position >= size)
				position = 0;
			PrefUtil.getInstance(context).put(REST_WIDGET_POSITION, position);
			int[] ids = new int[] { intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID) };
			onUpdate(context, AppWidgetManager.getInstance(context), ids);
			context.sendBroadcast(new Intent(
					AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, ids[0]));
		} else if (REST_WIDGET_PREV_ACTION.equals(action)) {
			List<RestItem> list = getList(context);
			int position = intent.getIntExtra(REST_WIDGET_POSITION, 0);
			if (--position < 0)
				position = list.size() - 1;
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
