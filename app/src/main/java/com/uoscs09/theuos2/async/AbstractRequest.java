package com.uoscs09.theuos2.async;


import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

public abstract class AbstractRequest<T> implements Request<T> {


    @Override
    public final T process(Void aVoid) throws Exception {
        return get();
    }

    @Override
    public final <V> Request<V> wrap(Processor<T, V> processor) {
        return new Wrapper<>(processor, this);
    }

    @Override
    public AsyncTask<Void, Integer, T> getAsync(@Nullable ResultListener<T> resultListener, @Nullable ErrorListener errorListener) {
        return getAsyncOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, resultListener, errorListener);
    }

    @Override
    public AsyncTask<Void, Integer, T> getAsyncOnExecutor(Executor executor, @Nullable ResultListener<T> resultListener, @Nullable ErrorListener errorListener) {
        return new Task<>(this, resultListener).setErrorListener(errorListener).executeOnExecutor(executor);
    }

    @Override
    public AsyncTask<Void, Integer, T> getAsyncOnExecutor(@Nullable ResultListener<T> resultListener, @Nullable ErrorListener errorListener) {
        return getAsyncOnExecutor(AsyncUtil.sEXECUTOR, resultListener, errorListener);
    }

    private static class Wrapper<V, T> extends AbstractRequest<T> {
        private Processor<V, T> processor;
        private Request<V> request;

        public Wrapper(Processor<V, T> processor, Request<V> request) {
            this.processor = processor;
            this.request = request;
        }

        @Override
        public T get() throws Exception {
            try {
                return processor.process(request.get());
            } finally {
                request = null;
                processor = null;
            }
        }

    }

}
