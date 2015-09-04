package com.uoscs09.theuos2.async;


import android.os.AsyncTask;

import java.util.concurrent.Executor;

public interface Request<T> extends Processor<Void, T> {

    T get() throws Exception;

    <V> Request<V> wrap(Processor<T, V> processor);

    AsyncTask<Void, Integer, T> getAsync(ResultListener<T> resultListener, ErrorListener errorListener);

    AsyncTask<Void, Integer, T> getAsyncOnExecutor(Executor executor, ResultListener<T> resultListener, ErrorListener errorListener);

    AsyncTask<Void, Integer, T> getAsyncOnExecutor(ResultListener<T> resultListener, ErrorListener errorListener);

    interface ErrorListener {
        void onError(Exception e);
    }

    interface ResultListener<T> {
        void onResult(T result);
    }

    /*
    interface ProgressUpdater {
        void notifyProgress(int progress);
    }
    */

    interface Builder<T> {
        Request<T> build();
    }

}
