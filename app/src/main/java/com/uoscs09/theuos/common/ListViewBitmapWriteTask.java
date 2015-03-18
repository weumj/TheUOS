package com.uoscs09.theuos.common;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ListView;

import com.javacan.asyncexcute.AsyncCallback;
import com.javacan.asyncexcute.AsyncExecutor;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.GraphicUtil;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

/** 리스트뷰를 비트맵으로 변환해 파일로 저장하는 작업을 비동기적으로 처리하는 클래스 */
public class ListViewBitmapWriteTask implements Callable<String>,
		AsyncCallback<String>, DialogInterface.OnClickListener {
	private WeakReference<Context> contextRef;
	private Dialog progress;
	private String fileName;
	private WeakReference<ListView> listViewRef;
	private AsyncExecutor<String> executor;

	public ListViewBitmapWriteTask( String fileName,ListView listView) {
        Context context = listView.getContext();
		this.contextRef = new WeakReference<>(context);
		progress = AppUtil.getProgressDialog(context, false,context.getText(R.string.progress_ongoing), this);
		this.fileName = fileName;
		listViewRef = new WeakReference<>(listView);
	}

	public void execute() {
		progress.show();
		if (executor != null && !executor.isCancelled()) {
			executor.cancel(true);
		}
		executor = new AsyncExecutor<>();
		executor.setCallable(this)
                .setCallback(this)
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public boolean cancel() {
        return executor != null && executor.cancel(true);
	}

	@Override
	public void onResult(String result) {
		Context context = contextRef.get();
		AppUtil.showToast(context, result + "\n " + context.getText(R.string.saved), true);
	}

	@Override
	public void exceptionOccured(Exception e) {
		AppUtil.showErrorToast(contextRef.get(), e, true);
	}

	@Override
	public void cancelled() {
		AppUtil.showCanceledToast(contextRef.get(), true);
	}

	@Override
	public void onPostExcute() {
		progress.dismiss();
	}

	@Override
	final public String call() throws Exception {
		Bitmap bitmap = null;
		try {
			bitmap = getBitmap();
			GraphicUtil.saveImageToFile(fileName, bitmap);
			return fileName;
		} finally {
			if (bitmap != null)
				bitmap.recycle();
		}
	}

	public Bitmap getBitmap() {
		ListView listView = listViewRef.get();
		if (listViewRef == null)
			return null;
        return GraphicUtil.getWholeListViewItemsToBitmap(listView);
	}

	/** progressDialog의 cancel listener */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		cancel();
	}

}
