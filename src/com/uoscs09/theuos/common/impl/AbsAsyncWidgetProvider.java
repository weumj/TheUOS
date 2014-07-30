package com.uoscs09.theuos.common.impl;

import java.util.concurrent.Callable;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import com.javacan.asyncexcute.AsyncCallback;
import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.common.util.AppUtil;

/** 비 동기 작업이 필요한 AppWidget을 위한 Abstract Class */
public abstract class AbsAsyncWidgetProvider<Data> extends AppWidgetProvider {

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		new AsyncLoader<Data>().excute(new Callable<Data>() {
			@Override
			public Data call() throws Exception {
				return doInBackGround(context, appWidgetManager, appWidgetIds);
			}
		}, new AsyncCallback.Base<Data>() {
			@Override
			public void onResult(Data result) {
				AbsAsyncWidgetProvider.this.onBackgroundTaskResult(context, appWidgetManager,
						appWidgetIds, result);
			}

			@Override
			public void exceptionOccured(Exception e) {
				AbsAsyncWidgetProvider.this.exceptionOccured(context,
						appWidgetManager, appWidgetIds, e);
			}
		});
	}

	protected abstract Data doInBackGround(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
			throws Exception;

	protected abstract void onBackgroundTaskResult(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds,
			Data result);

	protected void exceptionOccured(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds,
			Exception e) {
		AppUtil.showErrorToast(context, e, true);
	}
}
