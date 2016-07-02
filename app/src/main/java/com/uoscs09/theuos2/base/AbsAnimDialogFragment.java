package com.uoscs09.theuos2.base;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.uoscs09.theuos2.util.AnimUtil;
import com.uoscs09.theuos2.util.OptimizeStrategy;

public abstract class AbsAnimDialogFragment extends BaseDialogFragment {
    private int showingX = -1;
    private int showingY = -1;

    @NonNull
    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = createView();
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();

        onDialogCreated(dialog);

        if (showingX > 0 && showingY > 0) {
            return AnimUtil.applyRevealAnim(dialog, view, showingX, showingY);
        } else
            return AnimUtil.applyRevealAnim(dialog, view);
    }

    public void showFromView(FragmentManager fm, String tag, View initialAnimView) {
        int x, y;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && OptimizeStrategy.isSafeToOptimize()) {
            int[] array = new int[2];
            initialAnimView.getLocationOnScreen(array);
            //x = (initialAnimView.getWidth() - initialAnimView.getPaddingLeft() - initialAnimView.getPaddingRight()) / 2 + array[0] + initialAnimView.getPaddingLeft();
            x = 0;
            y = array[1];
        } else {
            x = y = 0;
        }

        this.showFromLocation(fm, tag, x, y);
    }

    public void showFromLocation(FragmentManager fm, String tag, int x, int y) {
        this.showingX = x;
        this.showingY = y;
        this.show(fm, tag);
    }

    protected abstract View createView();

    protected void onDialogCreated(Dialog d) {
    }

}
