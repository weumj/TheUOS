package com.uoscs09.theuos2.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.uoscs09.theuos2.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class ImageUtil {

    private ImageUtil() {
    }

    /**
     * 주어진 리스트뷰의 전체 아이템들을 하나의 통합된 비트맵으로 만든다.
     */
    public static Bitmap getWholeListViewItemsToBitmap(ListView listView, ListAdapter adapter, int color) {

        int itemscount = adapter.getCount();
        int allitemsheight = 0;
        List<Bitmap> bmps = new ArrayList<>();

        for (int i = 0; i < itemscount; i++) {
            View childView = adapter.getView(i, null, listView);

            Bitmap bitmap = childView.getDrawingCache(true);
            if (bitmap == null) {
                childView.measure(MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());
                childView.setDrawingCacheEnabled(true);
                childView.buildDrawingCache();
                bitmap = childView.getDrawingCache();
            }
            bmps.add(bitmap);
            allitemsheight += childView.getMeasuredHeight();
        }

        Bitmap bigbitmap = Bitmap.createBitmap(listView.getMeasuredWidth(), allitemsheight + itemscount, Bitmap.Config.ARGB_8888);
        bigbitmap.eraseColor(color);
        Canvas bigcanvas = new Canvas(bigbitmap);

        Paint paint = new Paint();
        int iHeight = 0;

        int size = bmps.size();
        Bitmap bmp, line;
        line = Bitmap.createBitmap(listView.getWidth(), 1, Bitmap.Config.ARGB_8888);
        line.eraseColor(Color.DKGRAY);
        for (int i = 0; i < size; i++) {
            bmp = bmps.get(i);
            bigcanvas.drawBitmap(bmp, 0, iHeight, paint);
            iHeight += bmp.getHeight();
            bigcanvas.drawBitmap(line, 0, iHeight, paint);
            iHeight += 1;
            bmp.recycle();
        }
        line.recycle();

        return bigbitmap;
    }

    /**
     * 해당 비트맵 이미지를 파일로 저장한다.
     *
     * @return 성공 여부
     */
    public static boolean saveImageToFile(String src, Bitmap img) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(src);
            return img.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } finally {
            IOUtil.closeStream(fos);
        }
    }

    /**
     * 주어진 뷰를 비트맵으로 캡쳐한다.<br>
     * {@code View.getDrawingCache()} 가 null을 반환할때 사용한다.
     */
    public static Bitmap createBitmapFromView(View v) throws IllegalArgumentException {

        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getWidth(), v.getHeight());
        v.draw(c);

        return b;
    }

    public static Bitmap drawOnBackground(Bitmap bitmap, int color) {
        Bitmap processed = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        processed.eraseColor(color);
        Canvas canvas = new Canvas(processed);

        canvas.drawBitmap(bitmap, 0, 0, new Paint());

        return processed;
    }

    /**
     * 두개의 비트맵을 합친다. bmp1이 위에 위치한다.
     */
    public static Bitmap merge(Bitmap bmp1, Bitmap bmp2) {
        Bitmap cs;

        int width, height;

        height = bmp1.getHeight() + bmp2.getHeight();
        width = bmp1.getWidth() > bmp2.getWidth() ? bmp1.getWidth() : bmp2.getWidth();

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(bmp1, 0f, 0f, null);
        comboImage.drawBitmap(bmp2, 0f, bmp1.getHeight(), null);

        return cs;
    }

    public static Drawable getDrawable(Context context, @DrawableRes int id) {
        return ResourcesCompat.getDrawable(context.getResources(), id, context.getTheme());
    }

    public static class ImageWriteProcessor {
        private final String fileName;

        public ImageWriteProcessor(String fileName) {
            this.fileName = fileName;
        }

        public String func(Bitmap bitmap) throws Exception {
            try {
                ImageUtil.saveImageToFile(fileName, bitmap);
                return fileName;
            } finally {
                if (bitmap != null)
                    bitmap.recycle();
            }
        }
    }

    public static class ListViewBitmapRequest {
        private final WeakReference<ListView> listViewRef;
        private final ListAdapter adapter;
        private final WeakReference<View> headerViewRef;

        ListViewBitmapRequest(ListAdapter originalAdapter, WeakReference<ListView> listViewRef, WeakReference<View> headerViewRef) {
            this.adapter = originalAdapter;
            this.listViewRef = listViewRef;
            this.headerViewRef = headerViewRef;
        }

        public Bitmap get() {
            ListView listView = listViewRef.get();
            if (listView == null)
                throw new IllegalArgumentException();

            Bitmap listViewBitmap = getWholeListViewItemsToBitmap(listView, adapter, ResourceUtil.getAttrColor(listView.getContext(), R.attr.cardBackgroundColor));

            Bitmap headerViewBitmap;
            if (headerViewRef != null) {
                headerViewBitmap = makeHeaderViewBitmap(headerViewRef.get());

                if (headerViewBitmap == null) return listViewBitmap;

                try {
                    return merge(headerViewBitmap, listViewBitmap);
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

            boolean newBitmapCreated = false;
            if (headerViewBitmap == null || headerViewBitmap.isRecycled()) {
                headerViewBitmap = createBitmapFromView(headerView);
                newBitmapCreated = true;
            }

            try {
                return drawOnBackground(headerViewBitmap, ResourceUtil.getAttrColor(headerView.getContext(), R.attr.cardBackgroundColor));
            } finally {
                if (headerViewBitmap != null && newBitmapCreated) headerViewBitmap.recycle();
            }

        }

        public static final class Builder {
            private final WeakReference<ListView> listViewRef;
            private final ListAdapter adapter;
            private WeakReference<View> headerViewRef;

            public Builder(ListView listView, ListAdapter originalAdapter) {
                this.listViewRef = new WeakReference<>(listView);
                adapter = originalAdapter;
            }

            public Builder setHeaderView(View headerView) {
                this.headerViewRef = new WeakReference<>(headerView);
                return this;
            }

            public Observable<Bitmap> build() {
                return Observable.fromCallable(new ListViewBitmapRequest(adapter, listViewRef, headerViewRef)::get);
            }
        }
    }
}
