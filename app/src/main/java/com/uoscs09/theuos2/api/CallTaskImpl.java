package com.uoscs09.theuos2.api;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Executor;

import mj.android.utils.task.Callable2;
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

    private Call<T> call;

    CallTaskImpl(Call<T> call) {
        this.call = call;
    }

    @Override
    public T get() throws Throwable {
        return handleResponse(call.execute());
    }

    @Override
    public Task<T> getAsync(ResultListener<T> r, ErrorListener e) {
        call.enqueue(new ResultCallback<T>(r, e));
        return this;
    }


    @Override
    public Task<T> getAsync(ResultListener<T> resultListener, ErrorListener errorListener, Executor executor) {
        Tasks.newTask(new CallCallable<>(call)).getAsync(resultListener, errorListener, executor);
        //Tasks.newTask(this::get).getAsync(resultListener, errorListener, executor);
        return this;
    }

    @Override
    public <V> Task<V> map(Func<T, V> func) {
        return Tasks.newTask(new CallFuncCallable<T, V>(new CallCallable<T>(call), func));
        //return Tasks.newTask(() -> func.func(get()));
    }

    @Override
    public boolean cancel() {
        call.cancel();
        return true;
    }

    @Override
    public CallTaskImpl<T> clone() {
        try {
            Object o = super.clone();
            if (o instanceof CallTaskImpl) {
                //noinspection unchecked
                CallTaskImpl<T> callTask = (CallTaskImpl<T>) o;
                callTask.call = call.clone();
                return callTask;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CallTaskImpl<>(call.clone());
    }


    private static <T> T handleResponse(Response<T> response) throws Throwable {
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw new IOException(response.errorBody().string());
        }
    }

    private static class CallFuncCallable<T, V> implements Callable2<V> {
        private CallCallable<T> callCallable;
        private Func<T, V> func;

        CallFuncCallable(CallCallable<T> callCallable, Func<T, V> func) {
            this.callCallable = callCallable;
            this.func = func;
        }

        @Override
        public V call() throws Throwable {
            try {
                return func.func(callCallable.call());
            } finally {
                callCallable = null;
                func = null;
            }
        }
    }

    private static class CallCallable<T> implements Callable2<T> {
        private retrofit2.Call<T> call;

        CallCallable(Call<T> call) {
            this.call = call;
        }

        @Override
        public T call() throws Throwable {
            try {
                return handleResponse(call.execute());
            } finally {
                call = null;
            }
        }
    }


    private static class ResultCallback<T> implements Callback<T> {
        private ResultListener<T> r;
        private ErrorListener e;

        ResultCallback(ResultListener<T> r, ErrorListener e) {
            this.r = r;
            this.e = e;
        }

        @Override
        public void onResponse(retrofit2.Call<T> call, Response<T> response) {
            try {
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
                deliverError(t, e);
            } finally {
                r = null;
                e = null;
            }
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
