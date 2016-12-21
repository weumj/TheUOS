package com.uoscs09.theuos2.api;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Executor;

import mj.android.utils.task.Callable2;
import mj.android.utils.task.Cancelable;
import mj.android.utils.task.DelayedTask;
import mj.android.utils.task.ErrorListener;
import mj.android.utils.task.Func;
import mj.android.utils.task.ResultListener;
import mj.android.utils.task.Task;
import mj.android.utils.task.Tasks;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class CallTaskImpl<T> implements Task<T> {

    private static final Executor CALLBACK_EXECUTOR = Tasks.UI_THREAD_EXECUTOR;
    private Call<T> call;

    CallTaskImpl(Call<T> call) {
        this.call = call;
    }

    @Override
    public T get() throws Throwable {
        try {
            return call.execute().body();
        } finally {
            call = null;
        }
    }

    @Override
    public DelayedTask<T> delayed() {
        return new CallDelayedTaskImpl<>(call);
    }

    @Override
    public <V> Task<V> map(Func<T, V> func) {
        return Tasks.newTask(new CallFuncCallable<>(new CallCallable<>(call), func));
    }

    @Override
    public CallTaskImpl<T> clone() {
        try {
            Object o = super.clone();
            if (o instanceof CallTaskImpl) {
                //noinspection unchecked
                CallTaskImpl<T> callTask = (CallTaskImpl<T>) o;
                callTask.call = this.call.clone();
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

    static class CallCallable<T> implements Callable2<T>, Cancelable {
        private Call<T> call;
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


    static class ResultCallback<T> implements Callback<T>, Cancelable {
        private ResultListener<T> r;
        private ErrorListener e;
        private Runnable last;
        private boolean isCanceled = false;

        ResultCallback(ResultListener<T> r, ErrorListener e, Runnable last) {
            this.r = r;
            this.e = e;
            this.last = last;
        }

        @Override
        public void onResponse(retrofit2.Call<T> call, Response<T> response) {
            try {
                if (isCanceled) return;
                deliverResult(handleResponse(response), r);
            } catch (Throwable throwable) {
                if (isCanceled)
                    throwable.printStackTrace();
                else
                    deliverError(throwable, e);
            } finally {
                deliverAtLast(last);
                clearRef();
            }
        }


        @Override
        public void onFailure(Call<T> call, Throwable t) {
            try {
                if (isCanceled) return;
                deliverError(t, e);
            } finally {
                deliverAtLast(last);
                clearRef();
            }
        }

        private void clearRef() {
            r = null;
            e = null;
            last = null;
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

        private static void deliverAtLast(Runnable r) {
            if (r != null) {
                CALLBACK_EXECUTOR.execute(r);
            } else {
                Log.w("TASK", "AtLast Runnable == null");
            }
        }

    }

}
