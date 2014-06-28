package com.uoscs09.theuos.common.impl;

import java.util.concurrent.Callable;

import android.os.AsyncTask;
import pkg.asyncexcute.AsyncCallback;
import pkg.asyncexcute.AsyncExecutor;

public class AsyncLoader<T> {

	/**
	 * 비동기 작업을 실행한다.
	 * 
	 * @param task
	 *            비동기 작업이 실시될 {@link Callable}
	 * @param l
	 *            작업 종료후 호출될 callback
	 */
	public void excute(Callable<T> task, OnTaskFinishedListener l) {
		getTasker(task, l).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * 비동기 작업을 실행한다.
	 * 
	 * @param task
	 *            비동기 작업이 실시될 {@link Callable}
	 * @param callback
	 *            작업 종료후 호출될 callback
	 */
	public void excute(Callable<T> task, AsyncCallback<T> callback) {
		new AsyncExecutor<T>().setCallable(task).setCallback(callback)
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * 결과를 반환하지 않는 비동기 작업을 실행한다.
	 * 
	 * @param task
	 *            비동기 작업이 실시될 {@link Callable}
	 */
	public void excute(Callable<T> task) {
		new AsyncExecutor<T>().setCallable(task).executeOnExecutor(
				AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private AsyncTask<Void, Void, T> getTasker(Callable<T> task,
			final OnTaskFinishedListener l) {
		return new AsyncExecutor<T>().setCallable(task).setCallback(
				new AsyncCallback.Base<T>() {
					public void onResult(T result) {
						if (l != null)
							l.onTaskFinished(false, result);
					}

					@Override
					public void exceptionOccured(Exception e) {
						if (l != null)
							l.onTaskFinished(true, e);
					}
				});
	}

	/** 비 동기 작업 후 호출될 listener */
	public interface OnTaskFinishedListener {
		/**
		 * 비 동기 작업 후 호출되는 메소드
		 * 
		 * @param isExceptionOccoured
		 *            Exception 발생 여부
		 * @param data
		 *            <li>Exception이 발생한 경우 : {@link Exception}객체</li> <li>
		 *            Exception이 발생하지 않은 경우 : {@link Callable} 에서 반환된 결과</li>
		 */
		public void onTaskFinished(boolean isExceptionOccoured, Object data);
	}

}
