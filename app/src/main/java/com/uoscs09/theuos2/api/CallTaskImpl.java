package com.uoscs09.theuos2.api;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Executor;

import mj.android.utils.task.Callable2;
import mj.android.utils.task.Cancelable;
import mj.android.utils.task.ErrorListener;
import mj.android.utils.task.Func;
import mj.android.utils.task.ResultListener;
import mj.android.utils.task.Task;
import mj.android.utils.task.Tasks;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class CallTaskImpl<T> implements Task<T>, Cloneable {

    private static final Executor CALLBACK_EXECUTOR = new Executor() {
        final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            handler.post(command);
        }
    };

    private CallCallable<T> callCallable;
    private ResultCallback<T> resultCallback;

    CallTaskImpl(Call<T> call) {
        this.callCallable = new CallCallable<T>(call);
    }

    @Override
    public T get() throws Throwable {
        try {
            return callCallable.call();
        } finally {
            callCallable = null;
        }
    }

    @Override
    public Task<T> getAsync(ResultListener<T> r, ErrorListener e) {
        callCallable.call.enqueue(resultCallback = new ResultCallback<T>(r, e));
        return this;
    }


    @Override
    public Task<T> getAsync(ResultListener<T> resultListener, ErrorListener errorListener, Executor executor) {
        Tasks.newTask(new CallCallable<>(callCallable.call)).getAsync(resultListener, errorListener, executor);
        //Tasks.newTask(this::get).getAsync(resultListener, errorListener, executor);
        return this;
    }

    @Override
    public <V> Task<V> map(Func<T, V> func) {
        return Tasks.newTask(new CallFuncCallable<T, V>(new CallCallable<T>(callCallable.call), func));
        //return Tasks.newTask(() -> func.func(get()));
    }

    @Override
    public boolean cancel() {
        callCallable.cancel();
        if (resultCallback != null)
            resultCallback.cancel();

        return true;
    }

    @Override
    public CallTaskImpl<T> clone() {
        try {
            Object o = super.clone();
            if (o instanceof CallTaskImpl) {
                //noinspection unchecked
                CallTaskImpl<T> callTask = (CallTaskImpl<T>) o;
                callTask.callCallable = new CallCallable<T>(callCallable.call.clone());
                return callTask;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CallTaskImpl<>(callCallable.call.clone());
    }


    private static <T> T handleResponse(Response<T> response) throws Throwable {
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw new IOException(response.errorBody().string());
        }
    }

    private static class CallFuncCallable<T, V> implements Callable2<V>, Cancelable {
        private CallCallable<T> callCallable;
        private Func<T, V> func;
        private boolean isCanceled = false;

        CallFuncCallable(CallCallable<T> callCallable, Func<T, V> func) {
            this.callCallable = callCallable;
            this.func = func;
        }

        @Override
        public V call() throws Throwable {
            try {
                if (isCanceled) {
                    callCallable.cancel();
                    Tasks.sendCancelToCancelable(func);
                    return null;
                }

                T t = callCallable.call();
                if (isCanceled) {
                    Tasks.sendCancelToCancelable(func);
                    return null;
                }

                V v = func.func(t);
                if (isCanceled) {
                    return null;
                }
                return v;
            } finally {
                callCallable = null;
                func = null;
            }
        }

        @Override
        public boolean cancel() {
            isCanceled = true;
            Tasks.sendCancelToCancelable(callCallable);
            Tasks.sendCancelToCancelable(func);
            return true;
        }
    }

    private static class CallCallable<T> implements Callable2<T>, Cancelable {
        private retrofit2.Call<T> call;
        private boolean isCanceled = false;

        CallCallable(Call<T> call) {
            this.call = call;
        }

        @Override
        public T call() throws Throwable {
            try {
                if (isCanceled) {
                    call.cancel();
                    return null;
                }

                Response<T> executed = call.execute();
                if (isCanceled) {
                    return null;
                }

                return handleResponse(executed);
            } finally {
                call = null;
            }
        }

        @Override
        public boolean cancel() {
            isCanceled = true;
            call.cancel();
            return true;
        }
    }


    private static class ResultCallback<T> implements Callback<T>, Cancelable {
        private ResultListener<T> r;
        private ErrorListener e;
        private boolean isCanceled = false;

        ResultCallback(ResultListener<T> r, ErrorListener e) {
            this.r = r;
            this.e = e;
        }

        @Override
        public void onResponse(retrofit2.Call<T> call, Response<T> response) {
            try {
                if (isCanceled)
                    return;

                deliverResult(handleResponse(response), r);
            } catch (Throwable throwable) {
                deliverError(throwable, e);
            } finally {
                r = null;
                e = null;
            }
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            try {
                if (isCanceled)
                    return;

                deliverError(t, e);
            } finally {
                r = null;
                e = null;
            }
        }

        @Override
        public boolean cancel() {
            isCanceled = true;
            return false;
        }

        private static <T> void deliverResult(T t, ResultListener<T> r) {
            if (r != null) {
                CALLBACK_EXECUTOR.execute(() -> r.onResult(t));
            } else {
                Log.w("TASK", "ResultListener == null, result : " + t);
            }
        }

        private static void deliverError(Throwable t, ErrorListener e) {
            if (e != null) {
                CALLBACK_EXECUTOR.execute(() -> e.onError(t));
            } else {
                Log.w("TASK", "ErrorListener == null, error : " + t);

            }
        }

    }

}
