package com.uoscs09.theuos2.async;


import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.CancellationException;

class Task<T> extends AsyncTask<Void, Integer, T> {
    private static final String TAG = "Task";
    private Request<T> request;
    private Exception mOccurredException;
    private Request.ResultListener<T> resultListener;
    private Request.ErrorListener errorListener;

    public Task(@NonNull Request<T> request, @Nullable Request.ResultListener<T> resultListener) {
        this.request = request;
        this.resultListener = resultListener;
    }

    public Task<T> setErrorListener(@Nullable Request.ErrorListener errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    @Override
    protected T doInBackground(Void... params) {
        try {
            return request.get();
        } catch (Exception e) {
            logException(e);
            setException(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(T result) {

        if (isCancelled())
            notifyCanceled();

        if (isExceptionOccurred())
            notifyException();
        else
            notifyResult(result);

    }

    protected void setException(Exception e) {
        mOccurredException = e;
    }

    private void notifyCanceled() {
        setException(new CancellationException("canceled."));
    }

    private boolean isExceptionOccurred() {
        return this.mOccurredException != null;
    }

    private void notifyException() {
        if (errorListener != null)
            errorListener.onError(mOccurredException);
    }

    private void notifyResult(T result) {
        if (resultListener != null)
            resultListener.onResult(result);
    }

    protected void logException(Exception e) {
        Log.e(TAG, "exception occurred while doing T background: " + e.getMessage(), e);
    }

}
