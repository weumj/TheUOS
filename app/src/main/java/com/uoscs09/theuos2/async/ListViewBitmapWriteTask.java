package com.uoscs09.theuos2.async;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.ImageUtil;

import java.lang.ref.WeakReference;

/**
 * 리스트뷰를 비트맵으로 변환해 파일로 저장하는 작업을 비동기적으로 처리하는 클래스
 */
public class ListViewBitmapWriteTask extends AsyncJob.Base<String> implements DialogInterface.OnClickListener {
    private final WeakReference<Context> contextRef;
    private final Dialog progress;
    private final String fileName;
    private final WeakReference<ListView> listViewRef;
    private final ListAdapter adapter;
    private AsyncTask<Void, Void, String> executor;

    public ListViewBitmapWriteTask(ListView listView, ListAdapter originalAdapter, String fileName, Dialog progress) {
        Context context = listView.getContext();
        this.contextRef = new WeakReference<>(context);
        this.fileName = fileName;
        listViewRef = new WeakReference<>(listView);
        this.adapter = originalAdapter;
        this.progress = progress;
    }

    public ListViewBitmapWriteTask(ListView listView, ListAdapter originalAdapter, String fileName) {
        Context context = listView.getContext();
        this.contextRef = new WeakReference<>(context);
        this.fileName = fileName;
        listViewRef = new WeakReference<>(listView);
        this.adapter = originalAdapter;
        progress = AppUtil.getProgressDialog(context, false, context.getText(R.string.progress_ongoing), this);
    }

    public void execute() {
        progress.show();
        if (executor != null && !executor.isCancelled()) {
            executor.cancel(true);
        }
        executor = AsyncUtil.execute(this);
    }

    public boolean cancel() {
        return AsyncUtil.cancelTask(executor);
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
            ImageUtil.saveImageToFile(fileName, bitmap);
            return fileName;
        } finally {
            if (bitmap != null)
                bitmap.recycle();
        }
    }

    public Bitmap getBitmap() {
        ListView listView = listViewRef.get();
        if (listView == null)
            return null;
        return ImageUtil.getWholeListViewItemsToBitmap(listView, adapter, AppUtil.getAttrColor(listView.getContext(), R.attr.cardBackgroundColor));
    }

    /**
     * progressDialog 의 cancel listener
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        cancel();
    }


    public static class TitleListViewBitmapWriteTask extends ListViewBitmapWriteTask {
        private final WeakReference<View> viewRef;

        public TitleListViewBitmapWriteTask(ListView listView, ListAdapter originalAdapter, String fileName, View title) {
            super(listView, originalAdapter, fileName);
            viewRef = new WeakReference<>(title);
        }

        public TitleListViewBitmapWriteTask(ListView listView, ListAdapter originalAdapter, String fileName, View title, Dialog progress) {
            super(listView, originalAdapter, fileName, progress);
            viewRef = new WeakReference<>(title);
        }

        @Override
        public Bitmap getBitmap() {
            Bitmap capture = null, titleBitmap = null, bitmap, whiteBackgroundTitleBitmap = null;
            View title = null;
            try {
                capture = super.getBitmap();
                title = viewRef.get();

                title.setDrawingCacheEnabled(true);
                title.buildDrawingCache(true);
                titleBitmap = title.getDrawingCache(true);
                if (titleBitmap == null)
                    titleBitmap = ImageUtil.createBitmapFromView(title);

                whiteBackgroundTitleBitmap = ImageUtil.drawOnBackground(titleBitmap, AppUtil.getAttrColor(title.getContext(), R.attr.cardBackgroundColor));
                bitmap = ImageUtil.merge(whiteBackgroundTitleBitmap, capture);

                return bitmap;
            } finally {
                if (capture != null)
                    capture.recycle();
                if (titleBitmap != null)
                    titleBitmap.recycle();
                if (title != null) {
                    title.destroyDrawingCache();
                    title.setDrawingCacheEnabled(false);
                }
                if (whiteBackgroundTitleBitmap != null) {
                    whiteBackgroundTitleBitmap.recycle();
                }
            }
        }


    }
}
