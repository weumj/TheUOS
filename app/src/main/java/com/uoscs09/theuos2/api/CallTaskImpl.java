package com.uoscs09.theuos2.api;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Executor;

import mj.android.utils.task.ErrorListener;
import mj.android.utils.task.Func;
import mj.android.utils.task.ResultListener;
import mj.android.utils.task.Task;
import mj.android.utils.task.Tasks;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class CallTaskImpl<T> implements Task<T> {
    final Call<T> call;
    private static final Executor CALLBACK_EXECUTOR = new Executor() {
        final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            handler.post(command);
        }
    };

    public CallTaskImpl(Call<T> call) {
        this.call = call;
    }

    private T handleResponse(Response<T> response) throws Throwable {

        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw new IOException(response.errorBody().string());
        }
    }

    @Override
    public T get() throws Throwable {
        return handleResponse(call.execute());
    }

    @Override
    public Task<T> getAsync(ResultListener<T> r, ErrorListener e) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                try {
                    deliverResult(handleResponse(response));
                } catch (Throwable throwable) {
                    deliverError(throwable);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                deliverError(t);
            }

            private void deliverResult(T t) {
                if (r != null) {
                    CALLBACK_EXECUTOR.execute(() -> r.onResult(t));
                } else {
                    Log.w("TASK", "ResultListener == null, result : " + t);
                }
            }

            private void deliverError(Throwable t) {
                if (e != null) {
                    CALLBACK_EXECUTOR.execute(() -> e.onError(t));
                } else {
                    Log.w("TASK", "ErrorListener == null, error : " + t);
                }
            }

        });
        return this;
    }


    @Override
    public Task<T> getAsync(ResultListener<T> resultListener, ErrorListener errorListener, Executor executor) {
        Tasks.newTask(this::get).getAsync(resultListener, errorListener, executor);
        return this;
    }

    @Override
    public <V> Task<V> wrap(Func<T, V> func) {
        return Tasks.newTask(() -> func.func(get()));
    }

    @Override
    public boolean cancel() {
        call.cancel();
        return true;
    }

    @Override
    public Task<T> clone() {
        return new CallTaskImpl<>(call.clone());
    }
}
