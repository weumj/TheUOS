package com.uoscs09.theuos2.async;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

/**
 * {@link AsyncTask#THREAD_POOL_EXECUTOR}을 이용하여 비동기 작업을 처리하는 클래스
 */
public class AsyncUtil {

    private AsyncUtil() {
    }

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 10;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "AsyncUtil #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>(128);
    private static final Executor sEXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);


    public static boolean isTaskRunning(@Nullable AsyncTask<?, ?, ?> task) {
        return task != null && task.getStatus() == AsyncTask.Status.RUNNING;
    }

    public static boolean isTaskCanceled(@Nullable AsyncTask<?, ?, ?> task) {
        return task == null || task.isCancelled();
    }

    public static boolean cancelTask(@Nullable AsyncTask<?, ?, ?> task) {
        return task == null || task.cancel(true);

    }

    /**
     * 비동기 작업을 실행한다.
     *
     * @param task 비동기 작업이 실시될 {@link Callable}
     * @param l    작업 종료후 호출될 callback
     */
    public static <Data> AsyncTask<Void, ?, Data> execute(@NonNull Callable<Data> task, OnTaskFinishedListener<Data> l) {
        return executeAsyncTask(getExecutor(task, l));
    }

    public static <Progress, V> AsyncTask<Void, Progress, V> execute(@NonNull AsyncTask<Void, Progress, V> task) {
        return executeAsyncTask(task);
    }

    /**
     * 비동기 작업을 실행한다.
     *
     * @param task     비동기 작업이 실시될 {@link Callable}
     * @param callback 작업 종료후 호출될 callback
     */
    public static <Data> AsyncTask<Void, ?, Data> execute(Callable<Data> task, AsyncCallback<Data> callback) {
        return executeAsyncTask(new AsyncExecutor<Data>().setCallable(task).setCallback(callback));
    }

    public static <Data> AsyncTask<Void, ?, Data> execute(AsyncJob<Data> job) {
        return executeAsyncTask(new AsyncExecutor<Data>().setCallable(job).setCallback(job));
    }

    private static <Progress, Data> AsyncTask<Void, Progress, Data> executeAsyncTask(AsyncTask<Void, Progress, Data> task) {
        return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 결과를 반환하지 않는 비동기 작업을 실행한다.
     *
     * @param r 비동기 작업이 실시될 {@link Runnable}
     */
    public static void execute(@NonNull Runnable r) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(r);
    }

    /**
     * 결과를 반환하지 않는 비동기 작업을 실행한다. <br>
     * <br>
     * <b>수행하는 작업은 {@link AsyncTask#THREAD_POOL_EXECUTOR}이 아닌 다른
     * {@link Executor}에서 처리된다.</b>
     *
     * @param r 비동기 작업이 실시될 {@link Runnable}
     */
    public static void executeFor(@NonNull Runnable r) {
        sEXECUTOR.execute(r);
    }

    private static <Data> AsyncTask<Void, ?, Data> getExecutor(@NonNull Callable<Data> task, final OnTaskFinishedListener<Data> l) {
        return new AsyncExecutor<Data>().setCallable(task).setCallback(
                new AsyncCallback.Base<Data>() {
                    public void onResult(Data result) {
                        if (l != null)
                            l.onTaskFinished(false, result, null);
                    }

                    @Override
                    public void exceptionOccured(Exception e) {
                        if (l != null)
                            l.onTaskFinished(true, null, e);
                    }
                }
        );
    }

    /**
     * 비 동기 작업 후 호출될 listener
     */
    public interface OnTaskFinishedListener<T> {
        /**
         * 비 동기 작업 후 호출되는 메소드
         *
         * @param isExceptionOccurred Exception 발생 여부
         * @param e                   Exception이 발생한 경우 : {@link Exception}객체, 아니면 null
         * @param data                Exception이 발생하지 않은 경우 : 작업한 결과 , 아니면 null
         */
        void onTaskFinished(boolean isExceptionOccurred, T data, Exception e);
    }

}
