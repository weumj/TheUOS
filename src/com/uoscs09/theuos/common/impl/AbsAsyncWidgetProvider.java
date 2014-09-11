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
				AbsAsyncWidgetProvider.this.onBackgroundTaskResult(context,
						appWidgetManager, appWidgetIds, result);
			}

			@Override
			public void exceptionOccured(Exception e) {
				AbsAsyncWidgetProvider.this.exceptionOccured(context,
						appWidgetManager, appWidgetIds, e);
			}
		});
	}

	/**
	 * 다른 Thread에서 작업을 할 때 호출된다.
	 * 
	 * @return 작업한 결과
	 */
	protected abstract Data doInBackGround(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
			throws Exception;

	/**
	 * 다른 Thread에서의 작업이 성공적으로 끝나고, 메인 Thread에서 호출된다.
	 * 
	 * @param result
	 *            - {@link #doInBackGround(Context, AppWidgetManager, int[])}에서
	 *            반환한 결과
	 */
	protected abstract void onBackgroundTaskResult(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds,
			Data result);

	/**
	 * 다른 Thread에서의 작업 도중, 처리되지 않은 Exception이 발생하였을 때 호출된다.
	 * 
	 * @param e
	 *            발생한 Exception
	 */
	protected void exceptionOccured(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds,
			Exception e) {
		AppUtil.showErrorToast(context, e, true);
	}
}
