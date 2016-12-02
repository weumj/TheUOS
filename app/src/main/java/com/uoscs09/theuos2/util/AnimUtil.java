package com.uoscs09.theuos2.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;

public class AnimUtil {
    public static Dialog applyRevealAnim(Dialog dialog, View v) {
        dialog.setOnShowListener(dialog1 -> AnimUtil.revealShow(v, null));
        dialog.setOnDismissListener(dialog1 -> AnimUtil.revealHide(v, null));
        dialog.setOnCancelListener(dialog1 -> AnimUtil.revealHide(v, null));
        return dialog;
    }

    public static Dialog applyRevealAnim(Dialog dialog, View v, int x, int y) {
        dialog.setOnShowListener(dialog1 -> AnimUtil.revealShow(v, x, y, null));
        dialog.setOnDismissListener(dialog1 -> AnimUtil.revealHide(v, null));
        dialog.setOnCancelListener(dialog1 -> AnimUtil.revealHide(v, null));
        return dialog;
    }

    public static void revealShow(View view, @Nullable Runnable animationEndAction) {
        revealShow(view, 0, view.getHeight(), animationEndAction);
    }

    public static void revealShow(View view, int x, int y, @Nullable Runnable animationEndAction) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;
        if (!view.isAttachedToWindow())
            return;
/*
        int w = view.getWidth();
        int h = view.getHeight();
        float maxRadius = (float) (Math.sqrt(w * w + h * h) / 2);

        if (x < 0)
            x = 0;
        if (y > h)
            y = h;

        Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, x, y, 0, maxRadius);
*/

        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;
        int dx = Math.max(cx, view.getWidth() - cx);
        int dy = Math.max(cy, view.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);


        Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx, y, 0, finalRadius);

        revealAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        if (animationEndAction != null) {
            revealAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animationEndAction.run();
                }
            });
        }
        //view.setVisibility(View.VISIBLE);

        revealAnimator.start();
    }

    public static void revealHide(View view, @Nullable Runnable animationEndAction) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;
        if (!view.isAttachedToWindow())
            return;

        int w = view.getWidth();
        int h = view.getHeight();
        float maxRadius = (float) (Math.sqrt(w * w + h * h) / 2);

        Animator anim = ViewAnimationUtils.createCircularReveal(view, w, 0, maxRadius, 0);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (animationEndAction != null)
                    animationEndAction.run();
                view.setVisibility(View.INVISIBLE);
            }
        });

        anim.start();
    }

    public static void startActivityWithScaleUp(Activity activity, Intent intent, View v) {
        ActivityCompat.startActivity(activity, intent, ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight()).toBundle());
    }

}
