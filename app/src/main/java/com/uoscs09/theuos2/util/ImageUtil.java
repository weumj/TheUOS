package com.uoscs09.theuos2.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.uoscs09.theuos2.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageUtil {

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
            if (fos != null)
                fos.close();
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

    public static Bitmap drawOnBackground(Bitmap bitmap, int color){
        Bitmap processed = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        processed.eraseColor(color);
        Canvas canvas = new Canvas(processed);

        canvas.drawBitmap(bitmap, 0 ,0 ,new Paint());

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
/*
    public static Drawable getTintDrawable(Context context, @DrawableRes int id, int color ){
        return getTintDrawable(getDrawable(context, id), color);
    }
  */


    public static Drawable getTintDrawable(Drawable drawable, int color) {
        Drawable drawable1 = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintMode(drawable1, PorterDuff.Mode.SRC_IN);
        DrawableCompat.setTint(drawable1, color);
        return drawable1;
    }

    public static Drawable getTintDrawableForMenu(Context context, Drawable drawable) {
        return getTintDrawable(drawable, context.getResources().getColor(AppUtil.getAttrValue(context, R.attr.color_actionbar_title)));
    }

    public static Drawable getTintDrawableForMenu(Context context, @DrawableRes int id) {
        return getTintDrawableForMenu(context, getDrawable(context, id));
    }

    public static Drawable getDrawable(Context context, @DrawableRes int id) {
        return ResourcesCompat.getDrawable(context.getResources(), id, context.getTheme());
    }

    public static Drawable getPageIcon(Context context, @StringRes int titleRes) {
        int drawableRes = AppUtil.getPageIcon(context, titleRes);

        if (titleRes == AppUtil.RESOURCE_NOT_EXIST)
            return null;

        else
            return getDrawable(context, drawableRes);

    }
/*
    public static Drawable getBitmapDrawableFromXml(Context context, @DrawableRes int res) {
        XmlResourceParser parser = null;
        try {
            Resources resources = context.getResources();

            Method method = resources.getClass().getDeclaredMethod("loadXmlResourceParser", Integer.TYPE, String.class);
            method.setAccessible(true);
            parser = (XmlResourceParser) method.invoke(resources, res, "drawable");

            parser.next();

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                int src = 0, tint = 0;

                String name = parser.getName();
                if (name.equals("bitmap")) {
                    for (int i = 0; i < parser.getAttributeCount(); i++) {

                        switch (parser.getAttributeName(i)) {
                            case "src":
                                src = parser.getAttributeResourceValue(i, 0);
                                break;

                            case "tint":
                                Class<?> clazz = parser.getClass();


                                Field f = clazz.getDeclaredField("mParseState");
                                f.setAccessible(true);
                                long mParseState = f.getLong(parser);

                                if (nativeGetAttributeData == null) {

                                    Field fmCachedXmlBlocks = resources.getClass().getDeclaredField("mCachedXmlBlocks");
                                    fmCachedXmlBlocks.setAccessible(true);
                                    Object mCachedXmlBlocks = fmCachedXmlBlocks.get(resources);

                                    Method[] ms = mCachedXmlBlocks.getClass().getComponentType().getDeclaredMethods();
                                    for (Method m : ms) {
                                        if (Modifier.isStatic(m.getModifiers()) && m.getName().equals("nativeGetAttributeData")) {
                                            nativeGetAttributeData = m;
                                            break;
                                        }
                                    }
                                    //nativeGetAttributeData = Class.forName("android.content.res.XmlBlock").getMethod("nativeGetAttributeData", Long.TYPE, Integer.TYPE);
                                    nativeGetAttributeData.setAccessible(true);
                                }

                                if (Build.VERSION.SDK_INT >= 21)
                                    tint = (int) nativeGetAttributeData.invoke(null, mParseState, i);
                                else
                                    tint = (int) nativeGetAttributeData.invoke(null, (int) mParseState, i);
                                break;
                        }
                    }

                    parser.close();
                    return getTintDrawable(getDrawable(context, src), resources.getColor(AppUtil.getAttrValue(context, tint)));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (parser != null) parser.close();
        }
        return null;
    }

    private static Method nativeGetAttributeData;
    */
}
