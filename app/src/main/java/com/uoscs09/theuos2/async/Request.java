package com.uoscs09.theuos2.async;


import android.os.AsyncTask;
import android.support.annotation.Nullable;

public interface Request<T> {

    T get() throws Exception;

    <V> Request<V> wrap(Processor<T, V> processor);

    AsyncTask<Void, Integer, T> getAsync(ResultListener<T> resultListener, ErrorListener errorListener);

    interface ErrorListener {
        void onError(Exception e);
    }

    interface ResultListener<T> {
        void onResult(T result);
    }

    interface ProgressUpdater {
        void notifyProgress(int progress);
    }

    interface Builder<T> {
        Request<T> build();
    }


    abstract class Base<T> implements Request<T> {
        boolean resultRequestCalled = false;

        @Override
        public final T get() throws Exception {
            if (resultRequestCalled) {
                throw new IllegalStateException("Cannot execute task:"
                        + " the task has already been executed ");
            }

            resultRequestCalled = true;
            return getInner();
        }

        protected abstract T getInner() throws Exception;

        @Override
        public <V> Request<V> wrap(Processor<T, V> processor) {
            return new Wrapper<>(processor, this);
        }

        @Override
        public AsyncTask<Void, Integer, T> getAsync(@Nullable ResultListener<T> resultListener, @Nullable ErrorListener errorListener) {
            return AsyncUtil.execute(
                    new Task<>(this, resultListener)
                            .setErrorListener(errorListener)
            );
        }


        private static class Wrapper<V, T> extends Base<T> {
            private final Processor<V, T> processor;
            private final Request<V> request;

            public Wrapper(Processor<V, T> processor, Request<V> request) {
                this.processor = processor;
                this.request = request;
            }

            @Override
            protected T getInner() throws Exception {
                return processor.process(request.get());
            }

        }

    }

}
