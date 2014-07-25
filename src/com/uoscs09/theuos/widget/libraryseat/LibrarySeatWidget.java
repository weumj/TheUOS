package com.uoscs09.theuos.widget.libraryseat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
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
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;
import com.uoscs09.theuos.tab.libraryseat.SeatItem;
import com.uoscs09.theuos.tab.libraryseat.TabLibrarySeatFragment;

public class LibrarySeatWidget extends AppWidgetProvider {
	public static final String LIBRARY_SEAT_WIDGET_REFRASH = "com.uoscs09.theuos.widget.libraryseat.REFRESH";
	public static final String LIBRARY_SEAT_WIDGET_DATA = "com.uoscs09.theuos.widget.libraryseat.DATA";

	private Callable<ArrayList<SeatItem>> mCallable = new Callable<ArrayList<SeatItem>>() {

		@SuppressWarnings("unchecked")
		@Override
		public ArrayList<SeatItem> call() throws Exception {
			String body = HttpRequest.getBody(TabLibrarySeatFragment.URL,
					StringUtil.ENCODE_EUC_KR);
			ArrayList<SeatItem> list = (ArrayList<SeatItem>) ParseFactory
					.create(ParseFactory.What.Seat, body, 0).parse();
			ArrayList<SeatItem> newList = new ArrayList<SeatItem>();
			final int[] filterArr = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
					23, 24, 25, 26, 27, 28 };
			for (int i : filterArr) {
				newList.add(list.get(i));
			}
			return newList;
		}
	};

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		new AsyncLoader<ArrayList<SeatItem>>().excute(mCallable,
				new AsyncLoader.OnTaskFinishedListener() {
					@SuppressWarnings("unchecked")
					@Override
					public void onTaskFinished(boolean isExceptionOccoured,
							Object data) {
						if (isExceptionOccoured) {
							AppUtil.showInternetConnectionErrorToast(context,
									true);
						} else {
							ArrayList<SeatItem> list = (ArrayList<SeatItem>) data;
							RemoteViews rv = new RemoteViews(context
									.getPackageName(),
									R.layout.widget_library_seat);
							for (int id : appWidgetIds) {
								Bundle bundle = new Bundle();
								bundle.putParcelableArrayList(
										LIBRARY_SEAT_WIDGET_DATA, list);
								Intent intent = new Intent(context,
										LibrarySeatListService.class)
										.putExtra(
												AppWidgetManager.EXTRA_APPWIDGET_ID,
												id).putExtra(
												LIBRARY_SEAT_WIDGET_DATA,
												bundle);
								intent.setData(Uri.parse(intent
										.toUri(Intent.URI_INTENT_SCHEME)));
								rv.setRemoteAdapter(android.R.id.list, intent);

								rv.setTextViewText(
										android.R.id.text1,
										new SimpleDateFormat("a hh:mm:ss",
												Locale.KOREA)
												.format(new Date())
												+ " 현재");
								Intent clickIntent = new Intent(context,
										LibrarySeatWidget.class);
								clickIntent
										.putExtra(
												AppWidgetManager.EXTRA_APPWIDGET_ID,
												id).setAction(
												LIBRARY_SEAT_WIDGET_REFRASH);
								rv.setOnClickPendingIntent(
										android.R.id.selectedIcon,
										PendingIntent
												.getBroadcast(
														context,
														0,
														clickIntent,
														PendingIntent.FLAG_UPDATE_CURRENT));
								rv.setImageViewResource(
										android.R.id.selectedIcon,
										R.drawable.ic_action_navigation_refresh);
								appWidgetManager.updateAppWidget(id, rv);
								appWidgetManager
										.notifyAppWidgetViewDataChanged(id,
												android.R.id.list);
							}
						}
					}
				});

		RemoteViews rv = new RemoteViews(context.getPackageName(),
				R.layout.widget_library_seat);
		for (int id : appWidgetIds) {
			rv.setTextViewText(android.R.id.text1,
					context.getText(R.string.progress_while_loading));
			rv.setOnClickPendingIntent(android.R.id.selectedIcon, null);
			appWidgetManager.updateAppWidget(id, rv);
			rv.setImageViewResource(android.R.id.selectedIcon,
					R.anim.loading_animation);
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
		}
		super.onReceive(context, intent);
	}
}
