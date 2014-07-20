package com.uoscs09.theuos.widget.timetable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.widget.RemoteViews;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;

public abstract class TimeTableWidget extends AppWidgetProvider {
	public final static String WIDGET_TIMETABLE_REFRESH = "com.uoscs09.theuos.widget.timetable.refresh";
	public final static String WIDGET_TIMETABLE_DAY = "WIDGET_TIMETABLE_DAY";
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(getComponentName(context));
		appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,
				R.id.widget_timetable_listview);
		new Handler().post(new UpdateThread(context, appWidgetManager,
				appWidgetIds));
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,
				R.id.widget_timetable_listview);
		new Handler().post(new UpdateThread(context, appWidgetManager,
				appWidgetIds));
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		context.stopService(new Intent(context, getListServiceClass()));
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
			RemoteViews views = getRemoteViews(context);

			for (int appWidgetId : appWidgetIds) {
				Intent adapterIntent = new Intent(context,
						getListServiceClass());
				adapterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetId);
				adapterIntent.setData(Uri.parse(adapterIntent
						.toUri(Intent.URI_INTENT_SCHEME)));
				views.setRemoteAdapter(R.id.widget_timetable_listview,
						adapterIntent);
				views.setEmptyView(R.id.widget_timetable_listview,
						R.id.widget_timetable_empty);

				Intent refreshIntent = getIntent(context, WIDGET_TIMETABLE_REFRESH);
				refreshIntent.setAction(WIDGET_TIMETABLE_REFRESH);
				refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetId);
				PendingIntent p = PendingIntent.getBroadcast(context, 0,
						refreshIntent, 0);
				views.setOnClickPendingIntent(R.id.widget_time_refresh, p);

				updateTitle(views);
				Intent serviceIntent = new Intent(context,
						getListServiceClass());
				serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetId);
				//context.startService(serviceIntent);

				appWidgetManager.updateAppWidget(appWidgetId, views);
				appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
						R.id.widget_timetable_listview);
			}
			return;
		}
	}

	protected abstract RemoteViews getRemoteViews(Context context);

	protected abstract Class<? extends WidgetTimetableListService> getListServiceClass();

	protected abstract Class<? extends TimeTableWidget> getWidgetClass();

	protected void updateTitle(RemoteViews rv) {
		SimpleDateFormat date = new SimpleDateFormat("yyyy MMM dd E",
				Locale.KOREA);
		rv.setTextViewText(R.id.widget_time_date, date.format(new Date()));
	}

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
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		AppWidgetManager m = AppWidgetManager.getInstance(context);
		if (WIDGET_TIMETABLE_REFRESH.equals(action)) {
			long z = 0;
			long waitTime = PrefUtil.getInstance(context).get(
					"widget_timetable_refresh", z);
			long wait = System.currentTimeMillis();
			long diff = wait - waitTime;
			if (diff < 0 || diff > 5000) {
				PrefUtil.getInstance(context).put("widget_timetable_refresh",
						wait);
			} else {
				AppUtil.showToast(context, R.string.progress_wait, true);
				return;
			}
			context.sendBroadcast(new Intent(
					AppWidgetManager.ACTION_APPWIDGET_UPDATE));
			int[] ids = new int[] { intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID) };
			onUpdate(context, m, ids);
			//AppUtil.showToast(context, "새로고침...", true);
			AppWidgetManager.getInstance(context)
					.notifyAppWidgetViewDataChanged(ids,
							R.id.widget_timetable_listview);
			return;
		} else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			context.sendBroadcast(new Intent(
					AppWidgetManager.ACTION_APPWIDGET_UPDATE));
		}
	}
}
