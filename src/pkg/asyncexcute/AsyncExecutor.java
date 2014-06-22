package pkg.asyncexcute;
import java.util.concurrent.Callable;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncExecutor<T> extends AsyncTask<Void, Void, T> {
	private static final String TAG = "AsyncExecutor";
	private AsyncCallback<T> callback;
	private Callable<T> callable;
	private Exception occuredException;
	
	public AsyncExecutor<T> setCallable(Callable<T> callable) {
		this.callable = callable;
		return this;
	}

	public AsyncExecutor<T> setCallback(AsyncCallback<T> callback) {
		this.callback = callback;
		processAsyncExecutorAware(callback);
		return this;
	}

	@SuppressWarnings("unchecked")
	private void processAsyncExecutorAware(AsyncCallback<T> callback) {
		if (callback instanceof AsyncExecutorAware) {
			((AsyncExecutorAware<T>) callback).setAsyncExecutor(this);
		}
	}

	@Override
	protected T doInBackground(Void... params) {
		try {
			return callable.call();
		} catch (Exception ex) {
			Log.e(TAG,
			"exception occured while doing in background: "
			+ ex.getMessage(), ex);
			this.occuredException = ex;
			return null;
		}
	}

	@Override
	protected void onPostExecute(T result) {
		callback.onPostExcute();
		if (isCancelled()) {
			notifyCanceled();
		}
		if (isExceptionOccured()) {
			notifyException();
			return;
		}
		notifyResult(result);
	}
	private void notifyCanceled() {
		if (callback != null)
			callback.cancelled();
	}
	private boolean isExceptionOccured() {
		return occuredException != null;
	}
	private void notifyException() {
		if (callback != null)
			callback.exceptionOccured(occuredException);
	}
	private void notifyResult(T result) {
		if (callback != null)
			callback.onResult(result);
	}
}
