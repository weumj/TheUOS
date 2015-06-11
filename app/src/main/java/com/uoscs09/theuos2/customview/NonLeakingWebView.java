package com.uoscs09.theuos2.customview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.NestedScrollingChild;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * stackoverflow에서 검색된 WebView의 subclass<br>
 * memory leak이 WebView보다 덜하다.
 */
public class NonLeakingWebView extends WebView implements NestedScrollingChild {
    private static Field sConfigCallback;

    static {
        try {
            sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
            sConfigCallback.setAccessible(true);
        } catch (Exception e) {
            // ignored
        }
    }

    public NonLeakingWebView(Context context) {
        this(context, null, 0);
    }

    public NonLeakingWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NonLeakingWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWebViewClient(new NonLeakingWebViewClient((Activity) context));
    }


    @Override
    public void destroy() {
        super.destroy();
        try {
            if (sConfigCallback != null)
                sConfigCallback.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class NonLeakingWebViewClient extends WebViewClient {
        protected final WeakReference<Activity> activityRef;

        public NonLeakingWebViewClient(Activity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                final Activity activity = activityRef.get();
                if (activity != null)
                    ActivityCompat.startActivity(activity, new Intent(Intent.ACTION_VIEW, Uri.parse(url)), ActivityOptionsCompat.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getContentHeight()).toBundle());
            } catch (RuntimeException ignored) {
                // ignore any url parsing exceptions
            }
            return true;
        }
    }
}
