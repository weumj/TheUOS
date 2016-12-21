package com.uoscs09.theuos2.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

import mj.android.utils.task.DelayedTask;
import mj.android.utils.task.ErrorListener;
import mj.android.utils.task.ResultListener;
import mj.android.utils.task.Tasks;
import retrofit2.Call;


class CallDelayedTaskImpl<T> implements DelayedTask<T> {
    private Call<T> call;
    private ResultListener<T> resultListener;
    private ErrorListener errorListener;
    private Runnable atLastListener;
    private CallTaskImpl.ResultCallback<T> resultCallback;

    CallDelayedTaskImpl(Call<T> call) {
        this.call = call;
    }

    @Override
    public DelayedTask<T> result(@Nullable ResultListener<T> resultListener) {
        this.resultListener = resultListener;
        return this;
    }

    @Override
    public DelayedTask<T> error(@Nullable ErrorListener errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    @Override
    public DelayedTask<T> atLast(@Nullable Runnable runnable) {
        this.atLastListener = runnable;
        return this;
    }

    @Override
    public DelayedTask<T> clone() {
        try {
            Object o = super.clone();
            if (o instanceof CallDelayedTaskImpl) {
                //noinspection unchecked
                CallDelayedTaskImpl<T> callTask = (CallDelayedTaskImpl<T>) o;
                callTask.call = this.call.clone();
                return callTask;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CallDelayedTaskImpl<>(this.call.clone());
    }

    @Override
    public void execute() {
        call.enqueue(resultCallback = new CallTaskImpl.ResultCallback<>(resultListener, errorListener, atLastListener));
    }

    @Override
    public void execute(@NonNull Executor executor) {
        Tasks.newTask(new CallTaskImpl.CallCallable<>(call))
                .delayed()
                .result(resultListener)
                .error(errorListener)
                .atLast(atLastListener)
                .execute(executor);
    }

    @Override
    public boolean cancel() {
        call.cancel();
        Tasks.sendCancelToCancelable(resultCallback);
        return false;
    }
}
