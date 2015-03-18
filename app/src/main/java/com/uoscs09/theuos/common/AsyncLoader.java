package com.uoscs09.theuos.common;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.javacan.asyncexcute.AsyncCallback;
import com.javacan.asyncexcute.AsyncExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** {@link AsyncTask#THREAD_POOL_EXECUTOR}을 이용하여 비동기 작업을 처리하는 클래스 */
public class AsyncLoader {

	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
	private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
	private static final int KEEP_ALIVE = 10;

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(@NonNull Runnable r) {
			return new Thread(r, "AsyncLoader #" + mCount.getAndIncrement());
		}
	};

	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>(128);
	private static final Executor sEXECUTOR = new ThreadPoolExecutor(
			CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
			sPoolWorkQueue, sThreadFactory);

	/**
	 * 비동기 작업을 실행한다.
	 * 
	 * @param task
	 *            비동기 작업이 실시될 {@link Callable}
	 * @param l
	 *            작업 종료후 호출될 callback
	 */
	public static <Data> AsyncTask<Void, Void, Data> excute(
			Callable<Data> task, OnTaskFinishedListener l) {
		return getTasker(task, l).executeOnExecutor(
				AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * 비동기 작업을 실행한다.
	 * 
	 * @param task
	 *            비동기 작업이 실시될 {@link Callable}
	 * @param callback
	 *            작업 종료후 호출될 callback
	 */
	public static <Data> AsyncTask<Void, Void, Data> excute(
			Callable<Data> task, AsyncCallback<Data> callback) {
		return new AsyncExecutor<Data>().setCallable(task)
				.setCallback(callback)
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

	/**
	 * 결과를 반환하지 않는 비동기 작업을 실행한다. <br>
	 * <br>
	 * <b>수행하는 작업은 {@link AsyncTask#THREAD_POOL_EXECUTOR}이 아닌 다른
	 * {@link Executor}에서 처리된다.</b>
	 * 
	 * @param r
	 *            비동기 작업이 실시될 {@link Runnable}
	 */
	public static void excuteFor(Runnable r) {
		sEXECUTOR.execute(r);
	}

	private static <Data> AsyncTask<Void, Void, Data> getTasker(
			Callable<Data> task, final OnTaskFinishedListener l) {
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
	public static interface OnTaskFinishedListener {
		/**
		 * 비 동기 작업 후 호출되는 메소드
		 * 
		 * @param isExceptionOccurred
		 *            Exception 발생 여부
		 * @param data
		 *            <li>Exception이 발생한 경우 : {@link Exception}객체</li> <li>
		 *            Exception이 발생하지 않은 경우 : {@link Callable} 에서 반환된 결과</li>
		 */
		public void onTaskFinished(boolean isExceptionOccurred, Object data);
	}

}
