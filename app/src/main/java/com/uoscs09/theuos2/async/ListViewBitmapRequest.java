package com.uoscs09.theuos2.async;


import android.graphics.Bitmap;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.ImageUtil;

import java.lang.ref.WeakReference;

public class ListViewBitmapRequest extends Request.Base<Bitmap> {
    private final WeakReference<ListView> listViewRef;
    private final ListAdapter adapter;
    private final WeakReference<View> headerViewRef;

    ListViewBitmapRequest(ListAdapter originalAdapter, WeakReference<ListView> listViewRef, WeakReference<View> headerViewRef) {
        this.adapter = originalAdapter;
        this.listViewRef = listViewRef;
        this.headerViewRef = headerViewRef;
    }

    @Override
    protected Bitmap getInner() throws Exception {

        ListView listView = listViewRef.get();
        if (listView == null)
            return null;
        Bitmap listViewBitmap = ImageUtil.getWholeListViewItemsToBitmap(listView, adapter, AppUtil.getAttrColor(listView.getContext(), R.attr.cardBackgroundColor));

        Bitmap headerViewBitmap;
        if (headerViewRef != null) {
            headerViewBitmap = makeHeaderViewBitmap(headerViewRef.get());

            if (headerViewBitmap == null)
                return listViewBitmap;

            try {
                return ImageUtil.merge(headerViewBitmap, listViewBitmap);
            } finally {
                if (listViewBitmap != null)
                    listViewBitmap.recycle();
                headerViewBitmap.recycle();
            }
        }

        return listViewBitmap;
    }

    private Bitmap makeHeaderViewBitmap(View headerView) {
        if (headerView == null)
            return null;
        headerView.setDrawingCacheEnabled(true);
        headerView.buildDrawingCache(true);

        Bitmap headerViewBitmap = headerView.getDrawingCache(true);

        if (headerViewBitmap == null)
            headerViewBitmap = ImageUtil.createBitmapFromView(headerView);
        try {
            return ImageUtil.drawOnBackground(headerViewBitmap, AppUtil.getAttrColor(headerView.getContext(), R.attr.cardBackgroundColor));
        } finally {
            if (headerViewBitmap != null)
                headerViewBitmap.recycle();
        }

    }

    public static final class Builder implements Request.Builder<Bitmap> {
        private final WeakReference<ListView> listViewRef;
        private final ListAdapter adapter;
        WeakReference<View> headerViewRef;

        public Builder(ListView listView, ListAdapter originalAdapter) {
            this.listViewRef = new WeakReference<>(listView);
            adapter = originalAdapter;
        }

        public Builder setHeaderView(View headerView) {
            this.headerViewRef = new WeakReference<>(headerView);
            return this;
        }

        @Override
        public Request<Bitmap> build() {
            return new ListViewBitmapRequest(adapter, listViewRef, headerViewRef);
        }
    }
}
