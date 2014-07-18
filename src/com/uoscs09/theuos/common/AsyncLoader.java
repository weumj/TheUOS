package com.uoscs09.theuos.common;

import java.util.concurrent.Callable;

import com.javacan.asyncexcute.AsyncCallback;
import com.javacan.asyncexcute.AsyncExecutor;

import android.os.AsyncTask;

public class AsyncLoader<Data> {
	/**
	 * 비동기 작업을 실행한다.
	 * 
	 * @param task
	 *            비동기 작업이 실시될 {@link Callable}
	 * @param l
	 *            작업 종료후 호출될 callback
	 */
	public void excute(Callable<Data> task, OnTaskFinishedListener l) {
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
	public void excute(Callable<Data> task, AsyncCallback<Data> callback) {
		new AsyncExecutor<Data>().setCallable(task).setCallback(callback)
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * 결과를 반환하지 않는 비동기 작업을 실행한다.
	 * 
	 * @param r
	 *            비동기 작업이 실시될 {@link Runnable}
	 */
	public static void excute(Runnable r) {
		AsyncTask.THREAD_POOL_EXECUTOR.execute(r);
	}

	private AsyncTask<Void, Void, Data> getTasker(Callable<Data> task,
			final OnTaskFinishedListener l) {
		return new AsyncExecutor<Data>().setCallable(task).setCallback(
				new AsyncCallback.Base<Data>() {
					public void onResult(Data result) {
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
