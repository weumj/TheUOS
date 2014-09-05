package com.uoscs09.theuos.widget.libraryseat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsAsyncWidgetProvider;
import com.uoscs09.theuos.common.util.IOUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;
import com.uoscs09.theuos.tab.libraryseat.SeatItem;
import com.uoscs09.theuos.tab.libraryseat.TabLibrarySeatFragment;

public class LibrarySeatWidget extends
		AbsAsyncWidgetProvider<ArrayList<SeatItem>> {
	public static final String LIBRARY_SEAT_WIDGET_REFRASH = "com.uoscs09.theuos.widget.libraryseat.REFRESH";
	public static final String LIBRARY_SEAT_WIDGET_DATA = "com.uoscs09.theuos.widget.libraryseat.DATA";
	private final static int[] STURDY_ROOM_NUMBER_ARRAY = { 0, 1, 2, 3, 4, 5,
			6, 7, 8, 9, 10, 11, 12, 23, 24, 25, 26, 27, 28 };

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		RemoteViews rv = new RemoteViews(context.getPackageName(),
				R.layout.widget_library_seat);
		for (int id : appWidgetIds) {
			rv.setTextViewText(android.R.id.text1,
					context.getText(R.string.progress_while_loading));
			rv.setOnClickPendingIntent(android.R.id.selectedIcon, null);
			rv.setEmptyView(android.R.id.list, android.R.id.empty);
			appWidgetManager.updateAppWidget(id, rv);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (LIBRARY_SEAT_WIDGET_REFRASH.equals(action)) {
			int[] appWidgetIds = new int[] { intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID) };
			if (appWidgetIds != null && appWidgetIds.length > 0) {
				this.onUpdate(context, AppWidgetManager.getInstance(context),
						appWidgetIds);
			}
		} else
			super.onReceive(context, intent);
	}

	@Override
	protected ArrayList<SeatItem> doInBackGround(Context context,
			AppWidgetManager appWidgetManager, int[] appWidgetIds)
			throws Exception {
		String body = HttpRequest.getBody(TabLibrarySeatFragment.URL,
				StringUtil.ENCODE_EUC_KR);
		@SuppressWarnings("unchecked")
		ArrayList<SeatItem> list = (ArrayList<SeatItem>) ParseFactory.create(
				ParseFactory.What.Seat, body, 0).parse();
		ArrayList<SeatItem> newList = new ArrayList<SeatItem>();

		for (int i : STURDY_ROOM_NUMBER_ARRAY) {
			SeatItem item = list.get(i);
			// if (Double.parseDouble(item.utilizationRate) < 50d)
			newList.add(item);
		}
		IOUtil.saveToFileSuppressed(context, IOUtil.FILE_LIBRARY_SEAT,
				Context.MODE_PRIVATE, newList);
		return newList;
	}

	@Override
	protected void onBackgroundTaskResult(Context context,
			AppWidgetManager appWidgetManager, int[] appWidgetIds,
			ArrayList<SeatItem> result) {
		for (int id : appWidgetIds) {
			RemoteViews rv = new RemoteViews(context.getPackageName(),
					R.layout.widget_library_seat);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList(LIBRARY_SEAT_WIDGET_DATA, result);
			Intent intent = new Intent(context, LibrarySeatListService.class)
					.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
					.putExtra(LIBRARY_SEAT_WIDGET_DATA, bundle);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			rv.setRemoteAdapter(android.R.id.list, intent);
			rv.setEmptyView(android.R.id.list, android.R.id.empty);

			rv.setTextViewText(android.R.id.text1, new SimpleDateFormat(
					"a hh:mm:ss", Locale.KOREA).format(new Date()));
			Intent clickIntent = new Intent(context, LibrarySeatWidget.class);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
					.setAction(LIBRARY_SEAT_WIDGET_REFRASH);
			rv.setOnClickPendingIntent(android.R.id.selectedIcon, PendingIntent
					.getBroadcast(context, 0, clickIntent,
							PendingIntent.FLAG_UPDATE_CURRENT));

			appWidgetManager.updateAppWidget(id, rv);
			appWidgetManager.notifyAppWidgetViewDataChanged(id,
					android.R.id.list);
		}
	}

	@Override
	protected void exceptionOccured(Context context,
			AppWidgetManager appWidgetManager, int[] appWidgetIds, Exception e) {
		super.exceptionOccured(context, appWidgetManager, appWidgetIds, e);
		for (int id : appWidgetIds) {
			RemoteViews rv = new RemoteViews(context.getPackageName(),
					R.layout.widget_library_seat);
			rv.setTextViewText(android.R.id.text1,
					context.getText(R.string.progress_fail));
			Intent clickIntent = new Intent(context, LibrarySeatWidget.class);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
					.setAction(LIBRARY_SEAT_WIDGET_REFRASH);
			rv.setOnClickPendingIntent(android.R.id.selectedIcon, PendingIntent
					.getBroadcast(context, 0, clickIntent,
							PendingIntent.FLAG_UPDATE_CURRENT));
			appWidgetManager.updateAppWidget(id, rv);
		}
	}
}
